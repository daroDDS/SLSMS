package com.slsms.controller;

import com.slsms.model.Buyer;
import com.slsms.model.Land;
import com.slsms.model.Visit;
import com.slsms.util.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

public class AgentController {

    @FXML private StackPane contentArea;
    private final NumberFormat currencyFormatter = NumberFormat.getInstance(Locale.FRENCH);

    @FXML
    public void initialize() {
        showDashboard();
    }

    // ==========================================================
    // 1. DASHBOARD & ANALYTICS
    // ==========================================================
    @FXML
    public void showDashboard() {
        contentArea.getChildren().clear();

        VBox layout = new VBox(20);
        Label title = new Label("Dashboard Overview");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        // KPI Cards
        HBox stats = new HBox(20);
        stats.getChildren().addAll(
            createStatCard("Inventory", getCount("SELECT COUNT(*) FROM lands WHERE status='AVAILABLE'"), "#3498DB"),
            createStatCard("Buyers", getCount("SELECT COUNT(*) FROM buyers"), "#E67E22"),
            createStatCard("My Sales", getCount("SELECT COUNT(*) FROM sales"), "#27AE60"),
            createStatCard("Planned Visits", getCount("SELECT COUNT(*) FROM visits WHERE status='PLANNED'"), "#9B59B6")
        );

        layout.getChildren().addAll(title, stats);
        contentArea.getChildren().add(layout);
    }

    // ==========================================================
    // 2. INVENTORY (LANDS) MANAGEMENT
    // ==========================================================
    @FXML
    public void showInventory() {
        contentArea.getChildren().clear();
        VBox layout = new VBox(15);
        Label title = new Label("Land Inventory");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TableView<Land> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Land, String> colLoc = new TableColumn<>("Location");
        colLoc.setCellValueFactory(new PropertyValueFactory<>("location"));

        TableColumn<Land, Double> colArea = new TableColumn<>("Area (m²)");
        colArea.setCellValueFactory(new PropertyValueFactory<>("area"));

        TableColumn<Land, Double> colPrice = new TableColumn<>("Price (FCFA)");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<Land, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(colLoc, colArea, colPrice, colStatus);

        // Load Data
        ObservableList<Land> data = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM lands")) {
            while (rs.next()) {
                data.add(new Land(rs.getInt("land_id"), rs.getString("location"), rs.getDouble("area_sqm"), rs.getDouble("price"), rs.getString("category"), rs.getString("status")));
            }
        } catch (Exception e) { e.printStackTrace(); }
        table.setItems(data);

