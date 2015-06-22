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
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.jpos.iso.ISOMsg;
import org.jpos.q2.nserver.NullMessage;
import org.jpos.q2.nserver.ProtocolHandler;

/**
 * @author Victor Salaman (vsalaman@gmail.com)
 */
public class DefaultISOMsgEncoder extends ProtocolEncoderAdapter
{
    ProtocolHandler protocolHandler;

    public DefaultISOMsgEncoder(ProtocolHandler protocolHandler)
    {
        this.protocolHandler = protocolHandler;
    }

    public void encode(IoSession session, Object msg, ProtocolEncoderOutput out) throws Exception
    {
        if(msg instanceof ISOMsg)
        {
            ISOMsg m = (ISOMsg) msg;
            m.setPackager(protocolHandler.getPackager());
            m.setDirection(ISOMsg.OUTGOING);
            byte[] b = m.pack();
            IoBuffer buffer = IoBuffer.allocate(b.length + protocolHandler.getHeaderLength() + 512);
            protocolHandler.writeMessageLength(buffer, b.length);
            protocolHandler.writeHeader(buffer, m);
            protocolHandler.writePayload(buffer, b);
            protocolHandler.writeTrailer(buffer, b);
            buffer.flip();
            out.write(buffer);
        }
        else if(msg instanceof NullMessage)
        {
            NullMessage m=(NullMessage)msg;
            final byte[] bytes = m.getBytes();
            IoBuffer buffer= IoBuffer.allocate(bytes.length);
            buffer.put(bytes);
            buffer.flip();
            out.write(buffer);
        }
    }
}
