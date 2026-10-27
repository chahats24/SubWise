
package com.subwise.member3;

import com.subwise.dao.SubscriptionDAO;

import com.subwise.model.Subscription;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminDashboardController {

    private static final String BG = "#0f172a";
    private static final String CARD = "#1e293b";
    private static final String MUTED = "#94a3b8";
    private static final String PURPLE = "#7c3aed";

    private final ExpiryChecker expiryChecker = new ExpiryChecker();
    private final PaymentProcessor paymentProcessor = new PaymentProcessor();

    private boolean expiryMonitorStarted = false;

    private BarChart<String, Number> categoryChart;

    private final Label totalLabel = new Label("0");
    private final Label activeLabel = new Label("0");
    private final Label expiringLabel = new Label("0");
    private final Label monthlySpendLabel = new Label("₹0.00");

    private final TableView<Subscription> table = new TableView<>();
    private final ObservableList<Subscription> tableItems =
        FXCollections.observableArrayList();

    // Cache one database snapshot so all dashboard components
    // use the same set of records.
    private List<Subscription> allSubscriptions = new ArrayList<>();

    private final Label statusLabel =
        new Label("Connecting to the database...");

    

    public void show(Stage stage) {
        

        Label title = new Label("Subwise Admin Dashboard");
        title.setFont(Font.font("Arial", 28));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label(
            "Manage subscriptions, monitor expiry dates and review spending."
        );
        subtitle.setTextFill(Color.web(MUTED));

        Button refresh = button("Refresh data");
        refresh.setOnAction(e -> refreshDashboard());

        Button startMonitor = button("Start expiry monitor");
        startMonitor.setOnAction(e -> {
            if (!expiryMonitorStarted) {
                expiryChecker.start();
                expiryMonitorStarted = true;

                statusLabel.setText(
                    "Expiry monitor started. Checks run hourly."
                );
            } else {
                statusLabel.setText(
                    "Expiry monitor is already running."
                );
            }
        });

        Button checkNow = button("Check expiries now");
        checkNow.setOnAction(e -> expiryChecker.checkNow());
        Button paymentDemo = button("Simulate Payment (Demo)");
        paymentDemo.setOnAction(e -> {
            Subscription selected =
                table.getSelectionModel().getSelectedItem();

            if (selected == null) {
                showMessage(
                    Alert.AlertType.WARNING,
                    "Select a subscription first."
                );
                return;
            }

            try {
                paymentProcessor.processDemoPayment(
                    selected.getPrice(),
                    this::refreshDashboard
                );
            } catch (IllegalArgumentException ex) {
                showMessage(
                    Alert.AlertType.ERROR,
                    ex.getMessage()
                );
            }
        });

        Button logout = button("Log out");
        logout.setOnAction(e -> {
            closeResources();
            new AdminLoginController().show(stage);
        });

        HBox actions = new HBox(
            10,
            refresh,
            startMonitor,
            checkNow,
            paymentDemo,
            logout
        );
        actions.setAlignment(Pos.CENTER_LEFT);
        

        HBox metrics = new HBox(
            14,
            metricCard("Subscriptions tracked", totalLabel),
            metricCard("Active subscriptions", activeLabel),
            metricCard("Expiring in 7 days", expiringLabel),
            metricCard("Monthly equivalent spend", monthlySpendLabel)
        );
        metrics.setAlignment(Pos.CENTER_LEFT);

        for (var node : metrics.getChildren()) {
            HBox.setHgrow(node, Priority.ALWAYS);
        }

        configureTable();

        TextField search = new TextField();
        search.setPromptText(
            "Search service, category, status or user ID..."
        );

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll(
            "All statuses",
            "Active",
            "Cancelled",
            "Expired"
        );
        statusFilter.setValue("All statuses");

        search.textProperty().addListener(
            (obs, oldValue, newValue) ->
                applyFilters(newValue, statusFilter.getValue())
        );

        statusFilter.setOnAction(e ->
            applyFilters(search.getText(), statusFilter.getValue())
        );

        HBox filters = new HBox(12, search, statusFilter);
        filters.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(search, Priority.ALWAYS);

        Label tableTitle = new Label("Subscription Management");
        tableTitle.setTextFill(Color.WHITE);
        tableTitle.setFont(Font.font("Arial", 20));

        categoryChart = createCategoryChart();

        Label chartTitle =
            new Label("Monthly-equivalent spend by category");
        chartTitle.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;"
        );

        VBox chartCard = new VBox(10, chartTitle, categoryChart);
        chartCard.setPadding(new Insets(16));
        chartCard.setStyle(
            "-fx-background-color: " + CARD + ";" +
            "-fx-background-radius: 12;"
        );

        statusLabel.setTextFill(Color.web(MUTED));
        statusLabel.setWrapText(true);

        VBox root = new VBox(
            18,
            title,
            subtitle,
            actions,
            metrics,
            tableTitle,
            filters,
            table,
            chartCard,
            statusLabel
        );

        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: " + BG + ";");

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle(
            "-fx-background: " + BG + ";" +
            "-fx-background-color: " + BG + ";"
        );

        refreshDashboard();

        stage.setTitle("Subwise - Admin Dashboard");
        stage.setScene(new Scene(scroll, 1200, 850));

        stage.setOnCloseRequest(e -> closeResources());

        stage.show();
    }

    private void configureTable() {
        table.setItems(tableItems);
        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );
        table.setPrefHeight(300);

        addColumn("ID", "subscriptionId", 55);
        addColumn("User ID", "userId", 75);
        addColumn("Service", "serviceName", 150);
        addColumn("Category", "category", 120);
        addColumn("Price", "price", 90);
        addColumn("Billing cycle", "billingCycle", 110);
        addColumn("Expiry date", "endDate", 110);
        addColumn("Status", "status", 100);
    }

    private void addColumn(
            String heading,
            String property,
            double width) {

        TableColumn<Subscription, Object> column =
            new TableColumn<>(heading);

        column.setCellValueFactory(
            new PropertyValueFactory<>(property)
        );
        column.setPrefWidth(width);

        table.getColumns().add(column);
    }

    private void refreshDashboard() {

        // PaymentProcessor may call the callback from another thread.
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::refreshDashboard);
            return;
        }

        try {
            // Admin-only database query: retrieves subscriptions
            // across users, unlike SubscriptionStore.getSubscriptions().
            List<Subscription> records =
                new SubscriptionDAO().getAllSubscriptionsForAdmin();

            allSubscriptions = new ArrayList<>(records);

            updateMetrics();
            tableItems.setAll(allSubscriptions);
            refreshCategoryChart();

            statusLabel.setText(
                "Connected to MySQL. Loaded "
                + allSubscriptions.size()
                + " subscription record(s) across users."
            );

        } catch (SQLException ex) {
            statusLabel.setText(
                "Database error: " + ex.getMessage()
            );

            showMessage(
                Alert.AlertType.ERROR,
                "Could not load subscriptions from MySQL.\n\n"
                + ex.getMessage()
            );
        }
    }

    private void updateMetrics() {
        LocalDate today = LocalDate.now();

        long active = allSubscriptions.stream()
            .filter(s -> s != null)
            .filter(s -> "Active".equalsIgnoreCase(s.getStatus()))
            .filter(s ->
                s.getEndDate() == null
                || !s.getEndDate().isBefore(today)
            )
            .count();

        long expiring = allSubscriptions.stream()
            .filter(s -> s != null)
            .filter(s -> "Active".equalsIgnoreCase(s.getStatus()))
            .filter(s -> s.getEndDate() != null)
            .filter(s -> {
                long days = ChronoUnit.DAYS.between(
                    today,
                    s.getEndDate()
                );
                return days >= 0 && days <= 7;
            })
            .count();

        totalLabel.setText(
            String.valueOf(allSubscriptions.size())
        );
        activeLabel.setText(String.valueOf(active));
        expiringLabel.setText(String.valueOf(expiring));

        double monthlySpend =
            ExpenseAnalyzer.activeMonthlySpend(allSubscriptions);

        monthlySpendLabel.setText(
            String.format(Locale.ROOT, "₹%.2f", monthlySpend)
        );
    }

    private void applyFilters(String query, String status) {
        String q = query == null
            ? ""
            : query.trim().toLowerCase(Locale.ROOT);

        String selectedStatus = status == null
            ? "All statuses"
            : status;

        List<Subscription> filtered = allSubscriptions.stream()
            .filter(s -> s != null)
            .filter(s ->
                q.isEmpty()
                || String.valueOf(s.getSubscriptionId()).contains(q)
                || String.valueOf(s.getUserId()).contains(q)
                || safe(s.getServiceName())
                    .toLowerCase(Locale.ROOT).contains(q)
                || safe(s.getCategory())
                    .toLowerCase(Locale.ROOT).contains(q)
                || safe(s.getStatus())
                    .toLowerCase(Locale.ROOT).contains(q)
            )
            .filter(s -> matchesStatus(s, selectedStatus))
            .toList();

        tableItems.setAll(filtered);
    }

    private boolean matchesStatus(
            Subscription subscription,
            String selectedStatus) {

        if ("All statuses".equalsIgnoreCase(selectedStatus)) {
            return true;
        }

        if ("Expired".equalsIgnoreCase(selectedStatus)) {
            return subscription.getEndDate() != null
                && subscription.getEndDate().isBefore(LocalDate.now());
        }

        return selectedStatus.equalsIgnoreCase(
            safe(subscription.getStatus())
        );
    }

    private BarChart<String, Number> createCategoryChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Category");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Monthly equivalent spend (₹)");
        yAxis.setForceZeroInRange(true);

        BarChart<String, Number> chart =
            new BarChart<>(xAxis, yAxis);

        chart.setTitle("");
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setPrefHeight(250);

        return chart;
    }

    private void refreshCategoryChart() {
        if (categoryChart == null) {
            return;
        }

        categoryChart.getData().clear();

        XYChart.Series<String, Number> series =
            new XYChart.Series<>();

        Map<String, Double> data =
            ExpenseAnalyzer.spendingByCategory(allSubscriptions);

        data.forEach((category, amount) ->
            series.getData().add(
                new XYChart.Data<>(category, amount)
            )
        );

        categoryChart.getData().add(series);
    }

    private VBox metricCard(String heading, Label value) {
        Label title = new Label(heading);
        title.setTextFill(Color.web(MUTED));

        value.setFont(Font.font("Arial", 24));
        value.setTextFill(Color.WHITE);

        VBox card = new VBox(12, title, value);
        card.setPadding(new Insets(18));
        card.setMinWidth(190);
        card.setStyle(
            "-fx-background-color: " + CARD + ";" +
            "-fx-background-radius: 12;"
        );

        return card;
    }

    private Button button(String text) {
        Button button = new Button(text);
        button.setStyle(
            "-fx-background-color: " + PURPLE + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;"
        );
        return button;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void showMessage(
            Alert.AlertType type,
            String message) {

        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeResources() {
        expiryChecker.close();
        paymentProcessor.close();
    }
}
