package socket;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

public class SocketConnection extends SocketController {

	private BufferedReader inStream;
	private PrintWriter outStream;

	public SocketConnection(int port) {
		super(port);
	}

	@Override
	public void sendMessage(OutMessage message) {
		this.outStream.write(message.toString());
	}

	@Override
	public void run() {
		try (ServerSocket listeningSocket = new ServerSocket(this.port)){
			while (true){
				this.waitForConnections(listeningSocket);
			}		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void waitForConnections(ServerSocket listeningSocket) {
		try {
			Socket activeSocket = listeningSocket.accept();
			inStream = new BufferedReader(new InputStreamReader(activeSocket.getInputStream()));
			outStream = new PrintWriter(activeSocket.getOutputStream());

			String received = "wauw";

			while (!Objects.equals(received, "")){
				received = inStream.readLine();
				System.out.println("received = " + received);
				this.notifyAll(InMessage.fromString(received));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

