package org.vitact.result;

import java.io.File;
import javafx.beans.value.*;
import javafx.collections.*;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.*;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitact.result.tasks.AnalyzerTask;

public class Controller {
	public Logger logger = LogManager.getRootLogger();
	public Label lInputFile;
	public Label lOutputFolder;
	public ChoiceBox<Integer> cbCorrecta;
	public ChoiceBox<Integer> cbReaccion;
	public CheckBox checkLateResponse;
	public Label lMessage;
	public Button bAnalyze;
	public ProgressBar progressBar;
	public Button bOutputFolder;
	public Button bInputFile;

	Integer[] marks = {1, 2, 3, 4, 5, 6, 7, 8, 9};
	Integer selectedCorrect = null, selectedReaction = null;
	private File chosenFile;
	private File chosenOutputFolder;
	private String labelInputFile;
	private String labelOutputFolder;
	private String labelMsg = "Selecciona los parámetros del estudio y pulsa analizar";
	private boolean lateClickIsOK = false;

	@FXML
	public void initialize() {
		ObservableList<Integer> list = FXCollections.observableArrayList();
		list.addAll(marks);

		cbCorrecta.setItems(list);
		cbReaccion.setItems(list);

		cbCorrecta.valueProperty().addListener((obs, oldVal, newVal) -> {
			logger.debug("Selected new Correct Mark: " + newVal);
			selectedCorrect = newVal;
		});
		cbReaccion.valueProperty().addListener((obs, oldVal, newVal) -> {
			logger.debug("Selected new Reaction Mark: " + newVal);
			selectedReaction = newVal;
		});

		labelInputFile = lInputFile.getText();
		if(!labelInputFile.endsWith(" "))
			labelInputFile = labelInputFile + " ";
		labelOutputFolder = lOutputFolder.getText();
		if(!labelOutputFolder.endsWith(" "))
			labelOutputFolder = labelOutputFolder + " ";

		//checkLateResponse.setSelected(lateClickIsOK);
		setMessage(this.labelMsg);
	}

	@FXML
	public void analyze(ActionEvent actionEvent) {
		// Check values
		String errorMessage = null;
		if(selectedCorrect == null)
			errorMessage = "Por favor selecciona la marca que indica el estímulo correcto";
		if(selectedReaction == null)
			errorMessage = "Por favor selecciona la marca que indica la reacción del sujeto";
		if(chosenFile == null)
			errorMessage = "Por favor selecciona el fichero de estudio";
		if(chosenOutputFolder == null)
			errorMessage = "Por favor selecciona la carpeta en la que almacenar el resultad";
		if(selectedCorrect == selectedReaction)
			errorMessage = "La marca de estímulo correcto no puede ser la misma que la de reacción";
		if(errorMessage != null) {
			showMessage(Alert.AlertType.ERROR, "Error", errorMessage);
			setMessage("Corrige los errores y pulsa analizar");
			return;
		}

		// GUI Handling
		disableElements(true);
		progressBar.setProgress(0);
		AnalyzerTask task = new AnalyzerTask();
		progressBar.progressProperty().unbind();
		progressBar.progressProperty().bind(task.progressProperty());
		task.messageProperty().addListener(new ChangeListener<String>() {
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				setMessage(newValue);
			}
		});

