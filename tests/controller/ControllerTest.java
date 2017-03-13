package controller;

import client.DummyClient;
import client.DummyGUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import socket.SocketConnection;


public class ControllerTest {
	private DummyClient client;
	private Controller controller;


	@Before
	public void setUp() throws Exception {
		controller = new Controller(
				new SocketConnection(6700),
				new DummyGUI()
		);
		controller.start();

		client = new DummyClient("localhost", 6700);
	}

	@After
	public void tearDown() throws Exception {
		client = null;
		controller = null;
	}

	@Test
	public void start() throws Exception {
		client.send("S");
		client.expectsReceivedContains("S S");
	}
}