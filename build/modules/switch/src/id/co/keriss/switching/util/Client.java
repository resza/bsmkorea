package id.co.keriss.switching.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.jpos.iso.ISOClientSocketFactory;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.SunJSSESocketFactory;
public class Client implements ISOClientSocketFactory{
	Socket requestSocket;
	OutputStream out;
 	InputStream in;
 	String message;
	void run()
	{
		try{
			byte[] cardno = ISOUtil.str2bcd("4377001023461259",false);
			//byte[] cardno = ISOUtil.str2bcd("4377000000101318",false);
			//byte[] cardno = ISOUtil.str2bcd("4377001000159165",false);
			//byte[] cardno = ISOUtil.str2bcd("5402251000000077",false);
			//byte[] cardno = ISOUtil.str2bcd("4377014000083913",false);
					byte [] verify ={00,(byte)0x4d,(byte)0x60,(byte)0x00,(byte)0x02,(byte)0x00,(byte)0x00,(byte)0x02,(byte)0x00,(byte)0x20,(byte)0x20,(byte)0x05,(byte)0x80,(byte)0x20,(byte)0xc0,(byte)0x00
							,(byte)0x04,(byte)0x97,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x29,(byte)0x00,(byte)0x51,(byte)0x00,(byte)0x02,(byte)0x00,(byte)0x37,cardno[0],cardno[1]
							,cardno[2],cardno[3],cardno[4],cardno[5],cardno[6],cardno[7],(byte)0xd1,(byte)0x60,(byte)0x82,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00
							,(byte)0x31,(byte)0x39,(byte)0x35,(byte)0x36,(byte)0x34,(byte)0x38,(byte)0x31,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x31,(byte)0x39,(byte)0x30,(byte)0x32,(byte)0x31,(byte)0x31
							,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x31,(byte)0x31,(byte)0x00,(byte)0x06,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x31};
					byte [] verifyOtherMerch ={00,(byte)0x4d,(byte)0x60,(byte)0x00,(byte)0x02,(byte)0x00,(byte)0x00,(byte)0x02,(byte)0x00,(byte)0x20,(byte)0x20,(byte)0x05,(byte)0x80,(byte)0x20,(byte)0xc0,(byte)0x00
							,(byte)0x04,(byte)0x97,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x29,(byte)0x00,(byte)0x51,(byte)0x00,(byte)0x02,(byte)0x00,(byte)0x37,cardno[0],cardno[1]
							,cardno[2],cardno[3],cardno[4],cardno[5],cardno[6],cardno[7],(byte)0xd1,(byte)0x60,(byte)0x82,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00
							,(byte)0x31,(byte)0x39,(byte)0x35,(byte)0x36,(byte)0x34,(byte)0x38,(byte)0x31,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x31,(byte)0x39,(byte)0x30,(byte)0x32,(byte)0x31,(byte)0x31
							,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x31,(byte)0x31,(byte)0x00,(byte)0x06,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x31};

					byte[] reversal = {
							00,(byte)0x50,(byte)0x60,(byte)0x00,(byte)0x19,(byte)0x00,(byte)0x00,(byte)0x04,(byte)0x00,(byte)0x60,(byte)0x24,(byte)0x05,(byte)0x80,(byte)0x00,(byte)0xc0,(byte)0x01
							,(byte)0x04,(byte)0x16,(byte)0x41,(byte)0x40,(byte)0x09,(byte)0x20,(byte)0x10,(byte)0x40,(byte)0x09,(byte)0x68,(byte)0x97,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x06
							,(byte)0x11,(byte)0x12,(byte)0x00,(byte)0x51,(byte)0x00,(byte)0x19,(byte)0x0f,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x31,(byte)0x31
							,(byte)0x32,(byte)0x33,(byte)0x34,(byte)0x35,(byte)0x36,(byte)0x37,(byte)0x38,(byte)0x39,(byte)0x30,(byte)0x31,(byte)0x32,(byte)0x33,(byte)0x34,(byte)0x35,(byte)0x00,(byte)0x10
							,(byte)0xdf,(byte)0x5c,(byte)0x07,(byte)0x00,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x00,(byte)0x06,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x30
							,(byte)0x30,(byte)0x31
					};
					byte[] test = {
							00,0x2b,0x60,0x00,0x01,0x00,0x00,0x08,0x00,0x20,0x00,0x01,0x00,0x00,(byte) 0xc0,0x00
							,0x00,(byte) 0x00,0x00,0x00,0x00,0x01,0x32,0x32,0x31,0x37,0x32,0x39,0x30,0x31,0x30,0x30
							,0x30,0x31,0x30,0x30,0x30,0x31,0x32,0x31,0x32,0x31,0x37,0x32,0x39
					};
					byte[] testAj ={
							00,(byte)0x2e,(byte)0x60,(byte)0x00,(byte)0x06,(byte)0x80,(byte)0x53,(byte)0x08,(byte)0x00,(byte)0x20,(byte)0x20,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0xc0,(byte)0x00
							,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x02,(byte)0x00,(byte)0x01,(byte)0x37,(byte)0x30,(byte)0x34,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x30
							,(byte)0x31,(byte)0x37,(byte)0x30,(byte)0x34,(byte)0x31,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x31,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x31
					};
					sendMessage(verify);
					in = requestSocket.getInputStream();
						try {
							System.out.print(in.read());
						} catch (Exception e) {
							e.printStackTrace();
						}
					//}
		}
		catch(UnknownHostException unknownHost){
			System.err.println("You are trying to connect to an unknown host!");
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
		finally{
			//4: Closing connection
			try{
				in.close();
				out.close();
				requestSocket.close();
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
	}
	void sendMessage(byte[] msg)
	{
		try{
			/*int port = 8202;
			requestSocket = new Socket("12 2.129.112.50", port);*/
			//String host = "61.8.79.178";
			String host = "localhost";
			//String host = "122.129.112.50";
			//int port = 7802;
			int port = 1413;
			System.out.println("Connected to "+host+" in port : "+port);
			requestSocket = new Socket(host, port);
			out = requestSocket.getOutputStream();
			requestSocket.setSoTimeout(3000);
			out.flush();
			out.write(msg);
			out.flush();
			in=requestSocket.getInputStream();
			try {
				System.out.print(in.read());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	
	void sendSSL(byte[] msg)
	{
		try{
			SunJSSESocketFactory sslsocketfactory = new SunJSSESocketFactory();
			sslsocketfactory.setPassword("123456");
			sslsocketfactory.setKeyPassword("123456");
			requestSocket = sslsocketfactory.createSocket("122.129.112.50", 8175);
			out = requestSocket.getOutputStream();
			out.flush();
			out.write(msg);
			out.flush();
			in=requestSocket.getInputStream();
			try {
				int c = 0;
				int len=0;
				int bufferSize = requestSocket.getReceiveBufferSize();
				System.out.println(bufferSize);
				byte[] bytes = new byte[bufferSize];
				while ((c = in.available()) > 0 || len < 10) { 
		            if (in.read (bytes) != c) 
		                throw new EOFException ("connection closed"); 
		                len += c;
		        }
				System.out.println("Incoming = "+ISOUtil.hexdump(bytes));
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		} catch (ISOException e1) {
			e1.printStackTrace();
		}
	}
	
	public static void main(String args[])
	{
		Client client = new Client();
		client.run();
	}
	@Override
	public Socket createSocket(String arg0, int arg1) throws IOException,
			ISOException {
		return null;
	}
}

