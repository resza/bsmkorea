package id.co.keriss.switching.dao;
import id.co.keriss.switching.ee.Moduleperm;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jpos.ee.DB;
import org.jpos.util.Log;

public class ModulepermDao {
	Session session;
	DB db;
	Log log;
	
	public ModulepermDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  @SuppressWarnings("unchecked")
	  public List<Moduleperm> findAll () throws HibernateException {
		  return session.createCriteria (Moduleperm.class).list();
      }
	  
	  public Moduleperm findById(Long id) throws HibernateException {
		    return (Moduleperm)session.load(Moduleperm.class, id);
	  }
	  public Moduleperm findByName(String name) throws HibernateException {
		  Query query = session.createQuery("from Moduleperm moduleperm where moduleperm.name='"+name+"'");
          return (Moduleperm) query.uniqueResult();
	  }
	  public Moduleperm findByModuleRole(String module, String role) throws HibernateException {
		  String q = "select moduleperm from Module module, Moduleperm moduleperm "  
			  		+ " where moduleperm.module=module " 
			  		+ " and module.name ='"+module+"' " 
			  		+ " and moduleperm.name='"+role+"' ";
		  //System.out.println("QueryString : "+q);
		  Query query = session.createQuery(q);
          return (Moduleperm) query.uniqueResult();
	  }

	  public Boolean hasModulePerm(String module, String role){
		  boolean result;
		  if(findByModuleRole(module, role)!=null){
			  result = true;  
		  }else{
			  result = false;
		  }
		  return result;
	  }
	  
	  public  void deleteModuleperm(Moduleperm module) {
		try {
	      session.delete(module);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }
	  public  void createModuleperm(Moduleperm module) {
	    try {
	      session.save(module);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void updateModuleperm(Moduleperm module) {
	    try {
	      session.update(module);
	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
	  
	  public  void saveUpdateModuleperm(Moduleperm module) {
		    try {
		      session.saveOrUpdate(module);
		    } catch (RuntimeException e) {
		        log.debug(e);
		    }
		  }
}
