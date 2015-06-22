package id.co.keriss.switching.dao;
import id.co.keriss.switching.action.page.Paging;
import id.co.keriss.switching.ee.Merchant;
import id.co.keriss.switching.ee.Transaction;
import id.co.keriss.switching.util.ReportUtil;

import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jpos.ee.DB;
import org.jpos.util.Log;

public class TransactionDao {
	Session session;
	DB db;
	Log log;
	
	public TransactionDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  @SuppressWarnings("unchecked")
	  public List<Transaction> findAll () throws HibernateException {
		  return session.createCriteria (Transaction.class).list();
      }
	  
	  public Transaction findById(Long id) throws HibernateException {
		    return (Transaction)session.load(Transaction.class, id);
	  }
	
	  public List<Transaction> findByTransDayly(String cardno) throws HibernateException {
		  Query query = session.createQuery("from Transaction t where t.card.cardno =:cardno"
		  				+ " and t.time between :fromDate and :toDate");
		  query.setParameter("cardno", cardno);
		  ReportUtil util =ReportUtil.getInstance();
		  query.setParameter("fromDate", ReportUtil.getInstance().dateStart(util.daynow()));
		  query.setParameter("toDate", ReportUtil.getInstance().dateEnd(util.daynow()));
          return  query.list();
	  }
	  
	  public List<Transaction> findByTransByParam(String cardno,Date fromDate, Date toDate) throws HibernateException {
		  System.out.println("cardno = "+cardno+""
		  					+ "from = "+fromDate+""
		  					+ "to = "+toDate);
		  Query query = session.createQuery("from Transaction t where t.card.cardno =:cardno"
		  				+ " and t.time between :fromDate and :toDate");
		  query.setParameter("cardno", cardno);
		  query.setParameter("fromDate", fromDate);
		  query.setParameter("toDate", toDate);
          return  query.list();
	  }
	  
	  public Paging findByTransByParams(String cardno,Date fromDate, Date toDate, int start, int count) throws HibernateException {
		  System.out.println("cardno = "+cardno+""
		  					+ "from = "+fromDate+""
		  					+ "to = "+toDate);
		  Query query = session.createQuery("from Transaction t where t.card.cardno =:cardno"
		  				+ " and t.time between :fromDate and :toDate");
		  query.setParameter("cardno", cardno);
		  query.setParameter("fromDate", fromDate);
		  query.setParameter("toDate", toDate);
          return  new Paging(query,start,count);
	  }
	  
	  public int sumByMerchantCard(String mname, String maddress, String cardno,Date fromDate, Date toDate){
		  String q="sum (t) from Transaction t where ";
		  q+=(mname!=null)?" t.merchant.name LIKE '%"+mname+"%'":" ";
		  q+=(maddress!=null)?" t.merchant.address LIKE '%"+maddress+"%'":" ";
		  q+=(cardno!=null)?" t.card.cardno LIKE '%"+cardno+"%'":" ";
		  Query query = session.createQuery(q);
		  return (Integer) query.uniqueResult();
	  }
	  
	  public List<Transaction> findByTransByStatus(String status,Date fromDate, Date toDate) throws HibernateException {
		  Query query = session.createQuery("from Transaction t where t.status =:status"
		  				+ " and t.time between :fromDate and :toDate");
		  query.setParameter("status", status);
		  query.setParameter("fromDate", fromDate);
		  query.setParameter("toDate", toDate);
          return  query.list();
	  }
	  
	  public Paging findByTransByStatuses(String status,Date fromDate, Date toDate, int start, int count) throws HibernateException {
		  Query query = session.createQuery("from Transaction t where t.status =:status"
	  				+ " and t.time between :fromDate and :toDate");
		  query.setParameter("status", status);
		  query.setParameter("fromDate", fromDate);
		  query.setParameter("toDate", toDate);
          return new Paging(query,start,count);
	  }
	  
	  public Integer countByTransByCardStatus(String status,Date fromDate, Date toDate) throws HibernateException {
		  Query query = session.createQuery("select count(t) from Transaction t where t.card.cardstatus.code =:status"
	  				+ " and t.time between :fromDate and :toDate");
		  query.setParameter("status", status);
		  query.setParameter("fromDate", fromDate);
		  query.setParameter("toDate", toDate);
		  return (query.uniqueResult()!=null)?((Long)query.uniqueResult()).intValue():null;
	  }
	  
