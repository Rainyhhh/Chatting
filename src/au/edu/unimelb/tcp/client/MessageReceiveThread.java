package au.edu.unimelb.tcp.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MessageReceiveThread implements Runnable {

	private Socket socket;
	private String identity = "";
	private String room_id = "";

	private static String new_room = "";
	private static String delete_room = "";

	private BufferedReader in;

	private JSONParser parser = new JSONParser();

	private boolean run = true;

	public MessageReceiveThread(Socket socket) throws IOException {
		this.socket = socket;
	}

	public static String getNew_room() {
		return new_room;
	}

	public static void setNew_room(String new_room) {
		MessageReceiveThread.new_room = new_room;
	}

	public static String getDelete_room() {
		return delete_room;
	}

	public static void setDelete_room(String delete_room) {
		MessageReceiveThread.delete_room = delete_room;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

		try {
			this.in = new BufferedReader(new InputStreamReader(
					socket.getInputStream(), "UTF-8"));
			JSONObject message;
			int i = 0;
			// receive the first several messages
			while (i < 4) {
				message = (JSONObject) parser.parse(in.readLine());
				MessageReceive(socket, message);
				i++;
			}
			while (run) {
				System.out.print("[" + room_id + "] " + identity + "> ");
				message = (JSONObject) parser.parse(in.readLine());
				MessageReceive(socket, message);
			}
			System.exit(0);
			in.close();
			socket.close();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void MessageReceive(Socket socket, JSONObject message)
			throws IOException, ParseException {
		String type = (String) message.get("type");
		// Server send new identity
		if (type.equals("newidentity")) {
			String identity = (String) message.get("identity");
			// new guest
			if (message.get("former").equals("")) {
				this.identity = identity;
				System.out.println("Connected to localhost as " + identity);
				// new identity is invalid
			} else if (message.get("former").equals(message.get("identity"))) {
				System.out.println("Requested identity invalid or in use");
				// identity changed successfully
			} else if (!message.get("former").equals(message.get("identity"))) {
				// if it is myself change the identity
				if (this.identity.equals(message.get("former"))) {
					this.identity = (String) message.get("identity");
				} else
					System.out.println();
				System.out.println(message.get("former") + " is now "
						+ message.get("identity"));
			}
		}
		// Server send a room list
		if (type.equals("roomlist")) {
			boolean isExisted = false;
			JSONArray array = (JSONArray) message.get("rooms");
			// print all the rooms
			for (int i = 0; i < array.size(); i++) {
				JSONObject obj = (JSONObject) array.get(i);
				System.out.println(obj.get("roomid") + ": " + obj.get("count")
						+ " guests");
			}
			// if client request creating room
			if (!new_room.equals("")) {
				for (int i = 0; i < array.size(); i++) {
					JSONObject obj = (JSONObject) array.get(i);
					// check if the new room has been added successfully and it
					// is the client created
					if (obj.get("roomid").equals(new_room)) {
						isExisted = true;
						if (!obj.get("count").equals((long) 0)) {
							System.out.println("Room " + new_room
									+ " is invalid or already in use.");
						} else {
							System.out.println("Room " + obj.get("roomid")
									+ " created.");
						}
						break;
					}
				}
				// room is not created successfully
				if (!isExisted)
					System.out.println("Room " + new_room
							+ " is invalid or already in use.");
				new_room = "";
			}
			// if client request deleting room
			if (!delete_room.equals("")) {
				for (int i = 0; i < array.size(); i++) {
					JSONObject obj = (JSONObject) array.get(i);
					// if the room has not been deleted
					if (obj.get("roomid").equals(delete_room)) {
						System.out.println("Room " + obj.get("roomid")
								+ " is invalid or you are not the owner.");
						delete_room = "";
						break;
					}
				}
				// room has
				if (!delete_room.equals(""))
					System.out.println("Room " + delete_room
							+ " is invalid or has been deleted.");
				delete_room = "";
			}

			return;
		}
		// Server send a room change
		if (type.equals("roomchange")) {
			// new guest
			if (message.get("former").equals("")
					&& message.get("identity").equals(identity)) {
				room_id = (String) message.get("roomid");
				System.out.println(message.get("identity") + " moves to "
						+ room_id);
			} else if (message.get("former").equals("")
					&& !message.get("identity").equals(identity)) {
				System.out.println();
				System.out.println(message.get("identity") + " moves to "
						+ room_id);
				// join other room unsuccessfully
			} else if (message.get("former").equals(message.get("roomid"))) {
				System.out.println("this room is invalid or non existent");
			} else if (message.get("roomid").equals("")) {
				// check if it is other guests or myself join other room
				// successfully
				if (message.get("identity").equals(identity)) {
					room_id = (String) message.get("roomid");
					System.out.println(message.get("identity") + " has quit!");
					in.close();
					run = false;
					MessageSendThread.setRun(false);
				} else {
					System.out.println();
					System.out.println(message.get("identity") + " has quit!");
				}
			} else {
				// check if it is other guests or myself join other room
				// successfully
				if (message.get("identity").equals(identity)) {
					room_id = (String) message.get("roomid");
					System.out.println(message.get("identity") + " moves from "
							+ message.get("former") + " to "
							+ message.get("roomid"));
					if (room_id.equals("MainHall")) {
						message = (JSONObject) parser.parse(in.readLine());
						MessageReceive(socket, message);
						message = (JSONObject) parser.parse(in.readLine());
						MessageReceive(socket, message);
					}
				} else {
					System.out.println();
					System.out.println(message.get("identity") + " moves from "
							+ message.get("former") + " to "
							+ message.get("roomid"));
				}
			}
			return;
		}
		// Server send a room contents
		if (type.equals("roomcontents")) {
			JSONArray array = (JSONArray) message.get("identities");
			System.out.print(message.get("roomid") + " contains");
			for (int i = 0; i < array.size(); i++) {
				System.out.print(" " + array.get(i));
				if (message.get("owner").equals(array.get(i))) {
					System.out.print("*");
				}
			}
			System.out.println();
			return;
		}
		if (type.equals("message")) {
			if (!message.get("identity").equals(identity))
				System.out.println();
			System.out.println(message.get("identity") + ": "
					+ message.get("content"));
			return;
		}
	}
}
