package com.slsms.controller;

import com.slsms.model.Buyer;
import com.slsms.model.Sale;
import com.slsms.model.User;
import com.slsms.model.Visit;
import com.slsms.util.DBConnection;
import com.slsms.util.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;

public class AdminController {

    @FXML private StackPane contentArea;
    private final NumberFormat currencyFormatter = NumberFormat.getInstance(Locale.FRENCH);

    @FXML
    public void initialize() {
        showDashboard();
    }

    // ==========================================================
    // 1. PROFILE
    // ==========================================================
    @FXML
    public void showProfile() {
        contentArea.getChildren().clear();
        UserSession user = UserSession.getInstance();

        VBox card = new VBox(20);
        card.setStyle("-fx-background-color: white; -fx-padding: 40; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);");
        card.setMaxWidth(400);
        card.setAlignment(Pos.CENTER);

        String initial = (user.getFullName() != null && !user.getFullName().isEmpty()) ? String.valueOf(user.getFullName().charAt(0)) : "A";
        Label avatar = new Label(initial);
        avatar.setStyle("-fx-background-color: #C0392B; -fx-text-fill: white; -fx-font-size: 40px; -fx-font-weight: bold; -fx-padding: 20 35; -fx-background-radius: 100;");

        Label name = new Label(user.getFullName());
        name.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        VBox details = new VBox(10);
        details.setAlignment(Pos.CENTER_LEFT);
        details.setMaxWidth(300);
        details.getChildren().addAll(
            new Label("Username: " + user.getUsername()),
            new Label("Role: SYSTEM ADMINISTRATOR"),
            new Label("Admin ID: #" + user.getUserId())
        );

        card.getChildren().addAll(avatar, name, new Separator(), details);
        contentArea.getChildren().add(card);
    }

    // ==========================================================
    // 2. DASHBOARD OVERVIEW
    // ==========================================================
    @FXML
    public void showDashboard() {
        contentArea.getChildren().clear();
        VBox layout = new VBox(20);
        Label title = new Label("Global Analytics");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        HBox stats = new HBox(20);
        
        // Calculate Global Stats
        double revenue = getDoubleValue("SELECT SUM(total_amount) FROM sales");
        String formattedRevenue = currencyFormatter.format(revenue);
        int totalSales = getIntValue("SELECT COUNT(*) FROM sales");
        int totalAgents = getIntValue("SELECT COUNT(*) FROM users WHERE role='AGENT'");
        int totalBuyers = getIntValue("SELECT COUNT(*) FROM buyers");

        stats.getChildren().addAll(
            createStatCard("Total Revenue", formattedRevenue + " FCFA", "#2980B9"),
            createStatCard("Total Sales", String.valueOf(totalSales), "#27AE60"),
            createStatCard("Total Agents", String.valueOf(totalAgents), "#8E44AD"),
            createStatCard("Total Buyers", String.valueOf(totalBuyers), "#E67E22")
        );

        layout.getChildren().addAll(title, stats);
        contentArea.getChildren().add(layout);
    }

