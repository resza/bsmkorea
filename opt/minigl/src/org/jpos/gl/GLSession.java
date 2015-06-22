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

import java.util.Map;
import java.util.Set;
import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;
import java.math.BigDecimal;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Criteria;
import org.hibernate.LockMode;
import org.hibernate.ScrollMode;
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.ScrollableResults;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Expression;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.type.LongType;
import org.hibernate.Hibernate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jpos.ee.DB;

/**
 * MiniGL facility entry point.
 *
 * @author <a href="mailto:apr@jpos.org">Alejandro Revilla</a>
 */
public class GLSession {
    private static Configuration cfg;
    private static SessionFactory sf;
    private static Map ruleCache;
    private GLUser user;
    private Session session;
    private static final Log log = LogFactory.getLog (GLSession.class);
    private long checkpoint;
    private DB db;
    public static final short[] LAYER_ZERO = new short[] { 0 };
    /** 
     * "0.00"
     */
    public static final BigDecimal ZERO = new BigDecimal ("0.00");
    /** 
     * "0" (no decimals)
     */
    public static final BigDecimal Z    = new BigDecimal ("0");

    static {
        try {
            // init ();
            ruleCache = new HashMap();
        } catch (Exception e) {
            log.fatal (e);
        }
    }
    /**
     * Construct a GLSession for a given user.
     *
     * User has to exist in MiniGL gluser table.
     * @see GLUser
     *
     * @param username the user name.
     */
    public GLSession (String username) 
        throws HibernateException, GLException
    {
        super();
        this.db = new DB();
        session = db.open();
        user = getUser (username);
        if (user == null) {
           close();
           throw new GLException ("Invalid user '" + username + "'");
        }
    }
    /**
     * Construct a GLSession using property <code>user.name</code>.
     * User has to exist in MiniGL gluser table.
     * @see User
     */
    public GLSession () throws HibernateException, GLException {
        this (System.getProperty ("user.name"));
    }
    /**
     * Construct a GLSession for a given user.
     *
     * User has to exist in MiniGL gluser table.
     * @see GLUser
     *
     * @param db EE DB
     * @param username the user name.
     */
    public GLSession (DB db, String username) 
        throws HibernateException, GLException
    {
        super();
        this.db = db;
        session = db.open();
        user = getUser (username);
        if (user == null) {
           close();
           throw new GLException ("Invalid user '" + username + "'");
        }
    }
    /**
     * Construct a GLSession using property <code>user.name</code>.
     * User has to exist in MiniGL gluser table.
     * @param db EE DB
     * @see User
     */
    public GLSession (DB db) throws HibernateException, GLException {
        this (db, System.getProperty ("user.name"));
    }
    /**
     * @param action name
     * @return true if user has permission to perform given action
     * @see GLPermission
     */
    public boolean hasPermission (String action) {
        Iterator iter = user.getPermissions().iterator();
        while (iter.hasNext()) {
            GLPermission p = (GLPermission) iter.next();
            if (p.getJournal() == null && action.equals (p.getName()))
                return true;
        }
        return false;
    }
    /**
     * @param action name
     * @throws GLException if user doesn't have permission.
     * @see GLPermission
     */
    public void checkPermission (String action) throws GLException {
        if (!hasPermission (action)) {
            throw new GLException (
                "User '" + user.getName() + "' (" + user.getId() + 
                ") does not have '" + action + "' permission."
            );
        }
    }
    /**
     * Grant permission to user.
     * In order to grant a permission, we need to have both the permission
     * and GRANT.
     *
     * @param userName user name
     * @param permName permission name
     */
    public void grant (String userName, String permName) 
        throws GLException, HibernateException
    {
        checkPermission (GLPermission.GRANT);
        checkPermission (permName);
        GLPermission perm = new GLPermission(permName);
        session.save (perm);
        GLUser u = getUser (userName);
        u.grant (perm);
    }
    /**
     * Revoke permission from user.
     * In order to grant a permission, we need to have both the permission
     * and GRANT.
     *
     * @param userName user name
     * @param permName permission name
     */
    public void revoke (String userName, String permName) 
        throws GLException, HibernateException
    {
        checkPermission (GLPermission.GRANT);
        GLUser u = getUser (userName);
        u.revoke (permName);
    }

