package id.co.keriss.switching.participant;

import java.io.Serializable;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMsg;
import org.jpos.transaction.Context;
import org.jpos.transaction.GroupSelector;

public class SwitchParticipant implements Configurable, GroupSelector {
	Configuration cfg;
	public String select(long arg0, Serializable context) {
		System.out.println("Process in SwitchParticipant");
		try {
			ISOMsg m = getRequest((Context) context);
			String groups = cfg.get (m.getMTI()+m.getString(3), null);
			if (groups==null){
				groups="default";
			}
			System.out.println("Print group value : "+groups);
			return groups;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void abort(long id, Serializable context) {}

	public void commit(long id, Serializable context) {}

	public int prepare(long id, Serializable context) {
		return PREPARED | READONLY | NO_JOIN;
	}
	
	private ISOMsg getRequest(Context context) {
		return (ISOMsg)context.get("REQUEST");
	}

	public void setConfiguration(Configuration cfg)
			throws ConfigurationException {
		this.cfg = cfg;
	}

}
