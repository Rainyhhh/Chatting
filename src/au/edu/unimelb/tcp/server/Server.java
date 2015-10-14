package au.edu.unimelb.tcp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;


public class Server {

	private static int count = 0;

	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = null;

		// connect to a server listening on port 4444 on localhost

		ComLineValues values = new ComLineValues();
		CmdLineParser parser = new CmdLineParser(values);
		try {
			int port = 4444;
			parser.parseArgument(args);
			// Server is listening on port 4444 default
			serverSocket = new ServerSocket(port);
		} catch (CmdLineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {

			System.out.println("Server is listening...");

			RoomInfo.getRooms().add(new Room("MainHall", ""));

			while (true) {
				// Server waits for a new connection
				Socket socket = serverSocket.accept();
				// Java creates new socket object for each connection.
				int client_id;
				// new guest come, count ++
				if(UnusedNames.getNames().empty()) {
					count ++;
					client_id = count;
				}
				else client_id = UnusedNames.getNames().pop();
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
