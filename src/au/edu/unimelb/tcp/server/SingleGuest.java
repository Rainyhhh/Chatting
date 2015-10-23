package au.edu.unimelb.tcp.server;

import javax.net.ssl.SSLSocket;

public class SingleGuest {


	SSLSocket socket;
	String guest_id;
	String password = "";

	public SingleGuest(SSLSocket socket, String guest_id) {
		this.socket = socket;
		this.guest_id = guest_id;
	}

	public SSLSocket getSocket() {
		return socket;
	}

	public void setSocket(SSLSocket socket) {
		this.socket = socket;
	}

	public String getGuest_id() {
		return guest_id;
	}

	public void setGuest_id(String guest_id) {
		this.guest_id = guest_id;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
