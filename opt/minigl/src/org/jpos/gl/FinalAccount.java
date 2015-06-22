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
import java.math.BigDecimal;
import java.text.ParseException;
import org.jdom.Element;

/**
 * Final (leaf) Account.
 * 
 * @see GLEntry
 * @see Account
 * @see CompositeAccount
 *
 * @author <a href="mailto:apr@jpos.org">Alejandro Revilla</a>
 */
public class FinalAccount extends Account {
    public FinalAccount () {
        super ();
    }
    public FinalAccount (Element elem, Account parent) throws ParseException {
        super ();
        setParent (parent);
        fromXML (elem);
    }
    public void setChildren (Set children) 
        throws IllegalArgumentException
    {
        throw new IllegalArgumentException (
            "Can't setChildren on FinalAccount"
        );
    }
    public Set getChildren () {
        return null;
    }
    public Element toXML () {
        Element elem = super.toXML (new Element ("account"));
        return elem;
    }
}

