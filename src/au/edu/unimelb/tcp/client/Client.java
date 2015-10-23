package au.edu.unimelb.tcp.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.json.simple.parser.ParseException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public class Client {
	
	private static final String CLIENT_KEY_STORE_PASSWORD = "11111111";  
    private static final String CLIENT_TRUST_KEY_STORE_PASSWORD = "11111111"; 
    
    private static SSLSocket sslSocket; 
	
	public static void init(String hostname, int port) {  
        try {  
            SSLContext ctx = SSLContext.getInstance("SSL");  
  
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");  
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");  
  
            KeyStore ks = KeyStore.getInstance("JKS");  
            KeyStore tks = KeyStore.getInstance("JKS");  
  
            ks.load(new FileInputStream("kclient.keystore"), CLIENT_KEY_STORE_PASSWORD.toCharArray());  
            tks.load(new FileInputStream("tclient.keystore"), CLIENT_TRUST_KEY_STORE_PASSWORD.toCharArray());  
  
            kmf.init(ks, CLIENT_KEY_STORE_PASSWORD.toCharArray());  
            tmf.init(tks);  
  
            ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);  
  
            sslSocket = (SSLSocket) ctx.getSocketFactory().createSocket(hostname, port);  
        } catch (Exception e) {  
            System.out.println(e);  
        }  
    }  

	public static void main(String[] args) throws IOException, ParseException {
		
		//SocketFactory factory= SSLSocketFactory.getDefault();
		
		
		// connect to a server listening on port 4444 on localhost
		try {
			ComLineValues values = new ComLineValues();
			CmdLineParser parser = new CmdLineParser(values);
			try {
				int port = 4444;
				parser.parseArgument(args);
				String hostname = values.getHost();
				
				init(hostname, port);
				//socket = factory.createSocket(hostname, port);
				//socket = new Socket(hostname, port);
			} catch (CmdLineException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
//			if (!args[0].equals("") && !args[1].equals("")) {
//				hostname = args[0];
//				port = Integer.parseInt(args[1]);
//			}
			
			Thread receiveThread = new Thread(new MessageReceiveThread(sslSocket));
			receiveThread.start();
			Thread sendThread = new Thread(new MessageSendThread(sslSocket));
			sendThread.start();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