    /**
     * Verifies user's permission in a given journal.
     * @param action name
     * @param j journal
     * @return true if user has permission to perform given action.
     * @see GLPermission
     * @see Journal
     */
    public boolean hasPermission (String action, Journal j) {
        Iterator iter = user.getPermissions().iterator();
        while (iter.hasNext()) {
            GLPermission p = (GLPermission) iter.next();
            Journal pj = p.getJournal();
            if (action.equals (p.getName()) && 
                (pj == null || (pj.getId() == j.getId())))
            {
                return true;
            }
        }
        return false;
    }
    /**
     * Check user's permission in a given journal.
     * @param action name
     * @param j journal
     * @throws GLException if user doesn't have permission.
     * @see GLPermission
     * @see Journal
     */
    public void checkPermission (String action, Journal j) throws GLException {
        if (!hasPermission (action, j)) {
            throw new GLException (
                "User '" + user.getName() + "' (" + user.getId() + 
                ") does not have '" + action + "' permission in journal '" + 
                j.getName() + "' (" + j.getId() + ")"
            );
        }
    }
    /**
     * @param code chart of account's code
     * @return top level chart with given code or null.
     * @throws HibernateException on database errors.
     * @throws GLException if users doesn't have global READ permission.
     * @see GLPermission
     */
    public Account getChart (String code) 
        throws HibernateException, GLException
    {
        checkPermission (GLPermission.READ);
        Query q = session.createQuery (
            "from acct in class org.jpos.gl.CompositeAccount where code=:code and parent is null"
        );
        q.setParameter ("code", code);
        Iterator iter = q.list().iterator();
        return (Account) (iter.hasNext() ? iter.next() : null);
    }
    /**
     * @param chart chart of accounts.
     * @param code  account's code.
     * @return account with given code in given chart, or null.
     *
     * @throws HibernateException on database errors.
     * @throws GLException if users doesn't have global READ permission.
     * @see GLPermission
     */
    public Account getAccount (Account chart, String code) 
        throws HibernateException, GLException
    {
        checkPermission (GLPermission.READ);
        Query q = session.createQuery (
            "from acct in class org.jpos.gl.Account where root=:chart and code=:code"
        );
        q.setLong ("chart", chart.getId());
        q.setParameter ("code", code);
        Iterator iter = q.list().iterator();
        return (Account) (iter.hasNext() ? iter.next() : null);
    }
    /**
     * Add account to parent.
     * Check permissions, parent's type and optional currency.
     *
     * @param parent parent account
     * @param acct account to add
     * @throws HibernateException
     * @throws GLException if user doesn't have permissions, or type mismatch
     */
    public void addAccount (CompositeAccount parent, Account acct) 
        throws HibernateException, GLException
    {
        checkPermission (GLPermission.WRITE);
        if (!parent.isChart() && !parent.equalsType (acct)) {
            StringBuffer sb = new StringBuffer ("Type mismatch ");
            sb.append (parent.getTypeAsString());
            sb.append ('/');
            sb.append (acct.getTypeAsString());
            throw new GLException (sb.toString());
        }
        String currencyCode = parent.getCurrencyCode();
        if (currencyCode != null 
            && !currencyCode.equals (acct.getCurrencyCode())) 
        {
            StringBuffer sb = new StringBuffer ("Currency mismatch ");
            sb.append (currencyCode);
            sb.append ('/');
            sb.append (acct.getCurrencyCode());
            throw new GLException (sb.toString());
        }
        acct.setRoot (parent.getRoot());
        session.save (acct);
        // acct.setParent (parent);
        parent.getChildren().add (acct);
    }
    /**
     * @param chart chart of accounts.
     * @param code  account's code.
     * @return final account with given code in given chart, or null.
     *
     * @throws HibernateException on database errors.
     * @throws GLException if users doesn't have global READ permission.
     * @see GLPermission
     */
    public FinalAccount getFinalAccount (Account chart, String code) 
        throws HibernateException, GLException
    {
        checkPermission (GLPermission.READ);
        Query q = session.createQuery (
            "from acct in class org.jpos.gl.FinalAccount where root=:chart and code=:code"
        );
        q.setLong ("chart", chart.getId());
        q.setParameter ("code", code);
        Iterator iter = q.list().iterator();
        return (FinalAccount) (iter.hasNext() ? iter.next() : null);
    }
    /**
     * @param chart chart of accounts.
     * @param code  account's code.
     * @return composite account with given code in given chart, or null.
     *
     * @throws HibernateException on database errors.
     * @throws GLException if users doesn't have global READ permission.
     * @see GLPermission
     */
    public CompositeAccount getCompositeAccount (Account chart, String code) 
        throws HibernateException, GLException
    {
        checkPermission (GLPermission.READ);
        Query q = session.createQuery (
            "from acct in class org.jpos.gl.CompositeAccount where root=:chart and code=:code"
        );
        q.setLong ("chart", chart.getId());
        q.setParameter ("code", code);
        Iterator iter = q.list().iterator();
        return (CompositeAccount) (iter.hasNext() ? iter.next() : null);
    }
    /**
     * @param chartName chart of account's code.
     * @param code  account's code.
     * @return account with given code in given chart, or null.
     *
     * @throws HibernateException on database errors.
     * @throws GLException if users doesn't have global READ permission.
     * @see GLPermission
     */
    public Account getAccount (String chartName, String code) 
        throws HibernateException, GLException
    {
        Account chart = getChart(chartName);
        if (chart == null)
            throw new GLException ("Chart '" + chartName + "' does not exist");
        return getAccount(chart, code);
    }
    /**
     * @param chartName chart of account's code.
     * @param code  account's code.
     * @return final account with given code in given chart, or null.
     *
     * @throws HibernateException on database errors.
     * @throws GLException if users doesn't have global READ permission.
     * @see GLPermission
     */
    public FinalAccount getFinalAccount (String chartName, String code) 
        throws HibernateException, GLException
    {
        Account chart = getChart(chartName);
        if (chart == null)
            throw new GLException ("Chart '" + chartName + "' does not exist");
        return getFinalAccount (chart, code);
    }
    /**
     * @param chartName chart of account's code.
     * @param code  account's code.
     * @return composite account with given code in given chart, or null.
     *
     * @throws HibernateException on database errors.
     * @throws GLException if users doesn't have global READ permission.
     * @see GLPermission
     */
    public CompositeAccount getCompositeAccount (String chartName, String code) 
        throws HibernateException, GLException
    {
        Account chart = getChart(chartName);
        if (chart == null)
            throw new GLException ("Chart '" + chartName + "' does not exist");
        return getCompositeAccount (chart, code);
    }

