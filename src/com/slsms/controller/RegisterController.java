package com.slsms.controller;

import com.slsms.util.DBConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class RegisterController {

    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label msgLabel;

    @FXML
    private void handleRegister() {
        String name = fullNameField.getText();
        String user = usernameField.getText();
        String pass = passwordField.getText();
        String role = roleComboBox.getValue();

        if (name.isEmpty() || user.isEmpty() || pass.isEmpty() || role == null) {
            msgLabel.setText("Please fill all fields!");
            return;
        }

        try {
            Connection conn = DBConnection.getConnection();
            String query = "INSERT INTO users (full_name, username, password, role) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, name);
            stmt.setString(2, user);
            stmt.setString(3, pass);
            stmt.setString(4, role);
            
            int result = stmt.executeUpdate();
            if (result > 0) {
                msgLabel.setStyle("-fx-text-fill: green;");
                msgLabel.setText("Registration Successful!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            msgLabel.setStyle("-fx-text-fill: red;");
            msgLabel.setText("Username already exists!");
        }
    }

    @FXML
    private void goToLogin() {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/resources/fxml/Login.fxml")), 600, 400));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}