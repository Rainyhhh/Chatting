package au.edu.unimelb.tcp.server;

import java.util.ArrayList;
import java.util.List;

public class OfflineUsers {

	private static List<SingleGuest> guests = new ArrayList<SingleGuest>();

	public static List<SingleGuest> getGuests() {
		return guests;
	}

	public static void setGuests(List<SingleGuest> guests) {
		OfflineUsers.guests = guests;
	}
		
}
