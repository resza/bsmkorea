/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2009 Alejandro P. Revilla
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.q2.nserver.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.q2.nserver.NullMessage;
import org.jpos.q2.nserver.ProtocolHandler;

import java.util.Arrays;

/**
 * @author Victor Salaman (vsalaman@gmail.com)
 *
 */
public class DefaultISOMsgDecoder extends CumulativeProtocolDecoder implements ProtocolDecoder
{
    private ProtocolHandler protocolHandler;

    private final static String DECODER_STATE_KEY = DefaultISOMsgDecoder.class.getName() + ".DECODER_STATE";

    private static final int STAGE_MSG_LEN=1;
    private static final int STAGE_MSG_HEADER=2;
    private static final int STAGE_MSG_PAYLOAD=3;
    private static final int STAGE_MSG_DONE=4;

    private static class DecoderCtx
    {
        int stage;
        int len = 0;
        byte[] header;
        byte[] payload;
        byte[] trailer;
    }

    public DefaultISOMsgDecoder(ProtocolHandler protocolHandler)
    {
        this.protocolHandler = protocolHandler;
    }

    protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception
    {
        long enterTime=System.currentTimeMillis();

        DecoderCtx ctx = (DecoderCtx) session.getAttribute(DECODER_STATE_KEY);
        if (ctx == null)
        {
            ctx = new DecoderCtx();
            ctx.stage=STAGE_MSG_LEN;
            session.setAttribute(DECODER_STATE_KEY, ctx);
        }

        while(in.remaining()>0)
        {
            if(System.currentTimeMillis()-enterTime>10000)
            {
                throw new ISOException("Decoding has aborted since we couldn't complete the decoding process in less than 10 seconds.");
            }

            switch(ctx.stage)
            {
                case STAGE_MSG_LEN:
                {
                    if(!protocolHandler.isLengthEncoded())
                    {
                        ctx.stage++;
                        break;
                    }
                    if (isDataAvailable(in, protocolHandler.getMessageLengthByteSize()))
                    {
                        ctx.len = protocolHandler.readMessageLength(in);
                        if(ctx.len==0 && protocolHandler.isUseZeroLengthAsKeepalive())
                        {
                            byte[] b=new byte[protocolHandler.getMessageLengthByteSize()];
                            Arrays.fill(b,(byte)0);
                            out.write(new NullMessage(b));
                            return true;
                        }
                        else if(ctx.len>0 && ctx.len<= protocolHandler.getMaxPacketLength())
                        {
                            ctx.stage++;
                        }
                        else
                        {
                            throw new ISOException(
                                "receive length " +ctx.len + " seems strange - maxPacketLength = " + protocolHandler.getMaxPacketLength());
                        }
                    }
                    else return false;
                }
                break;
                case STAGE_MSG_HEADER:
                {
                    if(!protocolHandler.containsHeader())
                    {
                        ctx.stage++;
                        break;
                    }
                    if (isDataAvailable(in, protocolHandler.getHeaderLength()))
                    {
                        ctx.header = protocolHandler.readHeader(in, protocolHandler.getHeaderLength());
                        int oldCtxLen=ctx.len;
                        ctx.len -= ctx.header.length;
                        if(ctx.len<0)
                        {
                            throw new ISOException(
                                "Header is bigger than specified " +
                                "(header_len+payload_len cannot be bigger than message_len),\n " +
                                "payload size used to be: "+oldCtxLen+", is now: "+ctx.len);
                        }
                        ctx.stage++;
                    }
                    else return false;
                }
                break;
                case STAGE_MSG_PAYLOAD:
                {
                    if (!protocolHandler.isLengthEncoded())
                    {
                        ctx.payload = protocolHandler.readStream(in);
                        ctx.stage=STAGE_MSG_DONE;
                        markDone(session, out, ctx);
                        return true;
                    }
                    else if (isDataAvailable(in, ctx.len))
                    {
                        if(ctx.len<=0 || ctx.len>protocolHandler.getMaxPacketLength())
                        {
                            throw new ISOException(
                                "payload length " +ctx.len + " seems strange - maxPacketLength = " + protocolHandler.getMaxPacketLength());
                        }
                        ctx.payload = protocolHandler.readPayload(in, ctx.len);
                        ctx.stage=STAGE_MSG_DONE;
                        markDone(session, out, ctx);
                        return true;
                    }
                    else return false;
                }
            }
        }
        return false;
    }

    private void markDone(IoSession session, ProtocolDecoderOutput out, DecoderCtx ctx) throws ISOException
    {
        out.write(buildMessage(ctx));
        session.removeAttribute(DECODER_STATE_KEY);
    }

    private boolean isDataAvailable(IoBuffer in, int len)
    {
        return in.remaining() >= len;
    }

    private Object buildMessage(DecoderCtx ctx) throws ISOException
    {
        ISOMsg m = protocolHandler.getPackager().createISOMsg();
        m.setPackager(protocolHandler.getPackager());
        m.setHeader(protocolHandler.getDynamicHeader(ctx.header));
        protocolHandler.unpack(m, ctx.payload);
        return m;
    }
}