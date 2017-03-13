package Observables;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class Observable<T> {

	private Set<Consumer<T>> observing;

	public Observable(){
		this.observing = new HashSet<>();
	}

	public void notifyAll(T value){
		for (Consumer<T> consumer : this.observing){
			consumer.accept(value);
		}
	}

	public void addObserver(Consumer<T> consumer){
		this.observing.add(consumer);
	}

	public void removeObserver(Consumer<T> consumer){
		this.observing.remove(consumer);
	}

}