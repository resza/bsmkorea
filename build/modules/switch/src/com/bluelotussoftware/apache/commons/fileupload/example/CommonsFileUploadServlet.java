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
import java.util.Iterator;
import java.util.List;
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
public class CommonsFileUploadServlet extends HttpServlet {

    private static final long serialVersionUID = -1945975988931576619L;
    private static final String TMP_DIR_PATH = "/var/tmp";
    private File tmpDir;
    private static final String DESTINATION_DIR_PATH = "files";
    private File destinationDir;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        log(getServletContext().getRealPath(DESTINATION_DIR_PATH));
        log(TMP_DIR_PATH);
        tmpDir = new File(TMP_DIR_PATH);
        if (!tmpDir.isDirectory()) {
            throw new ServletException(TMP_DIR_PATH + " is not a directory");
        }

        String realPath = getServletContext().getRealPath(DESTINATION_DIR_PATH);
        destinationDir = new File(realPath);

        if (!destinationDir.isDirectory()) {
            String files = getServletContext().getRealPath("/") + "/files";
            destinationDir = new File(files);
            destinationDir.mkdir();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        log("Content-Type: " + request.getContentType());

        DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
        /*
         *Set the size threshold, above which content will be stored on disk.
         */
        fileItemFactory.setSizeThreshold(10 * 1024 * 1024); //10 MB
         /*
         * Set the temporary directory to store the uploaded files of size above threshold.
         */
        fileItemFactory.setRepository(tmpDir);

        ServletFileUpload uploadHandler = new ServletFileUpload(fileItemFactory);
        try {
            /*
             * Parse the request
             */
            List items = uploadHandler.parseRequest(request);
            log("FileItems: " + items.toString());
            Iterator itr = items.iterator();
            while (itr.hasNext()) {

                FileItem item = (FileItem) itr.next();
                /*
                 * Handle Form Fields.
                 */
                if (item.isFormField()) {
                    out.println("File Name = " + item.getFieldName() + ", Value = " + item.getString());
                } else {
                    //Handle Uploaded files.
                    out.println("<html><head><title>CommonsFileUploadServlet</title></head><body><p>");
                    out.println("Field Name = " + item.getFieldName()
                            + "\nFile Name = " + item.getName()
                            + "\nContent type = " + item.getContentType()
                            + "\nFile Size = " + item.getSize());
                    out.println("</p>");
                    out.println("<img src=\"" + request.getContextPath() + "/files/" + item.getName() + "\"/>");
                    out.println("</body></html>");
                    /*
                     * Write file to the ultimate location.
                     */
                    File file = new File(destinationDir, item.getName());
                    item.write(file);
                }
                out.close();
            }
        } catch (FileUploadException ex) {
            log("Error encountered while parsing the request", ex);
        } catch (Exception ex) {
            log("Error encountered while uploading file", ex);
        }
    }
}