    // ==========================================================
    // 3. MANAGE AGENTS
    // ==========================================================
    @FXML
    public void showAgents() {
        contentArea.getChildren().clear();
        VBox layout = new VBox(15);
        Label title = new Label("Manage Sales Agents");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        // Add Agent Form
        HBox addBox = new HBox(10);
        addBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5;");
        addBox.setAlignment(Pos.CENTER_LEFT);
        
        TextField nameF = new TextField(); nameF.setPromptText("Full Name");
        TextField userF = new TextField(); userF.setPromptText("Username");
        PasswordField passF = new PasswordField(); passF.setPromptText("Password");
        Button addBtn = new Button("Add Agent"); addBtn.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white;");
        Label msg = new Label();

        TableView<User> table = new TableView<>();

        addBtn.setOnAction(e -> {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "INSERT INTO users (full_name, username, password, role) VALUES (?, ?, ?, 'AGENT')";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, nameF.getText());
                stmt.setString(2, userF.getText());
                stmt.setString(3, passF.getText());
                stmt.executeUpdate();
                msg.setText("Agent Added!"); msg.setStyle("-fx-text-fill: green;");
                nameF.clear(); userF.clear(); passF.clear();
                refreshAgentTable(table);
            } catch (Exception ex) { msg.setText("Error: " + ex.getMessage()); }
        });

        addBox.getChildren().addAll(new Label("New Agent:"), nameF, userF, passF, addBtn, msg);

        // Agent List
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<User, String> c1 = new TableColumn<>("ID"); c1.setCellValueFactory(new PropertyValueFactory<>("userId"));
        TableColumn<User, String> c2 = new TableColumn<>("Full Name"); c2.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        TableColumn<User, String> c3 = new TableColumn<>("Username"); c3.setCellValueFactory(new PropertyValueFactory<>("username"));
        table.getColumns().addAll(c1, c2, c3);
        
        refreshAgentTable(table);

        layout.getChildren().addAll(title, addBox, table);
        contentArea.getChildren().add(layout);
    }

    private void refreshAgentTable(TableView<User> table) {
        ObservableList<User> data = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM users WHERE role='AGENT'")) {
            while(rs.next()) {
                data.add(new User(rs.getInt("user_id"), rs.getString("username"), rs.getString("full_name"), "AGENT"));
            }
        } catch(Exception e) { e.printStackTrace(); }
        table.setItems(data);
    }

    // ==========================================================
    // 4. ALL SALES (READ ONLY + Agent Name)
    // ==========================================================
    @FXML
    public void showAllSales() {
        contentArea.getChildren().clear();
        VBox layout = new VBox(15);
        Label title = new Label("All Sales Records");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TableView<Sale> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<Sale, String> c1 = new TableColumn<>("Date"); c1.setCellValueFactory(new PropertyValueFactory<>("date"));
        TableColumn<Sale, String> c2 = new TableColumn<>("Land"); c2.setCellValueFactory(new PropertyValueFactory<>("landLocation"));
        TableColumn<Sale, String> c3 = new TableColumn<>("Buyer"); c3.setCellValueFactory(new PropertyValueFactory<>("buyerName"));
        TableColumn<Sale, String> c4 = new TableColumn<>("Agent"); c4.setCellValueFactory(new PropertyValueFactory<>("agentName"));
        TableColumn<Sale, Double> c5 = new TableColumn<>("Total Amount"); c5.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        TableColumn<Sale, Double> c6 = new TableColumn<>("Paid"); c6.setCellValueFactory(new PropertyValueFactory<>("paidAmount"));
        TableColumn<Sale, Double> c7 = new TableColumn<>("Balance"); c7.setCellValueFactory(new PropertyValueFactory<>("remainingBalance"));

        table.getColumns().addAll(c1, c2, c3, c4, c5, c6, c7);

        ObservableList<Sale> data = FXCollections.observableArrayList();
        String sql = "SELECT s.sale_id, l.location, b.name, u.full_name as agent_name, s.total_amount, s.paid_amount, s.remaining_balance, s.payment_method, s.sale_date " +
                     "FROM sales s " +
                     "JOIN lands l ON s.land_id = l.land_id " +
                     "JOIN buyers b ON s.buyer_id = b.buyer_id " +
                     "LEFT JOIN users u ON s.agent_id = u.user_id " +
                     "ORDER BY s.sale_date DESC";

        try (Connection conn = DBConnection.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                data.add(new Sale(
                    rs.getInt("sale_id"), 
                    rs.getString("location"), 
                    rs.getString("name"), 
                    rs.getString("agent_name"), // Agent Name
                    rs.getDouble("total_amount"),
                    rs.getDouble("paid_amount"),
                    rs.getDouble("remaining_balance"),
                    rs.getString("payment_method"),
                    rs.getString("sale_date")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        table.setItems(data);

        layout.getChildren().addAll(title, table);
        contentArea.getChildren().add(layout);
    }

    // ==========================================================
    // 5. ALL BUYERS (READ ONLY + Agent Name)
    // ==========================================================
    @FXML
    public void showAllBuyers() {
        contentArea.getChildren().clear();
        VBox layout = new VBox(15);
        Label title = new Label("All Registered Buyers");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TableView<Buyer> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<Buyer, String> c1 = new TableColumn<>("Name"); c1.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Buyer, String> c2 = new TableColumn<>("Phone"); c2.setCellValueFactory(new PropertyValueFactory<>("phone"));
        TableColumn<Buyer, String> c3 = new TableColumn<>("Budget"); c3.setCellValueFactory(new PropertyValueFactory<>("budget"));
        TableColumn<Buyer, String> c4 = new TableColumn<>("Location"); c4.setCellValueFactory(new PropertyValueFactory<>("location"));
        TableColumn<Buyer, String> c5 = new TableColumn<>("Managed By"); c5.setCellValueFactory(new PropertyValueFactory<>("agentName"));

        table.getColumns().addAll(c1, c2, c3, c4, c5);

        ObservableList<Buyer> data = FXCollections.observableArrayList();
        String sql = "SELECT b.*, u.full_name as agent_name FROM buyers b LEFT JOIN users u ON b.agent_id = u.user_id";

        try (Connection conn = DBConnection.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                data.add(new Buyer(
                    rs.getInt("buyer_id"), 
                    rs.getString("name"), 
                    rs.getString("phone"), 
                    rs.getDouble("budget_max"), 
                    rs.getString("preferred_location"),
                    rs.getString("agent_name") // Agent Name
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        table.setItems(data);

        layout.getChildren().addAll(title, table);
        contentArea.getChildren().add(layout);
    }

    // ==========================================================
    // 6. ALL VISITS (READ ONLY + Agent Name)
    // ==========================================================
    @FXML
    public void showAllVisits() {
        contentArea.getChildren().clear();
        VBox layout = new VBox(15);
        Label title = new Label("All Visit Records");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TableView<Visit> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<Visit, String> c1 = new TableColumn<>("Date"); c1.setCellValueFactory(new PropertyValueFactory<>("date"));
        TableColumn<Visit, String> c2 = new TableColumn<>("Buyer"); c2.setCellValueFactory(new PropertyValueFactory<>("buyerName"));
        TableColumn<Visit, String> c3 = new TableColumn<>("Land"); c3.setCellValueFactory(new PropertyValueFactory<>("landLocation"));
        TableColumn<Visit, String> c4 = new TableColumn<>("Agent"); c4.setCellValueFactory(new PropertyValueFactory<>("agentName"));
        TableColumn<Visit, String> c5 = new TableColumn<>("Status"); c5.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(c1, c2, c3, c4, c5);

        ObservableList<Visit> data = FXCollections.observableArrayList();
        String sql = "SELECT v.visit_id, DATE_FORMAT(v.visit_date, '%Y-%m-%d %H:%i') as fmt_date, v.status, b.name, l.location, u.full_name as agent_name " +
                     "FROM visits v " +
                     "JOIN buyers b ON v.buyer_id = b.buyer_id " +
                     "JOIN lands l ON v.land_id = l.land_id " +
                     "LEFT JOIN users u ON v.agent_id = u.user_id " +
                     "ORDER BY v.visit_date DESC";

        try (Connection conn = DBConnection.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                data.add(new Visit(
                    rs.getInt("visit_id"), 
                    rs.getString("name"), 
                    rs.getString("agent_name"), // Agent Name
                    rs.getString("location"), 
                    rs.getString("fmt_date"), 
                    rs.getString("status")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        table.setItems(data);

        layout.getChildren().addAll(title, table);
        contentArea.getChildren().add(layout);
    }

    // ==========================================================
    // HELPERS
    // ==========================================================
    private VBox createStatCard(String title, String value, String color) {
        VBox c = new VBox(5); c.setStyle("-fx-background-color: " + color + "; -fx-padding: 15; -fx-background-radius: 5;"); c.setPrefWidth(200);
        Label v = new Label(value); v.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");
        Label t = new Label(title); t.setStyle("-fx-text-fill: white;"); c.getChildren().addAll(v, t); return c;
    }

    private double getDoubleValue(String query) {
        try (Connection conn = DBConnection.getConnection(); ResultSet rs = conn.createStatement().executeQuery(query)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (Exception e) {} return 0.0;
    }

    private int getIntValue(String query) {
        try (Connection conn = DBConnection.getConnection(); ResultSet rs = conn.createStatement().executeQuery(query)) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {} return 0;
    }

    @FXML
    public void logout() {
        UserSession.cleanSession();
        try {
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/Login.fxml")), 600, 400));
        } catch (Exception e) {}
    }
}