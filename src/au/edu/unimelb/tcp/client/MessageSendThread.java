package au.edu.unimelb.tcp.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import org.json.simple.JSONObject;

public class MessageSendThread implements Runnable {

	private Socket socket;

	private DataOutputStream out;
	
	private static boolean run = true;

	public static boolean isRun() {
		return run;
	}

	public static void setRun(boolean run) {
		MessageSendThread.run = run;
	}

	// Reading from console
	private Scanner cmdin = new Scanner(System.in);

	public MessageSendThread(Socket socket) throws IOException {
		this.socket = socket;
		out = new DataOutputStream(socket.getOutputStream());
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (run) {
			String msg = cmdin.nextLine();
			try {
				MessageSend(socket, msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			out.close();
			this.socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void MessageSend(Socket socket, String msg) throws IOException {
		JSONObject sendToServer = new JSONObject();
		String []array = msg.split(" ");
		if(!array[0].startsWith("#")) {
			sendToServer = ClientMessages.Message("message", msg);
			out.write((sendToServer.toJSONString() + "\n").getBytes("UTF-8"));
			out.flush();
		}
		else if(array.length < 2 || array.length > 2) {
			if(array.length == 4 && array[0].startsWith("#kick")) {
				sendToServer = ClientMessages.Kick("kick", array[1], Integer.parseInt(array[2]), array[3]);
				out.write((sendToServer.toJSONString() + "\n").getBytes("UTF-8"));
				out.flush();
			}
			else if(array[0].startsWith("#list")) {
				sendToServer = ClientMessages.List("list");
				out.write((sendToServer.toJSONString() + "\n").getBytes("UTF-8"));
				out.flush();
			}
			else if(array[0].startsWith("#quit")) {
				sendToServer = ClientMessages.Quit("quit");
				out.write((sendToServer.toJSONString() + "\n").getBytes("UTF-8"));
				out.flush();
			}
			else System.out.println("Invalid command!");
		}
		else if(array[0].startsWith("#identitychange")) {
			sendToServer = ClientMessages.IdentityChange("identitychange", array[1]);
			out.write((sendToServer.toJSONString() + "\n").getBytes("UTF-8"));
			out.flush();
		}
		else if(array[0].startsWith("#join")) {
			sendToServer = ClientMessages.Join("join", array[1]);
			out.write((sendToServer.toJSONString() + "\n").getBytes("UTF-8"));
			out.flush();
		}
		
		else if(array[0].startsWith("#who")) {
			sendToServer = ClientMessages.Who("who", array[1]);
			out.write((sendToServer.toJSONString() + "\n").getBytes("UTF-8"));
			out.flush();
		}
		else if(array[0].startsWith("#createroom")) {
			sendToServer = ClientMessages.CreateRoom("createroom", array[1]);
			MessageReceiveThread.setNew_room(array[1]);
			out.write((sendToServer.toJSONString() + "\n").getBytes("UTF-8"));
			out.flush();
		}
		else if(array[0].startsWith("#delete")) {
			sendToServer = ClientMessages.Delete("delete", array[1]);
			MessageReceiveThread.setDelete_room(array[1]);
			out.write((sendToServer.toJSONString() + "\n").getBytes("UTF-8"));
			out.flush();
		}		
				
		// forcing TCP to send data immediately
	}
}
