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

package org.jpos.ee.action.sys;

import java.io.IOException;
import java.util.StringTokenizer;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

import org.jpublish.JPublishContext;
import org.jpublish.action.Action;
import com.anthonyeden.lib.config.Configuration;
import org.jpos.ee.action.ActionSupport;

import org.jpos.ee.DB;
import org.jpos.ee.Visitor;
import org.jpos.ee.BLException;
import org.jpos.ee.status.StatusManager;
import org.jpos.ee.status.StatusTag;
import org.jpos.util.DateUtil;
import org.jpos.util.Validator;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;

public class StatusNode extends ActionSupport {
    public void execute (JPublishContext context, Configuration cfg) {
        int PAGE_SIZE = 25;
        HttpServletRequest request = context.getRequest();
        int page = getPage (request.getParameter ("page"));
        try {
            DB db = getDB (context);
            StatusManager mgr = new StatusManager (db);
            mgr.check();    // just in case heartbeat is not running

            String parentId = request.getParameter("parentId");

            if (parentId==null || !Validator.isQuery(parentId)) {
                throw new BLException ("Invalid value in parameter parentId.");
            }

            context.put ("status", db.session().createCriteria(StatusTag.class)
                                   .add( Restrictions.eq("tag",parentId) )
                                   .list().iterator()
                         );
        } catch (Exception e) {
            error (context, e.getMessage());
            context.getSyslog().error (e);
        }
    }
}

