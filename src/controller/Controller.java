package controller;

import socket.SocketController;
import socket.SocketInMessage;
import socket.SocketOutMessage;
import weight.WeightInterfaceController;
import weight.KeyPress;

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

	public Controller(SocketController socketHandler, WeightInterfaceController uiController) {
		this.socketHandler = socketHandler;
		this.weightInterface = uiController;
	}

	@Override
	public void start() {
		this.socketHandler.addObserver(this::onSocketMessage);
		this.weightInterface.getKeyFeed().addObserver(this::onKeyPress);
		this.weightInterface.getWeightFeed().addObserver(this::onWeightChange);
		new Thread(this.socketHandler).start();
		new Thread(this.weightInterface).start();
	}

	public void onSocketMessage(SocketInMessage message) {
		switch (message.getType()) {
		case B:
			break;
		case D:
			weightInterface.showMessagePrimaryDisplay(message.getMessage());
			break;
		case Q:
			break;
		case RM204:
			break;
		case RM208:
			break;
		case S:
			break;
		case T:
			break;
		case DW:
			break;
		case K:
			handleKMessage(message);
			break;
		case P111:
			break;
		}
	}

	private void handleKMessage(SocketInMessage message) {
		switch (message.getMessage()) {
		case "1" :
			this.keyState = KeyState.K1;
			break;
		case "2" :
			this.keyState = KeyState.K2;
			break;
		case "3" :
			this.keyState = KeyState.K3;
			break;
		case "4" :
			this.keyState = KeyState.K4;
			break;
		default:
			socketHandler.sendMessage(new SocketOutMessage("ES"));
			break;
		}
	}

	public void onKeyPress(KeyPress keyPress) {
		switch (keyPress.getType()) {
		case SOFTBUTTON:
			break;
		case TARA:
			break;
		case TEXT:
			break;
		case ZERO:
			break;
		case C:
			break;
		case EXIT:
			break;
		case SEND:
			if (keyState.equals(KeyState.K4) || keyState.equals(KeyState.K3) ){
				socketHandler.sendMessage(new SocketOutMessage("K A 3"));
			}
			break;
		}

	}

	public void onWeightChange(Double newWeight) {
		// TODO Auto-generated method stub
	}

}
