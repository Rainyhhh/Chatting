package au.edu.unimelb.tcp.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;

import javax.net.ssl.SSLSocket;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ClientThread implements Runnable {
	private SSLSocket socket;
	private String guest_id;
	private String former_id = "";
	private int count;
	private boolean is_authenticated = false;

	private BufferedReader in;
	private DataOutputStream out;
	private JSONParser parser = new JSONParser();

	private boolean run = true;

	public ClientThread(SSLSocket socket, int client_id) throws IOException {
		this.socket = socket;
		this.count = client_id;
		this.guest_id = "guest" + client_id;
		out = new DataOutputStream(socket.getOutputStream());
		JSONObject new_identity = ServerMessages.NewIdentity(guest_id,
				former_id);
		out.write((new_identity.toJSONString() + "\n").getBytes("UTF-8"));
		RoomInfo.firstMove(guest_id, "MainHall", out, socket);
		JSONObject room_list = ServerMessages.RoomList();
		out.write((room_list.toJSONString() + "\n").getBytes("UTF-8"));
		out.flush();
	}

	@Override
	public void run() {
		try {
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream(), "UTF-8"));
			try {
				while (run) {
					JSONObject message = new JSONObject();
					String msg = in.readLine();
					if (msg != null) {
						message = (JSONObject) parser.parse(msg);
						MessageManage(message);
					} else
						quit();
					if (message == null)
						run = false;
				}
				in.close();
				socket.close();
			} catch (EOFException e) {
				if (socket != null) {
					socket.close();
					socket = null;
				}
				quit();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SocketException e) {
				if (socket != null) {
					socket.close();
					socket = null;
				}
				quit();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// A thread finishes if run method finishes
	}

	public void MessageManage(JSONObject message) throws IOException {
		String type = (String) message.get("type");
		// # identity change
		if (type.equals("identitychange")) {
			changeIdentity(message);
			return;
		}
		if (type.equals("join")) {
			join(message);
			return;
		}
		if (type.equals("createroom")) {
			if (is_authenticated) {
				createRoom(message);
			} else {
				out.write((ServerMessages.Message("System",
						"You are not authenticated!").toJSONString() + "\n")
						.getBytes("UTF-8"));
				out.flush();
			}
			return;
		}
		if (type.equals("list")) {
			out.write((ServerMessages.RoomList().toJSONString() + "\n")
					.getBytes("UTF-8"));
			out.flush();
			return;
		}
		if (type.equals("who")) {
			roomContents(message);
			return;
		}
		if (type.equals("kick")) {
			if (is_authenticated) {
				kick(message);
			} else {
				out.write((ServerMessages.Message("System",
						"You are not authenticated!").toJSONString() + "\n")
						.getBytes("UTF-8"));
				out.flush();
			}
			return;
		}
		if (type.equals("delete")) {
			if (is_authenticated) {
				delete(message);
			} else {
				out.write((ServerMessages.Message("System",
						"You are not authenticated!").toJSONString() + "\n")
						.getBytes("UTF-8"));
				out.flush();
			}
			return;
		}
		if (type.equals("message")) {
			message(message);
			return;
		}
		if (type.equals("quit")) {
			quit();
			return;
		}
		if (type.equals("authenticate")) {
			authenticate(message);
			return;
		}
	}

	/**
	 * match identitychange message
	 * 
	 * @param message
	 * @throws IOException
	 */
	public void changeIdentity(JSONObject message) throws IOException {
		String newIdentity = (String) message.get("identity");
		if (RoomInfo.changeIdentity(message, former_id, guest_id)) {
			if (count != 0) {
				UnusedNames.push(count);
				count = 0;
			}
			former_id = guest_id;
			guest_id = newIdentity;
			return;
		} else {
			former_id = guest_id;
			out.write((ServerMessages.NewIdentity(guest_id, former_id)
					.toJSONString() + "\n").getBytes("UTF-8"));
			out.flush();
			return;
		}
	}

	/**
	 * match join message
	 * 
	 * @param message
	 * @throws IOException
	 */
	public void join(JSONObject message) throws IOException {
		String newRoom = (String) message.get("roomid");
		RoomInfo.join(newRoom, guest_id, out);
	}

	/**
	 * match createroom message
	 * 
	 * @param message
	 * @throws IOException
	 */
	public void createRoom(JSONObject message) throws IOException {
		RoomInfo.createRoom(message, guest_id);
		out.write((ServerMessages.RoomList().toJSONString() + "\n")
				.getBytes("UTF-8"));
		out.flush();

	}

	/**
	 * match who message
	 * 
	 * @param message
	 * @throws IOException
	 */
	public void roomContents(JSONObject message) throws IOException {
		if (!RoomInfo.roomContents(message, out)) {
			out.write((ServerMessages.Message(guest_id,
					"System: Roomid is invalid!") + "\n").getBytes("UTF-8"));
			out.flush();
		}
	}

	/**
	 * match kick message
	 * 
	 * @param message
	 * @throws IOException
	 */
	public void kick(JSONObject message) throws IOException {
		RoomInfo.kick(message, guest_id, out);
	}

	/**
	 * match delete message
	 * 
	 * @param message
	 * @throws IOException
	 */
	public void delete(JSONObject message) throws IOException {
		RoomInfo.delete(message, guest_id);
		out.write((ServerMessages.RoomList().toJSONString() + "\n")
				.getBytes("UTF-8"));
		out.flush();
	}

	/**
	 * match message message
	 * 
	 * @param message
	 * @throws IOException
	 */
	public void message(JSONObject message) throws IOException {
		RoomInfo.message(message, guest_id);
	}

	/**
	 * match quit message
	 * 
	 * @throws IOException
	 */
	public void quit() throws IOException {
		if (count != 0) {
			UnusedNames.push(count);
			count = 0;
		}
		RoomInfo.quit(guest_id, socket, out);
		run = false;
	}

	/**
	 * match authenticate message
	 * 
	 * @param message
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public void authenticate(JSONObject message)
			throws UnsupportedEncodingException, IOException {
		String new_identity = (String) message.get("identity");
		String password = (String) message.get("password");
		if (RoomInfo.authenticate(guest_id, new_identity, password)) {
			if (count != 0) {
				UnusedNames.push(count);
				count = 0;
			}
			is_authenticated = true;
			former_id = guest_id;
			guest_id = new_identity;
		} else {
			out.write((ServerMessages.Message("System",
					"The identity been used or password is incorrect!")
					.toJSONString() + "\n").getBytes("UTF-8"));
			out.flush();
		}
	}

}