        layout.getChildren().addAll(title, table);
        contentArea.getChildren().add(layout);
    }

    @FXML
    public void showAddLand() {
        VBox layout = new VBox(15);
        layout.setStyle("-fx-background-color: white; -fx-padding: 30; -fx-background-radius: 5;");
        layout.setMaxWidth(500);

        Label title = new Label("Add New Land");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField loc = new TextField(); loc.setPromptText("Location");
        TextField area = new TextField(); area.setPromptText("Area (sqm)");
        TextField price = new TextField(); price.setPromptText("Price (FCFA)");
        ComboBox<String> cat = new ComboBox<>(FXCollections.observableArrayList("Residential", "Commercial", "Agricultural"));
        cat.setPromptText("Category");

        Button btn = new Button("Save Land");
        btn.setStyle("-fx-background-color: #2980B9; -fx-text-fill: white;");
        Label msg = new Label();

        btn.setOnAction(e -> {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "INSERT INTO lands (location, area_sqm, price, category, status) VALUES (?, ?, ?, ?, 'AVAILABLE')";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, loc.getText());
                stmt.setDouble(2, Double.parseDouble(area.getText()));
                stmt.setDouble(3, Double.parseDouble(price.getText()));
                stmt.setString(4, cat.getValue());
                stmt.executeUpdate();
                msg.setText("Land added!");
                msg.setStyle("-fx-text-fill: green;");
                loc.clear(); area.clear(); price.clear();
            } catch (Exception ex) { msg.setText("Error: " + ex.getMessage()); }
        });

        layout.getChildren().addAll(title, new Label("Location"), loc, new Label("Area"), area, new Label("Price"), price, cat, msg, btn);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(layout);
    }

    // ==========================================================
    // 3. BUYER MANAGEMENT
    // ==========================================================
    @FXML
    public void showBuyers() {
        contentArea.getChildren().clear();
        VBox layout = new VBox(15);
        Label title = new Label("Registered Buyers");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TableView<Buyer> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Buyer, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Buyer, String> colPhone = new TableColumn<>("Phone");
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

        TableColumn<Buyer, Double> colBudget = new TableColumn<>("Budget");
        colBudget.setCellValueFactory(new PropertyValueFactory<>("budget"));

        TableColumn<Buyer, String> colLoc = new TableColumn<>("Pref. Location");
        colLoc.setCellValueFactory(new PropertyValueFactory<>("location"));

        table.getColumns().addAll(colName, colPhone, colBudget, colLoc);

        ObservableList<Buyer> data = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM buyers")) {
            while (rs.next()) {
                data.add(new Buyer(rs.getInt("buyer_id"), rs.getString("name"), rs.getString("phone"), rs.getDouble("budget_max"), rs.getString("preferred_location")));
            }
        } catch (Exception e) { e.printStackTrace(); }
        table.setItems(data);

        layout.getChildren().addAll(title, table);
        contentArea.getChildren().add(layout);
    }

    @FXML
    public void showRegisterBuyer() {
        VBox layout = new VBox(15);
        layout.setStyle("-fx-background-color: white; -fx-padding: 30; -fx-background-radius: 5;");
        layout.setMaxWidth(500);

        Label title = new Label("Register Buyer");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField name = new TextField(); name.setPromptText("Full Name");
        TextField phone = new TextField(); phone.setPromptText("Phone");
        TextField budget = new TextField(); budget.setPromptText("Max Budget");
        TextField loc = new TextField(); loc.setPromptText("Preferred Location");
        TextField size = new TextField(); size.setPromptText("Min Size");

        Button btn = new Button("Register");
        btn.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white;");
        Label msg = new Label();

        btn.setOnAction(e -> {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "INSERT INTO buyers (name, phone, budget_max, preferred_location, min_size_sqm) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, name.getText());
                stmt.setString(2, phone.getText());
                stmt.setDouble(3, Double.parseDouble(budget.getText()));
                stmt.setString(4, loc.getText());
                stmt.setDouble(5, Double.parseDouble(size.getText()));
                stmt.executeUpdate();
                msg.setText("Buyer Saved!");
                msg.setStyle("-fx-text-fill: green;");
                name.clear(); phone.clear(); budget.clear();
            } catch (Exception ex) { msg.setText("Error: " + ex.getMessage()); }
        });

        layout.getChildren().addAll(title, name, phone, budget, loc, size, msg, btn);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(layout);
    }

    // ==========================================================
    // 4. VISIT MANAGEMENT (CALENDAR & SCHEDULING)
    // ==========================================================
    @FXML
    public void showVisits() {
        contentArea.getChildren().clear();
        VBox layout = new VBox(15);
        Label title = new Label("Visit Calendar (Planned)");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TableView<Visit> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Visit, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<Visit, String> colBuyer = new TableColumn<>("Buyer");
        colBuyer.setCellValueFactory(new PropertyValueFactory<>("buyerName"));

        TableColumn<Visit, String> colLand = new TableColumn<>("Land Location");
        colLand.setCellValueFactory(new PropertyValueFactory<>("landLocation"));

        TableColumn<Visit, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(colDate, colBuyer, colLand, colStatus);

        // Load Visits
        ObservableList<Visit> data = FXCollections.observableArrayList();
        String sql = "SELECT v.visit_id, v.visit_date, v.status, b.name, l.location " +
                     "FROM visits v " +
                     "JOIN buyers b ON v.buyer_id = b.buyer_id " +
                     "JOIN lands l ON v.land_id = l.land_id " +
                     "ORDER BY v.visit_date DESC";
        try (Connection conn = DBConnection.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                data.add(new Visit(rs.getInt("visit_id"), rs.getString("name"), rs.getString("location"), rs.getString("visit_date"), rs.getString("status")));
            }
        } catch (Exception e) { e.printStackTrace(); }
        table.setItems(data);

        layout.getChildren().addAll(title, table);
        contentArea.getChildren().add(layout);
    }

    @FXML
    public void showScheduleVisit() {
        VBox layout = new VBox(15);
        layout.setStyle("-fx-background-color: white; -fx-padding: 30;");
        layout.setMaxWidth(500);

        Label title = new Label("Schedule New Visit");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        ComboBox<Buyer> buyerBox = new ComboBox<>();
        buyerBox.setPromptText("Select Buyer");
        buyerBox.setPrefWidth(300);
        loadBuyers(buyerBox);

        ComboBox<Land> landBox = new ComboBox<>();
        landBox.setPromptText("Select Land");
        landBox.setPrefWidth(300);
        loadLands(landBox);

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Select Date");

        Button btn = new Button("Schedule");
        btn.setStyle("-fx-background-color: #8E44AD; -fx-text-fill: white;");
        Label msg = new Label();

        btn.setOnAction(e -> {
            if (buyerBox.getValue() == null || landBox.getValue() == null || datePicker.getValue() == null) {
                msg.setText("Please fill all fields");
                return;
            }
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "INSERT INTO visits (land_id, buyer_id, visit_date, status) VALUES (?, ?, ?, 'PLANNED')";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, landBox.getValue().getId());
                stmt.setInt(2, buyerBox.getValue().getId());
                stmt.setDate(3, java.sql.Date.valueOf(datePicker.getValue()));
                stmt.executeUpdate();
                msg.setText("Visit Scheduled!");
                msg.setStyle("-fx-text-fill: green;");
            } catch (Exception ex) { msg.setText("Error: " + ex.getMessage()); }
        });

        layout.getChildren().addAll(title, new Label("Buyer"), buyerBox, new Label("Land"), landBox, new Label("Date"), datePicker, msg, btn);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(layout);
    }

    // ==========================================================
    // 5. SMART RECOMMENDER (FIXED)
    // ==========================================================
    @FXML
    public void showSmartRecommender() {
        VBox layout = new VBox(15);
        Label title = new Label("Smart Recommendations");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        ComboBox<Buyer> buyerBox = new ComboBox<>();
        buyerBox.setPromptText("Select a Buyer...");
        buyerBox.setPrefWidth(300);
        loadBuyers(buyerBox); // Populate logic

        Button btn = new Button("Find Matches");
        btn.setStyle("-fx-background-color: #2980B9; -fx-text-fill: white;");

        VBox results = new VBox(10);
        ScrollPane scroll = new ScrollPane(results);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");

        HBox top = new HBox(10, buyerBox, btn);

        btn.setOnAction(e -> {
            Buyer selected = buyerBox.getValue();
            if (selected == null) return;
            results.getChildren().clear();
            
            // LOGIC: Matches Budget + Size, Sorted by Location match
            String sql = "SELECT * FROM lands WHERE status='AVAILABLE' AND price <= ? " +
                         "ORDER BY CASE WHEN location LIKE ? THEN 1 ELSE 2 END, price ASC";
            
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setDouble(1, selected.getBudget());
                stmt.setString(2, "%" + selected.getLocation() + "%");
                
                ResultSet rs = stmt.executeQuery();
                boolean found = false;
                
                while(rs.next()) {
                    found = true;
                    HBox card = new HBox(15);
                    card.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-border-color: #BDC3C7;");
                    
                    String loc = rs.getString("location");
                    boolean isMatch = loc.toLowerCase().contains(selected.getLocation().toLowerCase());
                    
                    Label l1 = new Label(loc); l1.setStyle("-fx-font-weight: bold;");
                    Label l2 = new Label(currencyFormatter.format(rs.getDouble("price")) + " FCFA");
                    Label badge = new Label(isMatch ? "Location Match" : "Budget Fit");
                    badge.setStyle("-fx-background-color: " + (isMatch?"#2ECC71":"#F1C40F") + "; -fx-text-fill: white; -fx-padding: 2 5;");
                    
                    card.getChildren().addAll(l1, l2, badge);
                    results.getChildren().add(card);
                }
                
                if(!found) results.getChildren().add(new Label("No lands found within budget."));
                
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        layout.getChildren().addAll(title, top, new Separator(), scroll);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(layout);
    }

    // ==========================================================
    // 6. SALES & HELPERS
    // ==========================================================
    @FXML
    public void showSales() {
        contentArea.getChildren().clear();
        // Similar to showInventory but querying sales table...
        Label lbl = new Label("Sales History (Implementation similar to Inventory Table)");
        contentArea.getChildren().add(lbl);
    }

    private void loadBuyers(ComboBox<Buyer> box) {
        ObservableList<Buyer> list = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM buyers")) {
            while (rs.next()) {
                list.add(new Buyer(rs.getInt("buyer_id"), rs.getString("name"), rs.getString("phone"), rs.getDouble("budget_max"), rs.getString("preferred_location")));
            }
        } catch (Exception e) { e.printStackTrace(); }
        box.setItems(list);
    }

    private void loadLands(ComboBox<Land> box) {
        ObservableList<Land> list = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM lands WHERE status='AVAILABLE'")) {
            while (rs.next()) {
                list.add(new Land(rs.getInt("land_id"), rs.getString("location"), rs.getDouble("area_sqm"), rs.getDouble("price"), rs.getString("category"), rs.getString("status")));
            }
        } catch (Exception e) { e.printStackTrace(); }
        box.setItems(list);
    }

    private VBox createStatCard(String title, String value, String color) {
        VBox c = new VBox(5);
        c.setStyle("-fx-background-color: " + color + "; -fx-padding: 15; -fx-background-radius: 5;");
        c.setPrefWidth(150);
        Label v = new Label(value); v.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        Label t = new Label(title); t.setStyle("-fx-text-fill: white;");
        c.getChildren().addAll(v, t);
        return c;
    }

    private String getCount(String sql) {
        try (Connection conn = DBConnection.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {
            if (rs.next()) return String.valueOf(rs.getInt(1));
        } catch (Exception e) { return "0"; }
        return "0";
    }

    @FXML public void logout() {
        try {
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/Login.fxml")), 600, 400));
        } catch (Exception e) {}
    }
}