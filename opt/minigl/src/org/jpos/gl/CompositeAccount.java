/*
 * MiniGL
 * Copyright (C) 2005 Alejandro Revilla
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.jpos.gl;

import java.util.Set;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.text.ParseException;
import org.jdom.Element;

/**
 * Composite Account 
 * 
 * @see GLEntry
 * @see Account
 * @see FinalAccount
 *
 * @author <a href="mailto:apr@jpos.org">Alejandro Revilla</a>
 */
public class CompositeAccount extends Account {
    Set children;
    public CompositeAccount () {
        super ();
    }
    public CompositeAccount (Element elem)
        throws ParseException 
    {
        super ();
        setParent (parent);
        fromXML (elem);
    }
    public CompositeAccount (Element elem, Account parent) 
        throws ParseException 
    {
        super ();
        setParent (parent);
        fromXML (elem);
    }
    public void setChildren (Set children) {
        this.children = children;
    }
    public Set getChildren () {
        if (children == null)
            children = new TreeSet();
        return children;
    }
    public Element toXML () {
        Element elem = super.toXML (new Element ("composite-account"));
        Iterator iter = getChildren().iterator();
        while (iter.hasNext()) {
            Account acct = (Account) iter.next();
            elem.addContent (acct.toXML ());
        }
        return elem;
    }
}
