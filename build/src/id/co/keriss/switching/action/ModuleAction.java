package id.co.keriss.switching.action;

import id.co.keriss.switching.dao.ModuleDao;
import id.co.keriss.switching.ee.Module;
import id.co.keriss.switching.ee.ModuleVO;
import id.co.keriss.switching.ee.Moduleperm;
import id.co.keriss.switching.util.ReportUtil;

import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.jpos.ee.DB;
import org.jpos.ee.action.ActionSupport;
import org.jpublish.JPublishContext;
import org.mortbay.log.Log;

import com.anthonyeden.lib.config.Configuration;

public class ModuleAction extends ActionSupport {
	private Boolean content = true;
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		System.out.println("Context type :"+context.getRequest());
		try{
		DB db = getDB(context);
		checkModuleAccess(context, db);
		ModuleDao mdao = new ModuleDao(db);
		List<Module> modules = (List<Module>) mdao.findByParent();
		List<ModuleVO> mvos = new Vector<ModuleVO>();
		for(Module module:modules){
			String temp="";
			Set<Moduleperm> perms = module.getModuleperm();
			for(Moduleperm perm:perms){
				temp+=" "+perm.getName()+" ";
			}
			ModuleVO mvo = new ModuleVO();
			mvo.setId(module.getId());
			mvo.setName(module.getName());
			mvo.setPerms("["+temp+"]");
			mvos.add(mvo);
		}
		context.put("modules", mvos);
		context.put("sysdate", ReportUtil.getInstance().daynow());
		context.put("systime", ReportUtil.getInstance().timenow());	
		}catch (Exception e) {
            e.printStackTrace();
			error (context, e.getMessage());
			Log.debug(e.getMessage());
            context.getSyslog().error (e);
            context.put("content", content);
		}
	}
}
