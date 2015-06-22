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

package org.jpos.q2.jetty;

import java.io.FileInputStream;
import org.jpos.q2.QBeanSupport;
import org.mortbay.jetty.Server;
import org.mortbay.xml.XmlConfiguration;

/**
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 * @jmx:mbean description="Jetty QBean" extends="org.jpos.q2.QBeanSupportMBean"
 */
public class Jetty6 extends QBeanSupport implements Jetty6MBean {
    String config;
    Server server;
    
    public void initService () throws Exception {
        server = new Server();
        FileInputStream fis = new FileInputStream(config);
        XmlConfiguration xml = new XmlConfiguration(fis);
        xml.configure(server);
    }
    public void startService () throws Exception {
        server.start ();
    }
    public void stopService () throws Exception {
        server.stop ();
    }
    /**
     * @jmx:managed-attribute description="Configuration File"
     */
    public void setConfig (String config) {
        this.config = config;
    }
    /**
     * @jmx:managed-attribute description="Configuration File"
     */
    public String getConfig () {
        return config;
    }
}