    /**
     * @param name journal's name.
     * @return journal or null.
     * @throws GLException if users doesn't have global READ permission.
     * @throws HibernateException on database errors.
     * @see GLPermission
     */
    public Journal getJournal (String name) 
        throws HibernateException, GLException
    {
        checkPermission (GLPermission.READ);
        Query q = session.createQuery (
            "from journal in class org.jpos.gl.Journal where name=:name"
        );
        q.setParameter ("name", name);
        Iterator iter = q.list().iterator();
        return (Journal) (iter.hasNext() ? iter.next() : null);
    }

    /**
     * Post transaction in a given journal.
     *
     * @param journal the journal.
     * @param txn the transaction.
     * @throws GLException if user doesn't have POST permission or any rule associated with this journal and/or account raises a GLException.
     * @throws HibernateException on database errors.
     * @see GLPermission
     * @see JournalRule
     */
    public void post (Journal journal, GLTransaction txn) 
        throws HibernateException, GLException
    {
        checkPermission (GLPermission.POST, journal);
        txn.setJournal (journal);
        txn.setTimestamp (new Date());
        if (txn.getPostDate() == null)
            txn.setPostDate (txn.getTimestamp());
        invalidateCheckpoints (txn);
        Collection rules = getRules (txn);
        // dumpRules (rules);
        applyRules (txn, rules);
        session.save (txn);
    }
    /**
     * Moves a transaction to a new journal
     * @param txn the Transaction
     * @param journal the New Journal
     * @throws GLException if user doesn't have POST permission on the old and new journals.
     * @throws HibernateException on database errors.
     */
    public void move (GLTransaction txn, Journal journal) 
        throws GLException, HibernateException
    {
        checkPermission (GLPermission.POST, journal);
        checkPermission (GLPermission.POST, txn.getJournal());
        invalidateCheckpoints (txn);    // invalidate in old journal
        txn.setJournal (journal);
        invalidateCheckpoints (txn);    // invalidate in new journal
        applyRules (txn, getRules (txn));
        session.update (txn);
    }

    /**
     * Summarize transactions in a journal.
     *
     * @param journal the journal.
     * @param start date (inclusive).
     * @param end date (inclusive).
     * @param description summary transaction's description
     * @return GLTransaction a summary transaction
     * @throws GLException if user doesn't have READ permission on this jounral.
     * @throws HibernateException on database/mapping errors
     */
    public GLTransaction summarize 
        (Journal journal, Date start, Date end, String description, short[] layers) 
        throws HibernateException, GLException
    {
        checkPermission (GLPermission.SUMMARIZE, journal);
        start = Util.floor (start);
        end   = Util.ceil (end);

        if (end.compareTo (start) < 0) {
            throw new GLException ("Invalid date range " 
                + Util.dateToString(start) + ":" + Util.dateToString (end));
        }
        Date lockDate = journal.getLockDate();
        if (lockDate != null && start.compareTo (lockDate) <= 0) {
            throw new GLException 
                ("Journal is locked at " + Util.dateToString (lockDate));
        }
        setLockDate (journal, end);

        GLTransaction txn = new GLTransaction (description);
        for (int i=0; i<layers.length; i++) {
            Iterator debits  = findSummarizedGLEntries (journal, start, end, false, layers[i]);
            Iterator credits = findSummarizedGLEntries (journal, start, end, true, layers[i]);
            while (debits.hasNext()) {
                Object[] obj = (Object[]) debits.next();
                txn.createDebit (
                    (FinalAccount) obj[0], 
                    (BigDecimal) obj[1], 
                    null, layers[i]
                );
            }
            while (credits.hasNext()) {
                Object[] obj = (Object[]) credits.next();
                txn.createCredit (
                    (FinalAccount) obj[0], 
                    (BigDecimal) obj[1], 
                    null, layers[i]
                );
            }
        }
        txn.setJournal (journal);
        txn.setTimestamp (new Date());
        txn.setPostDate (end);
        deleteGLTransactions (journal, start, end);
        session.save (txn); // force post - no rule validations
        journal.setLockDate (null);
        return txn;
    }

    /**
     * @param journal the journal.
     * @param id txn id
     * @return GLTransaction or null
     * @throws GLException if user doesn't have READ permission on this jounral.
     */
    public GLTransaction getTransaction (Journal journal, long id)
        throws HibernateException, GLException
    {
        GLTransaction txn = null;
        checkPermission (GLPermission.READ, journal);
        try {
            txn = (GLTransaction) 
                session.load (GLTransaction.class, new Long(id));
            if (txn.getJournal() != journal)
                throw new GLException (
                    "The transaction does not belong to the specified journal"
                );
        } catch (ObjectNotFoundException e) {
            // okay to happen
        }
        return txn;
    }

