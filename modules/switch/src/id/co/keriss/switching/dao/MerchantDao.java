package id.co.keriss.switching.dao;
import id.co.keriss.switching.action.page.Paging;
import id.co.keriss.switching.ee.Merchant;

import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jpos.ee.DB;
import org.jpos.util.Log;

public class MerchantDao {
	Session session;
	DB db;
	Log log;
	
	public MerchantDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  @SuppressWarnings("unchecked")
	  public List<Merchant> findAll () throws HibernateException {
		  return session.createCriteria (Merchant.class).list();
      }
	  
	  public Merchant findById(Long merchant_id) throws HibernateException {
		    return (Merchant)session.load(Merchant.class, merchant_id);
	  }
	
	  public Merchant findByTidMid(String tid,String mid) throws HibernateException {
		  Query query = session.createQuery("from Merchant merchant where merchant.tid='"+tid+"' and merchant.mid='"+mid+"'");
		  /*query.setParameter("tid", tid);
		  query.setParameter("mid", mid);*/
		  //System.out.println("QueryString : "+query.getQueryString());
          return (Merchant) query.uniqueResult();
	  }
	  
	  public Merchant findByCardnoParam(String tid, String mid, String cardno, Date lowlimit, Date highlimit) throws HibernateException {
		  Query query = session.createQuery("from Merchant merchant where merchant.tid=:tid and merchant.mid=:mid");
		  query.setParameter("tid", tid);
		  query.setParameter("mid", mid);
          return (Merchant) query.uniqueResult();
	  }
	  
