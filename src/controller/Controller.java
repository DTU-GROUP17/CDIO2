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
			if(this.waitingOnUserInput && message.getCommand() != Message.Command.RM20) {
				socketHandler.sendMessage(
					new OutMessage(message.getCommand())
						.halted()
				);
				return;
			}

			// If we are not waiting, then we will just invoke the method.
			method.invoke (this, message);

		} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
			socketHandler.sendMessage(new OutMessage(Message.Command.ES));
		}

	}

	private void setKeyStateAndSend(@NotNull KeyState state){
		this.keyState = state;
		socketHandler.sendMessage(new OutMessage(Message.Command.K).acknowledged());
	}

	/**
	 * Q - Quit
	 *
	 * @param message received from socket
	 */
	private void handleQMessage(@NotNull InMessage message) {
		socketHandler.sendMessage(new OutMessage(Message.Command.Q).acknowledged());
		System.exit(0);
	}

	/**
	 * DW - Empty screen
	 *
	 * @param message received from socket
	 */
	private void handleDWMessage(@NotNull InMessage message) {
		this.userInput = "";
		this.showPrimaryMessage(this.userInput);

		socketHandler.sendMessage(
			new OutMessage(Message.Command.DW)
				.acknowledged()
		);
	}

	/**
	 * RM20 - Activate/Deactivate user input of value/text
	 *
	 * @param message received from socket
	 */
	private void handleRM20Message(@NotNull InMessage message){
		try {
			// We start by checking if we are aborting user input
			// or if we are asking for a user input.
			int inputType = Integer.parseInt(message.getFlag(0));

			// Aborting user input.
			if(inputType == 0) {
				if(!this.waitingOnUserInput) {
					socketHandler.sendMessage(new OutMessage(Message.Command.RM20).halted());
					return;
				}

				this.stopWaitingOnUserInput();
				socketHandler.sendMessage(new OutMessage(Message.Command.RM20).acknowledged());
				return;
			}

			// Lets define our data received first.
			String promptString = message.getContent(0);
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

			this.showPrimaryMessage(promptString);

			// Sets the user input type and reset current user input.
			// Also sets the server to wait for user input.
			this.showUserInputAs = inputType;
			this.userInput = "";
			this.waitingOnUserInput = true;

			// Adds the unit to the display and shows it with the default text.
			this.userInputAppend = unitInput;
			this.showSecondaryMessage(defaultInput);

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

	/**
	 * P111 - Secondary display
	 *
	 * @param message received from socket
	 */
	private void handleP111Message(@NotNull InMessage message) {
		String value = "";
		try {
			value = message.getContent(0);
		} catch (MessageArgumentException e){
			socketHandler.sendMessage(new OutMessage(Message.Command.P111).wrongParameters());
		}

		if (value.length()<=30){
			this.showSecondaryMessage(value);
			socketHandler.sendMessage(new OutMessage(Message.Command.P111).acknowledged());
		} else {
			socketHandler.sendMessage(new OutMessage(Message.Command.P111).wrongParameters());
		}

	}

	/**
	 * T - Tare
	 *
	 * @param message received from socket
	 */
	private void handleTMessage(@NotNull InMessage message){
		this.taraValue = this.weightValue;
		socketHandler.sendMessage(
			new OutMessage(Message.Command.T)
				.weight()
				.addFlag(String.format("%10s", this.taraValue))
				.addFlag("kg")
		);
	}

	/**
	 * D - Primary display
	 *
	 * @param message received from socket
	 */
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

	/**
	 * S - Current weight
	 *
	 * @param message received from socket
	 */
	private void handleSMessage(@NotNull InMessage message){
		socketHandler.sendMessage(
			new OutMessage(Message.Command.S)
			.weight()
			.addFlag(String.format("%10s", this.getCurrentWeightValue()))
			.addFlag("kg")
		);
	}

	/**
	 * S - Key control
	 *
	 * @param message received from socket
	 */
	private void handleKMessage(@NotNull InMessage message){
		String value;
		try{
			value = message.getContent(0);
		} catch (MessageArgumentException e){
			socketHandler.sendMessage(new OutMessage(Message.Command.K).wrongParameters());
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
			socketHandler.sendMessage(new OutMessage(Message.Command.K).wrongParameters());
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
					.acknowledged()
						.addContent(this.userInput)
			);
			this.stopWaitingOnUserInput();
		}
	}

	private void stopWaitingOnUserInput() {
		this.waitingOnUserInput = false;
		this.userInput = "";
		this.userInputAppend = "";
		this.showPrimaryMessage(this.getCurrentWeightValue().toString());
		this.showSecondaryMessage("");
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
				.acknowledged()
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

	private void showPrimaryMessage(@NotNull String message)
	{
		this.weightInterface.showMessagePrimaryDisplay(message);
	}

	private void showSecondaryMessage(@NotNull String message)
	{
		String messageShow;
		switch (showUserInputAs) {
			case 8:
				messageShow = message;
				break;
			case 11:
				messageShow = new String(new char[message.length()-1]).replace("\0", message);
				break;
			default:
				messageShow = message;
		}
		this.weightInterface.showMessageSecondaryDisplay(messageShow+" "+this.userInputAppend);
	}
}
