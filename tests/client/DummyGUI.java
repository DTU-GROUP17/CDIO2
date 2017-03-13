package client;

import Observables.Observable;
import weight.KeyPress;
import weight.WeightInterfaceController;

public class DummyGUI implements WeightInterfaceController {
	public String primaryDisplay;


	@Override
	public Observable<KeyPress> getKeyFeed() {
		return new Observable<>();
	}

	@Override
	public Observable<Double> getWeightFeed() {
		return new Observable<>();
	}

	@Override
	public void showMessagePrimaryDisplay(String string) {
		this.primaryDisplay = string;
		System.out.println(primaryDisplay);
	}

	@Override
	public void showMessageSecondaryDisplay(String string) {

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
