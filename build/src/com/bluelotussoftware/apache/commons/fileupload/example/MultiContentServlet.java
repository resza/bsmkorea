/*
 * Copyright 2010-2011 Blue Lotus Software, LLC.
 * Copyright 2010-2011 John Yeary <jyeary@bluelotussoftware.com>.
 * Copyright 2010-2011 Alan O'Driscoll
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id$
 */
package com.bluelotussoftware.apache.commons.fileupload.example;

import java.io.*;
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
import org.apache.commons.io.IOUtils;

/**
 *
 * @author John Yeary <jyeary@bluelotussoftware.com>
 * @author Allan O'Driscoll
 * @version 1.0
 */
public class MultiContentServlet extends HttpServlet {

    private static final long serialVersionUID = -2045199313944348406L;
    private static final String DESTINATION_DIR_PATH = "files";
    private static String realPath;
    private static final boolean debug = false;

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
        PrintWriter writer = null;
        InputStream is = null;
        FileOutputStream fos = null;

        try {
            writer = response.getWriter();
        } catch (IOException ex) {
            log(MultiContentServlet.class.getName() + "has thrown an exception: " + ex.getMessage());
        }

        boolean isMultiPart = ServletFileUpload.isMultipartContent(request);

        if (isMultiPart) {
            log("Content-Type: " + request.getContentType());
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
                log("Could not parse request", ex);
            }

            ListIterator li = items.listIterator();

            while (li.hasNext()) {
                FileItem fileItem = (FileItem) li.next();
                if (fileItem.isFormField()) {
                    if (debug) {
                        processFormField(fileItem);
                    }
                } else {
                    writer.print(processUploadedFile(fileItem));
                }
            }
        }

        if ("application/octet-stream".equals(request.getContentType())) {
            log("Content-Type: " + request.getContentType());
            String filename = request.getHeader("X-File-Name");

            try {
                is = request.getInputStream();
                fos = new FileOutputStream(new File(realPath + filename));
                IOUtils.copy(is, fos);
                response.setStatus(HttpServletResponse.SC_OK);
                writer.print("{success: true}");
            } catch (FileNotFoundException ex) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                writer.print("{success: false}");
                log(MultiContentServlet.class.getName() + "has thrown an exception: " + ex.getMessage());
            } catch (IOException ex) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                writer.print("{success: false}");
                log(MultiContentServlet.class.getName() + "has thrown an exception: " + ex.getMessage());
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
            log("name: " + name + " value: " + value);
        }
    }

    private String processUploadedFile(FileItem item) {
        // Process a file upload
        if (!item.isFormField()) {
            try {
                item.write(new File(realPath + item.getName()));
                return "{success:true}";
            } catch (Exception ex) {
                log(FileUploadServlet.class.getName() + " has thrown an exception: " + ex.getMessage());
            }
        }
        return "{success:false}";
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Handles file upload data from application/octet-stream, and multipart/form-data.";
    }
}
