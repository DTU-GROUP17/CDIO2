package socket;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class OutMessage extends Message<OutMessage> {

	public OutMessage(Command command, ArrayList<String> flags, ArrayList<String> content) {
		super(command, flags, content);
	}

	public OutMessage(Command command) {
		super(command);
	}

	@Override
	public String toString() {
		String message = this.getCommand().name();
		if (!this.flags.isEmpty()){
			message += this.flags.stream().map(item -> " "+item).collect(Collectors.joining());
		}
		if (!this.content.isEmpty()){
			message += this.content.stream().map(item -> " \""+item+"\"").collect(Collectors.joining());
		}
		return message + "\r\n";
	}

}
