package socket;

import socket.SocketInMessage.MessageIn;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketConnection extends SocketController {

	private BufferedReader inStream;
	private DataOutputStream outStream;

	public SocketConnection(int port) {
		super(port);
	}

	@Override
	public void sendMessage(SocketOutMessage message) {
		if (outStream!=null){
			//TODO send something over the socket! 
		} else {
			//TODO maybe tell someone that connection is closed?
		}
	}

	@Override
	public void run() {
		//TODO some logic for listening to a socket //(Using try with resources for auto-close of socket)
		try (ServerSocket listeningSocket = new ServerSocket(this.port)){
			while (true){
				waitForConnections(listeningSocket); 	
			}		
		} catch (IOException e) {
			// TODO Maybe notify Controller?
			e.printStackTrace();
		}
	}

	private void waitForConnections(ServerSocket listeningSocket) {
		try {
			Socket activeSocket = listeningSocket.accept(); //Blocking call
			inStream = new BufferedReader(new InputStreamReader(activeSocket.getInputStream()));
			outStream = new DataOutputStream(activeSocket.getOutputStream());
			String inLine;
			//.readLine is a blocking call 
			//TODO How do you handle simultaneous input and output on socket?
			//TODO this only allows for one open connection - how would you handle multiple connections?
			while (true){
				inLine = inStream.readLine();
				System.out.println("inLine = " + inLine);
				if (inLine==null) break;
				switch (inLine.split(" ")[0]) {
				case "RM20": // Display a message in the secondary display and wait for response
					//TODO implement logic for RM command
					break;
				case "D":// Display a message in the primary display
					//TODO Refactor to make sure that faulty messages doesn't break the system
					this.notifyAll(new SocketInMessage(MessageIn.D, inLine.split(" ")[1]));
					break;
				case "DW": //Clear primary display
					//TODO implement
					break;
				case "P111": //Show something in secondary display
					//TODO implement
					break;
				case "T": // Tare the weight
					//TODO implement
					break;
				case "S": // Request the current load
					//TODO implement
					break;
				case "K":
					if (inLine.split(" ").length>1){
						this.notifyAll(new SocketInMessage(MessageIn.K, inLine.split(" ")[1]));
					}
					break;
				case "B": // Set the load
					//TODO implement
					break;
				case "Q": // Quit
					//TODO implement
					break;
				default: //Something went wrong?
					//TODO implement
					break;
				}
			}
		} catch (IOException e) {
			//TODO maybe notify mainController?
			e.printStackTrace();
		}
	}
}

