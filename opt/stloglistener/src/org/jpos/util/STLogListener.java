/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2007 Alejandro P. Revilla
 *
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

package org.jpos.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import org.jpos.iso.ISOMsg;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.Configurable;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.StringTemplateGroupLoader;
import org.antlr.stringtemplate.StringTemplateErrorListener;
import org.antlr.stringtemplate.PathGroupLoader;
import org.antlr.stringtemplate.language.AngleBracketTemplateLexer;


/**
 * @author apr
 * @since jPOS 6
 */
public class STLogListener implements LogListener, Configurable, StringTemplateErrorListener {
    StringTemplateGroup templates;
    String separator;

    public STLogListener () {
        super();
    }
    public synchronized LogEvent log (LogEvent ev) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream p = new PrintStream (baos);
        String className = ev.getClass().getName().replace(".", separator);
        StringTemplate t = templates.getInstanceOf(className);
        t.setAttribute ("date", new Date());
        t.setAttribute ("tag", ev.tag);
        t.setAttribute ("realm", ev.getRealm());
        Iterator i = ev.payLoad.iterator();
        while (i.hasNext()) {
            t.setAttribute ("evt", render (i.next()));
        }
        p.println (t.toString());
        p.flush();
        ev = new LogEvent (null) {
            protected String dumpHeader (PrintStream p, String s) { return ""; }
            protected void dumpTrailer (PrintStream p, String s) { }
        };
        ev.addMessage (baos.toString());
        return ev;
    }
    protected String render (Object obj) {
        Class clazz = obj.getClass();
        StringTemplate t = null;
        while (t == null) {
            try {
                String className = clazz.getName().replace(".", separator);
                t = templates.getInstanceOf(className);
                if (t != null) {
                    if (Object.class.equals (clazz) &&
                        obj instanceof Loggeable) 
                    {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        PrintStream p = new PrintStream (baos);
                        ((Loggeable)obj).dump (p, "");
                        t.setAttribute ("o", baos.toString());
                    } 
                    /* else if (obj instanceof ISOMsg) {
                        // special handling to our friend
                        ISOMsg m = (ISOMsg) obj;
                        t.setAttribute ("m", m);
                        t.setAttribute ("mti", m.getString(0));
                        Map fmap = new LinkedHashMap();
                        Map nmap = new LinkedHashMap();
                        ISOPackager p = m.getPackager();
                        for (int i=1; i<m.getMaxField(); i++) {
                            if (m.hasField(i)) {
                                String fldno = Integer.toString(i);
                                t.setAttribute ("f", fldno);
                                fmap.put (fldno, m.getString(i));
                                if (p != null) {
                                    nmap.put(fldno, p.getFieldDescription(m.getComponent(i), i));
                                }
                            }
                        }
                        t.setAttribute ("fields", fmap);
                        t.setAttribute ("names", nmap);
                    } */
                    else 
                        t.setAttribute ("o", obj);
                    break;
                } 
            } catch (IllegalArgumentException e) { }
            clazz = clazz.getSuperclass();
        }
        return t.toString();
    }
    public void setConfiguration (Configuration cfg) 
        throws ConfigurationException 
    {
        try {
            String skin = cfg.get ("skin", "jpos.stg");
            if (skin.toLowerCase().endsWith(".stg")) {
                StringTemplateGroupLoader loader = new PathGroupLoader(cfg.get ("path", "cfg"), this);
                skin = skin.substring(0,skin.length()-4);
                templates = loader.loadGroup (skin);
                separator = "_";
            } else {
                separator = "/";
                templates = new StringTemplateGroup (
                    "jpos", cfg.get ("path", "cfg") + separator + skin
                );
            }
            templates.registerRenderer (Date.class, new DateRenderer());
            templates.registerRenderer (ISOMsg.class, new ISOMsgRenderer());
        } catch (Exception e) {
            throw new ConfigurationException (e);
        }
    }
    public void error (String msg, Throwable t) {
        System.err.println (msg);
        t.printStackTrace (System.err);
    }
    public void warning (String msg) {
        System.err.println (msg);
    }
}

