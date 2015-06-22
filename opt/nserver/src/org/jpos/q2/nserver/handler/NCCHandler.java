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
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;

/**
 * @author Victor Salaman (vsalaman@gmail.com)
 */
public class NCCHandler extends NACHandler
{
    @Override
    public void writeMessageLength(IoBuffer out, int len)
    {
        try
        {
            byte[] l = ISOUtil.str2bcd(ISOUtil.zeropad(Integer.toString(len % 10000), 4), true);
            out.put(l);
        }
        catch (ISOException e)
        {
            Logger.log(new LogEvent(this, "send-message-length", e));
        }
    }

    @Override
    public int readMessageLength(IoBuffer in)
    {
        byte[] b = new byte[2];
        in.get(b);
        return Integer.parseInt(ISOUtil.bcd2str(b, 0, 4, true));
    }
}