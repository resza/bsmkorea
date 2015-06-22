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

import java.util.Iterator;
import java.util.Date;
import java.math.BigDecimal;
import java.text.ParseException;
import org.jdom.Element;
import org.jdom.Comment;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * GLEntry.
 * 
 * Represents a MiniGL Transaction Entry.
 *
 * @see GLTransaction
 * @see Account
 *
 * @author <a href="mailto:apr@jpos.org">Alejandro Revilla</a>
 */
public class GLEntry {
    private long id;
    private String detail;
    private boolean credit;
    private short layer;
    private BigDecimal amount;
    private FinalAccount account;
    private GLTransaction transaction;

    public GLEntry () {
        super();
    }

    /**
     * Constructs a GLEntry out of a JDOM Element as defined in
     * <a href="http://jpos.org/minigl.dtd">minigl.dtd</a>
     * @param elem 
     */
    public GLEntry (Element elem) throws ParseException {
        super();
        fromXML (elem);
    }
    /**
     * GLEntry id.
     *
     * @return internal id
     */
    public long getId() {
        return id;
    }
    /**
     * GLEntry id.
     * 
     * @param id internal id.
     */
    public void setId(long id) {
        this.id = id;
    }
    /**
     * Account.
     * @param account a final account
     */
    public void setAccount (FinalAccount account) {
        this.account = account;
    }
    /**
     * Account.
     * @return final account associated with GLEntry
     */
    public FinalAccount getAccount () {
        return account;
    }
    /**
     * Transaction.
     * @param transaction transaction associated with this GLEntry.
     */
    public void setTransaction (GLTransaction transaction) {
        this.transaction = transaction;
    }
    /**
     * Transaction.
     * @return transaction associated with this GLEntry.
     */
    public GLTransaction getTransaction () {
        return transaction;
    }
    /**
     * Optional GLEntry Detail.
     *
     * Although GLTransaction has a detail, MiniGL support additional
     * detail information at GLEntry level.
     *
     * @param detail optional entry level detail
     */
    public void setDetail (String detail) {
        this.detail = detail;
    }
    /**
     * Optional GLEntry Detail.
     *
     * Although GLTransaction has a detail, MiniGL support additional
     * detail information at GLEntry level.
     *
     * @return entry level detail (may be null)
     */
    public String getDetail () {
        return detail;
    }
    /**
     * Credit.
     * calling setCredit(true) automatically sets <code>debit</code> to false.
     * @param credit true if this GLEntry is a credit.
     */
    public void setCredit (boolean credit) {
        this.credit = credit;
    }
    /**
     * Debit.
     * calling setDebit(true) automatically sets <code>credit</code> to false.
     * @param debit true if this GLEntry is a credit.
     */
    public void setDebit (boolean debit) {
        this.credit = !debit;
    }
    /**
     * @return true if this entry is a credit.
     */
    public boolean isCredit () {
        return credit;
    }
    /**
     * @return true if this entry is a debit.
     */
    public boolean isDebit () {
        return !credit;
    }
    /**
     * Increase value.
     *
     * @return true if this entry increases the balance of its account.
     */
    public boolean isIncrease() {
        return 
            (isDebit() && account.isDebit()) ||
            (isCredit() && account.isCredit());
    }
    /**
     * Decrease value.
     *
     * @return true if this entry decreases the balance of its account.
     */
    public boolean isDecrease () {
        return !isIncrease();
    }
    /**
     * Amount.
     * @param amount the amount.
     */
    public void setAmount (BigDecimal amount) {
        this.amount = amount;
    }
    /**
     * Amount.
     * @return entry's amount.
     */
    public BigDecimal getAmount () {
        return amount;
    }
    /** 
     * Impact on balance.
     *
     * @return either +amount or -amount based on isIncrease/isDecrease.
     */
    public BigDecimal getImpact () {
        return isIncrease() ? amount : amount.negate();
    }
    public void setLayer (short layer) {
        this.layer = layer;
    }
    public short getLayer() {
        return layer;
    }
    /**
     * Parses a JDOM Element as defined in
     * <a href="http://jpos.org/minigl.dtd">minigl.dtd</a>
     */
    public void fromXML (Element elem) throws ParseException {
        setDetail (elem.getChild ("detail").getText());

        setCredit ("credit".equals (elem.getAttributeValue ("type")));
        setLayer (elem.getAttributeValue ("layer"));
        setAmount (new BigDecimal (elem.getChild ("amount").getText()));
    }
    /**
     * Creates a JDOM Element as defined in
     * <a href="http://jpos.org/minigl.dtd">minigl.dtd</a>
     */
    public Element toXML (boolean deep) {
        Element elem = new Element ("entry");
        if (getDetail() != null) {
            Element detail = new Element ("detail").setText (getDetail());
            elem.addContent (detail);
        }
        elem.setAttribute ("account", getAccount().getCode());
        elem.setAttribute ("type", isCredit() ? "credit" : "debit");
        if (layer != 0)
            elem.setAttribute ("layer", Integer.toString(layer));
        Element amount = new Element ("amount");
        amount.setText (getAmount().toString());
        elem.addContent (amount);
        return elem;
    }
    public boolean equals(Object other) {
        if ( !(other instanceof GLEntry) ) return false;
        GLEntry castOther = (GLEntry) other;
        return new EqualsBuilder()
            .append(this.getId(), castOther.getId())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getId())
            .toHashCode();
    }
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
    private void setLayer (String s) {
        if (s != null)
            setLayer (Short.parseShort (s));
    }
}

