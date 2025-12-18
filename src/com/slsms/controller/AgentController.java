package com.slsms.controller;

import com.slsms.model.Buyer;
import com.slsms.model.Land;
import com.slsms.model.Sale;
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
import java.time.LocalDate;
import java.util.Locale;

public class AgentController {

    @FXML private StackPane contentArea;
    private final NumberFormat currencyFormatter = NumberFormat.getInstance(Locale.FRENCH);

    // Helper: Get Logged-in Agent ID
    private int getAgentId() {
        if (UserSession.getInstance() == null) return 0;
        return UserSession.getInstance().getUserId();
    }

    @FXML
    public void initialize() {
        showDashboard();
    }

    // =============================================================
    // 1. PROFILE
    // =============================================================
    @FXML
    public void showProfile() {
        contentArea.getChildren().clear();
        UserSession user = UserSession.getInstance();

        VBox card = new VBox(20);
        card.setStyle("-fx-background-color: white; -fx-padding: 40; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);");
        card.setMaxWidth(400);
        card.setAlignment(Pos.CENTER);

        String initial = (user.getFullName() != null && !user.getFullName().isEmpty()) ? String.valueOf(user.getFullName().charAt(0)) : "U";
        Label avatar = new Label(initial);
        avatar.setStyle("-fx-background-color: #2C3E50; -fx-text-fill: white; -fx-font-size: 40px; -fx-font-weight: bold; -fx-padding: 20 35; -fx-background-radius: 100;");

        Label name = new Label(user.getFullName());
        name.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        VBox details = new VBox(10);
        details.setAlignment(Pos.CENTER_LEFT);
        details.setMaxWidth(300);
        details.getChildren().addAll(
            new Label("Username: " + user.getUsername()),
            new Label("Role: " + user.getRole()),
            new Label("Agent ID: #" + user.getUserId())
        );

        card.getChildren().addAll(avatar, name, new Separator(), details);
        contentArea.getChildren().add(card);
    }

    // =============================================================
    // 2. DASHBOARD
    // =============================================================
    @FXML
    public void showDashboard() {
        contentArea.getChildren().clear();
        VBox layout = new VBox(20);
        Label title = new Label("My Dashboard");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        int id = getAgentId();
        HBox stats = new HBox(20);
        stats.getChildren().addAll(
            createStatCard("My Lands", getCount("SELECT COUNT(*) FROM lands WHERE agent_id=" + id), "#3498DB"),
            createStatCard("My Buyers", getCount("SELECT COUNT(*) FROM buyers WHERE agent_id=" + id), "#E67E22"),
            createStatCard("My Sales", getCount("SELECT COUNT(*) FROM sales WHERE agent_id=" + id), "#27AE60"),
            createStatCard("Planned Visits", getCount("SELECT COUNT(*) FROM visits WHERE agent_id=" + id + " AND status='PLANNED'"), "#9B59B6")
        );
        layout.getChildren().addAll(title, stats);
        contentArea.getChildren().add(layout);
    }

    // =============================================================
    // 3. INVENTORY (My Lands)
    // =============================================================
    @FXML
    public void showInventory() {
        contentArea.getChildren().clear();
        VBox layout = new VBox(15);
        Label title = new Label("My Land Inventory");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TableView<Land> table = createLandTable();
        ObservableList<Land> data = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM lands WHERE agent_id = ?");
            stmt.setInt(1, getAgentId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) data.add(mapLand(rs));
        } catch (Exception e) { e.printStackTrace(); }
        table.setItems(data);
        
