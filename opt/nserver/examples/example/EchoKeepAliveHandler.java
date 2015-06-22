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

package example;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;
import org.apache.mina.filter.keepalive.KeepAliveRequestTimeoutHandler;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISODate;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.q2.nserver.KeepAliveHandler;
import org.jpos.space.PersistentSpace;
import org.jpos.space.Space;
import org.jpos.space.SpaceUtil;

import java.util.Date;
import java.util.TimeZone;

/**
 * This is an example Keep Alive Handler which sends an echo message (0800) to the connecting
 * client and expects a response. If a response is not sent by the client within a determined
 * timeout, the client is disconnected.
 *
 * This example will not work as is for you, as you'll most probably need to change how your 0800
 * message is constructed!!
 *
 * @author Victor Salaman (vsalaman@gmail.com)

 */
public class EchoKeepAliveHandler implements KeepAliveMessageFactory, KeepAliveHandler
{
    private Space psp;
    private int requestTimeout;
    private int requestInterval;

    public int getRequestInterval()
    {
        return requestInterval;
    }

    public void setRequestInterval(int requestInterval)
    {
        this.requestInterval = requestInterval;
    }

    public int getRequestTimeout()
    {
        return requestTimeout;
    }

    public void setRequestTimeout(int requestTimeout)
    {
        this.requestTimeout = requestTimeout;
    }

    public KeepAliveMessageFactory getKeepAliveMessageFactory()
    {
        return this;
    }

    public KeepAliveRequestTimeoutHandler getKeepAliveRequestTimeoutHandler()
    {
        return KeepAliveRequestTimeoutHandler.CLOSE;
    }

    public boolean isRequest(IoSession ioSession, Object o)
    {
        return isOfMTI(o, "0800");
    }

    public boolean isResponse(IoSession ioSession, Object o)
    {
        return isOfMTI(o, "0810");
    }

    private boolean isOfMTI(Object o, String in)
    {
        if(o instanceof ISOMsg)
        {
            ISOMsg m=(ISOMsg)o;
            try
            {
                String mti=m.getMTI();
                if(mti!=null && mti.equals(in)) return true;
            }
            catch (ISOException e)
            {
                return false;
            }
        }
        return false;
    }

    public Object getRequest(IoSession ioSession)
    {
        long traceNumber = SpaceUtil.nextLong(psp, "mcm.trace") % 1000000;

        try
        {
            ISOMsg m = new ISOMsg("0800");
            m.setHeader(new byte[]{0x49,0x53,0x4f,0x30,0x30,0x35,0x30,0x30,0x30,0x30,0x34,0x34});
            m.set(7, ISODate.getDateTime(new Date(), TimeZone.getTimeZone("GMT")));
            final String tn = Long.toString(traceNumber);
            m.set(11, ISOUtil.zeropad(tn, 6));
            m.set(70, "301");
            return m;
        }
        catch (ISOException e)
        {
            return null;
        }
    }

    public Object getResponse(IoSession ioSession, Object o)
    {
        return o;
    }

    public void setConfiguration(Configuration cfg) throws ConfigurationException
    {
        psp= PersistentSpace.getSpace(cfg.get("persistent-space","jdbm:mcm-echomgr"));
        requestTimeout = cfg.getInt("request-timeout",30);
        requestInterval = cfg.getInt("request-interval",10);
    }
}
