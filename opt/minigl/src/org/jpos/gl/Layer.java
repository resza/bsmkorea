package org.jpos.gl;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.text.ParseException;
import org.jdom.Element;
import org.jdom.Comment;

/** 
 * Journal level layer information.
*/
public class Layer implements Serializable {
    private short id;
    private String name;
    private org.jpos.gl.Journal journal;

    public Layer(short id, String name, org.jpos.gl.Journal journal) {
        this.id = id;
        this.name = name;
        this.journal = journal;
    }
    public Layer() {
        super();
    }
    /** minimal constructor */
    public Layer(short id, org.jpos.gl.Journal journal) {
        super();
        this.id = id;
        this.journal = journal;
    }
    public Layer (Element elem) throws ParseException {
        super();
        fromXML (elem);
    }
    public short getId() {
        return this.id;
    }
    public void setId(short id) {
        this.id = id;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public org.jpos.gl.Journal getJournal() {
        return this.journal;
    }
    public void setJournal(org.jpos.gl.Journal journal) {
        this.journal = journal;
    }
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .append("journal", getJournal())
            .toString();
    }
    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof Layer) ) return false;
        Layer castOther = (Layer) other;
        return new EqualsBuilder()
            .append(this.getId(), castOther.getId())
            .append(this.getJournal(), castOther.getJournal())
            .isEquals();
    }
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getId())
            .append(getJournal())
            .toHashCode();
    }
    public void fromXML (Element elem) throws ParseException {
        setId (Short.parseShort(elem.getAttributeValue ("id")));
        setName (elem.getText().trim());
    }
    public Element toXML () {
        Element e = new Element ("layer").setText(getName());
        e.setAttribute ("id", Short.toString(getId()));
        return e;
    }
}

