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

package org.jpos.q2.nserver;

import org.apache.mina.core.buffer.IoBuffer;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOHeader;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.header.BaseHeader;
import org.jpos.util.Logger;

import java.io.IOException;

/**
 * @author Victor Salaman (vsalaman@gmail.com)
 */
public abstract class BaseProtocolHandler implements ProtocolHandler
{
    protected ISOPackager packager;
    protected byte[] header = null;
    protected boolean overrideHeader;
    protected boolean useZeroLengthAsKeepalive=false;
    protected int maxPacketLength = 100000;

    protected Configuration configuration;
    protected Logger logger;
    protected String realm;

    public void setPackager(ISOPackager packager)
    {
        this.packager = packager;
    }

    public ISOPackager getPackager()
    {
        return packager;
    }

    public int getMaxPacketLength()
    {
        return maxPacketLength;
    }

    public void setMaxPacketLength(int maxPacketLength)
    {
        this.maxPacketLength = maxPacketLength;
    }

    public void setHeader(byte[] header)
    {
        this.header = header;
    }

    public void setHeader(String header)
    {
        setHeader(header.getBytes());
    }

    public boolean isUseZeroLengthAsKeepalive()
    {
        return useZeroLengthAsKeepalive;
    }

    public void setUseZeroLengthAsKeepalive(boolean useZeroLengthAsKeepalive)
    {
        this.useZeroLengthAsKeepalive = useZeroLengthAsKeepalive;
    }

    public ISOMsg createISOMsg()
    {
        return packager.createISOMsg();
    }

    public int readMessageLength(IoBuffer in)
    {
        if (isLengthEncoded())
        {
            int byteSize = getMessageLengthByteSize();
            if (byteSize == 2)
            {
                return in.getShort();
            }
            else if (byteSize == 4)
            {
                return in.getInt();
            }
        }
        return -1;
    }

    public int getHeaderLength()
    {
        return header != null ? header.length : 0;
    }

    public int getMessageLengthByteSize()
    {
        return -1;
    }

    public boolean isLengthEncoded()
    {
        return getMessageLengthByteSize() > 0;
    }

    public boolean containsHeader()
    {
        return getHeaderLength() > 0;
    }

    public byte[] readHeader(IoBuffer in, int len)
    {
        return readBytes(in, len);
    }

    public byte[] readPayload(IoBuffer in, int len)
    {
        return readBytes(in, len);
    }

    public byte[] readStream(IoBuffer in) throws IOException
    {
        return new byte[0];
    }

    public void writeMessageLength(IoBuffer out, int len)
    {
        if (isLengthEncoded())
        {
            int byteSize = getMessageLengthByteSize();
            if (byteSize == 2)
            {
                out.putShort((short) len);
            }
            else if (byteSize == 4)
            {
                out.putInt(len);
            }
        }
    }

    public void writeHeader(IoBuffer out, ISOMsg m)
    {
        if (containsHeader())
        {
            if (!overrideHeader && m.getHeader() != null)
            {
                out.put(m.getHeader());
            }
            else if (header != null)
            {
                out.put(header);
            }
        }
    }

    public void writePayload(IoBuffer out, byte[] b)
    {
        out.put(b);
    }

    public void writeTrailer(IoBuffer out, byte[] b)
    {
    }

    private byte[] readBytes(IoBuffer in, int len)
    {
        byte[] b = new byte[len];
        in.get(b);
        return b;
    }

    public void unpack(ISOMsg msg, byte[] b) throws ISOException
    {
        if (b.length > 0 && !shouldIgnore(msg.getHeader()))
        {
            msg.unpack(b);
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected boolean isRejected(byte[] b)
    {
        return false;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected boolean shouldIgnore(byte[] b)
    {
        return false;
    }

    public ISOHeader getDynamicHeader(byte[] image)
    {
        return image != null ? new BaseHeader(image) : null;
    }

    public boolean isOverrideHeader()
    {
        return overrideHeader;
    }

    public void setOverrideHeader(boolean overrideHeader)
    {
        this.overrideHeader = overrideHeader;
    }

    public Configuration getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) throws ConfigurationException
    {
        this.configuration = configuration;
        setOverrideHeader(configuration.getBoolean("override-header", false));
    }

    public Logger getLogger()
    {
        return logger;
    }

    public void setLogger(Logger logger)
    {
        this.logger = logger;
    }

    public void setLogger(Logger logger, String realm)
    {
        setLogger(logger);
        setRealm(realm);
    }

    public String getRealm()
    {
        return realm;
    }

    public void setRealm(String realm)
    {
        this.realm = realm;
    }
}
