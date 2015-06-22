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

import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.criterion.Expression;
import org.hibernate.Session;

/**
 * @author Alejandro Revilla
 */
public class UserManager {
    Session session;
    String digest;

    public UserManager (DB db) {
        super ();
        this.session = db.session();
    }
    public UserManager (Session session) {
        super ();
        this.session = session;
    }
    public User getUserByNick (String nick, boolean includeDeleted)
        throws HibernateException
    {
        try {
            Criteria crit = session.createCriteria (User.class)
                .add (Expression.eq ("nick", nick));
            if (!includeDeleted)
                crit = crit.add (Expression.eq ("deleted", Boolean.FALSE));
            return (User) crit.uniqueResult();
        } catch (ObjectNotFoundException e) { }
        return null;
    }
    public User getUserByNick (String nick)
        throws HibernateException
    {
        return getUserByNick (nick, false);
    }
    /**
     * @param nick name.
     * @param seed initial seed
     * @param pass hash
     * @throws BLException if invalid user/pass
     */
    public User getUserByNick (String nick, String seed, String pass) 
        throws HibernateException, BLException
    {
        User u = getUserByNick (nick);
        assertNotNull (u, "User does not exist");
        assertTrue (checkPassword (u, seed, pass), "Invalid password");
        return u;
    }
    /**
     * @param nick name.
     * @param seed initial seed
     * @param pass hash
     * @return true if password matches
     * @throws BLException if invalid user/pass
     */
    public boolean checkPassword (User u, String seed, String pass) 
        throws HibernateException, BLException
    {
        assertNotNull (seed, "Invalid seed");
        assertNotNull (pass, "Invalid pass");
        String password = u.getPassword();
        assertNotNull (password, "Password is null");
        String computedPass = EEUtil.getHash (seed, password);
        return pass.equals (computedPass);
    }
    /**
     * @return all users
     */
    public List findAll () throws HibernateException {
        return session.createCriteria (User.class)
                .add (Expression.eq ("deleted", Boolean.FALSE))
                .list();
    }
    private void assertNotNull (Object obj, String error) throws BLException {
        if (obj == null)
            throw new BLException (error);
    }
    private void assertTrue (boolean condition, String error) 
        throws BLException 
    {
        if (!condition)
            throw new BLException (error);
    }
}

