package controller;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import socket.*;
import weight.WeightInterfaceController;
import weight.KeyPress;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * Controller - integrating input from socket and ui. Implements ISocketObserver and IUIObserver to handle this.
 * @author Christian Budtz
 * @version 0.1 2017-01-24
 *
 */
public class Controller implements MainController {

	private SocketController socketHandler;
	private WeightInterfaceController weightInterface;
	private KeyState keyState = KeyState.K1;

	private double weightValue;
	private double taraValue;
	private String userInput;
	private String userInputAppend;
	private boolean waitingOnUserInput;
	private int showUserInputAs;

	public Controller(@NotNull SocketController socketHandler, @NotNull WeightInterfaceController uiController) {
		this.socketHandler = socketHandler;
		this.weightInterface = uiController;
		this.weightValue = 0.0;
		this.taraValue = 0.0;
		this.userInput = "";
		this.userInputAppend = "";
		this.showUserInputAs = 8;
	}

	@Override
	public void start() {
		this.socketHandler.addObserver(this::onSocketMessage);
		this.weightInterface.getKeyFeed().addObserver(this::onKeyPress);
		this.weightInterface.getWeightFeed().addObserver(this::onWeightChange);
		new Thread(this.weightInterface).start();
		new Thread(this.socketHandler).start();
		//TODO: If ui thread is called before it finishes loading it may crash.
	}

