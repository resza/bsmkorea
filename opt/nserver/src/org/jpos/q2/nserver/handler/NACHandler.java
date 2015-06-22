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
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.q2.nserver.BaseProtocolHandler;

/**
 * @author Victor Salaman (vsalaman@gmail.com)
 */
public class NACHandler extends BaseProtocolHandler
{
    protected boolean tpduSwap = true;

    @Override
    public int getMessageLengthByteSize()
    {
        return 2;
    }

    @Override
    public void writeHeader(IoBuffer out, ISOMsg m)
    {
        byte[] h = m.getHeader();
        if (h != null)
        {
            if (tpduSwap && h.length == 5)
            {
                // swap src/dest address
                byte[] tmp = new byte[2];
                System.arraycopy(h, 1, tmp, 0, 2);
                System.arraycopy(h, 3, h, 1, 2);
                System.arraycopy(tmp, 0, h, 3, 2);
            }
        }
        else
        {
            h = header;
        }
        if (h != null)
        {
            out.put(h);
        }
    }

    public void setTpduSwap(boolean tpduSwap)
    {
        this.tpduSwap = tpduSwap;
    }

    public void setHeader(String header)
    {
        this.header = ISOUtil.str2bcd(header, false);
    }

    public void setConfiguration(Configuration cfg) throws ConfigurationException
    {
        super.setConfiguration(cfg);
        tpduSwap = cfg.getBoolean("tpdu-swap", true);
    }
}