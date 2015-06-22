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

package org.jpos.q2.nserver.handler;

import org.apache.mina.core.buffer.IoBuffer;
import org.jpos.q2.nserver.BaseProtocolHandler;

import java.io.EOFException;
import java.io.IOException;

/**
 * @author Victor Salaman (vsalaman@gmail.com)
 */
public class BASE24Handler extends BaseProtocolHandler
{
    private static final int MAX_PACKET_SIZE = 4096;

    @Override
    public void writeTrailer(IoBuffer out, byte[] b)
    {
        out.put((byte) 3);
    }

    @Override
    public byte[] readStream(IoBuffer in) throws IOException
    {
        byte[] buf = new byte[MAX_PACKET_SIZE];
        int maxBuf = buf.length;
        int i = 0;
        while (in.hasRemaining())
        {
            byte b = in.get();
            if (b == 3)
            {
                break;
            }
            else if (b == -1)
            {
                throw new EOFException("connection closed");
            }
            if (i >= maxBuf)
            {
                throw new IOException("packet too long");
            }
            buf[i++] = b;
        }
        byte[] d = new byte[i];
        System.arraycopy(buf, 0, d, 0, i);
        return d;
    }
}
