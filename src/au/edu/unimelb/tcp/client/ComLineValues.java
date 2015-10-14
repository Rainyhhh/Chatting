package au.edu.unimelb.tcp.client;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;


public class ComLineValues {
	@Argument(required=true)
	private String host;
	
	@Option(required=false, name="-p",aliases="--port", usage="Server port number")
	private int port = 4444;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}


	
	
}
