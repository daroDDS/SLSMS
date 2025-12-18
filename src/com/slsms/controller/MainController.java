package com.slsms.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class MainController {

    @FXML private StackPane contentArea;

    @FXML
    private void showDashboard() {
        loadView("Dashboard Analytics");
    }

    @FXML
    private void showInventory() {
        loadView("Land Inventory Table");
    }
    
    @FXML
    private void showBuyers() {
        loadView("Buyer Management");
    }
    
    @FXML
    private void showSmartFeature() {
        loadView("Smart Land Recommendations");
    }

    // This checks if we are loading a real FXML or just testing
    private void loadView(String text) {
        contentArea.getChildren().clear();
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 24px; -fx-text-fill: #2C3E50;");
        contentArea.getChildren().add(label);
        // Later, we will replace this Label with:
        // contentArea.getChildren().add(FXMLLoader.load(getClass().getResource("Page.fxml")));
    }
}