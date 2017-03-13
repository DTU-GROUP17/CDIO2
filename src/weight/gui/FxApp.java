package weight.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import weight.WeightInterfaceController.InputType;

import java.util.Timer;
import java.util.TimerTask;

public class FxApp extends Application {
	static final String[] str_lower = {".", "abc", "def", "ghi", "jkl", "mno", "pqr", "stu", "vxy", "z"};
	static final String[] str_upper = {".", "ABC", "DEF", "GHI", "JKL", "MNO", "PQR", "STU", "VXY", "Z"};
	private final int DELAY = 333;
	private Text txtload, txtbottom;
	private final Text[] txtsft = new Text[6];
	private final Text[] txtinfo = new Text[4];
	@FXML
	private TextField userInput;
	private Slider slider;
	private Button btnexit, btnzero, btntara, btnsend, btnshift, btnCancel;
	private final Button[] btnsft = new Button[6];
	private final Button[] btnnum = new Button[10];
	private InputType inputType = InputType.NUMBERS;
	private boolean userInputPlaceholderTentative = false;
	private int caretPosition = 0;
	private WeightGUI l;
	private Timer timer;

	public FxApp() {
	}

	static void go() {
		launch();
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("local.fxml"));
			StackPane root = loader.load();

			Scene scene = new Scene(root, 974, 400);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setX(0);
			primaryStage.setY(0);
			primaryStage.setOnCloseRequest(t -> System.exit(0));

			txtload = (Text) loader.getNamespace().get("txt_load");
			txtinfo[0] = (Text) loader.getNamespace().get("txt_info_1");
			txtinfo[1] = (Text) loader.getNamespace().get("txt_info_2");
			txtinfo[2] = (Text) loader.getNamespace().get("txt_info_3");
			txtinfo[3] = (Text) loader.getNamespace().get("txt_info_4");
			txtbottom = (Text) loader.getNamespace().get("txt_bottom");

			userInput = (TextField) loader.getNamespace().get("userInput");
			userInput.caretPositionProperty().addListener((observable, oldValue, newValue) -> {
				/*
				 * when button is pressed, the textfield looses focus and caret is moved to 0.
				 * Hence the previousCaretPosition is the actual position.
				 */
				if (newValue.intValue() == 0 && caretPosition != 0) {
					userInput.positionCaret(caretPosition);
				}
			});
			userInput.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
				if (userInputPlaceholderTentative) {
					userInputPlaceholderTentative = false;
					userInput.setText(e.getText());
					caretPosition = 1;
					userInput.positionCaret(caretPosition);
				}
			});

			for (int i = 0; i < 6; i++) {
				txtsft[i] = (Text) loader.getNamespace().get("txt_softkey_" + (i + 1));
				btnsft[i] = (Button) loader.getNamespace().get("btn_softkey_" + (i + 1));
				final int j = i;
				btnsft[i].setOnAction(event -> onSoftKeyPressed(j));
			}

			slider = (Slider) loader.getNamespace().get("slider");
			slider.valueProperty().addListener(
					(observable, oldValue, newValue) -> onSliderValueChange(newValue.doubleValue())
			);

			btnexit = (Button) loader.getNamespace().get("btn_exit");
			btnexit.setOnAction(event -> onExitButtonPressed());

			btnzero = (Button) loader.getNamespace().get("btn_zero");
			btnzero.setOnAction(event -> onZeroButtonPressed());

			btntara = (Button) loader.getNamespace().get("btn_tara");
			btntara.setOnAction(event -> onTaraButtonPressed());

			btnsend = (Button) loader.getNamespace().get("btn_send");
			btnsend.setOnAction(event -> onSendButtonPressed());

			final FxAppInputBtnHandler inputHandler = new FxAppInputBtnHandler();
			for (int i = 0; i < 10; i++) {
				final int btn = i;
				btnnum[i] = (Button) loader.getNamespace().get("btn_" + (i));
				btnnum[i].setOnAction(event -> onNumBtnPressed(inputHandler, btn));
			}

			btnshift = (Button) loader.getNamespace().get("btn_shift");
			btnshift.setOnAction(event -> onShiftBtnPressed());

			btnzero = (Button) loader.getNamespace().get("btn_zero");
			btnzero.setOnAction(event -> onZeroButtonPressed());

			btnCancel = (Button) loader.getNamespace().get("btnCancel");
			btnCancel.setOnAction(event -> onCancelButtonPressed());


			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void init() throws Exception {
		WeightGUI.getInstance().setApp(this);
	}

	void setSim(WeightGUI l) {
		this.l = l;
	}


	//output
	private void onSliderValueChange(Double newValue) {
		l.onSliderValueChange(newValue);
	}

	private void onExitButtonPressed() {
		l.onExitButtonPressed();
	}

	private void onZeroButtonPressed() {
		l.onZeroButtonPressed();
	}

	private void onTaraButtonPressed() {
		l.onTaraButtonPressed();
	}

	private void onSendButtonPressed() {
		l.onSendButtonPressed();
	}

	private void onCancelButtonPressed() {
		l.onCancelButtonPressed();
	}

	private void onNumBtnPressed(final FxAppInputBtnHandler inputHandler, final int btn) {
		char c = inputHandler.onButtonPressed(btn, inputType, DELAY);
		if (timer == null) timer = new Timer();
		else {
			timer.cancel();
			timer = new Timer();
		}

		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				l.onNumBtnPressed(c);
			}
		}, DELAY);

		userInput.requestFocus();
	}

	private void onShiftBtnPressed() {
		toggle_input_type();
		userInput.requestFocus();
		userInput.positionCaret(caretPosition);
	}

	private void onSoftKeyPressed(int i) {
		l.onSoftBtnPressed(i);
	}

	//input
	void printLoad(final String load) {
		Platform.runLater(() ->
				txtload.setText(load.length() > 7 ? load.substring(0, 7) : load)
		);
	}

	void printBottom(final String msg) {
		Platform.runLater(() -> {
			txtbottom.setText(msg);
			txtinfo[3].setVisible(false);
			userInput.setVisible(false);
			txtbottom.setVisible(true);
		});
	}

	void softKeysShow(String[] sftkeys, int firstSoftkey, boolean[] sftkeysChecked) {
		int i = 0;
		while (i < txtsft.length && i + firstSoftkey < sftkeys.length) {
			int index = i + firstSoftkey;
			boolean checked;
			try {
				checked = sftkeysChecked[index];
			} catch (ArrayIndexOutOfBoundsException e) {
				checked = false;
			}
			txtsft[i].setText(sftkeys[index] + (checked ? "<" : ""));
			i++;
		}
	}

	//internal
	private void toggle_input_type() {
		switch (inputType) {
			case LOWER:
				setButtonsUpper();
				break;
			case UPPER:
				setButtonsNumbers();
				break;
			case NUMBERS:
				setButtonsLower();
				break;
		}
	}

	void setButtonsLower() {
		Platform.runLater(() -> {
			for (int i = 0; i < btnnum.length; i++) {
				btnnum[i].setText(str_lower[i]);
			}
			inputType = InputType.LOWER;
		});
	}

	void setButtonsUpper() {
		Platform.runLater(() -> {
			for (int i = 0; i < btnnum.length; i++) {
				btnnum[i].setText(str_upper[i]);
			}
			inputType = InputType.UPPER;
		});
	}

	void setButtonsNumbers() {
		Platform.runLater(() -> {
			for (int i = 0; i < btnnum.length; i++) {
				btnnum[i].setText("" + i);
			}
			inputType = InputType.NUMBERS;
		});
	}

}
