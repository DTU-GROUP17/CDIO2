package client;

import Observables.Observable;
import weight.KeyPress;
import weight.WeightInterfaceController;

public class DummyGUI implements WeightInterfaceController {
	public String primaryDisplay;
	public String secondaryDisplay;
	private Observable<KeyPress> keyPressObservable;
	private Observable<Double> doubleObservable;

	public DummyGUI() {
		this.keyPressObservable = new Observable<>();
		this.doubleObservable = new Observable<>();
	}

	@Override
	public Observable<KeyPress> getKeyFeed() {
		return keyPressObservable;
	}

	@Override
	public Observable<Double> getWeightFeed() {
		return doubleObservable;
	}

	@Override
	public void showMessagePrimaryDisplay(String string) {
		this.primaryDisplay = string;
	}

	@Override
	public void showMessageSecondaryDisplay(String string) {
		this.secondaryDisplay = string;
	}

	@Override
	public void changeInputType(InputType type) {

	}

	@Override
	public void setSoftButtonTexts(String[] texts) {

	}

	@Override
	public void run() {

	}
}
