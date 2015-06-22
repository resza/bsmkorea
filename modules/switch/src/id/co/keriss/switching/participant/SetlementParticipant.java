package id.co.keriss.switching.participant;


import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.iso.ISODate;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOSource;
import org.jpos.iso.ISOUtil;
import org.jpos.transaction.Context;
import org.jpos.transaction.TxnSupport;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;

public class SetlementParticipant extends TxnSupport {
	
	public SetlementParticipant() {}

	public void abort(long id, Serializable context) {
		
	}

	public void commit(long id, Serializable ctx) {
		System.out.println("THIS SETLEMENT");
		Context context = (Context) ctx;
    	Session session = (Session) getDB(context).session();
    	Transaction tx = (Transaction)context.get(TX);
    	ISOMsg m = (ISOMsg)context.get("REQUEST");
    	String rc = "00";
    	context.put(RC, rc);
    	settlementResponse(m, context);
	}

	public int prepare(long id, Serializable context) {
		return PREPARED;
	}

	
	private void settlementResponse(ISOMsg m, Context context) {
        try {
        	if(m.getString(3).equals("920000")){
        		Date d = new Date();
        		m.setResponseMTI ();
        		m.set (3, m.getString(3));
        		//m.set (7, ISODate.getDateTime(d));
        		m.set (11, m.getString(11));
        		m.set (12, ISODate.getTime(d));
        		m.set (13, ISODate.getDate(d));
        		m.set (24, m.getString(24));
        		String apprv = ISOUtil.zeropad((Math.random()+"").substring(0, 6),6);
        		m.set(37,apprv.replace(".", "1"));
        		m.unset(38);
        		//m.set(38, apprv);
        		m.set (39, "00");
                m.set (41, m.getString(41));
        	}
            m.unset (42);            
            m.unset (60);
            m.unset (63);
            ISOSource source = (ISOSource)context.get("ISOSOURCE");
            try {
				source.send(m);
			} catch (IOException e) {
				e.printStackTrace();
			}
        } catch (ISOException ex) {
            ex.printStackTrace();
        }        
    }
}
