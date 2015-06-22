package id.co.keriss.switching.action;

import id.co.keriss.switching.action.page.Paging;
import id.co.keriss.switching.dao.TransactionDao;
import id.co.keriss.switching.ee.Transaction;
import id.co.keriss.switching.ee.TransactionVO;
import id.co.keriss.switching.util.ReportUtil;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.jpos.ee.DB;
import org.jpos.ee.action.ActionSupport;
import org.jpublish.JPublishContext;
import org.mortbay.log.Log;

import com.anthonyeden.lib.config.Configuration;

public class TransactionAction extends ActionSupport {
	private Boolean content = true;
	private String from,to,merchant,tid,mid,card,status,reversal,qstatus="0";
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
	try{
		qstatus="0";
		int start = 0;
		HttpServletRequest  request  = context.getRequest();
		from = request.getParameter("fromdate")!=null?request.getParameter("fromdate"):ReportUtil.getInstance().now();
		to = request.getParameter("todate")!=null?request.getParameter("todate"):ReportUtil.getInstance().now();
		reversal = request.getParameter("reversal")==null?"":request.getParameter("reversal");
		merchant = request.getParameter("merchant")==null?"":request.getParameter("merchant");
		tid = request.getParameter("tid")==null?"":request.getParameter("tid");
		mid = request.getParameter("mid")==null?"":request.getParameter("mid");
		card = request.getParameter("card")==null?"":request.getParameter("card");
		status = request.getParameter("status")==null?"0":request.getParameter("status");
		System.out.println("Status : "+status);
		//status=status.equalsIgnoreCase("0")?"1":status;
		switch(Integer.decode(status)){
			case 1:
				qstatus="SUCCESS";
				break;
			case 2:
				qstatus="EXCEPTION";
				break;
			case 3:
				qstatus="DECLINED";
				break;
			case 4:
				qstatus="UPLOAD";
				break;
			default:
		}
		String paging = request.getParameter ("page");
			if (paging!=null){
            	int in = Integer.parseInt(paging);
            	int out = start;
            	if(in!=out){
            		start = in - 1;
            	}
            }
		ReportUtil util = ReportUtil.getInstance();
		DB db = getDB(context);
		TransactionDao tdao = new TransactionDao(db);
		//System.out.println("Reversal "+reversal);
		if(!reversal.equalsIgnoreCase("")){
			org.hibernate.Transaction tx = db.beginTransaction();
			Transaction t = tdao.findById(Long.decode(reversal));
			t.setStatus("REVERSAL");
			tdao.updateTransaction(t);
			tx.commit();
		}
		//Paging pager = tdao.findByTransByStatuses("SUCCESS", util.dateStart(util.now()), util.dateStart(util.now()), start, 11);
		Paging pager = tdao.findByTransByParametersStabil(qstatus, merchant,tid,mid,card,util.dateStart(from), util.dateEnd(to), start, 11);
		Long lastPage = pager.lastPage();
		context.put("lastPage", lastPage);
		context.put("pages", pager);
		List<Transaction> trans = (List<Transaction>) pager.getList();		
		List<TransactionVO> transactions = new Vector<TransactionVO>();
		for(Transaction tran:trans){
			TransactionVO tvo = new TransactionVO();
			//tvo.setId(tran.getId()+"");
			//tvo.setApprv(tran.getApprv());
			/*tvo.setBlockcodeAcc(tran.getCard()!=null?tran.getCard().getBlockcode().getAccountBlockcode():" - ");
			tvo.setBlockcodeCard(tran.getCard()!=null?tran.getCard().getBlockcode().getCardBlockcode():" - ");
			tvo.setCardno(tran.getCard()!=null?tran.getCard().getCardno():" - ");
			tvo.setDate(tran.getTime().toString());
			tvo.setMid(tran.getMerchant()!=null?tran.getMerchant().getMid():" - ");
			tvo.setEmbosname(tran.getCard()!=null?tran.getCard().getEmbose_name():" - ");
			tvo.setMerchant(tran.getMerchant()!=null?tran.getMerchant().getName():" - ");
			tvo.setStatus(tran.getCard()!=null?tran.getCard().getCardstatus().getDecsription():" - ");
			tvo.setTid(tran.getMerchant()!=null?tran.getMerchant().getTid():" - ");
			tvo.setCity(tran.getMerchant()!=null?tran.getMerchant().getCity():" - ");
			tvo.setTranstat(tran.getStatus());
			tvo.setMax(tran.getMerchant()!=null?tran.getMax()+"":" - ");
			tvo.setCount(tran.getMerchant()!=null?tran.getCount()+"":" - ");
			tvo.setUsed(tran.getMerchant()!=null?tran.getUsed()+"":" - ");*/
			//tvo.setUntil(tran.getUntilLimit());
			transactions.add(tvo);
		}
		context.put("fromdate", from);
		context.put("todate", to);
		context.put("from", from);
		context.put("to", to);
		context.put("merchant", merchant);
		context.put("tid", tid);
		context.put("mid", mid);
		context.put("card", card);
		context.put("status", status);
		context.put("tdate", ReportUtil.getInstance().dateTitle(from, to));
		context.put("sysdate", ReportUtil.getInstance().daynow());
		context.put("systime", ReportUtil.getInstance().timenow());	
		context.put("trans", transactions);
		context.put("totalrecord", pager.getAllresult().size());
		}catch (Exception e) {
            e.printStackTrace();
			error (context, e.getMessage());
			Log.debug(e.getMessage());
            context.getSyslog().error (e);
            context.put("content", content);
		}
	}
}
