package id.co.keriss.switching.ee;
// Generated Jun 17, 2015 9:38:51 PM by Hibernate Tools 3.2.2.GA


import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Kurs generated by hbm2java
 */
public class Kurs  implements java.io.Serializable {


     private Long id;
     private Long wontorp;
     private Date timestamp;
     private Set transaction = new HashSet(0);

    public Kurs() {
    }

    public Kurs(Long wontorp, Date timestamp, Set transaction) {
       this.wontorp = wontorp;
       this.timestamp = timestamp;
       this.transaction = transaction;
    }
   
    public Long getId() {
        return this.id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    public Long getWontorp() {
        return this.wontorp;
    }
    
    public void setWontorp(Long wontorp) {
        this.wontorp = wontorp;
    }
    public Date getTimestamp() {
        return this.timestamp;
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    public Set getTransaction() {
        return this.transaction;
    }
    
    public void setTransaction(Set transaction) {
        this.transaction = transaction;
    }




}


