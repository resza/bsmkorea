package id.co.keriss.switching.action;

import id.co.keriss.switching.dao.ModuleDao;
import id.co.keriss.switching.dao.ModulepermDao;
import id.co.keriss.switching.dao.RoleDao;
import id.co.keriss.switching.ee.Module;
import id.co.keriss.switching.ee.ModuleVO;
import id.co.keriss.switching.ee.Moduleperm;
import id.co.keriss.switching.ee.ModulepermVO;
import id.co.keriss.switching.ee.Role;
import id.co.keriss.switching.util.ReportUtil;

import java.io.File;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.action.ActionSupport;
import org.jpublish.JPublishContext;
import org.mortbay.log.Log;

import com.anthonyeden.lib.config.Configuration;

public class ModuleParameter extends ActionSupport {
	private Boolean content = true;
	private String id,submit;
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
	try{
		ReportUtil util = ReportUtil.getInstance();
		HttpServletRequest  request  = context.getRequest();
		id = request.getParameter("id");
		submit = request.getParameter("submit");
		DB db = getDB(context);
		Module module = new ModuleDao(db).findById(Long.decode(id));
		context.put("name", module.getName());
		checkModuleAccess(context, db);
		ModulepermDao moduledao = new ModulepermDao(db);
		if(submit!=null){
			setPermissions (request, module, db);
			restart();
			sendRedirect (context, context.getRequest().getContextPath() 
                    + "/lounge/module.html"
                );
		}
		Set<Module> chlds = (Set<Module>) module.getChild();
		List<ModuleVO> mvos = new Vector<ModuleVO>();
		for(Module child:chlds){
			String temp="";
			Set<Moduleperm> perms = child.getModuleperm();
			for(Moduleperm perm:perms){
				temp+=" "+perm.getName()+" ";
			}
			ModuleVO mvo = new ModuleVO();
			mvo.setId(child.getId());
			mvo.setName(child.getName());
			mvo.setPerms("["+temp+"]");
			mvos.add(mvo);
		}
		List<Role> roles = new RoleDao(db).findAll();
		List<ModulepermVO> mpvos = new Vector();
		for(Role role:roles){
			ModulepermVO mpvo = new ModulepermVO();
			mpvo.setName(role.getName());
			mpvo.setAuth(moduledao.hasModulePerm(module.getName(), role.getName()));
			mpvos.add(mpvo);
		}
		context.put("day", util.daynow());
		context.put("time", util.timenow());
		context.put("name", module.getName());
		context.put("module", module);
		context.put("id", id);
		context.put("childs", mvos);
		context.put("roles", mpvos);
		context.put("moduleperm", moduledao);
		}catch (Exception e) {
            e.printStackTrace();
			error (context, e.getMessage());
			Log.debug(e.getMessage());
            context.getSyslog().error (e);
            context.put("content", content);
		}
	}
    private void setPermissions (HttpServletRequest request, Module m, DB db) 
        {
    		try{
            org.hibernate.Transaction tx = db.beginTransaction();
            ModulepermDao mpdao = new ModulepermDao(db);
            Set<Moduleperm> perms = m.getModuleperm();
            for(Moduleperm perm:perms)mpdao.deleteModuleperm(perm);
            Enumeration en = request.getParameterNames ();
            while (en.hasMoreElements()) {
                String p = (String) en.nextElement();                
                if (p.startsWith ("_perm_") && p.length() > 6) {
                    String permName = p.substring (6);
                    Moduleperm moduleperm = new Moduleperm();
                    moduleperm.setModule(m);
                    moduleperm.setRole(new RoleDao(db).findByName(permName));
                    moduleperm.setName(permName);
                    mpdao.saveUpdateModuleperm(moduleperm);
                }
            }
            tx.commit();
    		}catch(Exception e){e.printStackTrace();}
        }
    private void restart(){
    	try{
    		Properties prop = new Properties();
            prop.load(new FileInputStream("config.properties"));
            String path = this.getClass().getProtectionDomain().getCodeSource().getLocation()+"";//prop.getProperty("currpath");
            path=path.trim();
            path=path.replace("file:", "");
            path=path.replace("/build/Lounge.jar", "");
            path=path.replace("/build/Loungedev.jar", "");
            String command=path+"/bin/restart";
     		System.out.println("Restart, command : "+command);
     		Process p = Runtime.getRuntime().exec(command);
     	}catch(Exception e){
     		e.printStackTrace();
     	}
    }
}
