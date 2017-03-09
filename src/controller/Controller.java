package controller;

import socket.*;
import weight.WeightInterfaceController;
import weight.KeyPress;

import java.util.Objects;

import static javafx.application.Application.launch;

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

	private Double weightValue;
	private Double taraValue;
	private String userInput;

	public Controller(SocketController socketHandler, WeightInterfaceController uiController) {
		this.socketHandler = socketHandler;
		this.weightInterface = uiController;
		this.weightValue = 0.0;
		this.taraValue = 0.0;
		this.userInput = "";
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

	public void onSocketMessage(InMessage message) {
		switch (message.getCommand()) {
		case B:
			//TODO ???
			break;
		case D:
			this.handleDMessage(message);
			break;
		case Q:
			System.exit(0);
			break;
		case RM20:
			break;
		case S:
			this.handleSMessage(message);
			break;
		case T:
			this.handleTMessage(message);
			break;
		case DW:
			break;
		case K:
			this.handleKMessage(message);
			break;
		case P111:
			handleP111Message(message);
			break;
		}
	}

	private void setKeyStateAndSend(KeyState state){
		this.keyState = state;
		socketHandler.sendMessage(new OutMessage(Message.Command.K).addFlag("A"));
	}

	private void handleRM20Message(InMessage message){
		try {
			if (!Objects.equals(message.getFlag(0), "8")){
				throw new MessageArgumentException();
			}

		} catch (MessageArgumentException e){
			socketHandler.sendMessage(new OutMessage(Message.Command.RM20).addFlag("L"));
		}
	}

	private void handleP111Message(InMessage message) {
		String value = "";
		try {
			value = message.getContent(0);
		} catch (MessageArgumentException e){
			socketHandler.sendMessage(new OutMessage(Message.Command.P111).addFlag("L"));
		}
		if (value.length()<=30){
			this.weightInterface.showMessageSecondaryDisplay(value);
		} else {
			socketHandler.sendMessage(new OutMessage(Message.Command.P111).addFlag("L"));
		}

	}

	private void handleTMessage(InMessage message){
		this.taraValue = this.weightValue;
		socketHandler.sendMessage(
			new OutMessage(Message.Command.S)
				.addFlag("S")
				.addFlag(String.format("%10s", this.taraValue))
				.addFlag("g")
		);
	}

	private void handleDMessage(InMessage message){
		try{
			this.weightInterface.showMessagePrimaryDisplay(message.getContent(0));
			socketHandler.sendMessage(
				new OutMessage(Message.Command.D)
					.addFlag("A")
			);
		} catch (MessageArgumentException e){
			socketHandler.sendMessage(
				new OutMessage(Message.Command.D)
					.addFlag("L")
			);
		}

	}

	private void handleSMessage(InMessage message){
		socketHandler.sendMessage(
			new OutMessage(Message.Command.S)
			.addFlag("S")
			.addFlag(String.format("%10s", this.getCurrentWeightValue()))
			.addFlag("g")
		);
	}

	private void handleKMessage(InMessage message){
		String value = "";
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

	public void onKeyPress(KeyPress keyPress) {
		if (keyState.equals(KeyState.K3) ){
			socketHandler.sendMessage(
				new OutMessage(Message.Command.K)
				.addFlag("C")
				.addFlag(Integer.toString(keyPress.getKeyNumber()))
			);
			return;
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
			break;
		case EXIT:
			break;
		case SEND:

			break;
		}
		if (keyState.equals(KeyState.K4)){
			socketHandler.sendMessage(
				new OutMessage(Message.Command.K)
				.addFlag("A")
				.addFlag(Integer.toString(keyPress.getKeyNumber()))
			);
		}

	}

	public void onWeightChange(Double value) {
		this.weightValue = value;
		this.weightInterface.showMessagePrimaryDisplay(this.getCurrentWeightValue().toString());
	}

	private Double getCurrentWeightValue(){
		return this.weightValue-this.taraValue;
	}

	private String harvestUserInpuy(){
		this.weightInterface.showMessageSecondaryDisplay("");
		String value = this.userInput;
		this.userInput = "";
		return value;
	}

}
