package com.slsms.controller;

import com.slsms.util.DBConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                errorLabel.setText("Database connection failed!");
                return;
            }

            String query = "SELECT role, user_id FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, user);
            stmt.setString(2, pass);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                int userId = rs.getInt("user_id");
                
                // Pass the user ID to the session (conceptually) or just redirect
                if ("ADMIN".equalsIgnoreCase(role)) {
                    redirect("AdminView.fxml", "Admin Dashboard");
                } else {
                    redirect("AgentView.fxml", "Sales Agent Dashboard");
                }
            } else {
                errorLabel.setText("Invalid credentials!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void goToRegister() {
        try {
            // Note the path: /fxml/Register.fxml (assuming resources is Source Root)
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Register.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 600, 500));
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Nav Error: " + e.getMessage());
        }
    }

    private void redirect(String fxmlFile, String title) {
        try {
            // 1. Load the correct file passed in the argument
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxmlFile));
            Parent root = loader.load();

            // 2. Close current login window
            Stage currentStage = (Stage) usernameField.getScene().getWindow();
            currentStage.close();
            
            // 3. Open new window
            Stage newStage = new Stage();
            newStage.setTitle("SLSMS - " + title);
            newStage.setScene(new Scene(root, 900, 600)); // Bigger size for main app
            newStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Load Error: " + e.getMessage());
        }
    }
}