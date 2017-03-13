package socket;

import java.util.ArrayList;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InMessage extends Message<InMessage> {

	private static final Pattern getFlags = Pattern.compile("(?<=\\s|^)(?<!\")([^\"\\s]*)(?!\")(?=\\s|$)");

	private static final Pattern getContent = Pattern.compile("(?<=(\\s|^)\")([^\"]*)(?=\"(\\s|$))");

	public InMessage(Command command, ArrayList<String> flags, ArrayList<String> content) {
		super(command, flags, content);
	}

	public InMessage(Command command) {
		super(command);
	}


	public static InMessage fromString(String gotten){
		Matcher m = getFlags.matcher(gotten);
		MatchResult mm = m.toMatchResult();
		m.find();
		String command = m.group();

		InMessage message;

		try{
			message = new InMessage(Command.valueOf(command));
		}
		catch (IllegalArgumentException e) {
			message = new InMessage(Command.UNKNOWN);
		}

		while (m.find()){
			message.addFlag(m.group());
		}

		m = getContent.matcher(gotten);
		while (m.find()){
			message.addContent(m.group());
		}

		return message;
	}

}
