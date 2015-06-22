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

import org.jpos.q2.QBeanSupport;
import org.jpos.space.Space;
import org.jpos.space.TSpace;
import org.jpos.space.SpaceFactory;
import org.jpos.core.ConfigurationException;
import org.jpos.util.NameRegistrar;
import org.jdom.Element;

/**
 * RemoteSpaceAdaptor
 * @author Alejandro Revilla
 */
public class ReplicatedSpaceAdaptor extends QBeanSupport {
    private Space sp = null;
    private ReplicatedSpace rs = null;
    private String rspaceUri = null;

    public ReplicatedSpaceAdaptor () {
        super ();
    }
    public void initService() throws ConfigurationException {
        Element e = getPersist ();
        Space sp  = SpaceFactory.getSpace (cfg.get ("space", ""));
        rspaceUri = cfg.get ("rspace", "rspace");
        try {
            rs = new ReplicatedSpace (
                sp,
                cfg.get ("group", "rspace"),
                cfg.get ("config", "cfg.jgroups.xml"),
                getLog().getLogger(),
                getLog().getRealm(),
                cfg.getBoolean ("trace"),
                cfg.getBoolean ("replicate", sp instanceof TSpace)
            );
            NameRegistrar.register (rspaceUri, rs);
        } catch (Throwable t) {
            throw new ConfigurationException (t);
        }
    }
    protected void stopService () throws Exception {
        if (rs != null)
            rs.close();
        NameRegistrar.unregister (rspaceUri);
    }
}

