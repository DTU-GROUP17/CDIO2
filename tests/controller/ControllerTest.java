package controller;

import client.DummyClient;
import client.DummyGUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import socket.Message;
import socket.OutMessage;
import socket.SocketConnection;


public class ControllerTest {
	private DummyClient client;
	private Controller controller;

	@Rule
	public final ExpectedSystemExit exit = ExpectedSystemExit.none();

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
	public void testQCommand() throws Exception {
		exit.expectSystemExitWithStatus(0);
		client.send("Q");

		client.expectsReceived(new OutMessage(Message.Command.Q).acknowledged());
	}

	@Test
	public void testDWCommandValid() throws Exception {
		client.send("DW \"Show me\"");

		client.expectsReceived(new OutMessage(Message.Command.DW).acknowledged());
	}
}