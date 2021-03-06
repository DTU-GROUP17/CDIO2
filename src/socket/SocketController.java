package socket;

import Observables.Observable;

public abstract class SocketController extends Observable<InMessage> implements Runnable{
	protected int port = 6700;

	public SocketController(int port) {
		super();
		this.port = port;
	}

	abstract public void sendMessage(OutMessage message);
}
