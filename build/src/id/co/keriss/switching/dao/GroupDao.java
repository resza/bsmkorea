package id.co.keriss.switching.dao;
import id.co.keriss.switching.action.page.Paging;
import id.co.keriss.switching.ee.MGroup;
import id.co.keriss.switching.ee.Merchant;
import id.co.keriss.switching.util.ReportUtil;

import java.math.BigInteger;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jpos.ee.DB;
import org.jpos.util.Log;

public class GroupDao {
	Session session;
	DB db;
	Log log;
	
	public GroupDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  @SuppressWarnings("unchecked")
	  public List<MGroup> findAll () throws HibernateException {
		  return session.createCriteria (MGroup.class).list();
      }
	  
	  public Paging findAllGroups(int start,int count) throws HibernateException {
		  return new Paging(session.createQuery("from MGroup group"),start,count);
      }
	  
	  public MGroup findById(Long id) throws HibernateException {
		    return (MGroup)session.load(MGroup.class, id);
	  }
	  
	  public MGroup findByMgpid(String mgpid) throws HibernateException {
		  	System.out.println("MGPID = "+mgpid);
		    return (MGroup)session.createQuery("from MGroup mg where mg.mgpid='"+mgpid+"'").uniqueResult();
	  }
	
	  @SuppressWarnings("unchecked")
	  public MGroup findGroupByStatBlock (String accbc, String cardbc, String status) throws HibernateException {
		  Query query = session.createQuery("select mgroup from MGroup mgroup, CardStatus cardstatus, Blockcode blockcode where "
		  		+ "blockcode.accountBlockcode='"+accbc+"' and "
		  		+ "blockcode.cardBlockcode='"+cardbc+"' and"
		  		+ "cardstatus.code='"+status+"'");
          return (MGroup)query.uniqueResult();
      }
	  
	  public Paging findGroupByMgpidTypeDesc (String mgpid, String type, String desc,int start,int count) throws HibernateException {
		  String q = "select p.mgroup from Product p ";
		  String qwhere,qmgpid,qtype,qdesc,nmtype,mtdesc;
		  qwhere=mgpid!=null|type!=null||desc!=null?" where ":" ";
		  qwhere=qwhere.trim().equalsIgnoreCase("")?"":qwhere;
		  qmgpid=mgpid!=null?" p.mgroup.mgpid LIKE '%"+mgpid+"%' ":" ";
		  qmgpid=qmgpid.contains("%%")?"":qmgpid;
		  qtype=type!=null?" p.product_type LIKE '%"+type+"%' ":" ";
		  qtype=qtype.contains("%%")?"":qtype;
		  qdesc=desc!=null?" p.mgroup.description LIKE '%"+desc+"%' ":" ";
		  qdesc=qdesc.contains("%%")?"":qdesc;
		  nmtype=!qmgpid.trim().equalsIgnoreCase("")&&!qtype.trim().equalsIgnoreCase("")?" and ":" ";
		  mtdesc=!qtype.trim().equalsIgnoreCase("")&&!qdesc.trim().equalsIgnoreCase("")?" and ":" ";
		  q+=qwhere+qmgpid+nmtype+qtype+mtdesc+qdesc;
		  System.out.println("QueryString : "+q);
		  Query query = session.createQuery(q);
		  System.out.println("QueryString = "+query.getQueryString());
          return new Paging(query,start,count);
      }
	  
	  public Paging findGroupByMgpidTypeDescRetProduct (String mgpid, String type, String desc,int start,int count) throws HibernateException {
		  String q = "select p from Product p ";
		  String qwhere,qmgpid,qtype,qdesc,nmtype,mtdesc;
		  qwhere=mgpid!=null|type!=null||desc!=null?" where ":" ";
		  qwhere=qwhere.trim().equalsIgnoreCase("")?"":qwhere;
		  qmgpid=mgpid!=null?" p.mgroup.mgpid LIKE '%"+mgpid+"%' ":" ";
		  qmgpid=qmgpid.contains("%%")?"":qmgpid;
		  qtype=type!=null?" p.product_type LIKE '%"+type+"%' ":" ";
		  qtype=qtype.contains("%%")?"":qtype;
		  qdesc=desc!=null?" p.mgroup.description LIKE '%"+desc+"%' ":" ";
		  qdesc=qdesc.contains("%%")?"":qdesc;
		  nmtype=!qmgpid.trim().equalsIgnoreCase("")&&!qtype.trim().equalsIgnoreCase("")?" and ":" ";
		  mtdesc=!qtype.trim().equalsIgnoreCase("")&&!qdesc.trim().equalsIgnoreCase("")?" and ":" ";
		  q+=qwhere+qmgpid+nmtype+qtype+mtdesc+qdesc;
		  Query query = session.createQuery(q);
		  System.out.println("QueryString = "+query.getQueryString());
          return new Paging(query,start,count);
      }
	  
