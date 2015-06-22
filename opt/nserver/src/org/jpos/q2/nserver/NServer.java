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

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.jdom.Element;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISORequestListener;
import org.jpos.q2.QBeanSupport;
import org.jpos.q2.QFactory;
import org.jpos.q2.nserver.codec.DefaultISOMsgCodecFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A high performance server QBean based on Java NIO, implemented as a
 * protocol over Apache MINA.
 * <p/>
 * NOTE: This server does not use jPos ISOChannels. Instead a generic transport has
 * been created and the protocol specific functionality has been decoupled into
 * a ProtocolHandler, since ISOChannels (unfortunately) are not transport agnostic.
 * <p/>
 * Since this code is not based on ISOChannels, this means that not all
 * ISOChannels are currently implemented as ProtocolHandlers. Therefore you should
 * port your own, or stick to QServer.
 * <p/>
 * This server would be of good use if you are in need of handling
 * thousands of simultaneuous incoming connections on a single server.
 * <p/>
 * An example Q2 descriptor for NServer:
 * <p/>
 * <pre>
 *   {@code
 *    <?xml version="1.0" encoding="UTF-8"?>
 *
 *    <server name="ath-bridge-prod" class="org.jpos.q2.nserver.NServer" logger="Q2">
 *
 *        <classpath>lib/mina-core-1.1.7.jar</classpath>
 *        <classpath>lib/slf4j-api-1.5.0.jar</classpath>
 *        <classpath>lib/slf4j-nop-1.5.0.jar</classpath>
 *
 *        <attr name="port" type="java.lang.Integer">2005</attr>
 *
 *        <protocol-handler
 *              class="org.jpos.q2.nserver.handler.AthProtocolHandler"
 *              packager="org.jpos.iso.packager.GenericPackager">
 *            <property name="packager-config" value="cfg/packager/ath.xml" />
 *        </protocol-handler>
 *
 *         <request-listener class="com.kontinium.isobridge.BridgeRequestListener">
 *             <property name="space" value="transient:default"/>
 *             <property name="queue" value="txnmgr"/>
 *             <property name="timeout" value="60000"/>
 *         </request-listener>
 *
 *    </server>
 *  }
 * </pre>
 *
 * @author Victor Salaman (vsalaman@gmail.com)
 */
public class NServer extends QBeanSupport implements NServerMBean, IoHandler
{
    private int port = 0;
    ProtocolHandler handler = null;
    private ISOMsgCodecFactory codecFactory = new DefaultISOMsgCodecFactory();
    NioSocketAcceptor acceptor = null;
    protected boolean ignoreISOExceptions;
    List<ISORequestListener> requestListeners;
    InetSocketAddress socketAddress;
    int connectedSessions=0;
    KeepAliveHandler keepAliveHandler;
    int timeout=-1;

    public void setPort(int port)
    {
        this.port = port;
    }

    public int getPort()
    {
        return port;
    }

    public int getConnectionCount()
    {
        return connectedSessions;
    }
    
    @Override
    protected void startService() throws Exception
    {
        if (port == 0)
        {
            throw new ConfigurationException("Port value not set");
        }
        socketAddress = new InetSocketAddress(port);
        newCodecFactory();
        addListeners();

        acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(codecFactory));

