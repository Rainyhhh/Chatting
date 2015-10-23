package au.edu.unimelb.tcp.server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLSocket;

import org.json.simple.JSONObject;

public class RoomInfo {

	static List<Room> rooms = new ArrayList<Room>();

	public static List<Room> getRooms() {
		return rooms;
	}

	public static void setRooms(List<Room> rooms) {
		RoomInfo.rooms = rooms;
	}

	public static Room getRoomContent(String roomid) {
		for (int i = 0; i < rooms.size(); i++) {
			if (rooms.get(i).getRoomid().equals(roomid)) {
				Room room = rooms.get(i);
				return room;
			}
		}
		return null;
	}

	public static SingleGuest getGuest(int room_no, int guest_no) {
		return rooms.get(room_no).getGuests().get(guest_no);
	}

	public static void setGuest(int room_no, int guest_no, String guest_id) {
		rooms.get(room_no).getGuests().get(guest_no).setGuest_id(guest_id);
	}

	/**
	 * 
	 * @param message
	 * @param former_id
	 * @param guest_id
	 * @return boolean
	 * @throws IOException
	 */
	public static synchronized boolean changeIdentity(JSONObject message,
			String former_id, String guest_id) throws IOException {
		boolean duplicated = false;
		String newIdentity = (String) message.get("identity");
		// check the identity format
		if (!newIdentity.matches("^[a-zA-Z][a-zA-Z0-9]{2,15}$")) {
			return false;
		}
		// check if the identity has been used
		for (int i = 0; i < OfflineUsers.getGuests().size(); i++) {
			if (OfflineUsers.getGuests().get(i).getGuest_id()
					.equals(newIdentity)) {
				duplicated = true;
				return false;
			}
		}
		for (int i = 0; i < RoomInfo.getRooms().size(); i++) {
			for (int j = 0; j < RoomInfo.getRooms().get(i).getGuests().size(); j++) {
				if (RoomInfo.getGuest(i, j).getGuest_id().equals(newIdentity)) {
					duplicated = true;
					return false;
				}
			}
		}
		// if the identity has been used
		if (duplicated == true) {
			return false;
		}
		// if the identity is valid
		else {
			former_id = guest_id;
			guest_id = newIdentity;
			for (int i = 0; i < RoomInfo.getRooms().size(); i++) {
				for (int j = 0; j < RoomInfo.getRooms().get(i).getGuests()
						.size(); j++) {
					// update the new identity in RoomInfo
					if (RoomInfo.getGuest(i, j).getGuest_id().equals(former_id)) {
						RoomInfo.setGuest(i, j, guest_id);
						// if he is owner of a room, change the name as well
						if (RoomInfo.getRooms().get(i).getOwner()
								.equals(former_id))
							RoomInfo.getRooms().get(i).setOwner(guest_id);
					}
					// send the message to all the guests
					DataOutputStream output = new DataOutputStream(RoomInfo
							.getGuest(i, j).getSocket().getOutputStream());
					output.write((ServerMessages.NewIdentity(guest_id,
							former_id).toJSONString() + "\n").getBytes("UTF-8"));
					output.flush();

				}
			}
			return true;
		}
	}

	/**
	 * 
	 * @param newRoom
	 * @param guest_id
	 * @param out
	 * @throws IOException
	 */
	public static synchronized void join(String newRoom, String guest_id,
			DataOutputStream out) throws IOException {
		String room = "";
		String former_room = "";
		boolean isExisted = false;
		// check if the new room is existed
		for (int i = 0; i < RoomInfo.getRooms().size(); i++) {
			if (newRoom.equals(RoomInfo.getRooms().get(i).getRoomid())) {
				isExisted = true;
				break;
			}
		}
		for (int i = 0; i < RoomInfo.getRooms().size(); i++) {
			for (int j = 0; j < RoomInfo.getRooms().get(i).getGuests().size(); j++) {
				if (guest_id.equals(RoomInfo.getGuest(i, j).getGuest_id())) {
					room = RoomInfo.getRooms().get(i).getRoomid();
					if (!isExisted) {
						former_room = room;
						out.write((ServerMessages.RoomChange(guest_id,
								former_room, room).toJSONString() + "\n")
								.getBytes("UTF-8"));
						out.flush();
						return;
					} else {
						if (KickInfo.check_isValid(guest_id, newRoom)) {
							former_room = room;
							room = newRoom;
							move(guest_id, former_room, room);
							return;
						} else {
							out.write((ServerMessages
									.Message(guest_id,
											"System: You cannot join this room currently!")
									.toJSONString() + "\n").getBytes("UTF-8"));
							out.flush();
						}
					}
					return;
				}
			}
		}

	}

