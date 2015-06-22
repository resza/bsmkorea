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

import java.util.Date;
import java.util.List;
import java.math.BigDecimal;

/**
 * Account Detail bulk response object.
 *
 * @author <a href="mailto:apr@jpos.org">Alejandro Revilla</a>
 * @see GLSession#getAccountDetail
 */
public class AccountDetail {
    Journal journal;
    Account account;
    Date end;
    Date start;
    BigDecimal initialBalance;
    BigDecimal finalBalance;
    List entries;
    short[] layers;

    /**
     * Constructs an AccountDetail bulk accessor.
     * @param journal the Journal.
     * @param account the account.
     * @param initialBalance initial balance (reporting currency).
     * @param finalBalance final balance (reporting currency).
     * @param start start date (inclusive).
     * @param end end date (inclusive).
     * @param entries list of GLEntries.
     */
    public AccountDetail(
        Journal journal, Account account,
        BigDecimal initialBalance, BigDecimal finalBalance,
        Date start, Date end, List entries, short[] layers)
    {
        super();
        this.journal               = journal;
        this.account               = account;
        this.initialBalance        = initialBalance;
        this.finalBalance          = finalBalance;
        this.start                 = start;
        this.end                   = end;
        this.entries               = entries;
        this.layers                = layers;
    }
    public Journal getJournal() {
        return journal;
    }
    public Account getAccount() {
        return account;
    }
    public BigDecimal getInitialBalance() {
        return initialBalance;
    }
    public BigDecimal getFinalBalance() {
        return finalBalance;
    }
    public Date getStart() {
        return start;
    }
    public Date getEnd() {
        return end;
    }
    public List getEntries() {
        return entries;
    }
    public short[] getLayers() {
        return layers;
    }
    /**
     * @return number of entries.
     */
    public int size() {
        return entries.size();
    }
}

