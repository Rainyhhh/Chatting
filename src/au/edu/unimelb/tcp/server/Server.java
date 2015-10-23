package au.edu.unimelb.tcp.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyStore;

import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public class Server {

	private static int count = 0;
	
	private static final String SERVER_KEY_STORE_PASSWORD = "11111111";  
    private static final String SERVER_TRUST_KEY_STORE_PASSWORD = "11111111"; 
    
    private static SSLServerSocket serverSocket;
	
	public static void init(int port) {  
        try {  
            SSLContext ctx = SSLContext.getInstance("SSL");  
  
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");  
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");  
  
            KeyStore ks = KeyStore.getInstance("JKS");  
            KeyStore tks = KeyStore.getInstance("JKS");  
  
            ks.load(new FileInputStream("kserver.keystore"), SERVER_KEY_STORE_PASSWORD.toCharArray());  
            tks.load(new FileInputStream("tserver.keystore"), SERVER_TRUST_KEY_STORE_PASSWORD.toCharArray());  
  
            kmf.init(ks, SERVER_KEY_STORE_PASSWORD.toCharArray());  
            tmf.init(tks);  
  
            ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);  
  
            serverSocket = (SSLServerSocket) ctx.getServerSocketFactory().createServerSocket(port);  
            serverSocket.setNeedClientAuth(true);   
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    } 

	public static void main(String[] args) throws IOException {

		// Create SSL server socket factory, which creates SSLServerSocket
		// instances
		//ServerSocketFactory factory = SSLServerSocketFactory.getDefault();
		//ServerSocket serverSocket = null;

		// connect to a server listening on port 4444 on localhost

		ComLineValues values = new ComLineValues();
		CmdLineParser parser = new CmdLineParser(values);
		try {
			int port = 4444;
			parser.parseArgument(args);
			
			init(port);
			// Server is listening on port 4444 default
			//serverSocket = factory.createServerSocket(port);
			//serverSocket = new ServerSocket(port);
		} catch (CmdLineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {

			System.out.println("Server is listening...");

			RoomInfo.getRooms().add(new Room("MainHall", ""));

			while (true) {
				// Server waits for a new connection
				SSLSocket socket = (SSLSocket) serverSocket.accept();
				// Java creates new socket object for each connection.
				int client_id;
				// new guest come, count ++
				if (UnusedNames.getNames().empty()) {
					count++;
					client_id = count;
				} else
					client_id = UnusedNames.getNames().pop();
				// A new thread is created per client
				Thread client = new Thread(new ClientThread(socket, client_id));
				// It starts running the thread by calling run() method
				client.start();
			}

		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (serverSocket != null)
				serverSocket.close();
		}
	}

}
