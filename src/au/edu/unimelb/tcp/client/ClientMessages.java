package au.edu.unimelb.tcp.client;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.json.simple.JSONObject;

public class ClientMessages {

	@SuppressWarnings("unchecked")
	public static JSONObject IdentityChange(String type, String identity) {
		JSONObject identity_change = new JSONObject();
		identity_change.put("type", type);
		identity_change.put("identity", identity);
		return identity_change;
	}

	@SuppressWarnings("unchecked")
	public static JSONObject Join(String type, String roomid) {
		JSONObject join = new JSONObject();
		join.put("type", type);
		join.put("roomid", roomid);
		return join;
	}

	@SuppressWarnings("unchecked")
	public static JSONObject List(String type) {
		JSONObject list = new JSONObject();
		list.put("type", type);
		return list;
	}

	@SuppressWarnings("unchecked")
	public static JSONObject Who(String type, String roomid) {
		JSONObject who = new JSONObject();
		who.put("type", type);
		who.put("roomid", roomid);
		return who;
	}

	@SuppressWarnings("unchecked")
	public static JSONObject CreateRoom(String type, String roomid) {
		JSONObject create_room = new JSONObject();
		create_room.put("type", type);
		create_room.put("roomid", roomid);
		return create_room;
	}

	@SuppressWarnings("unchecked")
	public static JSONObject Kick(String type, String roomid, int time,
			String identity) {
		JSONObject kick = new JSONObject();
		kick.put("type", type);
		kick.put("roomid", roomid);
		kick.put("time", time);
		kick.put("identity", identity);
		return kick;
	}

	@SuppressWarnings("unchecked")
	public static JSONObject Delete(String type, String roomid) {
		JSONObject delete = new JSONObject();
		delete.put("type", type);
		delete.put("roomid", roomid);
		return delete;
	}

	@SuppressWarnings("unchecked")
	public static JSONObject Message(String type, String content) {
		JSONObject message = new JSONObject();
		message.put("type", type);
		message.put("content", content);
		return message;
	}

	@SuppressWarnings("unchecked")
	public static JSONObject Quit(String type) {
		JSONObject quit = new JSONObject();
		quit.put("type", type);
		return quit;
	}

	/**
	 * message for authentication
	 * 
	 * @param type
	 * @param identity
	 * @param password
	 * @return
	 * @throws UnsupportedEncodingException 
	 * @throws NoSuchAlgorithmException 
	 */
	@SuppressWarnings("unchecked")
	public static JSONObject Authenticate(String type, String identity, String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		JSONObject account = new JSONObject();
		account.put("type", type);
		account.put("identity", identity);
		account.put("password", getMD5Str(password));
		return account;		
	}

	/**
	 * encryption for password using md5
	 * @param str
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	public static String getMD5Str(String str) throws NoSuchAlgorithmException,
			UnsupportedEncodingException {
		MessageDigest messageDigest = null;
		messageDigest = MessageDigest.getInstance("MD5");
		messageDigest.reset();
		messageDigest.update(str.getBytes("UTF-8"));
		byte[] byteArray = messageDigest.digest();
		StringBuffer md5StrBuff = new StringBuffer();
		for (int i = 0; i < byteArray.length; i++) {
			if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
				md5StrBuff.append("0").append(
						Integer.toHexString(0xFF & byteArray[i]));
			else
				md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
		}
		return md5StrBuff.toString();
	}

}
