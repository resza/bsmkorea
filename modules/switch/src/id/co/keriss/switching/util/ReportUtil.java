package id.co.keriss.switching.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.jpublish.JPublishContext;
import org.jpublish.Repository;
import org.jpublish.SiteContext;
import org.jpublish.repository.filesystem.FileSystemRepository;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.pdf.PdfWriter;

public class ReportUtil {
	private static ReportUtil instance = null;
	private HashMap tranStat;
	private boolean parseRunning = false;
	protected ReportUtil(){
		tranStat = new HashMap<String, String>();
		tranStat.put("1", "SUCCESS");
		tranStat.put("2", "EXCEPTION");
		tranStat.put("3", "DECLINE");
		tranStat.put("4", "UPLOAD");
	}
	public static ReportUtil getInstance() {
	      if(instance == null) {
	         instance = new ReportUtil();
	      }
	      return instance;
	}
	public String formatNumber(Long amount){
		NumberFormat formatter = NumberFormat.getCurrencyInstance();
		return ""+formatter.format(amount)+"";
	}
	
	public String formatNumber(Double amount){
		NumberFormat formatter = NumberFormat.getCurrencyInstance();
		return ""+formatter.format(amount)+"";
	}
	  public Date dateStart(String date){
		  Date dateVal = null;
		  SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
		  try {
			dateVal = format.parse(date);
		  } catch (ParseException e) {
			e.printStackTrace();
		  }
		  //System.out.println("dateVal ="+dateVal);
		  return dateVal;
	  }
	  
	  public Date dateEnd(String date){
		  Date dateVal = null;
		  SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
		  try {
			dateVal = format.parse(date);
			Calendar cal = Calendar.getInstance();
			cal.setTime(dateVal);
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			dateVal=cal.getTime();
		  } catch (ParseException e) {
			e.printStackTrace();
		  }
		  //System.out.println("dateEnd ="+dateVal);
		  return dateVal;
	  }
	  public Date dateEnd(Date date){
		  Date dateVal = null;
		  SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
		  try {
			dateVal = date;
			Calendar cal = Calendar.getInstance();
			cal.setTime(dateVal);
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			dateVal=cal.getTime();
		  } catch (Exception e) {
			e.printStackTrace();
		  }
		  //System.out.println("dateEnd ="+dateVal);
		  return dateVal;
	  }
	  public Date dateStart(Date date){
		  Date dateVal = null;
		  try {
			dateVal = date;
			Calendar cal = Calendar.getInstance();
			cal.setTime(dateVal);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 1);
			dateVal=cal.getTime();
		  } catch (Exception e) {
			e.printStackTrace();
		  }
		  //System.out.println("dateEnd ="+dateVal);
		  return dateVal;
	  }
	  public String dateTitle(String from, String to){
		  String date= from +" - "+ to;
		  if(from.equalsIgnoreCase(to))date = from;
		  return date;
	  }
	  public String now(){
		  SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		  return sdf.format(new Date());
	  }
	  
	  public String timenow(){
		  SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		  return sdf.format(new Date());
	  }
	  
	  public String timeString(Date date){
		  SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		  return sdf.format(date);
	  }
	  
	  public String daynow(){
		  SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		  return sdf.format(new Date());
	  }
	  
	  public String daynowd(){
		  SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		  return sdf.format(new Date());
	  }
	  public String timestamp(Date date){
		  SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		  return sdf.format(new Date());
	  }
	  
	  public String getTimestamp(Date date){
			String tmstamp="";
			tmstamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(date);
			//System.out.println("Timestamp = "+tmstamp);
			return tmstamp;
		}
	  
	  public String getTimestamp(){
			String tmstamp="";
			Date timestamp = Calendar.getInstance().getTime();
			tmstamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(timestamp);
			//System.out.println("Timestamp = "+tmstamp);
			return tmstamp;
		}
	  
	  public String day(Date date){
		  SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		  return sdf.format(date);
	  }
	  
	  public String stringDate(Date date){
		return new SimpleDateFormat("dd-MM-yyyy").format(date); 
	  }
	  public void toPdf(JPublishContext context,String template){
			String templateName="",path ="";
			HttpServletRequest request = context.getRequest();
			HttpServletResponse response = context.getResponse();
			template = request.getParameter("template");
			//System.out.println("Template : "+template);
			SiteContext sctx = context.getSiteContext();
			if(template.equalsIgnoreCase("index")){
				templateName = template+".html";
				path=sctx.getRoot().getPath()+"/content/report/other";
			}else{
				templateName = template+".html";
				path=sctx.getRoot().getPath()+"/content/report/other";
			}
			StringWriter stringWriter=new StringWriter();
			
			VelocityEngine ve = new VelocityEngine();
			try {
				//System.out.println("Path : "+path);
				ve.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH,path);
				ve.mergeTemplate(templateName, sctx.getCharacterEncodingManager().getMap(path).getPageEncoding(), context, stringWriter);
				
			} catch (Exception e1) {
				e1.printStackTrace();
			} 
			String mergedContent = stringWriter.toString();
			//System.out.println(mergedContent);
			try {
				OutputStream out = response.getOutputStream();
				response.setContentType("application/pdf");
				Document doc = new Document();
				HTMLWorker worker = new HTMLWorker(doc);
				PdfWriter.getInstance(doc, out);
				doc.open();
				worker.parse(new StringReader(mergedContent));
				doc.close();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
	  }
	  
	  public void toXls(JPublishContext context,String template){
			String templateName="",path ="";
			HttpServletRequest request = context.getRequest();
			HttpServletResponse response = context.getResponse();
			template = request.getParameter("template");
			SiteContext sctx = context.getSiteContext();
			if(template.equalsIgnoreCase("index")){
				templateName = template+".html";
				path=sctx.getRoot().getPath()+"/content";
			}else{
				path=sctx.getRoot().getPath()+"/content";
				templateName = template+".html";
			}
			StringWriter stringWriter = new StringWriter();
			VelocityEngine ve = new VelocityEngine();
			try {
				ve.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH,path);
				ve.mergeTemplate(templateName, "utf8", context, stringWriter);
			} catch (Exception e1) {
				e1.printStackTrace();
			} 
			String mergedContent = stringWriter.toString();
			try{
				response.setContentType("application/vnd.ms-excel");
	            response.setHeader("Content-Disposition","attachment; filename="+template+".xls" );
	            PrintWriter pw = response.getWriter();
	            System.out.print(mergedContent);
	            pw.flush();
	            pw.print(mergedContent);
	            //pw.flush();
	            //pw.close();
			}catch(Exception e){
				e.printStackTrace();
			}

	  }
	public boolean isParseRunning() {
		return parseRunning;
	}
	public void setParseRunning(boolean parseRunning) {
		this.parseRunning = parseRunning;
	}
	public HashMap getTranStat() {
		return tranStat;
	}
	public void setTranStat(HashMap tranStat) {
		this.tranStat = tranStat;
	}
}
