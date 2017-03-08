package weight;

import Observables.Observable;

public interface WeightInterfaceController extends Runnable {

	Observable<KeyPress> getKeyFeed();
	Observable<Double> getWeightFeed();

	void showMessagePrimaryDisplay(String string);
	void showMessageSecondaryDisplay(String string);
	void changeInputType(InputType type);
	void setSoftButtonTexts(String[] texts);
	
	public enum InputType {
		UPPER, LOWER, NUMBERS
	}

}