	  public Integer countByTransByTransStatus(String status,Date fromDate, Date toDate) throws HibernateException {
		  Query query = session.createQuery("select count(t) from Transaction t where t.status =:status"
	  				+ " and t.time between :fromDate and :toDate");
		  query.setParameter("status", status);
		  query.setParameter("fromDate", fromDate);
		  query.setParameter("toDate", toDate);
		  return (query.uniqueResult()!=null)?((Long)query.uniqueResult()).intValue():null;
	  }
	  
	  public Integer countByTransByAction(String action,Date fromDate, Date toDate) throws HibernateException {
		  Query query = session.createQuery("select count(t) from Transaction t where t.card.action =:action"
	  				+ " and t.time between :fromDate and :toDate");
		  query.setParameter("action", action);
		  query.setParameter("fromDate", fromDate);
		  query.setParameter("toDate", toDate);
		  return (query.uniqueResult()!=null)?((Long)query.uniqueResult()).intValue():null;
	  }
	  
	  public Integer countByTransByRelation(String relation,Date fromDate, Date toDate) throws HibernateException {
		  Query query = session.createQuery("select count(t) from Transaction t where t.card.relation =:relation"
	  				+ " and t.time between :fromDate and :toDate");
		  query.setParameter("relation", relation);
		  query.setParameter("fromDate", fromDate);
		  query.setParameter("toDate", toDate);
		  return (query.uniqueResult()!=null)?((Long)query.uniqueResult()).intValue():null;
	  }
	  
	  public Paging findByTransByParameters(String status,String merchant,String tid,String mid,String card,Date fromDate, Date toDate, int start, int count) throws HibernateException {
		  String q="",and1,and2,and3,and4, and5,qs,qm,qt,qmid,qcard,qtime;
		  qs=qm=qt=qmid=qcard=and1=and2=and3=and4=and5=qtime="";
		  q="from Transaction t where ";
		  System.out.println("Qstatus : "+status);
		  qs=!status.equalsIgnoreCase("0")?" t.status ='"+status+"'  ":"";
		  and1=!status.equalsIgnoreCase("0")&&!merchant.equalsIgnoreCase("")?" and ":" ";
		  qm=!(merchant.equalsIgnoreCase(""))?" t.merchant.name LIKE '%"+merchant+"%'  ":"";
		  and2=!merchant.equalsIgnoreCase("0")&&!tid.equalsIgnoreCase("")?" and ":" ";
		  qt=!(tid.equalsIgnoreCase(""))?" t.merchant.tid LIKE '%"+tid+"%'  ":"";
		  and3=!tid.equalsIgnoreCase("")&&!mid.equalsIgnoreCase("")?" and ":" ";
		  qmid=!(mid.equalsIgnoreCase(""))?" t.merchant.mid LIKE '%"+mid+"%'  ":"";
		  and4=!mid.equalsIgnoreCase("")&&!card.equalsIgnoreCase("")?" and ":" ";
		  qcard+=!(card.equalsIgnoreCase(""))?" t.card.cardno LIKE '%"+card+"%'  ":"";
		  and5=(qs+and1+qm+and2+qt+and3+qmid+and4+qcard).trim().length()==0?" ":" and ";
		  qtime=" t.time between :fromDate and :toDate ";
		  q+=qs+and1+qm+and2+qt+and3+qmid+and4+qcard+and5+qtime;
		  Query query = session.createQuery(q);
		  query.setParameter("fromDate", fromDate);
		  query.setParameter("toDate", toDate);
		  System.out.println("QueryString : "+query.getQueryString());
		  return  new Paging(query,start,count);
	  }
	  
