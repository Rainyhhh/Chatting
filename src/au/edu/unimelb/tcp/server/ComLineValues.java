package au.edu.unimelb.tcp.server;
import org.kohsuke.args4j.Option;


public class ComLineValues {
	
	@Option(required=false, name="-p",aliases="--port", usage="Server port number")
	private int port = 4444;

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}


	
	
}
