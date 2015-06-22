package id.co.keriss.switching.action;

import id.co.keriss.switching.action.page.Paging;
import id.co.keriss.switching.dao.TransactionDao;
import id.co.keriss.switching.ee.Transaction;
import id.co.keriss.switching.ee.TransactionVO;
import id.co.keriss.switching.util.ReportUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.jpos.ee.DB;
import org.jpos.ee.action.ActionSupport;
import org.jpublish.JPublishContext;
import org.mortbay.log.Log;

import com.anthonyeden.lib.config.Configuration;
import com.bluelotussoftware.apache.commons.fileupload.example.FileUploadServlet;
import com.bluelotussoftware.apache.commons.fileupload.example.MultiContentServlet;

public class MessagingForm extends ActionSupport {
	private Boolean content = true;
	org.apache.commons.logging.Log log;
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		System.out.println("Messaging execute ...");
		HttpServletRequest request = context.getRequest();
		HttpServletResponse response = context.getResponse();
		PrintWriter writer = null;
        InputStream is = null;
        FileOutputStream fos = null;
        log = context.getSyslog();
        try {
            writer = response.getWriter();
        } catch (IOException ex) {
            log.info(MultiContentServlet.class.getName() + "has thrown an exception: " + ex.getMessage());
        }

        boolean isMultiPart = ServletFileUpload.isMultipartContent(request);

        if (isMultiPart) {
        	System.out.println("Multipart ...");
            log.info("Content-Type: " + request.getContentType());
            // Create a factory for disk-based file items
            DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();

            /*
             * Set the file size limit in bytes. This should be set as an
             * initialization parameter
             */
            diskFileItemFactory.setSizeThreshold(1024 * 1024 * 10); //10MB.


            // Create a new file upload handler
            ServletFileUpload upload = new ServletFileUpload(diskFileItemFactory);

            List items = null;

            try {
                items = upload.parseRequest(request);
            } catch (FileUploadException ex) {
                log.info("Could not parse request", ex);
            }

            ListIterator li = items.listIterator();

            while (li.hasNext()) {
                FileItem fileItem = (FileItem) li.next();
                if (fileItem.isFormField()) {
                    //if (debug) {
                        processFormField(fileItem);
                    //}
                } else {
                    writer.print(processUploadedFile(fileItem));
                }
            }
        }
        System.out.println("Entering octet ..., "+request.getContentType());
        log.info("request : \n"+request);
        if ("application/octet-stream".equals(request.getContentType())) {
        	System.out.println("octet ...");
            log.info("Content-Type: " + request.getContentType());
            String filename = request.getHeader("X-File-Name");

            try {
                is = request.getInputStream();
                //fos = new FileOutputStream(new File(realPath + filename));
                IOUtils.copy(is, fos);
                response.setStatus(HttpServletResponse.SC_OK);
                writer.print("{success: true}");
            } catch (FileNotFoundException ex) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                writer.print("{success: false}");
                log.info(MultiContentServlet.class.getName() + "has thrown an exception: " + ex.getMessage());
            } catch (IOException ex) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                writer.print("{success: false}");
                log.info(MultiContentServlet.class.getName() + "has thrown an exception: " + ex.getMessage());
            } finally {
                try {
                    fos.close();
                    is.close();
                } catch (IOException ignored) {
                }
            }

            writer.flush();
            writer.close();
        }
	}
	
	private void processFormField(FileItem item) {
        // Process a regular form field
        if (item.isFormField()) {
            String name = item.getFieldName();
            String value = item.getString();
            log.info("name: " + name + " value: " + value);
        }
    }

    private String processUploadedFile(FileItem item) {
        // Process a file upload
        if (!item.isFormField()) {
            try {
                //item.write(new File(realPath + item.getName()));
                return "{success:true}";
            } catch (Exception ex) {
                log.info(FileUploadServlet.class.getName() + " has thrown an exception: " + ex.getMessage());
            }
        }
        return "{success:false}";
    }
}
