package id.co.keriss.micro.listener;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOSource;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.MUX;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;
import org.jpos.transaction.Context;
import org.jpos.util.Log;
import org.jpos.util.NameRegistrar;

public class LoungeISOServer extends Log implements ISORequestListener {
	public static final String REQUEST = "REQUEST";
	public static final String ISOSOURCE = "ISOSOURCE";
    public boolean process (ISOSource source, ISOMsg m) {
    	System.out.println("Process in LoungeISOServer");
    	Space sp = SpaceFactory.getSpace("tspace:default"); 
    	Context ctx  = new Context();
    	ctx.put(REQUEST,m);
		ctx.put(ISOSOURCE, source);
		sp.out("TXNMGR", ctx, 60000);
        return true;  
    }
}