        if(timeout==-1)
        {
            keepAliveHandler=newKeepAliveHandler();
            if(keepAliveHandler!=null)
            {
                KeepAliveFilter kaf=new KeepAliveFilter(keepAliveHandler.getKeepAliveMessageFactory(),
                                                        IdleStatus.READER_IDLE,
                                                        keepAliveHandler.getKeepAliveRequestTimeoutHandler(),
                                                        keepAliveHandler.getRequestInterval(),
                                                        keepAliveHandler.getRequestTimeout());

                acceptor.getFilterChain().addLast("keepalive", kaf);
            }
        }
        else if(timeout>0)
        {
            acceptor.getSessionConfig().setIdleTime(IdleStatus.READER_IDLE,timeout);
        }
        acceptor.setBacklog(100);
        acceptor.setReuseAddress(true);
        acceptor.setDefaultLocalAddress(socketAddress);
        acceptor.setHandler(this);
        acceptor.getSessionConfig().setReadBufferSize(2048);
        acceptor.bind();
    }

    @Override
    protected void stopService() throws Exception
    {
        acceptor.unbind();
        acceptor.dispose();
    }

    private void newCodecFactory() throws ConfigurationException
    {
        QFactory factory = getFactory();
        Element persist = getPersist();

        handler = newProtocolHandler();
        if (handler == null)
        {
            throw new ConfigurationException("Protocol handler is null");
        }

        keepAliveHandler = newKeepAliveHandler();

        Element codecFactoryElem = persist.getChild("codec-factory");
        if (codecFactoryElem != null)
        {
            codecFactory = (ISOMsgCodecFactory) factory.newInstance(codecFactoryElem.getAttributeValue("class"));
        }
        if (codecFactory == null)
        {
            throw new ConfigurationException("Codec Factory is null");
        }
        codecFactory.init(handler);
    }

    private ProtocolHandler newProtocolHandler() throws ConfigurationException
    {
        QFactory f=getFactory();
        Element e = getPersist().getChild("protocol-handler");
        if (e == null)
        {
            throw new ConfigurationException("protocol-handler element is required.");
        }

        String handlerName = e.getAttributeValue("class");
        String packagerName = e.getAttributeValue("packager");

        ProtocolHandler handler = (ProtocolHandler) f.newInstance(handlerName);
        f.setLogger(handler, e);
        f.setConfiguration(handler, e);

        ISOPackager packager = (ISOPackager) f.newInstance(packagerName);
        handler.setPackager(packager);
        f.setConfiguration(packager, e);

        QFactory.invoke(handler, "setHeader", e.getAttributeValue("header"));
        return handler;
    }

    private KeepAliveHandler newKeepAliveHandler() throws ConfigurationException
    {
        QFactory f=getFactory();
        Element e = getPersist().getChild("keep-alive-handler");
        if (e!=null)
        {
            String handlerName = e.getAttributeValue("class");

            KeepAliveHandler handler = (KeepAliveHandler) f.newInstance(handlerName);
            f.setConfiguration(handler, e);
            return handler;
        }
        return null;
    }

    @SuppressWarnings({"WhileLoopReplaceableByForEach"})
    private void addListeners() throws ConfigurationException
    {
        requestListeners = new ArrayList<ISORequestListener>();
        QFactory factory = getFactory();
        Iterator iter = getPersist().getChildren("request-listener").iterator();
        while (iter.hasNext())
        {
            Element l = (Element) iter.next();
            ISORequestListener listener = (ISORequestListener) factory.newInstance(l.getAttributeValue("class"));
            factory.setLogger(listener, l);
            factory.setConfiguration(listener, l);
            requestListeners.add(listener);
        }
    }

    public void sessionCreated(IoSession session) throws Exception
    {
    }

    public void sessionOpened(IoSession session) throws Exception
    {
        connectedSessions++;
        getLog().info(getName() + ".session" + session.getRemoteAddress(), "session started");
    }

    public void sessionClosed(IoSession session) throws Exception
    {
        connectedSessions--;
        getLog().info(getName() + ".session" + session.getRemoteAddress(), "session ended");
    }

    public void sessionIdle(IoSession session, IdleStatus idleStatus) throws Exception
    {
        if(timeout>0)
        {
            session.close(true);
        }
    }

    public void exceptionCaught(IoSession session, Throwable throwable) throws Exception
    {
        if (throwable instanceof ISOException && ignoreISOExceptions)
        {
            return;
        }

        getLog().error(getName() + ".session" + session.getRemoteAddress(), throwable);
        session.close(true);
    }

    @SuppressWarnings({"WhileLoopReplaceableByForEach"})
    public void messageReceived(IoSession session, Object o) throws Exception
    {
        if(o instanceof NullMessage)
        {
            session.write(o);
        }
        else if(o instanceof ISOMsg)
        {
            getLog().info(getName() + ".session" + session.getRemoteAddress(), o);
            ISOMsg m = (ISOMsg) o;
            Iterator iter = requestListeners.iterator();
            while (iter.hasNext())
            {
                final SessionISOSource source = new SessionISOSource(session);
                m.setSource(source);
                m.setDirection(ISOMsg.INCOMING);

                if (((ISORequestListener) iter.next()).process(source, m))
                {
                    break;
                }
            }
        }
    }

    public void messageSent(IoSession session, Object o) throws Exception
    {
        if (o instanceof ISOMsg)
        {
            getLog().info(getName() + ".session" + session.getRemoteAddress(), o);
        }
    }

    public String getCountersAsString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("connected=").append(acceptor.getManagedSessionCount());
        return sb.toString();
    }
}