	  public Paging findByTransByParametersStabil(String status,String merchant,String tid,String mid,String card,Date fromDate, Date toDate, int start, int count) throws HibernateException {
		  String q="from Transaction t ";
		  String qwhere="",qcondition="";
		  qcondition+="0".equalsIgnoreCase(status)?" ":" t.status ='"+status+"' ";
		  qcondition=qcondition.trim().length()<=0?" t.status!='UPLOAD' ":qcondition;
		  if(merchant.length()>0)qcondition+=qcondition.trim().length()>0?" and  t.merchant.name LIKE '%"+merchant+"%' ":" t.merchant.name LIKE '%"+merchant+"%' ";
		  if(tid.length()>0)qcondition+=qcondition.trim().length()>0?" and  t.merchant.tid LIKE '%"+tid+"%' ":" t.merchant.tid LIKE '%"+tid+"%' ";
		  if(mid.length()>0)qcondition+=qcondition.trim().length()>0?" and  t.merchant.mid LIKE '%"+mid+"%' ":" t.merchant.mid LIKE '%"+mid+"%' ";
		  if(card.length()>0)qcondition+=qcondition.trim().length()>0?" and  t.card.cardno LIKE '%"+card+"%' ":" t.card.cardno LIKE '%"+card+"%' ";
		  qcondition+=qcondition.trim().length()>0?" and t.time between :fromDate and :toDate ":" t.time between :fromDate and :toDate ";
		  qwhere=qcondition.trim().length()>0?" where ":"";
		  q+=qwhere+qcondition+" order by t.id desc ";
		  Query query = session.createQuery(q);
		  query.setParameter("fromDate", fromDate);
		  query.setParameter("toDate", toDate);
		  System.out.println("QueryString : "+query.getQueryString());
		  return  new Paging(query,start,count);
	  }
	  
	  public  void deleteTransaction(Transaction transaction) {
		try {
	      session.delete(transaction);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void createTransaction(Transaction transaction) {
	    try {
	      session.save(transaction);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void updateTransaction(Transaction transaction) {
	    try {
	      session.update(transaction);
	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
	  
	public int findByLimitParam(String cardno, String tid, String mid, Date fromDate, Date toDate) {
		String q="from Transaction t where (t.status='SUCCESS' or t.status='EXCEPTION') and t.card.cardno LIKE :cardno and t.merchant.tid=:tid and  t.merchant.mid=:mid and   t.time between :fromDate and :toDate";
		  Query query = session.createQuery(q);
		  query.setParameter("tid", tid);
		  query.setParameter("mid", mid);
		  query.setParameter("cardno", "%"+cardno+"%");
		  query.setParameter("fromDate", fromDate);
		  query.setParameter("toDate", toDate);
		  System.out.println("QueryString : "+query.getQueryString());
        return  query.list().size();
	}
	
	public int findByProductType(String cardno, String type, String mgpid,Date fromDate, Date toDate) {
		String q="from Transaction t where (t.status='SUCCESS' or t.status='EXCEPTION') and t.card.cardno LIKE :cardno and t.card.card_type=:type and t.merchant.mgroup.mgpid=:mgpid and t.time between :fromDate and :toDate";
		  Query query = session.createQuery(q);
		  query.setParameter("mgpid", mgpid);
		  /*query.setParameter("tid", tid);
		  query.setParameter("mid", mid);*/
		  query.setParameter("type", type);
		  query.setParameter("cardno", "%"+cardno+"%");
		  query.setParameter("fromDate", fromDate);
		  query.setParameter("toDate", toDate);
		  System.out.println("QueryString : "+query.getQueryString());
        return  query.list().size();
	}
	
	public Transaction findByTidMidApproval(String tid, String mid,
			String apprv) {
		Date today = new Date();
		Date fromDate = ReportUtil.getInstance().dateStart(today);
		Date toDate = ReportUtil.getInstance().dateEnd(today);
		String q="from Transaction t where "
				+ " t.merchant.tid=:tid and "
				+ " t.merchant.mid=:mid and "
				+ " t.apprv=:apprv and "
				+ " ";		  
		  q+=" t.time between :fromDate and :toDate";
		  Query query = session.createQuery(q);
		  query.setParameter("tid", tid);
		  query.setParameter("mid", mid);
		  query.setParameter("apprv", apprv);
		  query.setParameter("fromDate", fromDate);
		  query.setParameter("toDate", toDate);
		  System.out.println("QueryString Void : "+query.getQueryString());
		return (Transaction)query.uniqueResult();
	}
	public List<Transaction> findByMerchant(Merchant m) {
		String q="from Transaction t where t.merchant=:m";
		Query query = session.createQuery(q);	
		query.setParameter("m", m);
		return query.list();
	}
	public List<Transaction> findByProduct(String type, String mgpid) {
		String q="select t from Transaction t, MGroup mgroup, Product p where t.merchant.mgroup=mgroup and p.mgroup=mgroup "
				+ "and p.product_type=:type and mgroup.mgpid=:mgpid and t.card.card_type = :type";
		Query query = session.createQuery(q);	
		query.setParameter("type", type);
		query.setParameter("mgpid", mgpid);
		System.out.println("mgpid : "+mgpid+", type1 : "+type+"\nQueryString : "+query.getQueryString());
		return query.list();
	}  
	  
}
