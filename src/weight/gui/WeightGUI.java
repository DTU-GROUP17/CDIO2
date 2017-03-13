package weight.gui;

import Observables.Observable;
import weight.KeyPress;
import weight.WeightInterfaceController;

import java.util.Arrays;

public class WeightGUI implements WeightInterfaceController {
	private static WeightGUI instance;
	private FxApp fxApp;

	private Observable<KeyPress> keyFeed;
	private Observable<Double> weightFeed;

	public WeightGUI() {
		this.keyFeed = new Observable<>();
		this.weightFeed = new Observable<>();
		instance = this;
	}

	@Override
	public void run() {
		FxApp.go();
	}

	public static WeightGUI getInstance() {
		return instance;
	}

	public void setApp(FxApp fxApp) {
		this.fxApp = fxApp;
		fxApp.setSim(this);
	}

	@Override
	public Observable<KeyPress> getKeyFeed() {
		return this.keyFeed;
	}

	@Override
	public Observable<Double> getWeightFeed() {
		return this.weightFeed;
	}

	@Override
	public void showMessagePrimaryDisplay(String string) {
		this.fxApp.printLoad(string);
	}

	@Override
	public void showMessageSecondaryDisplay(String string) {
		fxApp.printBottom(string);
	}

	@Override
	public void changeInputType(InputType type) {
		switch(type){
		case LOWER:
			fxApp.setButtonsLower();
			break;
		case NUMBERS:
			fxApp.setButtonsNumbers();
			break;
		case UPPER:
			fxApp.setButtonsUpper();
			break;
		default:
			fxApp.setButtonsLower();
			break;
		}
	}

	@Override
	public void setSoftButtonTexts(String[] texts) {
		int firstSoftkey = 0;
		if (texts == null) {
			texts = new String[0];
		}
		boolean[] sftkeysChecked = new boolean[texts.length];
		Arrays.fill(sftkeysChecked, false);
		fxApp.softKeysShow(texts, firstSoftkey, sftkeysChecked);
	}

	void onSliderValueChange(Double newValue) {
		this.weightFeed.notifyAll(newValue/1000);
	}

	void onExitButtonPressed() {
		this.keyFeed.notifyAll(KeyPress.Exit());
	}

	void onZeroButtonPressed() {
		this.keyFeed.notifyAll(KeyPress.Zero());
	}

	void onTaraButtonPressed() {
		this.keyFeed.notifyAll(KeyPress.Tara());
	}

	void onSendButtonPressed() {
		this.keyFeed.notifyAll(KeyPress.Send());
	}

	void onNumBtnPressed(char btn){
		this.keyFeed.notifyAll(KeyPress.Character(btn));
	}

	void onSoftBtnPressed(int i) {
		this.keyFeed.notifyAll(KeyPress.SoftButton(i));
	}


	void onCancelButtonPressed() {
		this.keyFeed.notifyAll(KeyPress.Cancel());
	}
}
