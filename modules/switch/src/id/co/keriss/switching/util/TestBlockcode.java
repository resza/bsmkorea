package id.co.keriss.switching.util;

import java.util.Random;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;

public class TestBlockcode {

	public static void main(String[] args) throws ISOException {
		/*String accbc="  ";
		String cardbc="  ";
		//if(accbc.length()==0||cardbc.length()==0||accbc.trim().length()==0){System.out.println("Not Avalible");return;}
		System.out.println("lenght acccb :"+accbc.length()+", lenght cardcb :"+cardbc.length());
		accbc = (accbc!=null)?accbc.replaceAll("\\s+", ""):"";
		cardbc = (cardbc!=null)?cardbc.replaceAll("\\s+", ""):"";
		System.out.println("lenght acccb :"+accbc.length()+", lenght cardcb :"+cardbc.length());
		int pick=accbc.length()>cardbc.length()?accbc.length():cardbc.length();
		accbc=ISOUtil.padright(accbc, pick, ' ');
		cardbc=ISOUtil.padright(cardbc, pick, ' ');
		System.out.println("lenght acccb :"+accbc.length()+", lenght cardcb :"+cardbc.length());
		System.out.println("RANDOM : "+random());*/
		/*int count = 1;
		int days = 7;
		int res = 0;
		res = days / count;*/
		/*String res="file:/home/resza/Documents/workspace/lounge/build/Lounge.jar/bin/stop".trim();
		res=res.replace("file:", "");
		res=res.replace("/build/Lounge.jar", "");
		res=res.replace("/build/Loungedev.jar", "");
		System.out.println("res : "+res);*/
		int size = 30;
		double lines = 27.0;
		int temp = (int) Math.ceil(size/lines);
		System.out.println("result : "+temp);
	}
	public static String random(){
		return (new Random().nextInt(999999)+1+"").substring(0, 6);
	}

}