	  public Paging findGroupByMgpidTypeDescRetProductStabil (String mgpid, String type, String desc,int start,int count) throws HibernateException {
		  String q = "select p from Product p ";
		  String qwhere="",qcondition="";
		  qcondition+=mgpid!=null?" p.mgroup.mgpid LIKE '%"+mgpid+"%' ":" ";
		  if(type!=null)qcondition+=qcondition.trim().length()>0?" and  p.product_type LIKE '%"+type+"%' ":" p.product_type LIKE '%"+type+"%' ";
		  if(desc!=null)qcondition+=qcondition.trim().length()>0?" and  p.description LIKE '%"+desc+"%' ":" p.description LIKE '%"+desc+"%' ";
		  qwhere=qcondition.trim().length()>0?" where ":"";
		  q+=qwhere+qcondition+" order by p.id desc ";
		  Query query = session.createQuery(q);
		  System.out.println("QueryString = "+query.getQueryString());
          return new Paging(query,start,count);
      }
	  
	  public MGroup findByMgpidType (String mgpid, String type) throws HibernateException {
		  Query query = session.createQuery("from MGroup g, Product p where g.mgpid=:mgpid and p.mgroup.mgroup_id=g.mgroup_id and p.product_type=:type");
		  query.setParameter("mgpid", mgpid);
		  query.setParameter("type", type);
          return (MGroup)query.uniqueResult();
      }
	  public MGroup findByMerchant(Merchant merchant) throws HibernateException {
		  Query query = session.createQuery("select merc from MGroup g where merchant=:merchant ");
		  query.setParameter("merchant", merchant);
          return (MGroup)query.uniqueResult();
      }
	  public  void deleteMGroup(MGroup group) {
		try {
	      session.delete(group);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void createMGroup(MGroup group) {
	    try {
	      session.save(group);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }
	  
	  /*public void insertMGroup(MGroup group){
		  String mgpid, description, product_type, allow_supplement, outstanding, lastmaintain;
		  BigInteger id;
		  mgpid = group.getMgpid();
		  description = group.getDescription();
		  product_type = group.getProduct_type();
		  allow_supplement = group.isAllow_supplement()?"1":"0";
		  outstanding = group.getOutstanding()+"";
		  lastmaintain = ReportUtil.getInstance().getTimestamp(group.getLastmaintain());
		  id = nextSeq();
		  String q = "insert into MGroup (mgpid, description, product_type, allow_supplement, outstanding, lastmaintain, id) values ("+mgpid+", "+description+", "+product_type+", "+allow_supplement+", "+outstanding+", '"+lastmaintain+"', "+id+")";
		  System.out.println("QueryString : "+q);
		  Query query = session.createSQLQuery(q);
		  query.executeUpdate();
	  }*/
	  
	  /*public void updatingGroup(MGroup group){
		  String mgpid, description, product_type, allow_supplement, outstanding, lastmaintain;
		  Long id;
		  mgpid = group.getMgpid();
		  description = group.getDescription();
		  product_type = group.getProduct_type();
		  allow_supplement = group.isAllow_supplement()?"1":"0";
		  outstanding = group.getOutstanding()+"";
		  lastmaintain = ReportUtil.getInstance().getTimestamp(group.getLastmaintain());
		  id = group.getMgroup_id();
		  String q = "update MGroup set mgpid="+mgpid+", description="+description+", product_type="+product_type+", allow_supplement="+allow_supplement+", outstanding="+outstanding+", lastmaintain='"+lastmaintain+"' where id="+id;
		  System.out.println("QueryString : "+q);
		  Query query = session.createSQLQuery(q);
		  System.out.println("QueryString : "+query.getQueryString());
		  query.executeUpdate();
	  }*/
	  
	  public BigInteger nextSeq(){
		  String q = "select nextval('group_sequence');";
		  Query query = session.createSQLQuery(q);
		  return (BigInteger) query.uniqueResult();
	  }

	  public  void updateMGroup(MGroup group) {
	    try {
	      session.update(group);
	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
	  public  void saveUpdateCard(MGroup group) {
		    try {
		      session.saveOrUpdate(group);
		    } catch (RuntimeException e) {
		        log.debug(e);
		    }
		  }
}
