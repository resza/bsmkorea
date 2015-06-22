/*
 *  jPOS Extended Edition
 *  Copyright (C) 2005 Alejandro P. Revilla
 *  jPOS.org (http://jpos.org)
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.jpos.space;

import java.io.IOException;
import java.io.Serializable;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.Map;
import java.util.UUID;
import org.jpos.iso.ISOUtil;
import org.jpos.util.Log;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.View;
import org.jgroups.Message;
import org.jgroups.Address;
import org.jgroups.MessageListener;
import org.jgroups.MembershipListener;
import org.jgroups.ChannelException;
import org.jgroups.Receiver;
import org.jgroups.conf.XmlConfigurator;

public class ReplicatedSpace 
    extends Log
    implements Space, Receiver 
{
    Channel channel;
    String nodeName;
    String nodePrefix;
    String seqName;
    Space sp;
    View view;
    boolean trace;
    boolean replicate;
    public static final long TIMEOUT    = 15000L;
    public static final long MAX_WAIT   = 500L;
    public static final long ONE_MINUTE = 60000L;
    public static final long FIVE_MINUTES = 5*60000L;

    public ReplicatedSpace (
            Space sp, 
            String groupName, 
            String configFile, 
            Logger logger, 
            String realm,
            boolean trace, boolean replicate)
        throws ChannelException, IOException
    {
        super ();
        this.sp = sp;
        setLogger (logger, realm);
        initChannel(groupName, configFile);
        this.nodeName = channel.getLocalAddress().toString();
        this.nodePrefix = nodeName + ".";
        this.seqName  = nodeName + ".seq";
        this.trace = trace;
        this.replicate = replicate;
    }
    public ReplicatedSpace 
        (Space sp, String groupName, String configFile)
        throws ChannelException, IOException
    {
        this (sp, groupName, configFile, null, null, false, false);
    }
    public void close() throws IOException {
        block();
        channel.close();
    }
    public void out (Object key, Object value) { 
        out (key, value, 0L);
    }
    public void out (Object key, Object value, long timeout) { 
        getCoordinator();   
        try {
            Request r = new Request (Request.OUT, key, value, timeout);
            channel.send (new Message (null, null, r));
            sp.rd (new MD5Template (key, value), MAX_WAIT);
        } catch (ChannelException e) {
            throw new SpaceError (e);
        }
    }
    public void push (Object key, Object value) { 
        push (key, value, 0L);
    }
    public void push (Object key, Object value, long timeout) { 
        Address coordinator = getCoordinator();   
        try {
            Request r = new Request (Request.PUSH, key, value, timeout);
            channel.send (new Message (coordinator, null, r));
            sp.rd (new MD5Template (key, value), MAX_WAIT);
        } catch (ChannelException e) {
            throw new SpaceError (e);
        }
    }
    public Object rdp (Object key) {
        Request r = new Request (Request.RDP, key, 0);
        r.value = Long.toString (r.hashCode());
        sendToCoordinator (r);
        Object obj = sp.in (r.value, MAX_WAIT);
        if (obj instanceof NullPointerException)
            obj = null;
        return obj;
    }
    public Object inp (Object key) {
        Request r = new Request (Request.INP, key, 0);
        r.value = UUID.randomUUID().toString();
        sendToCoordinator (r);
        Object obj = sp.in (r.value, MAX_WAIT);
        if (obj instanceof NullPointerException)
            obj = null;
        return obj;
    }
    public void receive (Message msg) { 
        LogEvent evt = null;
        Object obj = msg.getObject();
        if (trace && logger != null) {
            evt = createTrace (" receive: " + msg.toString());
            if (obj != null) {
                evt.addMessage (" object: " + obj.toString());
            }
        }
        if (obj instanceof Request) {
            Request r = (Request) obj;
            switch (r.type) {
                case Request.OUT:
                    if (r.timeout != 0) 
                        sp.out (r.key, r.value, r.timeout + TIMEOUT);
                    else
                        sp.out (r.key, r.value);
                    synchronized (this) {
                        notifyAll();
                    }
                    break;
                case Request.PUSH:
                    if (r.timeout != 0) 
                        sp.push (r.key, r.value, r.timeout + TIMEOUT);
                    else
                        sp.push (r.key, r.value);
                    synchronized (this) {
                        notifyAll();
                    }
                    break;
                case Request.RDP:
                    send (msg.getSrc(), 
                        new Request (
                            Request.RDP_RESPONSE, 
                            r.value, // value is ref key for response
                            sp.rdp (r.key)
                        )
                    );
                    break;
                case Request.RDP_RESPONSE:
                    if (r.value == null)
                        r.value = new NullPointerException();
                    sp.out (r.key, r.value, MAX_WAIT);
                    break;
                case Request.INP:
                    Object v = sp.inp (r.key);
                    if (v != null) {
                        send (null,
                            new Request (
                                Request.INP_NOTIFICATION, 
                                r.key, // value is ref key for response
                                new MD5Template (r.key, v)
                            )
                        );
                    }
                    send (msg.getSrc(), 
                        new Request (
                            Request.INP_RESPONSE, 
                            r.value, // value is ref key for response
                            v
                        )
                    );
                    break;
                case Request.INP_RESPONSE:
                    if (r.value == null)
                        r.value = new NullPointerException();
                    sp.out (r.key, r.value, MAX_WAIT);
                    break;
                case Request.INP_NOTIFICATION:
                    // if not self notification
                    if (!channel.getLocalAddress().equals (msg.getSrc()))
                        sp.inp (r.value);
                    break;
                case Request.SPACE_COPY:
                    if (replicate && !isCoordinator() && sp instanceof TSpace) {
                        ((TSpace)sp).setEntries ((Map) r.value);
                        synchronized (this) {
                            notifyAll();
                        }
                    }
                    break;
            }
        } else if (evt != null) {
            evt.addMessage ("  class: " + obj.getClass().getName());
        }
        if (evt != null)
            Logger.log (evt);
    }
    public boolean existAny (Object[] keys) {
        for (int i=0; i<keys.length; i++) {
            if (rdp (keys[i]) != null)
                return true;
        }
        return false;
    }
    public boolean existAny (Object[] keys, long timeout) {
        long now = System.currentTimeMillis();
        long end = now + timeout;
        synchronized (sp) {
            while (((now = System.currentTimeMillis()) < end)) {
                if (existAny (keys))
                    return true;
                try {
                    sp.wait (end - now);
                } catch (InterruptedException e) { }
            }
        }
        return false;
    }
    // ----------------------------------------------------------------
   
    public synchronized Object rd (Object key) {
        Object obj;
        while ((obj = rdp (key)) == null) {
            try {
                wait (MAX_WAIT);
            } catch (InterruptedException e) { }
        }
        return obj;
    }
    public synchronized Object rd  (Object key, long timeout) {
        Object obj;
        long now = System.currentTimeMillis();
        long end = now + timeout;
        while ((obj = rdp (key)) == null && 
                ((now = System.currentTimeMillis()) < end))
        {
            try {
                wait (Math.min (MAX_WAIT, (end - now)));
            } catch (InterruptedException e) { }
        }
        return obj;
    }
    public synchronized Object in (Object key) {
        return in (key, Long.MAX_VALUE);
    }
    public synchronized Object in (Object key, long timeout) {
        Object obj;
        long now = System.currentTimeMillis();
        long end = now + timeout;
        while ((obj = inp (key)) == null && 
                ((now = System.currentTimeMillis()) < end))
        {
            try {
                wait (Math.min (MAX_WAIT, (end - now)));
            } catch (InterruptedException e) { }
        }
        return obj;
    }
    public void setTrace (boolean trace) {
        this.trace = trace;
    }
    public boolean isTrace() {
        return trace;
    }
    private Address getCoordinator () {
        assertChannel();
        if (view != null)
            return view.getMembers().get(0);

        throw new SpaceError ("Channel not ready - coordinator is null");
    }
    private void assertChannel () {
        if (!channel.isConnected())
            throw new SpaceError ("Channel is not connected");
    }
    /** Called when a member is suspected */
    public void suspect (Address suspected_mbr) {
        //
    }
    /** Block sending and receiving of messages until ViewAccepted is called */
    public void block () {
        this.view = null;
    }
    public void viewAccepted (View view) {
        this.view = view;
        if (logger != null) {
            LogEvent evt = createInfo ("view-accepted");
            evt.addMessage (view.toString());
            Logger.log (evt);
        }
        if (replicate && isCoordinator() && sp instanceof TSpace) {
            new Thread () {
                public void run() {
                    send (null,
                        new Request (
                            Request.SPACE_COPY, 
                            null, // value is ref key for response
                            ((TSpace)sp).getEntries()
                        )
                    );
                }
            }.start();
        }
    }
    public boolean isCoordinator () {
        return channel.getLocalAddress().equals (view.getMembers().get(0));
    }
    public void setState(byte[] new_state) {
        // 
    }
    public void setReplicate (boolean replicate) {
        this.replicate = replicate;
    }
    public boolean isReplicate () {
        return replicate;
    }
    public byte[] getState() {
        return "DummyState".getBytes();
    }
    private void commitOff() {
        if (sp instanceof JDBMSpace) 
            ((JDBMSpace)sp).setAutoCommit(false);
    }
    private void commitOn() {
        if (sp instanceof JDBMSpace) 
            ((JDBMSpace)sp).setAutoCommit(true);
    }
    private void send (Address destination, Request r) 
    {
        try {
            channel.send (new Message (destination, null, r));
        } catch (ChannelException e) {
            error (e);
        }
    }
    private void sendToCoordinator (Request r) 
    {
        while (true) {
            Address coordinator = getCoordinator();
            try {
                channel.send (new Message (coordinator, null, r));
                break;
            } catch (ChannelException e) {
                error ("error " + e.getMessage() + ", retrying");
                try {
                    Thread.sleep (MAX_WAIT);
                } catch (InterruptedException ex) { }
            }
        }
    }
    private void initChannel (String groupName, String configFile) 
        throws ChannelException, IOException 
    {
        InputStream config = new FileInputStream (configFile);
        XmlConfigurator conf = XmlConfigurator.getInstance (config);
        String props = conf.getProtocolStackString();
        channel = new JChannel (props);
        // channel.setOpt(Channel.GET_STATE_EVENTS, Boolean.TRUE);
        channel.setOpt(Channel.AUTO_RECONNECT, Boolean.TRUE);
        channel.setReceiver(this);
        channel.connect (groupName);
        info ("member: " + channel.getLocalAddress().toString());
    }
    public static class Request implements Serializable {
        static final long serialVersionUID = -7667532143057033544L;
        static final int OUT=1;
        static final int PUSH=2;
        static final int RDP=3;
        static final int RDP_RESPONSE=4;
        static final int INP=5;
        static final int INP_RESPONSE=6;
        static final int INP_NOTIFICATION=7;
        static final int SPACE_COPY=8;
        static final String[] types = { 
            "", "OUT", "PUSH", "RDP", "RDP_RESPONSE", 
            "INP", "INP_RESPONSE", "INP_NOTIFICATION", 
            "SPACE_COPY"
        };

        public int type=0;
        public Object key=null;
        public Object value=null;
        public long timeout=0;

        public Request() {
            super();
        }
        public Request(int type, Object key, Object value, long timeout) {
            super ();
            this.type    = type;
            this.key     = key;
            this.value   = value;
            this.timeout = timeout;
        }
        public Request(int type, Object key, Object value) {
            super ();
            this.type    = type;
            this.key     = key;
            this.value   = value;
        }
        public String toString() {
            StringBuffer sb=new StringBuffer();
            sb.append (type2String (type));
            if(key != null) {
                sb.append(" key=" + key);
            }
            if(value != null) {
                if (value instanceof byte[])
                    sb.append (" value=" + ISOUtil.hexString ((byte[]) value));
                else
                    sb.append(" value=" + value);
            }
            sb.append (" timeout=" + timeout);
            return sb.toString();
        }
        String type2String (int type) {
            return type < types.length ? types [type] : "invalid";
        }
    }
}