		// End task handling
		task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent t) {
				String message = "Analisis terminado: " + task.getValue();
				setMessage(message);
				progressBar.progressProperty().unbind();
				progressBar.progressProperty().setValue(0);
				disableElements(false);
				showMessage(Alert.AlertType.INFORMATION, "Info", message);
			}
		});
		task.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent t) {
				String message = "Analisis terminado de manera errónea, ver fichero de log";
				setMessage(message);
				progressBar.progressProperty().unbind();
				progressBar.progressProperty().setValue(0);
				disableElements(false);
				showMessage(Alert.AlertType.ERROR, "Error", message);
			}
		});

		// Task properties
		task.setInputFile(chosenFile);
		task.setOutputFolder(chosenOutputFolder);
		task.setCorrectMark(selectedCorrect);
		task.setReactionMark(selectedReaction);

		// Launch task
		new Thread(task).start();
	}

	@FXML
	public void exit(ActionEvent actionEvent) {
		System.exit(0);
	}

	public void openInput(ActionEvent actionEvent) {
		Scene scene = ((Button) actionEvent.getSource()).getScene();
		Stage stage = (Stage) scene.getWindow();
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Seleccionar Fichero Resultados");
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Study files (*.log)", "*.log");
		fileChooser.getExtensionFilters().add(extFilter);
		File baseFolder = new File(ResultsAnalyzer.BASE_FILE + ResultsAnalyzer.STUDY_BASE_DIR);
		if(!baseFolder.isDirectory()) {
			logger.warn(baseFolder.getName() + " is not a valid folder, using working folder (user.dir)");
			baseFolder = new File(System.getProperty("user.dir"));
		}
		fileChooser.setInitialDirectory(baseFolder);
		chosenFile = fileChooser.showOpenDialog(stage);

		if (chosenFile == null)
		{
			logger.warn("No resutls file opened, returning to main page");
			lInputFile.setText(labelInputFile + "NONE FOUND");
			return;
		}
		try
		{
			lInputFile.setText(labelInputFile + chosenFile.getAbsolutePath());
			chosenOutputFolder = new File(chosenFile.getParent());
			lOutputFolder.setText(labelOutputFolder + chosenOutputFolder.getAbsolutePath());
		}
		catch (Exception e)
		{
			// He cancelado el dialogo
			lInputFile.setText(labelInputFile + "NINGUNO");
		}
	}

	public void openOutput(ActionEvent actionEvent) {
		Scene scene = ((Button) actionEvent.getSource()).getScene();
		Stage stage = (Stage) scene.getWindow();
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Seleccionar Carpeta de Salida");
		String lastFolder =null;
		if(chosenOutputFolder != null)
			lastFolder = chosenOutputFolder.getName();
		File baseFolder = null;
		if(chosenFile==null)
			baseFolder =new File(ResultsAnalyzer.BASE_FILE + ResultsAnalyzer.STUDY_BASE_DIR);
		else
			baseFolder =new File(chosenFile.getParent());
		if(!baseFolder.isDirectory()) {
			logger.warn(baseFolder.getName() + " is not a valid folder, using working folder (user.dir)");
			baseFolder = new File(System.getProperty("user.dir"));
		}
		chooser.setInitialDirectory(baseFolder);
		chosenOutputFolder = chooser.showDialog(stage);

		if (chosenOutputFolder == null)
		{
			logger.warn("No output opened, using last one if any");
			if(lastFolder != null)
				chosenOutputFolder = new File(lastFolder);
			lOutputFolder.setText(labelOutputFolder + chosenOutputFolder);
			return;
		}
		try
		{
			lOutputFolder.setText(labelOutputFolder + chosenOutputFolder.getParent());
		}
		catch (Exception e)
		{
			// He cancelado el dialogo
			if(lastFolder != null) {
				chosenOutputFolder = new File(lastFolder);
				lOutputFolder.setText(labelOutputFolder + chosenOutputFolder.getParent());
			} else
				lOutputFolder.setText(chosenOutputFolder + "NINGUNO");
		}
	}

	public void dobleClick(ActionEvent actionEvent) {
		CheckBox checkBox = (CheckBox) actionEvent.getTarget();
		lateClickIsOK = checkBox.isSelected();
		logger.info("Clicked checkBox " + checkBox.isSelected());
	}

	private void setMessage(String message) {
		lMessage.setText(message);
		logger.info(message);
	}

	private void disableElements(boolean disable) {
		bInputFile.setDisable(disable);
		bOutputFolder.setDisable(disable);
		bAnalyze.setDisable(disable);
		cbReaccion.setDisable(disable);
		cbCorrecta.setDisable(disable);
	}

	private void showMessage(Alert.AlertType alertType, String title, String message) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setContentText(message);
		alert.show();
	}

}
