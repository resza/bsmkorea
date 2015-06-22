package id.co.keriss.switching.packager;

import java.util.BitSet;

import org.jpos.iso.IFB_AMOUNT;
import org.jpos.iso.IFB_BINARY;
import org.jpos.iso.IFB_BITMAP;
import org.jpos.iso.IFB_LLCHAR;
import org.jpos.iso.IFB_LLLBINARY;
import org.jpos.iso.IFB_LLLCHAR;
import org.jpos.iso.IFB_LLLNUM;
import org.jpos.iso.IFB_LLNUM;
import org.jpos.iso.IFB_NUMERIC;
import org.jpos.iso.IF_CHAR;
import org.jpos.iso.ISOBasePackager;
import org.jpos.iso.ISOBitMap;
import org.jpos.iso.ISOBitMapPackager;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOFieldPackager;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOMsgFieldPackager;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.ISO87BPackager;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;

public class ClientBillPaymentReqPackager extends ISO87BPackager {
    private static final boolean pad = true;
    protected ISOFieldPackager fld[] = {
            new IFB_NUMERIC (  4, "MESSAGE TYPE INDICATOR", true),
            new IFB_BITMAP  ( 16, "BIT MAP"),
            new IFB_LLNUM   ( 19, "PAN - PRIMARY ACCOUNT NUMBER", pad),
            new IFB_NUMERIC (  6, "PROCESSING CODE", true),
            new IFB_NUMERIC ( 12, "AMOUNT, TRANSACTION", true),
            new IFB_NUMERIC ( 12, "AMOUNT, SETTLEMENT", true),
            new IFB_NUMERIC ( 12, "AMOUNT, CARDHOLDER BILLING", true),
            new IFB_NUMERIC ( 10, "TRANSMISSION DATE AND TIME", true),
            new IFB_NUMERIC (  8, "AMOUNT, CARDHOLDER BILLING FEE", true),
            new IFB_NUMERIC (  8, "CONVERSION RATE, SETTLEMENT", true),
            new IFB_NUMERIC (  8, "CONVERSION RATE, CARDHOLDER BILLING", true),
            new IFB_NUMERIC (  6, "SYSTEM TRACE AUDIT NUMBER", true),
            new IFB_NUMERIC (  6, "TIME, LOCAL TRANSACTION", true),
            new IFB_NUMERIC (  4, "DATE, LOCAL TRANSACTION", true),
            new IFB_NUMERIC (  4, "DATE, EXPIRATION", true),
            new IFB_NUMERIC (  4, "DATE, SETTLEMENT", true),
            new IFB_NUMERIC (  4, "DATE, CONVERSION", true),
            new IFB_NUMERIC (  4, "DATE, CAPTURE", true),
            new IFB_NUMERIC (  4, "MERCHANTS TYPE", true),
            new IFB_NUMERIC (  3, "ACQUIRING INSTITUTION COUNTRY CODE", true),
            new IFB_NUMERIC (  3, "PAN EXTENDED COUNTRY CODE", true),
            new IFB_NUMERIC (  3, "FORWARDING INSTITUTION COUNTRY CODE", true),
            new IFB_NUMERIC (  3, "POINT OF SERVICE ENTRY MODE", true),
            new IFB_NUMERIC (  3, "CARD SEQUENCE NUMBER", true),
            new IFB_NUMERIC (  3, "NETWORK INTERNATIONAL IDENTIFIEER", true),
            new IFB_NUMERIC (  2, "POINT OF SERVICE CONDITION CODE", true),
            new IFB_NUMERIC (  2, "POINT OF SERVICE PIN CAPTURE CODE", true),
            new IFB_NUMERIC (  1, "AUTHORIZATION IDENTIFICATION RESP LEN",true),
            new IFB_AMOUNT  (  9, "AMOUNT, TRANSACTION FEE", true),
            new IFB_AMOUNT  (  9, "AMOUNT, SETTLEMENT FEE", true),
            new IFB_AMOUNT  (  9, "AMOUNT, TRANSACTION PROCESSING FEE", true),
            new IFB_AMOUNT  (  9, "AMOUNT, SETTLEMENT PROCESSING FEE", true),
            new IFB_LLNUM   ( 11, "ACQUIRING INSTITUTION IDENT CODE", pad),
            new IFB_LLNUM   ( 11, "FORWARDING INSTITUTION IDENT CODE", pad),
            new IFB_LLCHAR  ( 28, "PAN EXTENDED"),
            new IFB_LLNUM   ( 37, "TRACK 2 DATA", false),//Fld 35
            new IFB_LLLNUM  (104, "TRACK 3 DATA", false),
            new IF_CHAR     ( 12, "RETRIEVAL REFERENCE NUMBER"),
            new IF_CHAR     (  6, "AUTHORIZATION IDENTIFICATION RESPONSE"),
            new IF_CHAR     (  2, "RESPONSE CODE"),
            new IF_CHAR     (  3, "SERVICE RESTRICTION CODE"),
            new IF_CHAR     (  8, "CARD ACCEPTOR TERMINAL IDENTIFICACION"),
            new IF_CHAR     ( 15, "CARD ACCEPTOR IDENTIFICATION CODE" ),
            new IF_CHAR     ( 40, "CARD ACCEPTOR NAME/LOCATION"),
            new IFB_LLCHAR  ( 25, "ADITIONAL RESPONSE DATA"),
            new IFB_LLCHAR  ( 76, "TRACK 1 DATA"),
            new IFB_LLLCHAR (999, "ADITIONAL DATA - ISO"),
            new IFB_LLLCHAR (999, "ADITIONAL DATA - NATIONAL"),
            new ISOMsgFieldPackager(new IFB_LLLBINARY (999, "Field 48"), new F48Packager()),/* field 48 */
            new IF_CHAR     (  3, "CURRENCY CODE, TRANSACTION"),
            new IF_CHAR     (  3, "CURRENCY CODE, SETTLEMENT"),
            new IF_CHAR     (  3, "CURRENCY CODE, CARDHOLDER BILLING"   ),
            new IFB_BINARY  (  8, "PIN DATA"),
            new IFB_NUMERIC ( 16, "SECURITY RELATED CONTROL INFORMATION", true),
            new IFB_LLLCHAR (120, "ADDITIONAL AMOUNTS"),
            new IFB_LLLBINARY (999, "IC RELATED DATA"),
            new IFB_LLLCHAR (999, "RESERVED ISO"),
            new IFB_LLLCHAR (999, "RESERVED NATIONAL"),
            new IFB_LLLCHAR (999, "RESERVED NATIONAL"),
            new IFB_LLLNUM  (999, "RESERVED NATIONAL", false),
            new IFB_LLLNUM  (14, "RESERVED PRIVATE", false),//field 60 IFB_LLLNUM (14, "RESERVED PRIVATE", pad)
            new IFB_LLLNUM  (999, "RESERVED PRIVATE", false),
            new IFB_LLLCHAR (999, "RESERVED PRIVATE"),
            new IFB_LLLCHAR (999, "RESERVED PRIVATE"),
            new IFB_BINARY  (  8, "MESSAGE AUTHENTICATION CODE FIELD"),
            new IFB_BINARY  (  1, "BITMAP, EXTENDED"),
            new IFB_NUMERIC (  1, "SETTLEMENT CODE", true),
            new IFB_NUMERIC (  2, "EXTENDED PAYMENT CODE", true),
            new IFB_NUMERIC (  3, "RECEIVING INSTITUTION COUNTRY CODE", true),
            new IFB_NUMERIC (  3, "SETTLEMENT INSTITUTION COUNTRY CODE", true),
            new IFB_NUMERIC (  3, "NETWORK MANAGEMENT INFORMATION CODE", true),
            new IFB_NUMERIC (  4, "MESSAGE NUMBER", true),
            new IFB_NUMERIC (  4, "MESSAGE NUMBER LAST", true),
            new IFB_NUMERIC (  6, "DATE ACTION", true),
            new IFB_NUMERIC ( 10, "CREDITS NUMBER", true),
            new IFB_NUMERIC ( 10, "CREDITS REVERSAL NUMBER", true),
            new IFB_NUMERIC ( 10, "DEBITS NUMBER", true),
            new IFB_NUMERIC ( 10, "DEBITS REVERSAL NUMBER", true),
            new IFB_NUMERIC ( 10, "TRANSFER NUMBER", true),
            new IFB_NUMERIC ( 10, "TRANSFER REVERSAL NUMBER", true),
            new IFB_NUMERIC ( 10, "INQUIRIES NUMBER", true),
            new IFB_NUMERIC ( 10, "AUTHORIZATION NUMBER", true),
            new IFB_NUMERIC ( 12, "CREDITS, PROCESSING FEE AMOUNT", true),
            new IFB_NUMERIC ( 12, "CREDITS, TRANSACTION FEE AMOUNT", true),
            new IFB_NUMERIC ( 12, "DEBITS, PROCESSING FEE AMOUNT", true),
            new IFB_NUMERIC ( 12, "DEBITS, TRANSACTION FEE AMOUNT", true),
            new IFB_NUMERIC ( 16, "CREDITS, AMOUNT", true),
            new IFB_NUMERIC ( 16, "CREDITS, REVERSAL AMOUNT", true),
            new IFB_NUMERIC ( 16, "DEBITS, AMOUNT", true),
            new IFB_NUMERIC ( 16, "DEBITS, REVERSAL AMOUNT", true),
            new IFB_NUMERIC ( 42, "ORIGINAL DATA ELEMENTS", true),
            new IF_CHAR     (  1, "FILE UPDATE CODE"),
            new IF_CHAR     (  2, "FILE SECURITY CODE"),
            new IF_CHAR     (  6, "RESPONSE INDICATOR"),
            new IF_CHAR     (  7, "SERVICE INDICATOR"),
            new IF_CHAR     ( 42, "REPLACEMENT AMOUNTS"),
            new IFB_BINARY  ( 16, "MESSAGE SECURITY CODE"),
            new IFB_AMOUNT  ( 17, "AMOUNT, NET SETTLEMENT", pad),
            new IF_CHAR     ( 25, "PAYEE"),
            new IFB_LLNUM   ( 11, "SETTLEMENT INSTITUTION IDENT CODE", pad),
            new IFB_LLNUM   ( 11, "RECEIVING INSTITUTION IDENT CODE", pad),
            new IFB_LLCHAR  ( 17, "FILE NAME"),
            new IFB_LLCHAR  ( 28, "ACCOUNT IDENTIFICATION 1"),
            new IFB_LLCHAR  ( 28, "ACCOUNT IDENTIFICATION 2"),
            new IFB_LLLCHAR (100, "TRANSACTION DESCRIPTION"),
            new IFB_LLLCHAR (999, "RESERVED ISO USE"), 
            new IFB_LLLCHAR (999, "RESERVED ISO USE"), 
            new IFB_LLLCHAR (999, "RESERVED ISO USE"), 
            new IFB_LLLCHAR (999, "RESERVED ISO USE"), 
            new IFB_LLLCHAR (999, "RESERVED ISO USE"), 
            new IFB_LLLCHAR (999, "RESERVED ISO USE"), 
            new IFB_LLLCHAR (999, "RESERVED ISO USE"), 
            new IFB_LLLCHAR (999, "RESERVED NATIONAL USE"),
            new IFB_LLLCHAR (999, "RESERVED NATIONAL USE"),
            new IFB_LLLCHAR (999, "RESERVED NATIONAL USE"   ),
            new IFB_LLLCHAR (999, "RESERVED NATIONAL USE"),
            new IFB_LLLCHAR (999, "RESERVED NATIONAL USE"  ),
            new IFB_LLLCHAR (999, "RESERVED NATIONAL USE"),
            new IFB_LLLCHAR (999, "RESERVED NATIONAL USE"),
            new IFB_LLLCHAR (999, "RESERVED NATIONAL USE"),
            new IFB_LLLCHAR (999, "RESERVED PRIVATE USE"),
            new IFB_LLLCHAR (999, "RESERVED PRIVATE USE"),
            new IFB_LLLCHAR (999, "RESERVED PRIVATE USE"),
            new IFB_LLLCHAR (999, "RESERVED PRIVATE USE"),
            new IFB_LLLCHAR (999, "RESERVED PRIVATE USE"),
            new IFB_LLLCHAR (999, "RESERVED PRIVATE USE"),
            new IFB_LLLCHAR (999, "RESERVED PRIVATE USE"),
            new IFB_LLLCHAR (999, "RESERVED PRIVATE USE"),
            new IFB_BINARY  (  8, "MAC 2")
        };
    public ClientBillPaymentReqPackager() {
        super();
        setFieldPackager(fld);
    }
    
    protected class F48Packager extends ISOBasePackager 
	{
		protected ISOFieldPackager fld48[] = 
			{
				new org.jpos.iso.IFB_NUMERIC(2,     "Kode Group",true),
				new org.jpos.iso.IF_CHAR(12,    "Ref Inquery"),
	        	new org.jpos.iso.IF_CHAR(12,    "Kode Produk"),
	        	new org.jpos.iso.IF_CHAR(14,    "ID Pelanggan1/Kode Area"),
	        	new org.jpos.iso.IF_CHAR(12,    "ID Pelanggan1/No Tlp"),
	        	new org.jpos.iso.IF_CHAR(14,    "ID Pelanggan3")
	    	};
    	public F48Packager () 
    	{
        	super();
        	setFieldPackager(fld48);
    	}
	}
    protected boolean emitBitMap () {
    	return true;
	}
}