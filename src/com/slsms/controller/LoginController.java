package com.slsms.controller;

import com.slsms.util.DBConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
            String query = "SELECT role FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, user);
            stmt.setString(2, pass);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                
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
        }
    }

    @FXML
    private void goToRegister() {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/resources/fxml/Register.fxml")), 600, 500));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void redirect(String fxmlFile, String title) {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.close(); // Close login
            
            Stage newStage = new Stage();
            newStage.setTitle("SLSMS - " + title);
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/Register.fxml")), 600, 500));
            newStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}