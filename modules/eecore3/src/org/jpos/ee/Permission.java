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

package org.jpos.ee;

import java.io.Serializable;

import org.jpos.transaction.Constants;

public class Permission implements Serializable, Constants {
    String name;
    public Permission () {
        super ();
        setName ("");
    }
    public Permission (String name) {
        super ();
        setName (name);
    }
    public void setName (String name) {
        this.name = name;
    }
    public String getName () {
        return name;
    }
    public boolean equals (Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj instanceof Permission) {
            return this.getName().equals (((Permission)obj).getName());
	}
	return false;
    }
    public String toString () {
        return getName ();
    }
    public int hashCode() {
        return name.hashCode ();
    }
}

