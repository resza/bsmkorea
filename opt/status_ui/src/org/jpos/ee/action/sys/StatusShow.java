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
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import java.sql.SQLException;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

import org.jpublish.JPublishContext;
import org.jpublish.action.Action;
import com.anthonyeden.lib.config.Configuration;
import org.jpos.ee.action.ActionSupport;

import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.Visitor;
import org.jpos.ee.SysLogEvent;
import org.jpos.ee.status.Status;
import org.jpos.ee.status.StatusTag;
import org.jpos.ee.status.StatusManager;
import org.jpos.util.DateUtil;
import org.jpos.util.Validator;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;

public class StatusShow extends ActionSupport {
    public void execute (JPublishContext context, Configuration cfg) {
        try {
            HttpServletRequest request = context.getRequest();
            String id = request.getParameter ("id");
            if (id == null) {
                error (context, "The status id was not specified.", true);
                return;
            }
            if (!Validator.isName(id)) {
                error (context, "Nice try, but the status id is not valid.", true);
                return;
            }
            DB db = getDB (context);
            StatusManager mgr = new StatusManager (db);
            Status status = (Status) db.session().load (
                Status.class, id
            );
            mgr.check (status);
            context.put ("s", status);
            context.put ("dateUtil", new DateUtil());

            int errors = 0;
            String action = request.getParameter ("action");
            if ("Update".equals (action)) {
                User me = (User) context.getSession().getAttribute (USER);
                if (me == null) {
                    error (context, "Access denied.", true);
                    return;
                }
                String name = request.getParameter ("statusName");
                String tags = request.getParameter ("statusTags");
                String group = request.getParameter ("statusGroup");
                String timeout = request.getParameter ("statusTimeout");
                String timeoutState = request.getParameter ("timeoutState");
                String maxEventsString = request.getParameter ("maxEvents");

                if (!Validator.isName (name)) {
                    context.put ("errorName", "* Name has invalid characters");
                    errors++;
                }
                if (!Validator.isName (group)) {
                    context.put ("errorGroup", "* Group has invalid characters");
                    errors++;
                }
                if (!Validator.isName (tags)) {
                    context.put ("errorTags", "* Tags has invalid characters");
                    errors++;
                }
                if (!Validator.isName (timeoutState)) {
                    // we ignore the change and redisplay the select list.
                    // this could just happen with a user handcrafting an URL
                    errors++;
                }
                if (!Validator.isLong (timeout)) {
                    context.put ("errorTimeout", "* invalid timeout");
                    errors++;
                }
                if (!Validator.isLong (maxEventsString)) {
                    context.put ("errorMaxEvents", "* invalid number");
                    errors++;
                }
                if (errors > 0) {
                    context.put ("statusName", name);
                    context.put ("statusTags", tags);
                    context.put ("statusGroup", group);
                    context.put ("statusTimeout", timeout);
                    context.put ("timeoutState", timeoutState);
                    context.put ("maxEvents", maxEventsString);
                    context.put ("edit", Boolean.TRUE);
                    return;
                } else {
                    Transaction tx = db.beginTransaction();
                    long t = Long.parseLong (timeout);
                    int maxEvents = Integer.parseInt (maxEventsString);
                    StringBuffer sb = new StringBuffer();
                    recordChange ("name", status.getName(), name, sb);
                    recordChange (
                        "groupName", status.getGroupName(), group, sb
                    );
                    recordChange ("timeout", status.getTimeout()/1000, t, sb);
                    recordChange ("maxEvents", status.getMaxEvents(), maxEvents, sb);
                    recordChange (
                         "timeoutState", 
                         status.getTimeoutState(), 
                         timeoutState, sb
                    );
                    status.setName (name);
                    status.setGroupName (group);
                    setTags (db, status, tags, sb);
                    status.setTimeout (t*1000);
                    status.setMaxEvents (maxEvents);
                    status.setTimeoutState (timeoutState);
                    if (status.isExpired() && !status.hasExpired()) {   
                        // new timeout in effect - move to OK
                        status.setState (Status.OK);
                        status.setExpired (false);
                    } else if (status.isExpired()) {
                        status.setState (timeoutState);
                    }
                    if (sb.length() > 0)
                        status.logRevision (sb.toString(), me);
                    tx.commit();
                    context.put ("message", "The status has been updated.");
                }
            } else if (action != null && action.length() > 0 && status.getValidCommands() != null) {
                // attempt to execute command
                User me = (User) context.getSession().getAttribute (USER);
                if (me == null) {
                    error (context, "Access denied.", true);
                    return;
                }
                if (status.getValidCommands().indexOf(action) >= 0) {
                    mgr.setNextCommand (status.getId(), action);
                    context.put ("message", action + " command has been scheduled for execution");
                } 
            }
            context.put ("statusName", status.getName());
            context.put ("statusTags", status.getTagsAsString());
            context.put ("statusGroup", status.getGroupName());
            context.put ("statusTimeout", Long.toString(status.getTimeout()/1000));
            context.put ("timeoutState", status.getTimeoutState());
            context.put ("maxEvents", Integer.toString (status.getMaxEvents()));
        } catch (ObjectNotFoundException e) {
            error (context, "The status does not exist.", true);
        } catch (HibernateException e) {
            context.getSyslog().error (e);
            error (context, e.getMessage(), true);
        } catch (NumberFormatException e) {
            error (context, "We have received an invalid status id.", true);
        } catch (SQLException e) {
            context.getSyslog().error (e);
            error (context, e.getMessage(), true);
        }
    }
    private void setTags (DB db, Status status, String taglist, StringBuffer sb) {
        List oldTags = new ArrayList();
        Iterator it = status.getTags().iterator();
        while (it.hasNext()) {
            oldTags.add(((StatusTag)it.next()).getTag());
        }
        Set newTags = status.getTags();
        newTags.clear();
        db.session().flush();

        StringTokenizer st = new StringTokenizer(taglist);
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            StatusTag stag = new StatusTag(token);
            if (!oldTags.contains(token)) {
                if (sb.length()>0) {
                    sb.append(BR);
                }
                sb.append("Added tag: "+token);
            } else {
                oldTags.remove(token);
            }
            newTags.add (stag);
        }
        it = oldTags.iterator();
        while (it.hasNext()) {
            if (sb.length()>0) {
                sb.append(BR);
            }
            String stag = (String) it.next();
            sb.append("Removed tag: "+stag);
        }
        
    }
}

