package controller;

import socket.SocketConnection;
import weight.gui.WeightGUI;

/**
 * Simple class to fire up application and inject implementations
 * @author Christian
 *
 */
public class Main {

	public static void main(String[] args) {
		new Controller(
			new SocketConnection(6700),
			new WeightGUI()
		)
		.start();
	}
}