        layout.getChildren().addAll(title, table);
        contentArea.getChildren().add(layout);
    }

    @FXML
    public void showAddLand() {
        VBox layout = new VBox(15);
        layout.setStyle("-fx-background-color: white; -fx-padding: 30;"); layout.setMaxWidth(500);
        Label title = new Label("Add Land"); title.setStyle("-fx-font-size: 20px;");
        
        TextField loc = new TextField(); loc.setPromptText("Location");
        TextField area = new TextField(); area.setPromptText("Area (sqm)");
        TextField price = new TextField(); price.setPromptText("Price (FCFA)");
        ComboBox<String> cat = new ComboBox<>(FXCollections.observableArrayList("Residential", "Commercial", "Agricultural"));
        cat.setPromptText("Category");
        Button btn = new Button("Save"); btn.setStyle("-fx-background-color: #2980B9; -fx-text-fill: white;");
        Label msg = new Label();

        btn.setOnAction(e -> {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "INSERT INTO lands (location, area_sqm, price, category, status, agent_id) VALUES (?, ?, ?, ?, 'AVAILABLE', ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, loc.getText());
                stmt.setDouble(2, Double.parseDouble(area.getText()));
                stmt.setDouble(3, Double.parseDouble(price.getText()));
                stmt.setString(4, cat.getValue());
                stmt.setInt(5, getAgentId());
                stmt.executeUpdate();
                msg.setText("Land Added!"); msg.setStyle("-fx-text-fill: green;");
            } catch (Exception ex) { msg.setText("Error: " + ex.getMessage()); }
        });
        layout.getChildren().addAll(title, loc, area, price, cat, msg, btn);
        contentArea.getChildren().clear(); contentArea.getChildren().add(layout);
    }

    // =============================================================
    // 4. BUYERS (My Buyers)
    // =============================================================
    @FXML
    public void showBuyers() {
        contentArea.getChildren().clear();
        VBox layout = new VBox(15);
        Label title = new Label("My Registered Buyers");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TableView<Buyer> table = createBuyerTable();
        ObservableList<Buyer> data = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM buyers WHERE agent_id = ?");
            stmt.setInt(1, getAgentId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) data.add(mapBuyer(rs));
        } catch (Exception e) { e.printStackTrace(); }
        table.setItems(data);
        layout.getChildren().addAll(title, table);
        contentArea.getChildren().add(layout);
    }

    @FXML
    public void showRegisterBuyer() {
        VBox layout = new VBox(15); layout.setStyle("-fx-background-color: white; -fx-padding: 30;"); layout.setMaxWidth(500);
        Label title = new Label("Register Buyer"); title.setStyle("-fx-font-size: 20px;");
        
        TextField name = new TextField(); name.setPromptText("Name");
        TextField phone = new TextField(); phone.setPromptText("Phone");
        TextField budget = new TextField(); budget.setPromptText("Budget");
        TextField loc = new TextField(); loc.setPromptText("Pref Location");
        TextField size = new TextField(); size.setPromptText("Min Size");
        Button btn = new Button("Save"); btn.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white;");
        Label msg = new Label();

        btn.setOnAction(e -> {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "INSERT INTO buyers (name, phone, budget_max, preferred_location, min_size_sqm, agent_id) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, name.getText());
                stmt.setString(2, phone.getText());
                stmt.setDouble(3, Double.parseDouble(budget.getText()));
                stmt.setString(4, loc.getText());
                stmt.setDouble(5, Double.parseDouble(size.getText()));
                stmt.setInt(6, getAgentId());
                stmt.executeUpdate();
                msg.setText("Buyer Registered!"); msg.setStyle("-fx-text-fill: green;");
            } catch (Exception ex) { msg.setText("Error: " + ex.getMessage()); }
        });
        layout.getChildren().addAll(title, name, phone, budget, loc, size, msg, btn);
        contentArea.getChildren().clear(); contentArea.getChildren().add(layout);
    }

    // =============================================================
    // 5. VISITS (My Visits)
    // =============================================================
    @FXML
    public void showVisits() {
        contentArea.getChildren().clear();
        VBox layout = new VBox(15);
        Label title = new Label("My Visits");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TableView<Visit> table = createVisitTable();
        ObservableList<Visit> data = FXCollections.observableArrayList();
        String sql = "SELECT v.visit_id, DATE_FORMAT(v.visit_date, '%Y-%m-%d %H:%i') as fmt_date, v.status, b.name, l.location " +
                     "FROM visits v JOIN buyers b ON v.buyer_id = b.buyer_id JOIN lands l ON v.land_id = l.land_id " +
                     "WHERE v.agent_id = ? ORDER BY v.visit_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, getAgentId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                // Pass "Me" as agent name since this is the agent's own view
                data.add(new Visit(rs.getInt("visit_id"), rs.getString("name"), "Me", rs.getString("location"), rs.getString("fmt_date"), rs.getString("status")));
            }
        } catch (Exception e) { e.printStackTrace(); }
        table.setItems(data);
        layout.getChildren().addAll(title, table);
        contentArea.getChildren().add(layout);
    }

    @FXML
    public void showScheduleVisit() {
        VBox layout = new VBox(15); layout.setStyle("-fx-background-color: white; -fx-padding: 30;"); layout.setMaxWidth(500);
        Label title = new Label("Schedule Visit"); title.setStyle("-fx-font-size: 20px;");

        ComboBox<Buyer> buyerBox = new ComboBox<>(); buyerBox.setPromptText("Select Your Buyer"); buyerBox.setPrefWidth(300);
        loadMyBuyers(buyerBox);

        ComboBox<Land> landBox = new ComboBox<>(); landBox.setPromptText("Select Your Land"); landBox.setPrefWidth(300);
        loadMyAvailableLands(landBox); // Only loads agent's lands

        HBox dateTimeBox = new HBox(10);
        DatePicker datePicker = new DatePicker();
        ComboBox<String> timeBox = new ComboBox<>();
        timeBox.setPromptText("Time");
        timeBox.getItems().addAll("08:00", "09:00", "10:00", "11:00", "12:00", "14:00", "15:00", "16:00", "17:00");
        dateTimeBox.getChildren().addAll(datePicker, timeBox);

        Button btn = new Button("Schedule"); btn.setStyle("-fx-background-color: #8E44AD; -fx-text-fill: white;");
        Label msg = new Label();
        
        btn.setOnAction(e -> {
            if(buyerBox.getValue() == null || landBox.getValue() == null || datePicker.getValue() == null || timeBox.getValue() == null) {
                msg.setText("Fill all fields"); return;
            }
            try (Connection conn = DBConnection.getConnection()) {
                String dt = datePicker.getValue() + " " + timeBox.getValue() + ":00";
                String sql = "INSERT INTO visits (land_id, buyer_id, agent_id, visit_date, status) VALUES (?, ?, ?, ?, 'PLANNED')";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, landBox.getValue().getId());
                stmt.setInt(2, buyerBox.getValue().getId());
                stmt.setInt(3, getAgentId());
                stmt.setString(4, dt);
                stmt.executeUpdate();
                msg.setText("Visit Scheduled!"); msg.setStyle("-fx-text-fill: green;");
            } catch (Exception ex) { msg.setText("Error: " + ex.getMessage()); }
        });
        layout.getChildren().addAll(title, buyerBox, landBox, dateTimeBox, msg, btn);
        contentArea.getChildren().clear(); contentArea.getChildren().add(layout);
    }

    // =============================================================
    // 6. SALES MANAGEMENT (Detailed)
    // =============================================================
    @FXML
    public void showSales() {
        contentArea.getChildren().clear();
        VBox layout = new VBox(15);
        Label title = new Label("My Sales & Payments");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TableView<Sale> table = createSaleTable(); 
        ObservableList<Sale> data = FXCollections.observableArrayList();
        
        // Fetch detailed financial info
        String sql = "SELECT s.sale_id, l.location, b.name, s.total_amount, s.paid_amount, s.remaining_balance, s.payment_method, s.sale_date " +
                     "FROM sales s " +
                     "JOIN lands l ON s.land_id = l.land_id " +
                     "JOIN buyers b ON s.buyer_id = b.buyer_id " +
                     "WHERE s.agent_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, getAgentId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                data.add(new Sale(
                    rs.getInt("sale_id"), 
                    rs.getString("location"), 
                    rs.getString("name"), 
                    "Me", // Placeholder for agent name
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

    @FXML
    public void showRecordSale() {
        VBox layout = new VBox(15); layout.setStyle("-fx-background-color: white; -fx-padding: 30;"); layout.setMaxWidth(600);
        Label title = new Label("Record Sale"); title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        
        ComboBox<Land> landBox = new ComboBox<>(); landBox.setPromptText("Select Land"); loadMyAvailableLands(landBox);
        ComboBox<Buyer> buyerBox = new ComboBox<>(); buyerBox.setPromptText("Select Buyer"); loadMyBuyers(buyerBox);
        
        TextField totalPriceField = new TextField(); totalPriceField.setPromptText("Total Sale Price (FCFA)");
        
        HBox typeBoxContainer = new HBox(10);
        ComboBox<String> typeBox = new ComboBox<>(FXCollections.observableArrayList("Full Payment", "Installment")); typeBox.setPromptText("Type");
        ComboBox<String> methodBox = new ComboBox<>(FXCollections.observableArrayList("Cash", "Visa Card", "Mobile Money")); methodBox.setPromptText("Method");
        typeBoxContainer.getChildren().addAll(typeBox, methodBox);

        TextField paidNowField = new TextField(); paidNowField.setPromptText("Amount Paid Now");
        Label balanceLabel = new Label("Balance: 0 FCFA"); balanceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #E74C3C;");

        // Logic for auto-filling and balance calc
        typeBox.setOnAction(e -> {
            if ("Full Payment".equals(typeBox.getValue()) && !totalPriceField.getText().isEmpty()) {
                paidNowField.setText(totalPriceField.getText()); paidNowField.setDisable(true);
                balanceLabel.setText("Balance: 0 FCFA");
            } else {
                paidNowField.setDisable(false); paidNowField.clear();
            }
        });
        paidNowField.setOnKeyReleased(e -> {
            try {
                double t = Double.parseDouble(totalPriceField.getText());
                double p = Double.parseDouble(paidNowField.getText());
                balanceLabel.setText("Balance: " + currencyFormatter.format(t - p) + " FCFA");
            } catch(Exception ex) { balanceLabel.setText("Invalid"); }
        });

        DatePicker datePicker = new DatePicker(LocalDate.now());
        Button btn = new Button("Confirm Sale"); btn.setStyle("-fx-background-color: #D35400; -fx-text-fill: white;");
        Label msg = new Label();

        btn.setOnAction(e -> {
            if(landBox.getValue() == null || buyerBox.getValue() == null || methodBox.getValue() == null) {
                msg.setText("Fill all fields!"); return;
            }
            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false);
                double total = Double.parseDouble(totalPriceField.getText());
                double paid = Double.parseDouble(paidNowField.getText());
                String pType = typeBox.getValue().equals("Full Payment") ? "FULL" : "INSTALLMENT";

                // Insert Sale
                String sqlSale = "INSERT INTO sales (land_id, buyer_id, agent_id, total_amount, paid_amount, remaining_balance, payment_type, payment_method, sale_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement stmtSale = conn.prepareStatement(sqlSale, Statement.RETURN_GENERATED_KEYS);
                stmtSale.setInt(1, landBox.getValue().getId());
                stmtSale.setInt(2, buyerBox.getValue().getId());
                stmtSale.setInt(3, getAgentId());
                stmtSale.setDouble(4, total);
                stmtSale.setDouble(5, paid);
                stmtSale.setDouble(6, total - paid);
                stmtSale.setString(7, pType);
                stmtSale.setString(8, methodBox.getValue());
                stmtSale.setDate(9, java.sql.Date.valueOf(datePicker.getValue()));
                stmtSale.executeUpdate();
                
                // Get Sale ID and Insert Payment History
                ResultSet rsK = stmtSale.getGeneratedKeys();
                int sId = 0; if(rsK.next()) sId = rsK.getInt(1);
                PreparedStatement stmtPay = conn.prepareStatement("INSERT INTO payments (sale_id, amount, method, payment_date) VALUES (?, ?, ?, ?)");
                stmtPay.setInt(1, sId); stmtPay.setDouble(2, paid); stmtPay.setString(3, methodBox.getValue()); stmtPay.setDate(4, java.sql.Date.valueOf(datePicker.getValue()));
                stmtPay.executeUpdate();

                // Update Land
                PreparedStatement stmtLand = conn.prepareStatement("UPDATE lands SET status='SOLD' WHERE land_id=?");
                stmtLand.setInt(1, landBox.getValue().getId());
                stmtLand.executeUpdate();

                conn.commit();
                msg.setText("Sale Recorded!"); msg.setStyle("-fx-text-fill: green;");
                loadMyAvailableLands(landBox);
            } catch (Exception ex) { msg.setText("Error: " + ex.getMessage()); }
        });
        
        layout.getChildren().addAll(title, landBox, buyerBox, totalPriceField, typeBoxContainer, paidNowField, balanceLabel, datePicker, msg, btn);
        contentArea.getChildren().clear(); contentArea.getChildren().add(layout);
    }

    // =============================================================
    // 7. SMART RECOMMENDER (Filtered by Agent's Lands)
    // =============================================================
    @FXML
    public void showSmartRecommender() {
        VBox layout = new VBox(15);
        Label title = new Label("Smart Recommendations");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        ComboBox<Buyer> buyerBox = new ComboBox<>(); buyerBox.setPromptText("Select Your Buyer..."); buyerBox.setPrefWidth(300);
        loadMyBuyers(buyerBox);
        Button btn = new Button("Find Matches"); btn.setStyle("-fx-background-color: #2980B9; -fx-text-fill: white;");
        VBox results = new VBox(10);
        ScrollPane scroll = new ScrollPane(results); scroll.setFitToWidth(true); scroll.setStyle("-fx-background-color: transparent;");
        
        btn.setOnAction(e -> {
            Buyer selected = buyerBox.getValue();
            if(selected == null) return;
            results.getChildren().clear();
            
            // Only search Agent's OWN available lands
            String sql = "SELECT * FROM lands WHERE status='AVAILABLE' AND agent_id = ? AND price <= ? ORDER BY CASE WHEN location LIKE ? THEN 1 ELSE 2 END, price ASC";
            try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, getAgentId());
                stmt.setDouble(2, selected.getBudget());
                stmt.setString(3, "%" + selected.getLocation() + "%");
                ResultSet rs = stmt.executeQuery();
                boolean found = false;
                while(rs.next()) {
                    found = true;
                    HBox card = new HBox(15); card.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-border-color: #BDC3C7; -fx-border-radius: 5;");
                    Label l1 = new Label(rs.getString("location")); l1.setStyle("-fx-font-weight: bold;");
                    Label l2 = new Label(currencyFormatter.format(rs.getDouble("price")) + " FCFA");
                    boolean match = rs.getString("location").toLowerCase().contains(selected.getLocation().toLowerCase());
                    Label badge = new Label(match ? "Location Match" : "Budget Fit");
                    badge.setStyle("-fx-background-color: " + (match?"#2ECC71":"#F1C40F") + "; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 10;");
                    Region r = new Region(); HBox.setHgrow(r, Priority.ALWAYS);
                    card.getChildren().addAll(l1, r, l2, badge);
                    results.getChildren().add(card);
                }
                if(!found) results.getChildren().add(new Label("No matches found in your inventory."));
            } catch (Exception ex) { ex.printStackTrace(); }
        });
        layout.getChildren().addAll(title, new HBox(10, buyerBox, btn), new Separator(), scroll);
        contentArea.getChildren().clear(); contentArea.getChildren().add(layout);
    }

    // =============================================================
    // 8. HELPERS & TABLES
    // =============================================================
    private TableView<Land> createLandTable() {
        TableView<Land> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<Land, String> c1 = new TableColumn<>("Location"); c1.setCellValueFactory(new PropertyValueFactory<>("location"));
        TableColumn<Land, Double> c2 = new TableColumn<>("Area"); c2.setCellValueFactory(new PropertyValueFactory<>("area"));
        TableColumn<Land, Double> c3 = new TableColumn<>("Price"); c3.setCellValueFactory(new PropertyValueFactory<>("price"));
        TableColumn<Land, String> c4 = new TableColumn<>("Category"); c4.setCellValueFactory(new PropertyValueFactory<>("category"));
        TableColumn<Land, String> c5 = new TableColumn<>("Status"); c5.setCellValueFactory(new PropertyValueFactory<>("status"));
        table.getColumns().addAll(c1, c2, c3, c4, c5);
        return table;
    }

    private TableView<Buyer> createBuyerTable() {
        TableView<Buyer> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<Buyer, String> c1 = new TableColumn<>("Name"); c1.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Buyer, String> c2 = new TableColumn<>("Phone"); c2.setCellValueFactory(new PropertyValueFactory<>("phone"));
        TableColumn<Buyer, Double> c3 = new TableColumn<>("Budget"); c3.setCellValueFactory(new PropertyValueFactory<>("budget"));
        TableColumn<Buyer, String> c4 = new TableColumn<>("Location"); c4.setCellValueFactory(new PropertyValueFactory<>("location"));
        table.getColumns().addAll(c1, c2, c3, c4);
        return table;
    }

    private TableView<Visit> createVisitTable() {
        TableView<Visit> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<Visit, String> c1 = new TableColumn<>("Date"); c1.setCellValueFactory(new PropertyValueFactory<>("date"));
        TableColumn<Visit, String> c2 = new TableColumn<>("Buyer"); c2.setCellValueFactory(new PropertyValueFactory<>("buyerName"));
        TableColumn<Visit, String> c3 = new TableColumn<>("Land"); c3.setCellValueFactory(new PropertyValueFactory<>("landLocation"));
        TableColumn<Visit, String> c4 = new TableColumn<>("Status"); c4.setCellValueFactory(new PropertyValueFactory<>("status"));
        table.getColumns().addAll(c1, c2, c3, c4);
        return table;
    }

    private TableView<Sale> createSaleTable() {
        TableView<Sale> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<Sale, String> c1 = new TableColumn<>("Date"); c1.setCellValueFactory(new PropertyValueFactory<>("date"));
        TableColumn<Sale, String> c2 = new TableColumn<>("Land"); c2.setCellValueFactory(new PropertyValueFactory<>("landLocation"));
        TableColumn<Sale, String> c3 = new TableColumn<>("Buyer"); c3.setCellValueFactory(new PropertyValueFactory<>("buyerName"));
        TableColumn<Sale, Double> c4 = new TableColumn<>("Total"); c4.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        TableColumn<Sale, Double> c5 = new TableColumn<>("Paid"); c5.setCellValueFactory(new PropertyValueFactory<>("paidAmount"));
        TableColumn<Sale, Double> c6 = new TableColumn<>("Balance"); c6.setCellValueFactory(new PropertyValueFactory<>("remainingBalance"));
        TableColumn<Sale, String> c7 = new TableColumn<>("Method"); c7.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        table.getColumns().addAll(c1, c2, c3, c4, c5, c6, c7);
        return table;
    }

    private void loadMyBuyers(ComboBox<Buyer> box) {
        ObservableList<Buyer> list = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM buyers WHERE agent_id = ?")) {
            stmt.setInt(1, getAgentId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapBuyer(rs));
        } catch (Exception e) {}
        box.setItems(list);
    }

    private void loadMyAvailableLands(ComboBox<Land> box) {
        ObservableList<Land> list = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM lands WHERE status='AVAILABLE' AND agent_id = ?")) {
            stmt.setInt(1, getAgentId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapLand(rs));
        } catch (Exception e) {}
        box.setItems(list);
    }

    private Land mapLand(ResultSet rs) throws SQLException {
        return new Land(rs.getInt("land_id"), rs.getString("location"), rs.getDouble("area_sqm"), rs.getDouble("price"), rs.getString("category"), rs.getString("status"));
    }

    private Buyer mapBuyer(ResultSet rs) throws SQLException {
        return new Buyer(rs.getInt("buyer_id"), rs.getString("name"), rs.getString("phone"), rs.getDouble("budget_max"), rs.getString("preferred_location"));
    }

    private VBox createStatCard(String title, String value, String color) {
        VBox c = new VBox(5); c.setStyle("-fx-background-color: " + color + "; -fx-padding: 15; -fx-background-radius: 5;"); c.setPrefWidth(150);
        Label v = new Label(value); v.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        Label t = new Label(title); t.setStyle("-fx-text-fill: white;"); c.getChildren().addAll(v, t); return c;
    }
    
    private String getCount(String sql) {
        try (Connection conn = DBConnection.getConnection(); ResultSet rs = conn.createStatement().executeQuery(sql)) {
            if (rs.next()) return String.valueOf(rs.getInt(1));
        } catch (Exception e) {} return "0";
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