package au.edu.unimelb.tcp.client;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.simple.parser.ParseException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public class Client {

	public static void main(String[] args) throws IOException, ParseException {
		Socket socket = null;
		
		
		// connect to a server listening on port 4444 on localhost
		try {
			ComLineValues values = new ComLineValues();
			CmdLineParser parser = new CmdLineParser(values);
			try {
				int port = 4444;
				parser.parseArgument(args);
				String hostname = values.getHost();
				socket = new Socket(hostname, port);
			} catch (CmdLineException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
//			if (!args[0].equals("") && !args[1].equals("")) {
//				hostname = args[0];
//				port = Integer.parseInt(args[1]);
//			}
			
			Thread receiveThread = new Thread(new MessageReceiveThread(socket));
			receiveThread.start();
			Thread sendThread = new Thread(new MessageSendThread(socket));
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
