package com.slsms.controller;

import com.slsms.util.DBConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.Locale;

public class AdminController {

    @FXML private StackPane contentArea;

    @FXML
    public void initialize() {
        showDashboard(); // 
    }

    // --- DASHBOARD ---
    @FXML
    public void showDashboard() {
        contentArea.getChildren().clear();

        VBox dashboard = new VBox(20);
        Label title = new Label("Dashboard Overview");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        HBox statsContainer = new HBox(20);
        
        // 1. Get Data from DB
        double revenue = getDoubleValue("SELECT SUM(final_price) FROM sales");
        int landsSold = getIntValue("SELECT COUNT(*) FROM sales");
        int activeAgents = getIntValue("SELECT COUNT(*) FROM users WHERE role='AGENT'");

        // 2. Format Currency (FCFA)
        String formattedRevenue = NumberFormat.getInstance(Locale.FRENCH).format(revenue);

        statsContainer.getChildren().addAll(
            createCard("Total Revenue", formattedRevenue + " FCFA", "#2980B9"),
            createCard("Lands Sold", String.valueOf(landsSold), "#27AE60"),
            createCard("Active Agents", String.valueOf(activeAgents), "#8E44AD")
        );

        dashboard.getChildren().addAll(title, statsContainer);
        contentArea.getChildren().add(dashboard);
    }

    // --- MANAGE AGENTS (Placeholder) ---
    @FXML
    public void showAgents() {
        contentArea.getChildren().clear();
        Label title = new Label("Manage Agents");
        title.setStyle("-fx-font-size: 24px; -fx-text-fill: #2C3E50;");
        Label sub = new Label("Agent list functionality coming soon...");
        contentArea.getChildren().addAll(new VBox(10, title, sub));
    }

    // --- SETTINGS (Placeholder) ---
    @FXML
    public void showSettings() {
        contentArea.getChildren().clear();
        Label title = new Label("System Settings");
        title.setStyle("-fx-font-size: 24px; -fx-text-fill: #2C3E50;");
        Label sub = new Label("Configuration options...");
        contentArea.getChildren().addAll(new VBox(10, title, sub));
    }

    // --- HELPERS ---
    private VBox createCard(String title, String value, String colorHex) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: " + colorHex + "; -fx-padding: 20; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 1);");
        card.setPrefWidth(250);

        Label lblVal = new Label(value);
        lblVal.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");
        
        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 14px;");

        card.getChildren().addAll(lblVal, lblTitle);
        return card;
    }

    private double getDoubleValue(String query) {
        try (Connection conn = DBConnection.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(query)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0.0;
    }

    private int getIntValue(String query) {
        try (Connection conn = DBConnection.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(query)) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    @FXML
    public void logout() {
        try {
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/Login.fxml")), 600, 400));
        } catch (Exception e) { e.printStackTrace(); }
    }
}