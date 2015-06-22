package id.co.keriss.switching.util;

import id.co.keriss.switching.action.page.Paging;
import id.co.keriss.switching.dao.TransactionDao;
import id.co.keriss.switching.ee.Transaction;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Hibernate;
import org.jpos.ee.DB;
import org.jpos.iso.ISOUtil;
 
public class ServletMultiUpload extends HttpServlet{
	org.hibernate.Transaction tx;
	DB db = new DB();
	String header, footer, buffer,resp="";
	String reptype="";
	String from,to,merchant,tid,mid,card,status,qstatus="0";
	List<Transaction> trans=null;
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws IOException{
		System.out.println("Get Context type : "+request.getContentType());
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException{
				System.out.println("Post Context type : "+request.getContentType());
	}
	
	public String center (String s, int length) {
	    if (s.length() > length) {
	        return s.substring(0, length);
	    } else if (s.length() == length) {
	        return s;
	    } else {
	        int leftPadding = (length - s.length()) / 2; 
	        StringBuilder leftBuilder = new StringBuilder();
	        for (int i = 0; i < leftPadding; i++) {
	            leftBuilder.append(" ");
	        }

	        int rightPadding = length - s.length() - leftPadding;
	        StringBuilder rightBuilder = new StringBuilder();
	        for (int i = 0; i < rightPadding; i++) 
	            rightBuilder.append(" ");

	        return leftBuilder.toString() + s 
	                + rightBuilder.toString();
	    }
	}
	private void closedb(DB db){
		if (db != null) {
            try {
                if (tx != null) {
                    try {
                        tx.commit ();
                    } catch (RuntimeException t) {
                        t.printStackTrace();
                        try {
                            tx.rollback();
                        } catch (RuntimeException rte) {
                            t.printStackTrace();
                        }
                    }
                }
                db.close ();
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }
        }
	}
}