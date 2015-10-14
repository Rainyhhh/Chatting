package au.edu.unimelb.tcp.server;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ServerMessages {

	/**
	 * NewIdentity
	 * 
	 * @param guest_id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static JSONObject NewIdentity(String guest_id, String former) {

		JSONObject new_identity = new JSONObject();
		new_identity.put("type", "newidentity");
		new_identity.put("former", former);
		new_identity.put("identity", guest_id);

		return new_identity;
	}

	/**
	 * RoomList
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static JSONObject RoomList() {

		JSONObject room_list = new JSONObject();
		JSONArray room_count = new JSONArray();
		room_list.put("type", "roomlist");
		for (int i = 0; i < RoomInfo.getRooms().size(); i++) {
			JSONObject single_room = new JSONObject();
			single_room.put("roomid", RoomInfo.getRooms().get(i).getRoomid());
			single_room.put("count", RoomInfo.getRooms().get(i).getGuests()
					.size());
			room_count.add(single_room);
		}

		room_list.put("rooms", room_count);

		return room_list;

	}

	/**
	 * RoomChange
	 * 
	 * @param guest_id
	 * @param former
	 * @param room_id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static JSONObject RoomChange(String guest_id, String former,
			String room_id) {

		JSONObject room_change = new JSONObject();
		room_change.put("type", "roomchange");
		room_change.put("identity", guest_id);
		room_change.put("former", former);
		room_change.put("roomid", room_id);
		return room_change;

	}

	/**
	 * RoomContents
	 * @param room_id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static JSONObject RoomContents(String room_id) {

		JSONObject room_contents = new JSONObject();
		room_contents.put("type", "roomcontents");
		room_contents.put("roomid", room_id);
		Room room = RoomInfo.getRoomContent(room_id);
		JSONArray guests = new JSONArray();
		for (int i = 0; i < room.getGuests().size(); i++) {
			guests.add(room.getGuests().get(i).getGuest_id());
		}
		room_contents.put("identities", guests);
		room_contents.put("owner", room.getOwner());
		return room_contents;

	}

	/**
	 * Messages
	 * @param identity
	 * @param content
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static JSONObject Message(String identity, String content) {

		JSONObject message = new JSONObject();
		message.put("type", "message");
		message.put("identity", identity);
		message.put("content", content);

		return message;

	}
	

}