    /**
     * @param journal the journal.
     * @param start date (inclusive).
     * @param end date (inclusive).
     * @param destinationJournal where to move former transactions. A null value
would delete former transactions.
     * @param searchString optional search string
     * @param findByPostDate true to find by postDate, false to find by timestamp
     * @param pageNumber the page number
     * @param pageSize the page size
     * @return list of transactions
     * @throws GLException if user doesn't have READ permission on this jounral.
     */
    public List findTransactions 
        (Journal journal, Date start, Date end, String searchString, 
         boolean findByPostDate, int pageNumber, int pageSize)
        throws HibernateException, GLException
    {
        checkPermission (GLPermission.READ, journal);
        String dateField = findByPostDate ? "postDate" : "timestamp";
        if (findByPostDate) {
            if (start != null)
                start = Util.floor (start);
            if (end != null)
                end   = Util.ceil (end);
        }
        Criteria crit = session.createCriteria (GLTransaction.class)
            .add (Expression.eq ("journal", journal));

        if (start != null && start.equals (end))
            crit.add (Expression.eq (dateField, start));
        else {
            if (start != null) 
                crit.add (Expression.ge (dateField, start));
            if (end != null) 
                crit.add (Expression.le (dateField, end));
        }
        if (searchString != null)
            crit.add (Expression.like ("detail", "%" + searchString + "%"));

        if (pageSize > 0 && pageNumber > 0) {
            crit.setMaxResults (pageSize);
            crit.setFirstResult (pageSize * (pageNumber - 1));
        }
        return crit.list();
    }

    /**
     * @param journal the journal.
     * @param start date (inclusive).
     * @param end date (inclusive).
     * @param destinationJournal where to move former transactions. A null value
would delete former transactions.
     * @param searchString optional search string
     * @param findByPostDate true to find by postDate, false to find by timestamp
     * @return list of transactions
     * @throws GLException if user doesn't have READ permission on this jounral.
     */
    public List findTransactions 
        (Journal journal, Date start, Date end, String searchString, boolean findByPostDate)
        throws HibernateException, GLException
    {
        return findTransactions (journal, start, end, searchString, findByPostDate, 0, 0);
    }

    /**
     * @return user object associated with this session.
     */
    public GLUser getUser() {
        return user;
    }
    /**
     * Current Balance for account in a given journal.
     * @param journal the journal.
     * @param acct the account.
     * @return current balance.
     * @throws GLException if user doesn't have READ permission on this jounral.
     */
    public BigDecimal getBalance (Journal journal, Account acct) 
        throws HibernateException, GLException
    {
        return getBalances (journal, acct, null, true) [0];
    }
    /**
     * Current Balance for account in a given journal.
     * @param journal the journal.
     * @param acct the account.
     * @param layers the layers.
     * @return current balance.
     * @throws GLException if user doesn't have READ permission on this jounral.
     */
    public BigDecimal getBalance (Journal journal, Account acct, short layer) 
        throws HibernateException, GLException
    {
        return getBalances (journal, acct, null, true, new short[] { layer }) [0];
    }
    /**
     * Current Balance for account in a given journal.
     * @param journal the journal.
     * @param acct the account.
     * @param layers the layers.
     * @return current balance.
     * @throws GLException if user doesn't have READ permission on this jounral.
     */
    public BigDecimal getBalance (Journal journal, Account acct, short[] layers) 
        throws HibernateException, GLException
    {
        return getBalances (journal, acct, null, true, layers) [0];
    }
    /**
     * Current Balance for account in a given journal.
     * @param journal the journal.
     * @param acct the account.
     * @param layers comma separated list of layers
     * @return current balance.
     * @throws GLException if user doesn't have READ permission on this jounral.
     */
    public BigDecimal getBalance (Journal journal, Account acct, String layers) 
        throws HibernateException, GLException
    {
        return getBalances (journal, acct, null, true, toLayers(layers)) [0];
    }
    /**
     * Balance for account in a given journal in a given date.
     * @param journal the journal.
     * @param acct the account.
     * @param date date (inclusive).
     * @return balance at given date.
     * @throws GLException if user doesn't have READ permission on this jounral.
     */
    public BigDecimal getBalance (Journal journal, Account acct, Date date) 
        throws HibernateException, GLException
    {
        return getBalances (journal, acct, date, true) [0];
    }
    /**
     * Balance for account in a given journal in a given date.
     * @param journal the journal.
     * @param acct the account.
     * @param date date (inclusive).
     * @param layers layers
     * @return balance at given date.
     * @throws GLException if user doesn't have READ permission on this jounral.
     */
    public BigDecimal getBalance (Journal journal, Account acct, Date date, short layer) 
        throws HibernateException, GLException
    {
        return getBalances (journal, acct, date, true, new short[] { layer }) [0];
    }
    /**
     * Balance for account in a given journal in a given date.
     * @param journal the journal.
     * @param acct the account.
     * @param date date (inclusive).
     * @param layers layers
     * @return balance at given date.
     * @throws GLException if user doesn't have READ permission on this jounral.
     */
    public BigDecimal getBalance (Journal journal, Account acct, Date date, short[] layers) 
        throws HibernateException, GLException
    {
        return getBalances (journal, acct, date, true, layers) [0];
    }
    /**
     * Get Both Balances at given date
     * @param journal the journal.
     * @param acct the account.
     * @param date date (inclusive).
     * @param inclusive either true or false
     * @return array of 2 BigDecimals with balance and entry count.
     * @throws GLException if user doesn't have READ permission on this jounral.
     */
    public BigDecimal[] getBalances 
        (Journal journal, Account acct, Date date, boolean inclusive) 
        throws HibernateException, GLException
    {
        return getBalances (journal, acct, date, inclusive, LAYER_ZERO);
    }

