package id.co.keriss.switching.participant;


import id.co.keriss.switching.util.ResponseCode;

import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOSource;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.MUX;
import org.jpos.iso.ISOFilter.VetoException;
import org.jpos.transaction.Context;
import org.jpos.transaction.TxnSupport;

public class TempParticipant extends TxnSupport implements ResponseCode {

	public TempParticipant() {}

	public void abort(long id, Serializable ctx) {}

	public void commit(long id, Serializable ctx) {
		ISOMsg message = (ISOMsg)((Context)ctx).get("REQUEST");
		ISOSource source = (ISOSource)((Context)ctx).get("ISOSOURCE");
		ISOMsg msg = new ISOMsg();
		//Temporary temp = new Temporary();
		MUX mux = (MUX)((Context)ctx).get("mux");
		try {
			
			int year = Calendar.getInstance().get(Calendar.YEAR);
			msg.setHeader(message.getISOHeader());
			msg.setMTI(message.getMTI());
			//temp.setMti(message.getMTI()+message.getString(3));
			msg.set(3,message.getString(3));
			msg.set(4,message.getString(4));
			//temp.setAmount(Long.valueOf(ISOUtil.unPadLeft(message.getString(4),'0'),10)/100);
			msg.set(11,message.getString(11));
			//temp.setTrace(Long.valueOf(ISOUtil.unPadLeft(message.getString(11),'0'),10));
			if(message.getString(35)!=null){
				String tmpcard = message.getString(35).split("=")[0];
				msg.set(2,tmpcard);
				//temp.setCardno(ISOUtil.protect(tmpcard));
			}else{
				msg.set(2,message.getString(2));
				//temp.setCardno(ISOUtil.protect(message.getString(2)));
			}
			msg.set(41,message.getString(41));
			//temp.setTid(message.getString(41));
			msg.set(42,message.getString(42));
			//temp.setTid(message.getString(42));
			message = mux.request(message, 60000);
			if(message!=null){
				msg.set(12,message.getString(12));			
				msg.set(13,message.getString(13));
				try{
					//temp.setDate(new SimpleDateFormat("MMddhhmmssyyyy").parse(message.getString(13)+message.getString(12)+year));
				}catch(Exception e){
					e.printStackTrace();
				}
				msg.set(24, message.getString(24));
				msg.set(38,message.getString(38));
				msg.set(39,ISOUtil.zeropad(FORMAT_ERROR,2));
				//temp.setApprv(message.getString(38));
				//if(message.getString(39).equalsIgnoreCase("00"))((Context)ctx).put("ISOMSG", msg);
				/*temp=null;
				((Context)ctx).put("DAO", tdao);*/
				System.out.println("message respose : "+message);
			}else{
				((Context)ctx).put("ISOMSG", message);
				System.out.print("message respose : NULL");
			}
		} catch (ISOException e) {
			getLog("Q2", "client-simulator").error(e);
		}
		try {
			if(message==null)System.out.println("message before send is : null");
			source.send(msg);
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
