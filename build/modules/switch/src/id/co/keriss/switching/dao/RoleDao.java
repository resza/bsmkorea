package id.co.keriss.switching.dao;

import id.co.keriss.switching.ee.Role;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jpos.ee.DB;
import org.jpos.util.Log;

public class RoleDao {
	Session session;
	DB db;
	Log log;
	
	public RoleDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  @SuppressWarnings("unchecked")
	  public List<Role> findAll () throws HibernateException {
		  return session.createCriteria (Role.class).list();
      }
	  
	  public Role findById(Long id) throws HibernateException {
		    return (Role)session.load(Role.class, id);
	  }
	
	  public Role findByName(String name) throws HibernateException {
		  Query query = session.createQuery("from Role role where role.name=:name");
		  query.setParameter("name", name);
          return (Role) query.uniqueResult();
	  }
	  
	  public  void deleteLog(Role roles) {
		try {
	      session.delete(roles);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void createLog(Role roles) {
	    try {
	      session.save(roles);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void updateLog(Role roles) {
	    try {
	      session.update(roles);
	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
	  
	  public  void saveUpdateLog(Role roles) {
		    try {
		      session.saveOrUpdate(roles);
		    } catch (RuntimeException e) {
		        log.debug(e);
		    }
		  }
}