    /**
     * Get Both Balances at given date
     * @param journal the journal.
     * @param acct the account.
     * @param date date (inclusive).
     * @param inclusive either true or false
     * @param layers the layers 
     * @return array of 2 BigDecimals with balance and entry count.
     * @throws GLException if user doesn't have READ permission on this jounral.
     */
    public BigDecimal[] getBalances 
        (Journal journal, Account acct, Date date, boolean inclusive, short[] layers) 
        throws HibernateException, GLException
    {
        checkPermission (GLPermission.READ, journal);
        BigDecimal balance[] = { ZERO, Z };
        if (acct.getChildren() != null) {
            if (acct.isChart()) {
                return getChartBalances 
                    (journal, (CompositeAccount) acct, date, inclusive, layers);
            }
            Iterator iter = acct.getChildren().iterator();
            while (iter.hasNext()) {
                Account a = (Account) iter.next();
                BigDecimal[] b = getBalances (journal, a, date, inclusive, layers);
                balance[0] = balance[0].add (b[0]);
                session.evict (a);
            }
        }
        else if (acct instanceof FinalAccount) {
            Criteria crit = session.createCriteria (GLEntry.class)
                .add (Expression.eq ("account", acct))
                .add (Expression.in ("layer", toShortArray (layers)));
            crit = crit.createCriteria ("transaction")
                    .add (Expression.eq ("journal", journal));
            if (date != null) {
                if (inclusive) {
                    crit.add (Expression.lt ("postDate", Util.tomorrow (date)));
                }
                else {
                    date = Util.floor (date);
                    crit.add (Expression.lt ("postDate", date));
                }
            }
            Checkpoint chkp = 
                getRecentCheckpoint (journal, acct, date, inclusive, layers);
            if (chkp != null) {
                balance[0] = chkp.getBalance();
                crit.add (Expression.gt ("postDate", chkp.getDate()));
            }
            List l = crit.list();
            balance[0] = applyEntries (balance[0], l);
            balance[1] = new BigDecimal (l.size()); // hint for checkpoint
        } 
        return balance;
    }

    /**
     * AccountDetail for date range
     * @param journal the journal.
     * @param acct the account.
     * @param start date (inclusive).
     * @param end date (inclusive).
     * @return Account detail for given period.
     * @throws GLException if user doesn't have READ permission on this jounral.
     */
    public AccountDetail getAccountDetail 
        (Journal journal, Account acct, Date start, Date end, short[] layers) 
        throws HibernateException, GLException
    {
        checkPermission (GLPermission.READ);
        start = Util.floor (start);
        end   = Util.ceil (end);
        Criteria crit = session.createCriteria (GLEntry.class)
            .add (Expression.eq ("account", acct))
            .add (Expression.in ("layer", toShortArray (layers)));
        crit = crit.createCriteria ("transaction")
            .add (Expression.eq ("journal", journal))
            .add (Expression.ge ("postDate", start))
            .add (Expression.le ("postDate", end));

        BigDecimal initialBalance[] = getBalances (journal, acct, start, false, layers);
        List entries = crit.list();
        BigDecimal finalBalance = applyEntries (initialBalance[0], entries);

        return new AccountDetail (
                journal, acct, 
                initialBalance[0], finalBalance,
                start, end, entries, layers );
    }
    /**
     * @param journal the journal.
     * @param acct the account.
     * @param date date (null for last checkpoint)
     * @param inclusive either true or false
     * @return Most recent check point for given date.
     * @throws GLException if user doesn't have CHECKPOINT permission on this jounral.
     */
    public Checkpoint getRecentCheckpoint
        (Journal journal, Account acct, Date date, boolean inclusive, short[] layers) 
        throws HibernateException, GLException
    {
        checkPermission (GLPermission.CHECKPOINT, journal);

        Criteria crit = session.createCriteria (Checkpoint.class)
            .add (Expression.eq ("journal", journal))
            .add (Expression.eq ("account", acct));

        if (layers != null)
           crit.add (Expression.eq ("layers", layersToString(layers)));

        if (date != null) {
           if (inclusive)
               crit.add (Expression.le ("date", date));
           else
               crit.add (Expression.lt ("date", date));
        }
        crit.addOrder (Order.desc ("date"));
        crit.setMaxResults (1); 
        return (Checkpoint) crit.uniqueResult();
    }

