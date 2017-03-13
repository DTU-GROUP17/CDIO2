package socket;

import java.util.ArrayList;

public abstract class Message<T extends Message<T>> {

	private Command command;
	protected ArrayList<String> flags;
	protected ArrayList<String> content;

	public Message(Command command, ArrayList<String> flags, ArrayList<String> content){
		this.command = command;
		this.flags = flags;
		this.content = content;
	}

	public Message(Command command){
		this(command, new ArrayList<>(), new ArrayList<>());
	}

	public enum Command {
		RM20, D, DW, T, S, B, Q, P111, K, UNKNOWN, ES
	}

	public Command getCommand() {
		return this.command;
	}

	public T addContent(String item) {
		this.content.add(item);
		return (T) this;
	}

	public T addFlag(String item) {
		this.flags.add(item);
		return (T) this;
	}

	public T waitingForUserInput() {
		return this.addFlag("B");
	}

	public T wrongParameters() {
		return this.addFlag("L");
	}

	public T aborted() {
		return this.addFlag("C");
	}

	public T acknowledged() {
		return this.addFlag("A");
	}

	public T weight() {
		return this.addFlag("S");
	}

	public T halted() {
		return this.addFlag("I");
	}

	public String getContent(int index) throws MessageArgumentException {
		try {
			return this.content.get(index);
		} catch (IndexOutOfBoundsException e) {
			throw new MessageArgumentException();
		}
	}

	public String getFlag(int index) throws MessageArgumentException {
		try {
			return this.flags.get(index);
		} catch (IndexOutOfBoundsException e) {
			throw new MessageArgumentException();
		}
	}

}
