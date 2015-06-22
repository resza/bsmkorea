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

/**
 * Permissions are associated with Users and optionally with Journals.
 *
 * A {@link User} can have some system wide permissions and some
 * others specific to a given {@link Journal}.
 *
 * @see User
 * @see Journal
 * @author <a href="mailto:apr@jpos.org">Alejandro Revilla</a>
 */
public class GLPermission {
    public static final String READ       = "read";
    public static final String WRITE      = "write";
    public static final String POST       = "post";
    public static final String CHECKPOINT = "checkpoint";
    public static final String SUMMARIZE  = "summarize";
    public static final String GRANT      = "grant";

    long id;
    String name;
    GLUser user;
    Journal journal;

    /**
     * Constructs a Permission object with a "" name.
     *
     * The name can be modified later by calling {@link #setName}.
     */
    public GLPermission () {
        super ();
        setName ("");
    }
    /**
     * Constructs a Permission with a given name.
     * @param name the Name.
     */
    public GLPermission (String name) {
        super ();
        setName (name);
    }
    /**
     * internal id 
     */
    public void setId (long id) {
        this.id = id;
    }
    /**
     * @return internal Id
     */
    public long getId() {
        return id;
    }
    /**
     * @param name permission's name.
     */
    public void setName (String name) {
        this.name = name;
    }
    /**
     * @return permission's name.
     */
    public String getName () {
        return name;
    }
    /**
     * Permission belongs to user.
     * @param user
     */
    public void setUser (GLUser user) {
        this.user = user;
    }
    /**
     * @return the user.
     */
    public GLUser getUser () {
        return user;
    }
    /**
     * Permission is associated with journal.
     * @param journal the journal.
     */
    public void setJournal (Journal journal) {
        this.journal = journal;
    }
    /**
     * @return associated journal
     */
    public Journal getJournal () {
        return journal;
    }
    public boolean equals (Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj instanceof GLPermission) {
            return getId() == ((GLPermission)obj).getId();
	}
	return false;
    }
    public String toString () {
        return getName ();
    }
}