    /**
     * @param journal the Journal
     * @param acct the account
     * @param date checkpoint date (inclusive)
     * @param threshold minimum number of  GLEntries required to create a checkpoint
     * @throws GLException if user doesn't have CHECKPOINT permission on this jounral.
     */
    public void createCheckpoint 
        (Journal journal, Account acct, Date date, int threshold)
        throws HibernateException, GLException
    {
        createCheckpoint (journal, acct, date, threshold, LAYER_ZERO);
    }
    /**
     * @param journal the Journal
     * @param acct the account
     * @param date checkpoint date (inclusive)
     * @param layers taken into account in this checkpoint
     * @param threshold minimum number of  GLEntries required to create a checkpoint
     * @throws GLException if user doesn't have CHECKPOINT permission on this jounral.
     */
    public void createCheckpoint 
        (Journal journal, Account acct, Date date, int threshold, short[] layers) 
        throws HibernateException, GLException
    {
        if (date == null)
            throw new GLException ("Invalid checkpoint date");
        checkPermission (GLPermission.CHECKPOINT, journal);
        // Transaction tx = session.beginTransaction();
        session.lock (journal, LockMode.UPGRADE);
        createCheckpoint0 (journal, acct, date, threshold, layers);
        // tx.commit();
    }
    /**
     * Lock a journal.
     * @param journal the journal.
     * @throws HibernateException on database errors.
     * @throws GLException if user doesn't have POST permission on this jounral.
     */
    public void lock (Journal journal) 
        throws HibernateException, GLException
    {
        checkPermission (GLPermission.POST, journal);
        session.lock (journal, LockMode.UPGRADE);
    }
    /**
     * Lock an account in a given journal.
     * @param journal the journal.
     * @param acct the account.
     * @throws GLException if user doesn't have POST permission on this jounral.
     * @throws HibernateException on database errors.
     */
    public void lock (Journal journal, Account acct) 
        throws HibernateException, GLException
    {
        checkPermission (GLPermission.POST, journal);
        AccountLock lck = getLock (journal, acct, false);
        if (lck == null) {
            // Transaction tx = session.beginTransaction();
            session.lock (journal, LockMode.UPGRADE);
            lck = getLock (journal, acct, true);
            // tx.commit();
            lck = getLock (journal, acct, false);   // re-get it
        }
    }
    
    /**
     * Open underlying Hibernate session.
     * @throws HibernateException
     */
    public synchronized Session open () throws HibernateException {
        return db.open();
    }
    /**
     * Close underlying Hibernate session.
     * @throws HibernateException
     */
    public synchronized void close () throws HibernateException {
        db.close();
    }
    /**
     * @return underlying Hibernate Session.
     */
    public Session session () {
        return db.session();
    }
    /**
     * @return Hibernate's session factory
     */
    public SessionFactory getSessionFactory () {
        return sf;
    }
    /**
     * Begin hibernate transaction.
     * @return new Transaction
     */
    public Transaction beginTransaction() throws HibernateException {
        return session.beginTransaction();
    }
    /**
     * Begin hibernate transaction.
     * @param timeout timeout in seconds
     * @return new Transaction
     */
    public Transaction beginTransaction(int timeout) throws HibernateException {
        Transaction tx = session.beginTransaction();
        if (timeout > 0)
            tx.setTimeout (timeout);
        return tx;
    }
    public GLUser getUser (String nick) throws HibernateException
    {
        return (GLUser) session.createCriteria (GLUser.class)
                .add (Expression.eq ("nick", nick))
                .uniqueResult();
    }
    /**
     * set a journal's lockDate
     * @param journal the Journal
     * @param lockDate the lock date.
     * @throws HibernateException on database errors.
     * @throws GLException if users doesn't have global READ permission.
     */
    public void setLockDate (Journal journal, Date lockDate) 
        throws GLException, HibernateException
    {
        checkPermission (GLPermission.WRITE, journal);
        // Transaction tx = session.beginTransaction();
        session.lock (journal, LockMode.UPGRADE);
        journal.setLockDate (lockDate);
        // tx.commit();
    }

    // -----------------------------------------------------------------------
    // PUBLIC HELPERS 
    // -----------------------------------------------------------------------
    public short[] toLayers (String layers) {
        StringTokenizer st = new StringTokenizer (layers, ", ");
        short[] sa = new short[st.countTokens()];
        for (int i=0; st.hasMoreTokens(); i++) 
            sa[i] = Short.parseShort (st.nextToken());
        return sa;
    }