	  public  void deleteMerchant(Merchant Merchant) {
		try {
	      session.delete(Merchant);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void createMerchant(Merchant Merchant) {
	    try {
	      session.save(Merchant);
	    } catch (RuntimeException e) {
	    	e.printStackTrace();
	    	log.debug(e);
	    }
	  }

	  public  void updateMerchant(Merchant Merchant) {
	    try {
	      session.update(Merchant);
	    } catch (RuntimeException e) {
	    	e.printStackTrace();
	        log.debug(e);
	    }
	  }
	  
	  public  void saveUpdateMerchant(Merchant Merchant) {
		    try {
		      session.saveOrUpdate(Merchant);
		    } catch (RuntimeException e) {
		        log.debug(e);
		    }
		  }
	public Paging findByPartParam(String name, String address, String city,int start,int count) {
		String qwhere,qname,qaddress,qcity,and1,and2;
		qwhere=qname=qaddress=qcity=and1=and2="";
		String q = "from Merchant merchant ";
		qwhere=name!=null||address!=null||city!=null?" where ":" ";
		qwhere=qwhere.trim().equalsIgnoreCase("")?"":qwhere;
		qname=name!=null?" merchant.name LIKE '%"+name+"%' ":" ";
		qname=qname.contains("%%")?"":qname;
		qaddress=address!=null?" merchant.address LIKE '%"+address+"%' ":" ";
		qaddress=qaddress.contains("%%")?"":qaddress;
		qcity=city!=null?" merchant.city LIKE '%"+city+"%' ":" ";
		qcity=qcity.contains("%%")?"":qcity;
		and1=!qname.trim().equalsIgnoreCase("")&&!qaddress.trim().equalsIgnoreCase("")?" and ":" ";
		and2=!qaddress.trim().equalsIgnoreCase("")&&!qcity.trim().equalsIgnoreCase("")?" and ":" ";
		q+=qwhere+qname+and1+qaddress+and2+qcity;
		Query query = session.createQuery(q);
		System.out.println("QueryString : "+query.getQueryString());
		return  new Paging(query,start,count);
	}
	
	public Paging findByPartParamStabil(String name, String address, String city,int start,int count) {
		String qwhere="",qcondition="";
		String q = "from Merchant merchant ";
		qcondition+=name!=null?" merchant.name LIKE '%"+name+"%' ":" ";
		if(address!=null)qcondition+=qcondition.trim().length()>0?" and  merchant.address LIKE '%"+address+"%' ":" merchant.address LIKE '%"+address+"%' ";
		if(city!=null)qcondition+=qcondition.trim().length()>0?" and  merchant.city LIKE '%"+city+"%' ":" merchant.city LIKE '%"+city+"%' ";
		qwhere=qcondition.trim().length()>0?" where ":"";
		q+=qwhere+qcondition+" order by merchant.id desc ";
		Query query = session.createQuery(q);
		System.out.println("QueryString : "+query.getQueryString());
		return  new Paging(query,start,count);
	}
	
	public Paging findByParams(String tid, String mid, String name, String address, String city,int start,int count) {
		System.out.println("City "+city);
		String q = "from Merchant merchant ";
		String qwhere,qtid,ntmid,qmid,nmname,qname,nnaddr,qaddress,nacity,qcity;
		qwhere=tid!=null|mid!=null||name!=null||address!=null||city!=null?" where ":" ";
		qwhere=qwhere.trim().equalsIgnoreCase("")?"":qwhere;
		qtid=tid!=null?" merchant.tid LIKE '%"+tid+"%' ":" ";
		qtid=qtid.contains("%%")?"":qtid;
		qmid=mid!=null?" merchant.mid LIKE '%"+mid+"%' ":" ";
		qmid=qmid.contains("%%")?"":qmid;
		qname=name!=null?" merchant.name LIKE '%"+name+"%' ":" ";
		qname=qname.contains("%%")?"":qname;
		qaddress=address!=null?" merchant.address LIKE '%"+address+"%' ":" ";
		qaddress=qaddress.contains("%%")?"":qaddress;
		qcity=city!=null?" merchant.city LIKE '%"+city+"%' ":" ";
		qcity=qcity.contains("%%")?"":qcity;
		ntmid=!qtid.trim().equalsIgnoreCase("")&&!qmid.trim().equalsIgnoreCase("")?" and ":" ";
		nmname=!qmid.trim().equalsIgnoreCase("")&&!qname.trim().equalsIgnoreCase("")?" and ":" ";
		nnaddr=!qname.trim().equalsIgnoreCase("")&&!qaddress.trim().equalsIgnoreCase("")?" and ":" ";
		nacity=!qaddress.trim().equalsIgnoreCase("")&&!qcity.trim().equalsIgnoreCase("")?" and ":" ";
		q+=qwhere+qtid+ntmid+qmid+nmname+qname+nnaddr+qaddress+nacity+qcity;
		Query query = session.createQuery(q);
		return  new Paging(query,start,count);
	}
	public Paging findByParamsStabil(String tid, String mid, String name, String address, String city,int start,int count) {
		String q = "from Merchant merchant ";
		String qwhere="",qcondition="";
		qcondition+=tid!=null?" merchant.tid LIKE '%"+tid+"%' ":" ";
		if(mid!=null)qcondition+=qcondition.trim().length()>0?" and  merchant.mid LIKE '%"+mid+"%' ":" merchant.mid LIKE '%"+mid+"%' ";
		if(name!=null)qcondition+=qcondition.trim().length()>0?" and  merchant.name LIKE '%"+name+"%' ":" merchant.name LIKE '%"+name+"%' ";
		if(address!=null)qcondition+=qcondition.trim().length()>0?" and  merchant.address LIKE '%"+address+"%' ":" merchant.address LIKE '%"+address+"%' ";
		if(city!=null)qcondition+=qcondition.trim().length()>0?" and  merchant.city LIKE '%"+city+"%' ":" merchant.city LIKE '%"+city+"%' ";
		qwhere=qcondition.trim().length()>0?" where ":"";
		q+=qwhere+qcondition+" order by merchant.id desc";
		Query query = session.createQuery(q);
		return  new Paging(query,start,count);
	}
}