	/**
	 * Handles all the incoming messages from the socket.
	 *
	 * It will check for methods named <code>handle{command}Message(InMessage)</code>
	 * and execute them, else it will tell the client that
	 * we do not understand the given command.
	 * It will return an halting command if <code>waitingOnUserInput</code>
	 * is true.
	 *
	 * @param message received from socket
	 */
	private void onSocketMessage(@NotNull InMessage message) {
		Class<?> c = this.getClass();
		Method method;

		try {
			// We start by checking if a method with the given command exist,
			// if not we return an unknown message to the user.
			method = c.getDeclaredMethod ("handle"+message.getCommand()+"Message", InMessage.class);

			// Afterwards we check if we are waiting any user input, if we
			// are, then we return the halting command.
			if(this.waitingOnUserInput) {
				socketHandler.sendMessage(
					new OutMessage(message.getCommand())
						.halted()
				);
			}

			// If we are not waiting, then we will just invoke the method.
			method.invoke (this, message);

		} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
			socketHandler.sendMessage(new OutMessage(Message.Command.ES));
		}

	}

	private void setKeyStateAndSend(@NotNull KeyState state){
		this.keyState = state;
		socketHandler.sendMessage(new OutMessage(Message.Command.K).addFlag("A"));
	}

	private void handleQMessage(@NotNull InMessage message) {
		System.exit(0);
	}

	private void handleDWMessage(@NotNull InMessage message) {
		this.userInput = "";
		this.showPrimaryMessage(this.userInput);

		socketHandler.sendMessage(
				new OutMessage(Message.Command.DW)
					.addFlag("A")
		);
	}

	private void handleRM20Message(@NotNull InMessage message){
		try {
			// Lets define our data received first.
			String promptString = message.getContent(0);
			int inputType = Integer.parseInt(message.getFlag(0));
			String defaultInput = message.getContent(1);
			String unitInput = message.getContent(2);

			// Validation of received data
			if(promptString.length() > 24) {
				throw new MessageArgumentException();
			}
			if(unitInput.length() > 7) {
				throw new MessageArgumentException();
			}
			if(inputType != 8) {
				throw new MessageArgumentException();
			}


			this.showPrimaryMessage(message.getContent(0));

			// Sets the user input type and reset current user input.
			this.showUserInputAs = Integer.parseInt(message.getFlag(0));
			this.userInput = "";

			// Adds the unit to the display and shows it with the default text.
			this.userInputAppend = message.getContent(2);
			this.showSecondaryMessage(message.getContent(1));

			// Sends the executed but waiting for user input command.
			socketHandler.sendMessage(
					new OutMessage(Message.Command.RM20)
						.waitingForUserInput()
			);

		} catch (MessageArgumentException|NumberFormatException e){
			socketHandler.sendMessage(
					new OutMessage(Message.Command.RM20)
						.wrongParameters()
			);
		}
	}

	private void handleP111Message(@NotNull InMessage message) {
		String value = "";
		try {
			value = message.getContent(0);
		} catch (MessageArgumentException e){
			socketHandler.sendMessage(new OutMessage(Message.Command.P111).wrongParameters());
		}

		if (value.length()<=30){
			this.showSecondaryMessage(value);
		} else {
			socketHandler.sendMessage(new OutMessage(Message.Command.P111).wrongParameters());
		}

	}

	private void handleTMessage(@NotNull InMessage message){
		this.taraValue = this.weightValue;
		socketHandler.sendMessage(
			new OutMessage(Message.Command.S)
				.addFlag("S")
				.addFlag(String.format("%10s", this.taraValue))
				.addFlag("g")
		);
	}

	private void handleDMessage(@NotNull InMessage message){
		try{
			this.showPrimaryMessage(message.getContent(0));
			socketHandler.sendMessage(
				new OutMessage(Message.Command.D)
					.acknowledged()
			);
		} catch (MessageArgumentException e){
			socketHandler.sendMessage(
				new OutMessage(Message.Command.D)
					.wrongParameters()
			);
		}

	}

	private void handleSMessage(@NotNull InMessage message){
		socketHandler.sendMessage(
			new OutMessage(Message.Command.S)
			.addFlag("S")
			.addFlag(String.format("%10s", this.getCurrentWeightValue()))
			.addFlag("g")
		);
	}

	private void handleKMessage(@NotNull InMessage message){
		String value;
		try{
			value = message.getContent(0);
		} catch (MessageArgumentException e){
			socketHandler.sendMessage(new OutMessage(Message.Command.K).addFlag("L"));
			return;
		}
		switch (value) {
		case "1" :
			this.setKeyStateAndSend(KeyState.K1);
			break;
		case "2" :
			this.setKeyStateAndSend(KeyState.K2);
			break;
		case "3" :
			this.setKeyStateAndSend(KeyState.K3);
			break;
		case "4" :
			this.setKeyStateAndSend(KeyState.K4);
			break;
		default:
			socketHandler.sendMessage(new OutMessage(Message.Command.K).addFlag("L"));
			break;
		}
	}

	private void onKeyPress(@NotNull KeyPress keyPress) {
		if(keyState.equals(KeyState.K3)) {
			HandleK3KeyState(keyPress);
			return;
		}

		if(keyState.equals(KeyState.K4)) {
			HandleK4KeyState(keyPress);
		}

		System.out.println(keyPress.getCharacter());
		System.out.println(keyPress.getKeyNumber());
		System.out.println(keyPress.getType());


		switch (keyPress.getType()) {
			case SOFTBUTTON:
				break;
			case TARA:
				break;
			case TEXT:
				this.userInput += keyPress.getCharacter();
				break;
			case ZERO:
				break;
			case C:
				this.userInput = "";
				break;
			case EXIT:
				handleExitKeyPress();
				break;
			case SEND:
				handleSendKeyPress();
				break;
		}

		// Shows the new character on the display.
		this.showSecondaryMessage(this.userInput);
	}

	private void handleSendKeyPress() {
		// If we are waiting on user input and send is pressed,
		// we will send the user input and no longer be waiting
		// for user input.
		if(waitingOnUserInput) {
			socketHandler.sendMessage(
				new OutMessage(Message.Command.RM20)
					.addFlag("A")
						.addContent(this.userInput)
			);
			this.waitingOnUserInput = false;
			this.userInput = "";
		}
	}

	private void handleExitKeyPress() {
		// If we are waiting on user input and exit is pressed,
		// we will send an aborted message and no longer be
		// waiting for user input.
		if(waitingOnUserInput) {
			this.waitingOnUserInput = false;
			socketHandler.sendMessage(
				new OutMessage(Message.Command.RM20)
					.aborted()
			);
		}
		this.userInput = "";
	}

	private void HandleK4KeyState(@NotNull KeyPress keyPress) {
		socketHandler.sendMessage(
			new OutMessage(Message.Command.K)
				.addFlag("A")
				.addFlag(Integer.toString(keyPress.getKeyNumber()))
		);
	}

	private void HandleK3KeyState(@NotNull KeyPress keyPress) {
		socketHandler.sendMessage(
			new OutMessage(Message.Command.K)
				.addFlag("C")
				.addFlag(Integer.toString(keyPress.getKeyNumber()))
		);
	}

	private void onWeightChange(double value) {
		this.weightValue = value;
		this.showPrimaryMessage(this.getCurrentWeightValue().toString());
	}

	@NotNull
	@Contract(pure = true)
	private Double getCurrentWeightValue(){
		return this.weightValue-this.taraValue;
	}

	private void showPrimaryMessage(@NotNull String string)
	{
		this.weightInterface.showMessagePrimaryDisplay(string);
	}

	private void showSecondaryMessage(@NotNull String string)
	{
		this.weightInterface.showMessageSecondaryDisplay(string+" "+this.userInputAppend);
	}
}
