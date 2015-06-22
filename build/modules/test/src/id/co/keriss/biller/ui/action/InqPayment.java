/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2009 Alejandro P. Revilla
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

package id.co.keriss.biller.ui.action;

import id.co.keriss.switching.packager.ClientBillInstallmentReqPackager;
import id.co.keriss.switching.packager.ClientBillPaymentReqPackager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import org.jdom.Element;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.ISOFilter.VetoException;
import org.jpos.iso.channel.NACChannel;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.iso.packager.ISO87BPackager;
import org.jpos.ui.UI;
import org.jpos.ui.UIAware;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogListener;

public class InqPayment implements ActionListener, UIAware {
    private NACChannel channel;
    private ISOMsg response;
	public UI ui;
    public InqPayment () {
        super();
        createChannel();
    }
    public void setUI (UI ui, Element e) {
        this.ui = ui;
    }
    public void actionPerformed (ActionEvent ev) {
    	try {
    		channel.setPackager(new ClientBillPaymentReqPackager());
    		if(!channel.isConnected())channel.connect();
    		channel.send(payment());
    		channel.setPackager(new ISO87BPackager());
			response = channel.receive();
			channel.disconnect();
		} catch (VetoException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ISOException e) {
			e.printStackTrace();
		}
    }
    
    private ISOMsg payment() throws ISOException {
    	ISOMsg m = new ISOMsg();
        m.setMTI ("0100");
        m.set (3, "720000");                            //Processing Code
        m.set (11, "300682");                           //Systems Trace Number
        m.set (22, "022");
        m.set (24, "001");                               //NII
        m.set (41, "12345678");                         //Terminal ID
        m.set (42, "123456789012345");                  //Merchant ID
        ISOMsg inner = new ISOMsg(48);                              //Data WOM
        	inner.set(1, "1");                                    //Kode Group
        	inner.set(2, ISOUtil.zeropad("010103",6));		//Ref Inq
        	inner.set(3, ISOUtil.zeropad("38495",12));		//Kode Produk
        	inner.set(4, ISOUtil.zeropad("01010",4));		//ID Pelanggan1
        	inner.set(5, ISOUtil.zeropad("010103100507",12));//ID Pelanggan2
        	inner.set(6, ISOUtil.zeropad("010103100507",14));//ID Pelanggan2
        m.set (inner);

        ISOMsg inner62 = new ISOMsg(62);
        inner62.set (1, "123");
        inner62.set (2, "300682");
        m.set (inner62);
        return m;
    }

    private void createChannel(){
       byte[] b = new byte[5];
        try {
            Logger logger = new Logger();
            logger.addListener (new SimpleLogListener (System.out));
            channel = new NACChannel("127.0.0.1", 8702, new ISO87BPackager(),b);
            ((LogSource)channel).setLogger (logger, "channel-payment");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}

