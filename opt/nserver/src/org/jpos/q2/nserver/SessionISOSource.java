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

import org.apache.mina.core.session.IoSession;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOSource;

import java.io.IOException;

/**
 * An adapter which implements the ISOSource interface to expose our NIO session.
 */
public class SessionISOSource implements ISOSource
{
    IoSession session;

    public SessionISOSource(IoSession session)
    {
        this.session = session;
    }

    public void send(ISOMsg isoMsg) throws IOException, ISOException
    {
        if (session.isConnected())
        {
            session.write(isoMsg);
        }
    }

    public boolean isConnected()
    {
        return session.isConnected();
    }

    public IoSession getSession()
    {
        return session;
    }
}
