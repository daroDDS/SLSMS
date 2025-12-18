package com.slsms.controller;

import com.slsms.util.DBConnection;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class AgentController {

    @FXML private StackPane contentArea;

    // --- UC09: ADD LAND ---
    @FXML
    public void showAddLand() {
        VBox layout = new VBox(15);
        layout.getStyleClass().add("content-card");
        layout.setMaxWidth(500);

        Label title = new Label("Add New Land Inventory");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField locField = new TextField(); locField.setPromptText("Location (e.g. Dakar, Almadies)");
        TextField areaField = new TextField(); areaField.setPromptText("Area (m²)");
        TextField priceField = new TextField(); priceField.setPromptText("Price");
        ComboBox<String> catBox = new ComboBox<>(FXCollections.observableArrayList("Residential", "Commercial", "Agricultural"));
        catBox.setPromptText("Category");
        catBox.setMaxWidth(Double.MAX_VALUE);

        Button saveBtn = new Button("Save Land");
        saveBtn.getStyleClass().add("button-primary");
        Label msgLabel = new Label();

        saveBtn.setOnAction(e -> {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "INSERT INTO lands (location, area_sqm, price, category, status) VALUES (?, ?, ?, ?, 'AVAILABLE')";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, locField.getText());
                stmt.setDouble(2, Double.parseDouble(areaField.getText()));
                stmt.setDouble(3, Double.parseDouble(priceField.getText()));
                stmt.setString(4, catBox.getValue());
                stmt.executeUpdate();
                msgLabel.setText("Land added successfully!");
                msgLabel.setStyle("-fx-text-fill: green;");
            } catch (Exception ex) {
                msgLabel.setText("Error: " + ex.getMessage());
                msgLabel.setStyle("-fx-text-fill: red;");
            }
        });

        layout.getChildren().addAll(title, new Label("Location"), locField, new Label("Size (m²)"), areaField, 
                                    new Label("Price"), priceField, new Label("Category"), catBox, msgLabel, saveBtn);
        
        contentArea.getChildren().clear();
        contentArea.getChildren().add(layout);
    }

    // --- UC11: REGISTER BUYER ---
    @FXML
    public void showRegisterBuyer() {
        VBox layout = new VBox(15);
        layout.getStyleClass().add("content-card");
        layout.setMaxWidth(500);

        Label title = new Label("Register New Buyer");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField nameField = new TextField(); nameField.setPromptText("Full Name");
        TextField phoneField = new TextField(); phoneField.setPromptText("Phone Number");
        TextField budgetField = new TextField(); budgetField.setPromptText("Max Budget");
        TextField prefLocField = new TextField(); prefLocField.setPromptText("Preferred Location");
        TextField minSizeField = new TextField(); minSizeField.setPromptText("Min Size (m²)");

        Button saveBtn = new Button("Register Buyer");
        saveBtn.getStyleClass().add("button-primary");
        Label msgLabel = new Label();

        saveBtn.setOnAction(e -> {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "INSERT INTO buyers (name, phone, budget_max, preferred_location, min_size_sqm) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, nameField.getText());
                stmt.setString(2, phoneField.getText());
                stmt.setDouble(3, Double.parseDouble(budgetField.getText()));
                stmt.setString(4, prefLocField.getText());
                stmt.setDouble(5, Double.parseDouble(minSizeField.getText()));
                stmt.executeUpdate();
                msgLabel.setText("Buyer registered!");
                msgLabel.setStyle("-fx-text-fill: green;");
            } catch (Exception ex) {
                msgLabel.setText("Error: " + ex.getMessage());
                msgLabel.setStyle("-fx-text-fill: red;");
            }
        });

        layout.getChildren().addAll(title, nameField, phoneField, budgetField, prefLocField, minSizeField, msgLabel, saveBtn);
        
        contentArea.getChildren().clear();
        contentArea.getChildren().add(layout);
    }

    // --- UC18: SMART RECOMMENDER ---
    @FXML
    public void showSmartRecommender() {
        VBox layout = new VBox(15);
        layout.setStyle("-fx-padding: 20;");
        
        Label title = new Label("Smart Land Recommender");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Step 1: Select Buyer
        HBox searchBox = new HBox(10);
        ComboBox<String> buyerBox = new ComboBox<>();
        buyerBox.setPromptText("Select a Buyer...");
        buyerBox.setPrefWidth(300);
        
        // Load buyers into combo box
        List<Integer> buyerIds = new ArrayList<>(); // Helper to store IDs matching the index
        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT buyer_id, name FROM buyers");
            while(rs.next()) {
                buyerBox.getItems().add(rs.getString("name"));
                buyerIds.add(rs.getInt("buyer_id"));
            }
        } catch(Exception e) { e.printStackTrace(); }

        Button recommendBtn = new Button("Get Recommendations");
        recommendBtn.getStyleClass().add("button-primary");
        
        searchBox.getChildren().addAll(buyerBox, recommendBtn);

        // Results Area
        VBox resultsBox = new VBox(10);
        ScrollPane scroll = new ScrollPane(resultsBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");

        recommendBtn.setOnAction(e -> {
            int index = buyerBox.getSelectionModel().getSelectedIndex();
            if(index < 0) return;
            int buyerId = buyerIds.get(index);
            resultsBox.getChildren().clear();
            
            // Logic: Find lands matching budget, size, and location (fuzzy match)
            findMatches(buyerId, resultsBox);
        });

        layout.getChildren().addAll(title, searchBox, new Separator(), scroll);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(layout);
    }

    private void findMatches(int buyerId, VBox resultsContainer) {
        String buyerSql = "SELECT * FROM buyers WHERE buyer_id = " + buyerId;
        
        try (Connection conn = DBConnection.getConnection();
             ResultSet rsBuyer = conn.createStatement().executeQuery(buyerSql)) {
            
            if(rsBuyer.next()) {
                double budget = rsBuyer.getDouble("budget_max");
                double minSize = rsBuyer.getDouble("min_size_sqm");
                String prefLoc = rsBuyer.getString("preferred_location");

                // Smart Query: Matches budget & size, orders by location relevance
                String landSql = "SELECT * FROM lands WHERE status='AVAILABLE' " +
                                 "AND price <= ? AND area_sqm >= ? " +
                                 "ORDER BY CASE WHEN location LIKE ? THEN 1 ELSE 2 END, price ASC";
                
                PreparedStatement stmt = conn.prepareStatement(landSql);
                stmt.setDouble(1, budget);
                stmt.setDouble(2, minSize);
                stmt.setString(3, "%" + prefLoc + "%");
                
                ResultSet rsLand = stmt.executeQuery();
                
                boolean found = false;
                while(rsLand.next()) {
                    found = true;
                    VBox card = new VBox(5);
                    card.getStyleClass().add("content-card");
                    
                    String loc = rsLand.getString("location");
                    double price = rsLand.getDouble("price");
                    double area = rsLand.getDouble("area_sqm");
                    
                    Label l1 = new Label(loc + " - " + area + "m²");
                    l1.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
                    Label l2 = new Label("Price: $" + price);
                    Label l3 = new Label("Category: " + rsLand.getString("category"));
                    
                    // Match Score Badge
                    Label badge = new Label(loc.contains(prefLoc) ? "Top Match (Location)" : "Budget Match");
                    badge.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 10;");
                    
                    card.getChildren().addAll(badge, l1, l2, l3);
                    resultsContainer.getChildren().add(card);
                }
                
                if(!found) {
                    resultsContainer.getChildren().add(new Label("No properties found matching this buyer's criteria."));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void logout() {
        try {
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/Login.fxml")), 600, 400));
        } catch (Exception e) { e.printStackTrace(); }
    }
}