/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2007 Alejandro P. Revilla
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.ee.action;

import id.co.keriss.switching.ee.Module;
import id.co.keriss.switching.dao.ModuleDao;
import id.co.keriss.switching.ee.Moduleperm;
import id.co.keriss.switching.dao.ModulepermDao;

import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpublish.JPublishContext;

import com.anthonyeden.lib.config.Configuration;

public class CheckAccess extends ActionSupport {
    public void execute (JPublishContext context, Configuration cfg) {
    	String tab = (String) context.getPage().get ("tab");
    	String perms;
        DB db = getDB(context);
		ModuleDao moduledao = new ModuleDao(db);
		Module module = moduledao.findByName(tab);
		//System.out.println("tab : "+tab+", module : "+module);
		String temp="";
		if(module!=null){
			Set<Moduleperm> moduleperms = module.getModuleperm();
			for(Moduleperm modperm:moduleperms){
				temp+=modperm.getRole().getName()+" ";
			}
		}
		perms = (module!=null)?temp.trim():(String) context.getPage().get ("perms");
        HttpSession         session  = context.getSession();
        HttpServletRequest  request  = context.getRequest();

        User user = (User) session.getAttribute (USER);

        context.getSyslog().info ("CHECK: " 
                + context.getRequest().getRequestURI() 
                + " user=" + user + " perms=" + perms);
        boolean stay=false;
        if (user != null) {
            if (perms != null) {
                context.getSyslog().info ("Checking permissions: " + perms);
                StringTokenizer st = new StringTokenizer (perms);
                boolean deny = false;
                while (st.hasMoreTokens()) {
                    String permName = st.nextToken();
                    if (permName.startsWith ("!")) {
                        if (user.hasPermission (permName.substring(1)))
                            deny = true;
                    } else if (!user.hasPermission (permName)&&!stay) {
                        context.getSyslog().info (" user doesn't have " + permName);
                        deny = true;
                    } else if(user.hasPermission (permName)&&!stay){
                    	context.getSyslog().info (" user have " + permName);
                    	stay = true;
                        deny = false;
                    }
                }
                if (deny) {
                    sendRedirect (context, request.getContextPath() 
                        + "/stop.html"
                    );
                }
            }
            return; // nothing to do
        }
        String originalUri = request.getRequestURI ();
        String queryString = request.getQueryString ();

        String loginUrl    = request.getContextPath() + "/login.html";
        if (queryString != null && queryString.length() > 0)
            originalUri += "?" + queryString;
        if (!originalUri.endsWith ("login.html"))
            context.getSession().setAttribute("redirect", originalUri);

        context.getSyslog().info ("  redirecting to " + loginUrl);
        sendRedirect (context, loginUrl);
    }
}

