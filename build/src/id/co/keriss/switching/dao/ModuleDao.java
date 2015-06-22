package id.co.keriss.switching.dao;
import id.co.keriss.switching.ee.Module;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jpos.ee.DB;
import org.jpos.util.Log;

public class ModuleDao {
	Session session;
	DB db;
	Log log;
	
	public ModuleDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  @SuppressWarnings("unchecked")
	  public List<Module> findAll () throws HibernateException {
		  return session.createCriteria (Module.class).list();
      }
	  
	  public Module findById(Long id) throws HibernateException {
		    return (Module)session.load(Module.class, id);
	  }
	  
	  public Module findByName(String name) throws HibernateException {
		  String q = "from Module module where module.name='"+name+"'";
		  //System.out.println("QueryString : "+q);
		  Query query = session.createQuery(q);
          return (Module) query.uniqueResult();
	  }
	  
	  public List<Module> findByParent() throws HibernateException {
		  Query query = session.createQuery("from Module module where module.parent=null");
          return query.list();
	  }
	  
	  public Boolean isParent(String name) throws HibernateException {
		  Query query = session.createQuery("from Module module where module.name='"+name+"' "
		  		+ " and module.name=null");
          return query.uniqueResult()!=null?true:false;
	  }
	  
	  public  void deleteModule(Module module) {
		try {
	      session.delete(module);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }
	  public  void createModule(Module module) {
	    try {
	      session.save(module);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    } catch (Exception ex){
	    	ex.printStackTrace();
	    }
	  }

	  public  void updateModule(Module module) {
	    try {
	      session.update(module);
	      session.flush();
	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
	  
	  public  void saveUpdateModule(Module module) {
		    try {
		      session.saveOrUpdate(module);
		    } catch (RuntimeException e) {
		        log.debug(e);
		    }
		  }
	public Module findByNameParent(String name) {
		Query query = session.createQuery("from Module module where module.name='"+name+"' and module.parent=null");
    return (Module) query.uniqueResult();
	}
}
