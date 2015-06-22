package id.co.keriss.switching.dao;
import id.co.keriss.switching.ee.Product;
import id.co.keriss.switching.ee.MGroup;
import id.co.keriss.switching.ee.Merchant;
import id.co.keriss.switching.ee.Product;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jpos.ee.DB;
import org.jpos.util.Log;

public class ProductDao {
	Session session;
	DB db;
	Log log;
	
	public ProductDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  @SuppressWarnings("unchecked")
	  public List<Product> findAll () throws HibernateException {
		  return session.createCriteria (Product.class).list();
      }
	  
	  public Product findById(Long id) throws HibernateException {
		    return (Product)session.load(Product.class, id);
	  }
	  public Product findByMerchant(String mgpid, String product_type) throws HibernateException {
		  String q = "from Product product where product.product_type=:product_type and product.mgroup.mgpid=:mgpid";
		  Query query = session.createQuery(q);
		  query.setParameter("mgpid", mgpid);
		  query.setParameter("product_type", product_type);
		  System.out.println("QueryString : "+query.getQueryString());
          return (Product) query.uniqueResult();
	  }

	  public  void deleteProduct(Product product) {
		try {
			System.out.println("Delete Product : "+product.getProduct_type());
	      session.delete(product);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }
	  public  void createProduct(Product product) {
	    try {
	      session.save(product);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void updateProduct(Product product) {
	    try {
	      session.update(product);
	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
	  
	  public  void saveUpdateProduct(Product product) {
		    try {
		      session.saveOrUpdate(product);
		    } catch (RuntimeException e) {
		        log.debug(e);
		    }
		  }
}
