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

import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.filter.firewall.Subnet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Whitelist filter (based on Mina's Blacklist filter).
 */
public class WhitelistFilter extends IoFilterAdapter
{
    private final List<Subnet> whitelist = new CopyOnWriteArrayList<Subnet>();
    private boolean ignoreOnEmpty=true;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public boolean isIgnoreOnEmpty()
    {
        return ignoreOnEmpty;
    }

    public void setIgnoreOnEmpty(boolean ignoreOnEmpty)
    {
        this.ignoreOnEmpty = ignoreOnEmpty;
    }

    public void setWhitelist(InetAddress[] addresses)
    {
        if (addresses == null)
        {
            throw new NullPointerException("addresses");
        }
        whitelist.clear();
        for (InetAddress addr : addresses)
        {
            allow(addr);
        }
    }

    public void setSubnetWhitelist(Subnet[] subnets)
    {
        if (subnets == null)
        {
            throw new NullPointerException("Subnets must not be null");
        }
        whitelist.clear();
        for (Subnet subnet : subnets)
        {
            allow(subnet);
        }
    }

    public void setWhitelist(Iterable<InetAddress> addresses)
    {
        if (addresses == null)
        {
            throw new NullPointerException("addresses");
        }

        whitelist.clear();

        for (InetAddress address : addresses)
        {
            allow(address);
        }
    }

    public void setSubnetWhitelist(Iterable<Subnet> subnets)
    {
        if (subnets == null)
        {
            throw new NullPointerException("Subnets must not be null");
        }
        whitelist.clear();
        for (Subnet subnet : subnets)
        {
            allow(subnet);
        }
    }

    public void allow(InetAddress address)
    {
        if (address == null)
        {
            throw new NullPointerException("Adress to block can not be null");
        }

        allow(new Subnet(address, 32));
    }

    public void allow(Subnet subnet)
    {
        if (subnet == null)
        {
            throw new NullPointerException("Subnet can not be null");
        }

        whitelist.add(subnet);
    }

    public void disallow(InetAddress address)
    {
        if (address == null)
        {
            throw new NullPointerException("Adress to unblock can not be null");
        }

        disallow(new Subnet(address, 32));
    }

    public void disallow(Subnet subnet)
    {
        if (subnet == null)
        {
            throw new NullPointerException("Subnet can not be null");
        }
        whitelist.remove(subnet);
    }

    @Override
    public void sessionCreated(NextFilter nextFilter, IoSession session)
    {
        if (isAllowed(session))
        {
            nextFilter.sessionCreated(session);
        }
        else
        {
            blockSession(session);
        }
    }

    @Override
    public void sessionOpened(NextFilter nextFilter, IoSession session)
            throws Exception
    {
        if (isAllowed(session))
        {
            nextFilter.sessionOpened(session);
        }
        else
        {
            blockSession(session);
        }
    }

    @Override
    public void sessionClosed(NextFilter nextFilter, IoSession session)
            throws Exception
    {
        if (isAllowed(session))
        {
            nextFilter.sessionClosed(session);
        }
        else
        {
            blockSession(session);
        }
    }

    @Override
    public void sessionIdle(NextFilter nextFilter, IoSession session,
                            IdleStatus status) throws Exception
    {
        if (isAllowed(session))
        {
            nextFilter.sessionIdle(session, status);
        }
        else
        {
            blockSession(session);
        }
    }

    @Override
    public void messageReceived(NextFilter nextFilter, IoSession session,
                                Object message)
    {
        if (isAllowed(session))
        {
            nextFilter.messageReceived(session, message);
        }
        else
        {
            blockSession(session);
        }
    }

    @Override
    public void messageSent(NextFilter nextFilter, IoSession session,
                            WriteRequest writeRequest) throws Exception
    {
        if (isAllowed(session))
        {
            nextFilter.messageSent(session, writeRequest);
        }
        else
        {
            blockSession(session);
        }
    }

    private void blockSession(IoSession session)
    {
        logger.warn("Remote address is not allowed; closing.");
        session.close(true);
    }

    private boolean isAllowed(IoSession session)
    {
        if(ignoreOnEmpty) return true;

        SocketAddress remoteAddress = session.getRemoteAddress();
        if (remoteAddress instanceof InetSocketAddress)
        {
            InetAddress address = ((InetSocketAddress) remoteAddress).getAddress();

            // check all subnets
            for (Subnet subnet : whitelist)
            {
                if (subnet.inSubnet(address))
                {
                    return true;
                }
            }
        }
        return false;
    }
}
