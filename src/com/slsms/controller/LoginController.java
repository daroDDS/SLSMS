package com.slsms.controller;

import com.slsms.util.DBConnection;
import com.slsms.util.UserSession; // Import the session
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

        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, user);
            stmt.setString(2, pass);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // 1. SAVE SESSION DATA
                UserSession.setSession(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("full_name"),
                    rs.getString("role")
                );

                // 2. REDIRECT
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
            errorLabel.setText("Connection Error");
        }
    }

    private void redirect(String fxmlFile, String title) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxmlFile));
        Parent root = loader.load();
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
        
        Stage newStage = new Stage();
        newStage.setTitle("SLSMS - " + title);
        newStage.setScene(new Scene(root, 1000, 650));
        newStage.show();
    }
    
    @FXML
    private void goToRegister() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Register.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 600, 500));
        } catch (Exception e) { e.printStackTrace(); }
    }
}