	/**
	 * 
	 * @param identity
	 * @param newRoom
	 * @param out
	 * @param socket
	 * @throws IOException
	 */
	public static synchronized void firstMove(String identity, String newRoom,
			DataOutputStream out, SSLSocket socket) throws IOException {
		RoomInfo.getRooms().get(0).getGuests()
				.add(new SingleGuest(socket, identity));
		for (int j = 0; j < RoomInfo.getRooms().get(0).getGuests().size(); j++) {

			DataOutputStream output = new DataOutputStream(RoomInfo
					.getGuest(0, j).getSocket().getOutputStream());
			output.write((ServerMessages.RoomChange(identity, "", newRoom)
					.toJSONString() + "\n").getBytes("UTF-8"));
			output.flush();
			if (RoomInfo.getGuest(0, j).getGuest_id().equals(identity)) {
				output.write((ServerMessages.RoomContents(newRoom)
						.toJSONString() + "\n").getBytes("UTF-8"));
				output.flush();
			}

		}
	}

	/**
	 * 
	 * @param identity
	 * @param roomid
	 * @param newRoom
	 * @throws IOException
	 */
	public static synchronized void move(String identity, String roomid,
			String newRoom) throws IOException {
		SingleGuest self = null;
		// move this guest to the new one
		String former_room = roomid;
		// find and remove the guest
		for (int i = 0; i < RoomInfo.getRooms().size(); i++) {
			if (former_room.equals(RoomInfo.getRooms().get(i).getRoomid())) {
				for (int j = 0; j < RoomInfo.getRooms().get(i).getGuests()
						.size(); j++) {
					if (RoomInfo.getGuest(i, j).getGuest_id().equals(identity)) {
						self = RoomInfo.getGuest(i, j);
						RoomInfo.getRooms().get(i).getGuests().remove(self);
						break;
					}
				}
				// if no owner has quit and room is empty, delete it
				if (RoomInfo.getRooms().get(i).getGuests().size() == 0
						&& RoomInfo.getRooms().get(i).getOwner().equals("")
						&& !RoomInfo.getRooms().get(i).getRoomid()
								.equals("MainHall")) {
					RoomInfo.getRooms().remove(i);
				} else {
					// send move information to all the guests from previous
					// room
					for (int j = 0; j < RoomInfo.getRooms().get(i).getGuests()
							.size(); j++) {

						DataOutputStream output = new DataOutputStream(RoomInfo
								.getGuest(i, j).getSocket().getOutputStream());
						output.write((ServerMessages.RoomChange(identity,
								former_room, newRoom).toJSONString() + "\n")
								.getBytes("UTF-8"));
						output.flush();

					}
				}
				break;
			}
		}
		// send move information to all the guests from the new room
		for (int i = 0; i < RoomInfo.getRooms().size(); i++) {
			if (newRoom.equals(RoomInfo.getRooms().get(i).getRoomid())) {
				RoomInfo.getRooms().get(i).getGuests().add(self);
				for (int j = 0; j < RoomInfo.getRooms().get(i).getGuests()
						.size(); j++) {

					DataOutputStream output = new DataOutputStream(RoomInfo
							.getGuest(i, j).getSocket().getOutputStream());
					output.write((ServerMessages.RoomChange(identity,
							former_room, newRoom).toJSONString() + "\n")
							.getBytes("UTF-8"));
					output.flush();

					if (RoomInfo.getGuest(i, j).getGuest_id().equals(identity)
							&& newRoom.equals("MainHall")) {
						output.write((ServerMessages.RoomContents(newRoom)
								.toJSONString() + "\n").getBytes("UTF-8"));
						output.write((ServerMessages.RoomList().toJSONString() + "\n")
								.getBytes("UTF-8"));
						output.flush();
					}

				}
				break;
			}
		}

	}

	/**
	 * 
	 * @param message
	 * @param guest_id
	 * @throws IOException
	 */
	public static synchronized void createRoom(JSONObject message,
			String guest_id) throws IOException {
		String newRoom = (String) message.get("roomid");
		boolean isExisted = false;
		boolean isMatched = false;
		// check the roomid format
		if (newRoom.matches("^[a-zA-Z][a-zA-Z0-9]{2,31}$")) {
			isMatched = true;
		}
		// check if the new room is existed
		for (int i = 0; i < RoomInfo.getRooms().size(); i++) {
			if (newRoom.equals(RoomInfo.getRooms().get(i).getRoomid())) {
				isExisted = true;
				break;
			}
		}
		// if room is qualified, add a new room to the roomlist, move the owner
		// to the new room
		if (!isExisted && isMatched) {
			RoomInfo.getRooms().add(new Room(newRoom, guest_id));
		}
	}

