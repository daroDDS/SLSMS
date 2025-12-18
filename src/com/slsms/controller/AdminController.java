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
import java.sql.Statement;

public class AdminController {

    @FXML private StackPane contentArea;

    @FXML
    public void initialize() {
        showDashboard(); // Load dashboard by default
    }

    @FXML
    private void showDashboard() {
        contentArea.getChildren().clear();

        // Create Dashboard Layout Programmatically for simplicity
        VBox dashboard = new VBox(20);
        
        Label title = new Label("Dashboard Overview");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        // KPI Cards Row
        HBox statsBox = new HBox(20);
        statsBox.getChildren().addAll(
            createStatCard("Total Revenue", getDbCount("SELECT SUM(final_price) FROM sales") + " $"),
            createStatCard("Lands Sold", getDbCount("SELECT COUNT(*) FROM sales")),
            createStatCard("Active Agents", getDbCount("SELECT COUNT(*) FROM users WHERE role='AGENT'"))
        );

        // Recent Activity Section (Placeholder)
        VBox recentActivity = new VBox(10);
        recentActivity.getStyleClass().add("content-card");
        recentActivity.getChildren().add(new Label("Recent Sales Activity"));
        recentActivity.getChildren().add(new Label("• Villa in Almadies sold for $250,000"));
        recentActivity.getChildren().add(new Label("• Plot in Diamniadio sold for $15,000"));

        dashboard.getChildren().addAll(title, statsBox, recentActivity);
        contentArea.getChildren().add(dashboard);
    }

    private VBox createStatCard(String title, String value) {
        VBox card = new VBox(10);
        card.getStyleClass().add("stat-card");
        card.setPrefWidth(250);
        
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("stat-label");
        
        Label lblValue = new Label(value);
        lblValue.getStyleClass().add("stat-value");
        
        card.getChildren().addAll(lblValue, lblTitle);
        return card;
    }

    private String getDbCount(String query) {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                String val = rs.getString(1);
                return val == null ? "0" : val;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0";
    }

    @FXML
    public void logout() {
        try {
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/Login.fxml")), 600, 400));
        } catch (Exception e) { e.printStackTrace(); }
    }
}