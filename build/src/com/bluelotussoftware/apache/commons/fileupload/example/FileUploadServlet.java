/*
 *  Copyright 2010 Blue Lotus Software, LLC.
 *  Copyright 2010 John Yeary <jyeary@bluelotussoftware.com>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
/*
 * $Id$
 */
package com.bluelotussoftware.apache.commons.fileupload.example;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.ListIterator;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 *
 * @author John Yeary <jyeary@bluelotussoftware.com>
 * @version 1.0
 */
public class FileUploadServlet extends HttpServlet {

    private static final long serialVersionUID = -9095319294507383073L;
    private static final String DESTINATION_DIR_PATH = "files";
    private static String realPath;

    /**
     * {@inheritDoc}
     *
     * @param config
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        realPath = getServletContext().getRealPath(DESTINATION_DIR_PATH) + "/";
    }

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        boolean isMultiPart = ServletFileUpload.isMultipartContent(request);
        log("isMultipartContent: " + isMultiPart);
        log("Content-Type: " + request.getContentType());
        if (isMultiPart) {

            // Create a factory for disk-based file items
            DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();

            /*
             * Set the file size limit in bytes. This should be set as an
             * initialization parameter
             */
            diskFileItemFactory.setSizeThreshold(1024 * 1024 * 10); //10MB.


            // Create a new file upload handler
            ServletFileUpload upload = new ServletFileUpload(diskFileItemFactory);

            // Parse the request
            List items = null;
            try {
                items = upload.parseRequest(request);
            } catch (FileUploadException ex) {
                log("Could not parse request", ex);
            }

            PrintWriter out = response.getWriter();
            out.print("<html><head><title>SUCCESS</title></head><body><h1>DONE!</h1>");

            ListIterator li = items.listIterator();
            while (li.hasNext()) {
                FileItem fileItem = (FileItem) li.next();
                if (fileItem.isFormField()) {
                    processFormField(fileItem);
                } else {
                    out.append(processUploadedFile(fileItem));
                }
            }
            out.print("</body></html>");
            out.flush();
            out.close();

        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private void processFormField(FileItem item) {
        // Process a regular form field
        if (item.isFormField()) {
            String name = item.getFieldName();
            String value = item.getString();
            log("name: " + name + " value: " + value);
        }
    }

    private String processUploadedFile(FileItem item) {
        StringBuilder sb = new StringBuilder();
        // Process a file upload
        if (!item.isFormField()) {
            String fieldName = item.getFieldName();
            String fileName = item.getName();
            String contentType = item.getContentType();
            boolean isInMemory = item.isInMemory();
            long sizeInBytes = item.getSize();
            try {
                item.write(new File(realPath + item.getName()));
            } catch (Exception ex) {
               log(FileUploadServlet.class.getName() + " has thrown an exception: " + ex.getMessage());
            }
            sb.append("<p>fieldName: ").append(fieldName).append("</p>").append("<p>fileName: ").append(fileName).append("</p>").append("<p>contentType: ").append(contentType).append("</p>").append("<p>isInMemory: ").append(isInMemory).append("</p>").append("<p>sizeInBytes: ").append(sizeInBytes).append("</p>");
        }
        return sb.toString();
    }
}
