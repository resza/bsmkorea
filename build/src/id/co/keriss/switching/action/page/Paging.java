package id.co.keriss.switching.action.page;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jpos.ee.DB;

public class Paging implements Serializable{
	
	Session session;
	public Paging(DB db) {
		super ();
        this.session = db.session();
	}
	public Paging (Session session) {
        super ();
        this.session = session;
    }
	 private Query query;
	 private List allresult;
	 private List results;
	 private int pageSize;
	 private int page;
	 private int listCount;
	 
	 public Paging(Query query, int page, int pageSize) {
		 this.page = page;
		 this.pageSize = pageSize;
		 this.query=query;
		 this.allresult=query.list();
		 this.listCount= query.list().size();
		 results = query.setFirstResult((page * pageSize)-page).setMaxResults(
				 	pageSize + 1).list();

	 }
	 
	 public boolean isNextPage() {
		 return results.size() > (pageSize-1);
		 }
	 
	 public boolean isNextPage2() {
		 return results.size()-1 > (pageSize -1);
		 }

	 public boolean isPreviousPage() {
	 return page > 0;
	 }

	 public boolean isPreviousPage2() {
		 return page +2 > 0;
		 }
	 
	 public List getList() {
	 return isNextPage() ? results.subList(0, pageSize - 1) : results;
	 }

	 public int getPageSize() {
	 return pageSize;
	 }
	 
	 public List getAllresult() {
		return allresult;
	}
	public void setAllresult(List allresult) {
		this.allresult = allresult;
	}
	public int resultsSize(){
		 return results.size();
	 }

	 public void setPageSize(int pageSize) {
	 this.pageSize = pageSize;
	 }

	 public int getPage() {
	 return page;
	 }

	 public void setPage(int page) {
	 this.page = page;
	 }
	 	 
	 public Long lastPage(){
		 Long totalmod=new Long(0);
		 Long totalpage=new Long(0);
		 Long total = new Long(listCount);
		 if(total==null){
			  total=new Long(0);
		 }
		 totalmod = total % 10;
		 totalpage = total/10;
		 if(totalmod != 0){
			 totalpage = totalpage + 1;
		 }
		 return totalpage;
	 }
}