	/**
	 * 
	 * @param message
	 * @param out
	 * @throws IOException
	 */
	public static synchronized boolean roomContents(JSONObject message,
			DataOutputStream out) throws IOException {
		boolean isExisted = false;
		// check if the room exists
		for (int i = 0; i < RoomInfo.getRooms().size(); i++) {
			if (RoomInfo.getRooms().get(i).getRoomid()
					.equals(message.get("roomid"))) {
				isExisted = true;
				break;
			}
		}
		String room_no = (String) message.get("roomid");
		if (isExisted) {
			out.write((ServerMessages.RoomContents(room_no).toJSONString() + "\n")
					.getBytes("UTF-8"));
			out.flush();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 
	 * @param message
	 * @param guest_id
	 * @param out
	 * @throws IOException
	 */
	public static synchronized void kick(JSONObject message, String guest_id,
			DataOutputStream out) throws IOException {
		boolean isValid = false;
		boolean isIn = false;
		boolean timeValid = false;
		String roomid = (String) message.get("roomid");
		long time = (long) message.get("time");
		String identity = (String) message.get("identity");
		for (int i = 0; i < RoomInfo.getRooms().size(); i++) {
			// check if this room exists and if he is the owner
			if (roomid.equals(RoomInfo.getRooms().get(i).getRoomid())
					&& RoomInfo.getRooms().get(i).getOwner().equals(guest_id)) {
				isValid = true;
				for (int j = 0; j < RoomInfo.getRooms().get(i).getGuests()
						.size(); j++) {
					// check if the guest to be kicked is valid
					if (RoomInfo.getGuest(i, j).getGuest_id().equals(identity)) {
						isIn = true;
						if (time > 0) {
							timeValid = true;
							// move the target guest to mainhall
							move(identity, roomid, "MainHall");
							long invalid_time = System.currentTimeMillis()
									+ time * 1000;
							KickInfo.getKicks()
									.add(new OneKick(identity, invalid_time,
											roomid));
							return;
						}
					}
				}
			}
		}
		if (!isValid) {
			out.write((ServerMessages
					.Message(guest_id,
							"System: You are not the owner of this room or roomid is in valid!")
					.toJSONString() + "\n").getBytes("UTF-8"));
			out.flush();
		} else if (!isIn) {
			out.write((ServerMessages.Message(guest_id,
					"System: This guest is not in the room!").toJSONString() + "\n")
					.getBytes("UTF-8"));
			out.flush();
		} else if (!timeValid) {
			out.write((ServerMessages
					.Message(guest_id, "System: Invalid time!").toJSONString() + "\n")
					.getBytes("UTF-8"));
			out.flush();
		}
	}

	/**
	 * 
	 * @param message
	 * @param guest_id
	 * @throws IOException
	 */
	public static synchronized void delete(JSONObject message, String guest_id)
			throws IOException {
		String roomid = (String) message.get("roomid");
		for (int i = 0; i < RoomInfo.getRooms().size(); i++) {
			// if room is valid
			if (roomid.equals(RoomInfo.getRooms().get(i).getRoomid())) {
				// if this guest is the owner
				if (RoomInfo.getRooms().get(i).getOwner().equals(guest_id)) {
					int number = RoomInfo.getRooms().get(i).getGuests().size() - 1;
					while (number >= 0) {
						move(RoomInfo.getGuest(i, number).getGuest_id(),
								roomid, "MainHall");
						number--;
					}
					RoomInfo.getRooms().remove(i);
					return;
				}
			}
		}
	}

	/**
	 * 
	 * @param message
	 * @param guest_id
	 * @throws IOException
	 */
	public static void message(JSONObject message, String guest_id)
			throws IOException {
		String content = (String) message.get("content");
		for (int i = 0; i < RoomInfo.getRooms().size(); i++) {
			for (int j = 0; j < RoomInfo.getRooms().get(i).getGuests().size(); j++) {
				// find the room where guest is in
				if (guest_id.equals(RoomInfo.getGuest(i, j).getGuest_id())) {
					// send the message to the guest who in the same room
					for (int x = 0; x < RoomInfo.getRooms().get(i).getGuests()
							.size(); x++) {

						DataOutputStream output = new DataOutputStream(RoomInfo
								.getGuest(i, x).getSocket().getOutputStream());
						output.write((ServerMessages.Message(guest_id, content)
								.toJSONString() + "\n").getBytes("UTF-8"));
						output.flush();

					}
				}
			}
		}
	}

	/**
	 * 
	 * @param guest_id
	 * @param socket
	 * @param out
	 * @throws IOException
	 */
	public static synchronized void quit(String guest_id, SSLSocket socket,
			DataOutputStream out) throws IOException {
		boolean authenticated = false;
		for (int i = 0; i < RoomInfo.getRooms().size(); i++) {
			for (int j = 0; j < RoomInfo.getRooms().get(i).getGuests().size(); j++) {
				// find the room where guest is in
				if (guest_id.equals(RoomInfo.getGuest(i, j).getGuest_id())) {
					// if the user is authenticated
					if (!RoomInfo.getGuest(i, j).getPassword().equals("")) {
						OfflineUsers.getGuests().add(RoomInfo.getGuest(i, j));
						authenticated = true;
					}
					RoomInfo.getRooms().get(i).getGuests().remove(j);
					if (RoomInfo.getRooms().get(i).getGuests().size() > 0) {
						for (int x = 0; x < RoomInfo.getRooms().get(i)
								.getGuests().size(); x++) {
							// send the message to the guest who in the same
							// room
							if (RoomInfo.getGuest(i, x).getSocket() != null) {
								DataOutputStream output = new DataOutputStream(
										RoomInfo.getGuest(i, x).getSocket()
												.getOutputStream());
								output.write((ServerMessages.RoomChange(
										guest_id,
										RoomInfo.getRooms().get(i).getRoomid(),
										"").toJSONString() + "\n")
										.getBytes("UTF-8"));
								output.flush();
							}
						}
					}
					if (socket != null) {
						out.write((ServerMessages.RoomChange(guest_id,
								RoomInfo.getRooms().get(i).getRoomid(), "")
								.toJSONString() + "\n").getBytes("UTF-8"));
						out.close();
					}
				}
			}
			if (!authenticated) {
				if (guest_id.equals(RoomInfo.getRooms().get(i).getOwner())) {
					RoomInfo.getRooms().get(i).setOwner("");
				}
				// if no one is in the room and the owner has quit, close
				// this room
				if (RoomInfo.getRooms().get(i).getGuests().size() == 0
						&& RoomInfo.getRooms().get(i).getOwner().equals("")
						&& !RoomInfo.getRooms().get(i).getRoomid()
								.equals("MainHall")) {
					RoomInfo.getRooms().remove(i);
					i--;
				}
			}
		}
		return;
	}

	/**
	 * authenticate a guest, if the user exists and no one is using it and
	 * password is correct, then remove the previous one and change the current
	 * one. if not exists, add a new one.
	 * 
	 * @param former_id
	 * @param identity
	 * @param password
	 * @return success or fail
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public static synchronized boolean authenticate(String former_id,
			String identity, String password)
			throws UnsupportedEncodingException, IOException {
		// check if the identity has been used
		for (int i = 0; i < OfflineUsers.getGuests().size(); i++) {
			if (OfflineUsers.getGuests().get(i).getGuest_id().equals(identity)
					&& OfflineUsers.getGuests().get(i).getPassword()
							.equals(password)) {
				OfflineUsers.getGuests().remove(i);
				break;
			} else if (OfflineUsers.getGuests().get(i).getGuest_id()
					.equals(identity)) {
				return false;
			}
		}

		for (int i = 0; i < RoomInfo.getRooms().size(); i++) {
			for (int j = 0; j < RoomInfo.getRooms().get(i).getGuests().size(); j++) {
				// if the identity exists
				if (RoomInfo.getGuest(i, j).getGuest_id().equals(identity)) {
					return false;
				}
			}
		}

		// change the identity and password for the guest
		for (int i = 0; i < RoomInfo.getRooms().size(); i++) {
			for (int j = 0; j < RoomInfo.getRooms().get(i).getGuests().size(); j++) {
				if (RoomInfo.getGuest(i, j).getGuest_id().equals(former_id)) {
					RoomInfo.getGuest(i, j).setGuest_id(identity);
					RoomInfo.getGuest(i, j).setPassword(password);
				}
				// send the message to all the guests
				DataOutputStream output = new DataOutputStream(RoomInfo
						.getGuest(i, j).getSocket().getOutputStream());
				output.write((ServerMessages.NewIdentity(identity, former_id)
						.toJSONString() + "\n").getBytes("UTF-8"));
				output.flush();
			}
		}
		return true;

	}

}

class Room {

	String roomid;
	String owner;
	List<SingleGuest> guests = new ArrayList<SingleGuest>();

	public Room(String roomid, String owner) {
		this.roomid = roomid;
		this.owner = owner;
	}

	public List<SingleGuest> getGuests() {
		return guests;
	}

	public void setGuests(List<SingleGuest> guests) {
		this.guests = guests;
	}

	public String getRoomid() {
		return roomid;
	}

	public void setRoomid(String roomid) {
		this.roomid = roomid;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}
}
