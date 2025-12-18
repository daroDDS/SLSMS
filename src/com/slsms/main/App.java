package com.slsms.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the Login FXML
        	Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            
            Scene scene = new Scene(root, 600, 400);
            primaryStage.setTitle("SLSMS Login");
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}