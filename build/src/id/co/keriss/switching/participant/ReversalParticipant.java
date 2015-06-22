package id.co.keriss.switching.participant;


import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOSource;
import org.jpos.iso.MUX;
import org.jpos.iso.ISOFilter.VetoException;
import org.jpos.transaction.Context;
import org.jpos.transaction.TxnSupport;

public class ReversalParticipant extends TxnSupport {

	public ReversalParticipant() {}

	public void abort(long id, Serializable ctx) {}

	public void commit(long id, Serializable ctx) {
		ISOMsg message = (ISOMsg)((Context)ctx).get("REQUEST");
		ISOSource source = (ISOSource)((Context)ctx).get("ISOSOURCE");
		try {
			message.setResponseMTI();
			message.set(39, "00");
		} catch (ISOException e) {
			getLog("Q2", "client-simulator").error(e);
		}
		try {
			if(message==null)System.out.println("message before send is : null");
			source.send(message);
		} catch (VetoException e) {
			getLog("Q2", "incoming-request-listener").error(e);
		} catch (IOException e) {
			getLog("Q2", "incoming-request-listener").error(e);
		} catch (ISOException e) {
			getLog("Q2", "incoming-request-listener").error(e);
		}

	}

	public int prepare(long id, Serializable context) {
		return PREPARED;
	}
	
}
