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

/**
 * @author Victor Salaman (vsalaman@gmail.com)
 */
public class CSHandler extends BaseProtocolHandler
{
    @Override
    public int getMessageLengthByteSize()
    {
        return 4;
    }

    @Override
    public boolean isUseZeroLengthAsKeepalive()
    {
        return true;
    }

    @Override
    public int readMessageLength(IoBuffer in)
    {
        int len=in.getShort();
        in.getShort();
        return len;
    }

    @Override
    public void writeMessageLength(IoBuffer out, int len)
    {
        out.putShort((short) len);
        out.putShort((short) 0);
    }
}
