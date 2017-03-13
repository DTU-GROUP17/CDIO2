package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static org.assertj.core.api.Assertions.assertThat;

public class DummyClient{
	private static final String LINE_SEPARATOR = "\r\n";

	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;


	public DummyClient(String uri, int port) throws Exception {
		this.socket = new Socket(uri, port);
		this.out = new PrintWriter(socket.getOutputStream(), true);
		this.in = new BufferedReader(
				new InputStreamReader(socket.getInputStream())
		);
	}

	public DummyClient send(String send) {
		out.print(send + LINE_SEPARATOR);
		out.flush();
		return this;
	}

	public DummyClient expectsReceived(String receiving) throws Exception{
		assertThat(receiving).isEqualTo(in.readLine());
		return this;
	}

	public DummyClient expectsReceivedContains(String receiving) throws Exception {
		assertThat(in.readLine()).contains(receiving);
		return this;
	}
}
