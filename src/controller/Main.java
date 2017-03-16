package controller;

import socket.SocketConnection;
import weight.gui.WeightGUI;

/**
 * Simple class to fire up application and inject implementations
 * @author Christian
 *
 */
public class Main {
	public static int port = 8000;

	public static void main(String[] args) {
		try {
			port = args.length == 1 ? Integer.parseInt(args[0]) : port;
		}
		catch (NumberFormatException ignored) {
		}


		new Controller(
			new SocketConnection(port),
			new WeightGUI()
		)
		.start();
	}
}
