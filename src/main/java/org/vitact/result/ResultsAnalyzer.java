package org.vitact.result;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ResultsAnalyzer extends Application {
    public static String BASE_FILE = "c://";
    public static final String STUDY_BASE_DIR = "estudios";

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("Controller.fxml"));
        primaryStage.setTitle("Result Analyzer");
        primaryStage.setScene(new Scene(root, 450, 325));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
