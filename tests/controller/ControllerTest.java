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
import weight.KeyPress;

import static org.assertj.core.api.Assertions.assertThat;

public class ControllerTest {
	private DummyClient client;
	private Controller controller;
	private DummyGUI gui;
	private SocketConnection socketConnection;

	@Rule
	public final ExpectedSystemExit exit = ExpectedSystemExit.none();

	@Before
	public void setUp() throws Exception {
		socketConnection = new SocketConnection(6700);
		controller = new Controller(
				socketConnection,
				new DummyGUI()
		);
		controller.start();

		client = new DummyClient("localhost", 6700);
		gui = (DummyGUI) controller.weightInterface;
	}

	@After
	public void tearDown() throws Exception {
		socketConnection.serverSocket.close();
		client = null;
		controller = null;
		gui = null;
	}

	@Test
	public void testQCommand() throws Exception {
		exit.expectSystemExitWithStatus(0);
		client.send("Q");
		client.expectsReceived(new OutMessage(Message.Command.Q).acknowledged());
	}

	@Test
	public void testDWCommand() throws Exception {
		client.send("DW");

		client.expectsReceived(new OutMessage(Message.Command.DW).acknowledged());
		assertThat(gui.primaryDisplay).isEqualTo(controller.getCurrentWeightValue().toString());
	}

	@Test
	public void testRM20CommandValid() throws Exception {
		client.send("RM20 8 \"Valid string\" \"0.00\" \"kg\"");

		client.expectsReceived(new OutMessage(Message.Command.RM20).waitingForUserInput());
		assertThat(gui.primaryDisplay).isEqualTo("Valid string");
		assertThat(gui.secondaryDisplay).isEqualTo("0.00 kg");

		// Emulate pressing ABC followed by send.
		gui.getKeyFeed().notifyAll(KeyPress.Character('A'));
		gui.getKeyFeed().notifyAll(KeyPress.Character('B'));
		gui.getKeyFeed().notifyAll(KeyPress.Character('C'));
		gui.getKeyFeed().notifyAll(KeyPress.Send());

		client.expectsReceived(new OutMessage(Message.Command.RM20).acknowledged().addContent("ABC"));
	}

	@Test
	public void testRM20CommandInvalid() throws Exception {
		client.send("RM20 8 Invalid string \"0.00\" \"kg\"");

		client.expectsReceived(new OutMessage(Message.Command.RM20).wrongParameters());
	}

	@Test
	public void testRM20CommandAbortUserInput() throws Exception {
		// Start a wait for user input, and wait for client to receive response
		client.send("RM20 8 \"Valid string\" \"0.00\" \"kg\"");
		client.expectsReceived(new OutMessage(Message.Command.RM20).waitingForUserInput());

		// Send abort waiting for user input.
		client.send("RM20 0");

		client.expectsReceived(new OutMessage(Message.Command.RM20).acknowledged());
	}

	@Test
	public void testHaltingCommandsWhenWaitingForUserInput() throws Exception {
		// Send RM20 for halting commands.
		client.send("RM20 8 \"Valid string\" \"0.00\" \"kg\"");
		client.expectsReceived(new OutMessage(Message.Command.RM20).waitingForUserInput());

		// Send halted command.
		client.send("S");
		client.expectsReceived(new OutMessage(Message.Command.S).halted());
	}

	@Test
	public void testP111Command() throws Exception {
		client.send("P111 \"secondary display\"");
		client.expectsReceived(new OutMessage(Message.Command.P111).acknowledged());

		assertThat(gui.secondaryDisplay).isEqualTo("secondary display");
	}

	@Test
	public void testTCommand() throws Exception {
		// Set the weight so we have something to tare.
		gui.getWeightFeed().notifyAll(200.00);
		client.send("T");

		client.expectsReceived(new OutMessage(Message.Command.T).weightWithValue(200,"kg"));

		//Test if weight is 0 now.
		assertThat(controller.getCurrentWeightValue()).isEqualTo(0);
	}

	@Test
	public void testDCommand() throws Exception {
		client.send("D \"Test\"");

		client.expectsReceived(new OutMessage(Message.Command.D).acknowledged());
		assertThat(gui.primaryDisplay).contains("Test");
	}

	@Test
	public void testSCommand() throws Exception {
		// Test with empty weight
		client.send("S");
		client.expectsReceived(new OutMessage(Message.Command.S).weightWithValue(0,"kg"));

		// Test with weight not empty
		gui.getWeightFeed().notifyAll(200.00);
		client.send("S");
		client.expectsReceived(new OutMessage(Message.Command.S).weightWithValue(200,"kg"));
	}

	@Test
	public void testUnknownCommand() throws Exception {
		client.send("Unknown command");
		client.expectsReceived(new OutMessage(Message.Command.ES));
	}

	@Test
	public void testBCommand() throws Exception {
		client.send("B 200.0");
		client.expectsReceived(new OutMessage(Message.Command.B).acknowledged());

		// check if the weight is the new weight.
		client.send("S");
		client.expectsReceived(new OutMessage(Message.Command.S).weightWithValue(200,"kg"));
	}
 }