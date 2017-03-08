package socket;

public class SocketInMessage {
	private MessageIn type;
	private String message;

	public SocketInMessage(MessageIn type, String message) {
		this.message=message;
		this.type=type;
	}
	
	public String getMessage() {
		return message;
	}
	
	public MessageIn getType() {
		return type;
	}
	
	public enum MessageIn {
		RM204, RM208, D, DW, T, S, B, Q, P111, K
	}

}
