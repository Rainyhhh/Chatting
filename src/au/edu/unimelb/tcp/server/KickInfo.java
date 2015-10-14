package au.edu.unimelb.tcp.server;

import java.util.ArrayList;
import java.util.List;

public class KickInfo {

	private static List<OneKick> kicks = new ArrayList<OneKick>();

	public static List<OneKick> getKicks() {
		return kicks;
	}

	public static void setKicks(List<OneKick> kicks) {
		KickInfo.kicks = kicks;
	}
	
	public synchronized static boolean check_isValid(String identity, String roomid) {
		for(int i = 0; i < kicks.size(); i ++) {
			if(kicks.get(i).getIdentity().equals(identity) && kicks.get(i).getRoomid().equals(roomid)) {
				if(kicks.get(i).getInvalid_period() >= System.currentTimeMillis()) {
					return false;
				}
				else kicks.remove(i);
				break;
			}
		}
		return true;
	}

}

class OneKick {
	
	String identity;
	long invalid_period;
	String roomid;
	
	public OneKick(String identity, long time, String roomid) {
		this.identity = identity;
		this.invalid_period = time;
		this.roomid = roomid;
	}
	
	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public long getInvalid_period() {
		return invalid_period;
	}

	public void setInvalid_period(long invalid_period) {
		this.invalid_period = invalid_period;
	}

	public String getRoomid() {
		return roomid;
	}

	public void setRoomid(String roomid) {
		this.roomid = roomid;
	}

}