    // -----------------------------------------------------------------------
    // PRIVATE METHODS
    // -----------------------------------------------------------------------
    private AccountLock getLock (Journal journal, Account acct, boolean create) 
        throws HibernateException
    {
        AccountLock lck = new AccountLock (journal, acct);
        try {
            lck = (AccountLock) 
                session.load (AccountLock.class, lck, LockMode.UPGRADE);
        } catch (ObjectNotFoundException e) {
            if (create) 
                session.save (lck);
            else
                lck = null;
        }
        return lck;
    }
    private void createCheckpoint0 
        (Journal journal, Account acct, Date date, int threshold, short[] layers) 
        throws HibernateException, GLException
    {
        if (acct instanceof CompositeAccount) {
            Iterator iter = ((CompositeAccount) acct).getChildren().iterator();
            while (iter.hasNext()) {
                Account a = (Account) iter.next();
                createCheckpoint0 (journal, a, date, threshold, layers);
            }
        }
        else if (acct instanceof FinalAccount) {
            Date sod = Util.floor (date);   // sod = start of day
            invalidateCheckpoints (journal, new Account[] { acct }, sod, sod, layers);
            BigDecimal b[] = getBalances (journal, acct, date, true, layers);
            if (b[1].intValue() >= threshold) {
                Checkpoint c = new Checkpoint ();
                c.setDate (date);
                c.setBalance (b[0]);
                c.setJournal (journal);
                c.setAccount (acct);
                c.setLayers (layersToString(layers));
                session.save (c);
            }
        } 
    }
    private Account[] getAccounts (GLTransaction txn) {
        List list = txn.getEntries();
        Account[] accounts = new Account[list.size()];
        Iterator iter = list.iterator();
        for (int i=0; iter.hasNext(); i++) {
            GLEntry entry = (GLEntry) iter.next();
            accounts[i] = entry.getAccount();
        }
        return accounts;
    }
    private List getAccountHierarchyIds (Account acct) 
        throws GLException
    {
        if (acct == null)
            throw new GLException ("Invalid entry - account is null");
        Account p = acct;
        List l = new ArrayList();
        for (int i=0; p != null; i++) {
            l.add (new Long (p.getId()));
            p = p.getParent();
        }
        return l;
    }
    private void invalidateCheckpoints (GLTransaction txn)
        throws HibernateException 
    {
        Account[] accounts = getAccounts (txn);
        invalidateCheckpoints (
            txn.getJournal(), accounts, txn.getPostDate(), null, null
        );
    }
    private void invalidateCheckpoints
        (Journal journal, Account[] accounts, Date start, Date end, short[] layers) 
        throws HibernateException
    {
        Criteria crit = session.createCriteria (Checkpoint.class)
            .add (Expression.eq ("journal", journal))
            .add (Expression.in ("account", accounts));

        if (layers != null)
            crit.add (Expression.eq ("layers", layersToString(layers)));
        if (start.equals (end))
            crit.add (Expression.eq ("date", start));
        else {
            crit.add (Expression.ge ("date", start));
            if (end != null) {
                crit.add (Expression.le ("date", end));
            }
        }
        Iterator iter = crit.list().iterator();
        while (iter.hasNext()) {
            Checkpoint cp = (Checkpoint) iter.next();
            session.delete (cp);
        }
        session.flush();
    }
    private BigDecimal applyEntries (BigDecimal balance, List entries) 
        throws GLException
    {
        Iterator iter = entries.iterator();
        while (iter.hasNext()) {
            GLEntry entry = (GLEntry) iter.next();
            if (entry.isIncrease ()) {
                balance = balance.add (entry.getAmount());
            }
            else if (entry.isDecrease()) {
                balance = balance.subtract (entry.getAmount());
            }
            else {
                throw new GLException (
                    entry.toString() + " has invalid account type"
                );
            }
        }
        return balance;
    }
    private static void init () throws Exception {
        synchronized (GLSession.class) {
            if (cfg != null)
                return;

            cfg = new Configuration();
            cfg.configure ();
            sf = cfg.buildSessionFactory();
        }
    }
    private Object getRuleImpl (String clazz) throws GLException
    {
        Object impl = ruleCache.get (clazz);
        if (impl == null) {
            synchronized (ruleCache) {
                impl = ruleCache.get (clazz);
                if (impl == null) {
                    try {
                        Class cls = Class.forName (clazz);
                        impl = cls.newInstance();
                        ruleCache.put (clazz, impl);
                    } catch (Exception e) {
                        throw new GLException ("Invalid rule " + clazz, e);
                    }
                }
            }
        }
        return impl;
    }
    private void dumpRules (Collection rules) {
        log.warn ("--- rules ---");
        Iterator iter = rules.iterator();
        while (iter.hasNext()) {
            log.warn (iter.next());
        }
    }
    private void addRules 
        (Map ruleMap, Journal journal, List acctHierarchy, int offset)
        throws HibernateException, GLException
    {
        Query q = session.createQuery (
            "from org.jpos.gl.RuleInfo where journal=:journal and account in (:accts) order by id"
        );
        q.setParameter ("journal", journal);
        q.setParameterList ("accts", acctHierarchy, new LongType());
        q.setCacheable (true);
        q.setCacheRegion ("rules");
        Iterator iter = q.iterate();

        while (iter.hasNext()) {
            RuleInfo ri  = (RuleInfo) iter.next();
            RuleEntry k  = new RuleEntry (ri, ri.getAccount());
            RuleEntry re = (RuleEntry) ruleMap.get (k.getKey());
            if (re == null) 
                ruleMap.put (k.getKey(), re = k);

            re.addOffset (offset);
        }
    }
    private void applyRules (GLTransaction txn, Collection rules) 
        throws HibernateException, GLException
    {
        Iterator iter = rules.iterator();
        while (iter.hasNext()) {
            RuleEntry re = (RuleEntry) iter.next();
            RuleInfo  ri = re.getRuleInfo();
            JournalRule rule = (JournalRule) getRuleImpl (ri.getClazz());
            rule.check (
                this, txn, ri.getParam(), re.getAccount(), 
                re.getOffsets(), ri.getLayerArray()
            );
        }
    }
    private Collection getRules (GLTransaction txn) 
        throws HibernateException, GLException
    {
        Map map           = new LinkedHashMap ();
        Journal journal   = txn.getJournal();

        Query q = session.createQuery (
          "from org.jpos.gl.RuleInfo where journal=:journal and account is null order by id"
        );
        q.setParameter ("journal", journal);
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
            RuleInfo  ri = (RuleInfo) iter.next();
            RuleEntry re = new RuleEntry (ri);
            map.put (re.getKey(), re);
        }
        iter = txn.getEntries().iterator();
        for (int i=0; iter.hasNext(); i++) {
            GLEntry entry = (GLEntry) iter.next();
            addRules (map, journal, 
                getAccountHierarchyIds (entry.getAccount()), i);
        }
        return map.values();
    }
    private BigDecimal[] getChartBalances 
        (Journal journal, CompositeAccount acct, Date date, boolean inclusive, short[] layers) 
        throws HibernateException, GLException
    {
        BigDecimal balance[] = { ZERO, ZERO };
        Iterator iter = ((CompositeAccount) acct).getChildren().iterator();
        while (iter.hasNext()) {
            Account a = (Account) iter.next();
            BigDecimal[] b = getBalances (journal, a, date, inclusive, layers);
            if (a.isDebit()) {
                balance[0] = balance[0].add (b[0]);
                balance[1] = balance[1].add (b[1]);
            } else if (a.isCredit()) {
                balance[0] = balance[0].subtract (b[0]);
                balance[1] = balance[1].subtract (b[1]);
            } else {
                throw new GLException ("Account " + a + " has wrong type");
            }
            session.evict (a);
        }
        return balance;
    }
    private Iterator findSummarizedGLEntries 
        (Journal journal, Date start, Date end, boolean credit, short layer)
        throws HibernateException, GLException
    {
        StringBuffer qs = new StringBuffer (
            "select entry.account, sum(entry.amount)" +
            " from org.jpos.gl.GLEntry entry," +
            " org.jpos.gl.GLTransaction txn" +
            " where txn.id = entry.transaction" +
            " and credit = :credit" +
            " and txn.journal = :journal" +
            " and entry.layer = :layer"
        );
        boolean equalDate = start.equals (end);
        if (equalDate) {
            qs.append (" and txn.postDate = :date");
        } else {
            qs.append (" and txn.postDate >= :start");
            qs.append (" and txn.postDate <= :end");
        }
        qs.append (" group by entry.account");
        Query q = session.createQuery (qs.toString());
        q.setLong ("journal", journal.getId());
        q.setParameter ("credit", credit ? "Y" : "N");
        q.setShort ("layer", layer);
        if (equalDate)
            q.setParameter ("date", start);
        else {
            q.setParameter ("start", start);
            q.setParameter ("end", end);
        }
        return q.iterate();
    }
    private void deleteGLTransactions (Journal journal, Date start, Date end)
        throws HibernateException, GLException
    {
        boolean equalDate = start.equals (end);

        StringBuffer qs = new StringBuffer (
            "from org.jpos.gl.GLTransaction where journal = :journal"
        );
        if (equalDate) {
            qs.append (" and postDate = :date");
        } else {
            qs.append (" and postDate >= :start");
            qs.append (" and postDate <= :endDate");
        }
        Query q = session.createQuery (qs.toString());
        q.setLong ("journal", journal.getId());
        if (equalDate)
            q.setParameter ("date", start);
        else {
            q.setParameter ("start", start);
            q.setParameter ("endDate", end);
        }
        ScrollableResults sr = q.scroll(ScrollMode.FORWARD_ONLY);
        while (sr.next()) {
            session.delete (sr.get(0));
        }
    }
    private void reset() {
        checkpoint = System.currentTimeMillis();
    }
    private long checkPoint() {
        long now  = System.currentTimeMillis();
        long diff = now - checkpoint;
        checkpoint = now;
        return diff;
    }
    private void checkPoint (String s) {
        System.out.println (s + " [" + checkPoint() + "ms]");
    }
    private static Short[] toShortArray (short[] i) {
        if (i == null)
            return new Short[0];
        Short[] sa = new Short[i.length];
        for (int j=0; j<i.length; j++)
            sa[j] = new Short(i[j]);
        return sa;
    }
    private String layersToString (short[] layers) {
        StringBuffer sb = new StringBuffer();
        Arrays.sort (layers);
        for (int i=0; i<layers.length; i++) {
            if (i>0)
                sb.append ('.');
            sb.append (Short.toString(layers[i]));
        }
        return sb.toString();
    }
}

