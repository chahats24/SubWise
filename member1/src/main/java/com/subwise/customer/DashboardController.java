package com.subwise.customer;

import com.subwise.App;
import com.subwise.model.Subscription;
import com.subwise.model.User;
import com.subwise.utils.SubscriptionStore;
import com.subwise.utils.UserStore;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardController {

    private static final String BG = "#090E1D";
    private static final String CARD = "#151E31";
    private static final String BORDER = "#293650";
    private static final String PURPLE = "#7838F5";
    private static final String HOVER_PURPLE = "#9257FF";
    private static final String TEXT_PRIMARY = "#F1F5F9";
    private static final String TEXT_MUTED = "#94A3B8";
    private static final String GREEN = "#10B981";
    private static final String AMBER = "#F59E0B";
    private static final String RED = "#EF4444";

    public void show(Stage stage) {

        User currentUser = UserStore.getCurrentUser();
        String userName = (currentUser != null && currentUser.getName() != null)
                ? currentUser.getName() : "Member";

        // Brand & Header
        Label logo = new Label("SUBWISE");
        logo.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 26));
        logo.setTextFill(Color.web("#9257FF"));

        Label subtitle = new Label("Welcome back, " + userName + "! Here is your recurring expenses overview.");
        subtitle.setFont(Font.font("Arial", 13));
        subtitle.setTextFill(Color.web(TEXT_MUTED));

        VBox headerText = new VBox(4, logo, subtitle);

        // Action Buttons Row (Top Navigation)
        Button addButton = createPrimaryButton("+ Add Subscription");
        addButton.setOnAction(e -> new AddSubscriptionController().show(stage));

        Button manageButton = createSecondaryButton("Manage Subscriptions");
        manageButton.setOnAction(e -> new ManageSubscriptionsController().show(stage));

        Button paymentsButton = createSecondaryButton("Payment History");
        paymentsButton.setOnAction(e -> new PaymentHistoryController().show(stage));

        Button profileButton = createSecondaryButton("My Profile");
        profileButton.setOnAction(e -> new ProfileController().show(stage));

        Button logoutButton = createDangerButton("Log out");
        logoutButton.setOnAction(e -> {
            UserStore.setCurrentUser(null);
            App.showWelcome(stage);
        });

        HBox topActions = new HBox(10, addButton, manageButton, paymentsButton, profileButton, logoutButton);
        topActions.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(headerText, Priority.ALWAYS);

        HBox navBar = new HBox(16, headerText, topActions);
        navBar.setAlignment(Pos.CENTER_LEFT);
        navBar.setPadding(new Insets(16, 20, 16, 20));
        styleCard(navBar);

        // Metric Statistics Cards
        List<Subscription> subscriptions = SubscriptionStore.getSubscriptions();
        long activeCount = SubscriptionStore.getActiveCount();
        double monthlySpend = SubscriptionStore.getMonthlySpending();
        int totalCount = subscriptions.size();

        VBox spendCard = createMetricCard(
                "MONTHLY ESTIMATED SPEND",
                String.format(Locale.ROOT, "₹%.2f", monthlySpend),
                GREEN
        );
        VBox activeCard = createMetricCard(
                "ACTIVE SUBSCRIPTIONS",
                String.valueOf(activeCount),
                "#A855F7"
        );
        VBox totalCard = createMetricCard(
                "TOTAL TRACKED",
                String.valueOf(totalCount),
                TEXT_PRIMARY
        );

        HBox statsRow = new HBox(16, spendCard, activeCard, totalCard);
        statsRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(spendCard, Priority.ALWAYS);
        HBox.setHgrow(activeCard, Priority.ALWAYS);
        HBox.setHgrow(totalCard, Priority.ALWAYS);

        // Category Analytics (PieChart + Spending Progress Bars)
        Map<String, Double> categorySpending = SubscriptionStore.getCategoryWiseSpending();
        HBox analyticsSection = createAnalyticsSection(categorySpending);

        // Upcoming Expirations Section
        VBox expirySection = createExpirySection(subscriptions);

        // Subscriptions Overview List
        VBox recentSection = createSubscriptionsListSection(subscriptions, stage);

        // Main Layout Scroll Container
        VBox content = new VBox(20, navBar, statsRow, analyticsSection, expirySection, recentSection);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: " + BG + ";");

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: " + BG + "; -fx-background-color: " + BG + ";");

        Scene scene = new Scene(scrollPane, 1060, 750);
        stage.setTitle("SubWise - Customer Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    private HBox createAnalyticsSection(Map<String, Double> categorySpending) {
        // Pie Chart
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        if (categorySpending != null) {
            for (Map.Entry<String, Double> entry : categorySpending.entrySet()) {
                if (entry.getValue() > 0) {
                    pieData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
                }
            }
        }

        PieChart pieChart = new PieChart(pieData);
        pieChart.setTitle("Category Spending Share");
        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(true);
        pieChart.setAnimated(false);
        pieChart.setPrefHeight(270);
        pieChart.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-pie-label-visible: true;" +
            "-fx-text-fill: white;"
        );

        // Apply slice colors
        String[] sliceColors = {
            "#7838F5", "#3B82F6", "#10B981", "#F59E0B", "#EC4899", "#06B6D4", "#8B5CF6"
        };
        int cIdx = 0;
        for (PieChart.Data data : pieData) {
            String color = sliceColors[cIdx % sliceColors.length];
            if (data.getNode() != null) {
                data.getNode().setStyle("-fx-pie-color: " + color + ";");
            }
            cIdx++;
        }

        VBox chartCard = new VBox(10, pieChart);
        chartCard.setPadding(new Insets(16));
        styleCard(chartCard);
        HBox.setHgrow(chartCard, Priority.ALWAYS);

        // Progress List Card
        Label listTitle = new Label("Spending by Category");
        listTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        listTitle.setTextFill(Color.web(TEXT_PRIMARY));

        VBox progressList = new VBox(12);
        if (categorySpending == null || categorySpending.isEmpty()) {
            Label empty = new Label("No active subscriptions to show spending breakdown.");
            empty.setTextFill(Color.web(TEXT_MUTED));
            progressList.getChildren().add(empty);
        } else {
            double maxSpend = 0;
            for (double val : categorySpending.values()) {
                maxSpend = Math.max(maxSpend, val);
            }
            for (Map.Entry<String, Double> entry : categorySpending.entrySet()) {
                progressList.getChildren().add(createCategoryRow(entry.getKey(), entry.getValue(), maxSpend));
            }
        }

        VBox progressCard = new VBox(14, listTitle, progressList);
        progressCard.setPadding(new Insets(20));
        styleCard(progressCard);
        HBox.setHgrow(progressCard, Priority.ALWAYS);

        HBox section = new HBox(16, chartCard, progressCard);
        section.setAlignment(Pos.TOP_CENTER);
        return section;
    }

    private HBox createCategoryRow(String category, double amount, double maxSpending) {
        Label name = new Label(category);
        name.setTextFill(Color.web(TEXT_PRIMARY));
        name.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        name.setPrefWidth(120);

        ProgressBar bar = new ProgressBar(maxSpending > 0 ? amount / maxSpending : 0);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setPrefHeight(10);
        bar.setStyle("-fx-accent: " + PURPLE + "; -fx-control-inner-background: #1A263B;");
        HBox.setHgrow(bar, Priority.ALWAYS);

        Label amountLabel = new Label(String.format(Locale.ROOT, "₹%.2f", amount));
        amountLabel.setTextFill(Color.web(GREEN));
        amountLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        amountLabel.setPrefWidth(85);
        amountLabel.setAlignment(Pos.CENTER_RIGHT);

        HBox row = new HBox(12, name, bar, amountLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private VBox createExpirySection(List<Subscription> subscriptions) {
        Label title = new Label("Upcoming Expirations (Next 7 Days)");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setTextFill(Color.web(TEXT_PRIMARY));

        VBox expiryRows = new VBox(10);
        LocalDate today = LocalDate.now();
        int count = 0;

        if (subscriptions != null) {
            for (Subscription sub : subscriptions) {
                if (sub.getEndDate() == null || !"Active".equalsIgnoreCase(sub.getStatus())) {
                    continue;
                }
                long days = ChronoUnit.DAYS.between(today, sub.getEndDate());
                if (days >= 0 && days <= 7) {
                    expiryRows.getChildren().add(createExpiryRow(sub, days));
                    count++;
                }
            }
        }

        if (count == 0) {
            Label allSet = new Label("You're all set! No subscriptions are expiring in the next 7 days.");
            allSet.setTextFill(Color.web(TEXT_MUTED));
            allSet.setFont(Font.font("Arial", 13));
            expiryRows.getChildren().add(allSet);
        }

        VBox card = new VBox(14, title, expiryRows);
        card.setPadding(new Insets(20));
        styleCard(card);
        return card;
    }

    private HBox createExpiryRow(Subscription sub, long daysRemaining) {
        Label alertTag = new Label(daysRemaining <= 1 ? "URGENT" : "EXPIRING SOON");
        alertTag.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        alertTag.setPadding(new Insets(4, 8, 4, 8));
        alertTag.setStyle(
            "-fx-background-color: " + (daysRemaining <= 1 ? "rgba(239, 68, 68, 0.2)" : "rgba(245, 158, 11, 0.2)") + ";" +
            "-fx-text-fill: " + (daysRemaining <= 1 ? RED : AMBER) + ";" +
            "-fx-border-color: " + (daysRemaining <= 1 ? RED : AMBER) + ";" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;"
        );

        Label name = new Label(sub.getServiceName());
        name.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        name.setTextFill(Color.web(TEXT_PRIMARY));

        String remainingText = (daysRemaining == 0)
                ? "Expires today (" + sub.getEndDate() + ")"
                : (daysRemaining == 1)
                ? "Expires tomorrow (" + sub.getEndDate() + ")"
                : "Expires in " + daysRemaining + " days (" + sub.getEndDate() + ")";

        Label dateLabel = new Label(remainingText);
        dateLabel.setTextFill(Color.web(TEXT_MUTED));
        dateLabel.setFont(Font.font("Arial", 12));

        VBox info = new VBox(3, name, dateLabel);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label price = new Label(String.format(Locale.ROOT, "₹%.2f", sub.getPrice()));
        price.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        price.setTextFill(Color.web("#C084FC"));

        HBox row = new HBox(12, alertTag, info, price);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.setStyle(
            "-fx-background-color: #1A263B;" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: #334460;" +
            "-fx-border-radius: 8;"
        );
        return row;
    }

    private VBox createSubscriptionsListSection(List<Subscription> subscriptions, Stage stage) {
        Label title = new Label("Your Active Subscriptions");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setTextFill(Color.web(TEXT_PRIMARY));

        VBox list = new VBox(10);
        if (subscriptions == null || subscriptions.isEmpty()) {
            Label empty = new Label("No subscriptions tracked yet. Click '+ Add Subscription' above to start!");
            empty.setTextFill(Color.web(TEXT_MUTED));
            list.getChildren().add(empty);
        } else {
            for (Subscription sub : subscriptions) {
                list.getChildren().add(createSubscriptionItem(sub));
            }
        }

        VBox card = new VBox(14, title, list);
        card.setPadding(new Insets(20));
        styleCard(card);
        return card;
    }

    private HBox createSubscriptionItem(Subscription sub) {
        Label name = new Label(sub.getServiceName());
        name.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        name.setTextFill(Color.web(TEXT_PRIMARY));

        Label category = new Label(sub.getCategory() == null ? "General" : sub.getCategory());
        category.setFont(Font.font("Arial", 11));
        category.setPadding(new Insets(3, 8, 3, 8));
        category.setStyle("-fx-background-color: #25334D; -fx-text-fill: #CBD5E1; -fx-background-radius: 6;");

        VBox left = new VBox(4, name, category);
        HBox.setHgrow(left, Priority.ALWAYS);

        String cycle = sub.getBillingCycle() == null ? "monthly" : sub.getBillingCycle().toLowerCase();
        Label price = new Label(String.format(Locale.ROOT, "₹%.2f / %s", sub.getPrice(), cycle));
        price.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        price.setTextFill(Color.web("#C084FC"));

        String statusStr = sub.getStatus() == null ? "Active" : sub.getStatus();
        Label statusBadge = new Label(statusStr.toUpperCase());
        statusBadge.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        statusBadge.setPadding(new Insets(4, 8, 4, 8));
        if ("Active".equalsIgnoreCase(statusStr)) {
            statusBadge.setStyle("-fx-background-color: rgba(16, 185, 129, 0.16); -fx-text-fill: #10B981; -fx-background-radius: 12;");
        } else if ("Expired".equalsIgnoreCase(statusStr)) {
            statusBadge.setStyle("-fx-background-color: rgba(239, 68, 68, 0.16); -fx-text-fill: #EF4444; -fx-background-radius: 12;");
        } else {
            statusBadge.setStyle("-fx-background-color: rgba(148, 163, 184, 0.16); -fx-text-fill: #94A3B8; -fx-background-radius: 12;");
        }

        HBox row = new HBox(16, left, price, statusBadge);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 16, 12, 16));
        row.setStyle(
            "-fx-background-color: #1A263B;" +
            "-fx-background-radius: 9;" +
            "-fx-border-color: #2D3D58;" +
            "-fx-border-radius: 9;"
        );
        return row;
    }

    private VBox createMetricCard(String heading, String value, String valueColor) {
        Label title = new Label(heading);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        title.setTextFill(Color.web(TEXT_MUTED));

        Label val = new Label(value);
        val.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 26));
        val.setTextFill(Color.web(valueColor));

        VBox card = new VBox(6, title, val);
        card.setPadding(new Insets(18, 20, 18, 20));
        styleCard(card);
        return card;
    }

    private void styleCard(Pane card) {
        card.setStyle(
            "-fx-background-color: " + CARD + ";" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 14;"
        );
    }

    private Button createPrimaryButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        btn.setStyle("-fx-background-color: " + PURPLE + "; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + HOVER_PURPLE + "; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + PURPLE + "; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;"));
        return btn;
    }

    private Button createSecondaryButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", 13));
        btn.setStyle("-fx-background-color: #1A263B; -fx-text-fill: #CBD5E1; -fx-border-color: #394963; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 7 14; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #243550; -fx-text-fill: white; -fx-border-color: #4D6385; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 7 14; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #1A263B; -fx-text-fill: #CBD5E1; -fx-border-color: #394963; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 7 14; -fx-cursor: hand;"));
        return btn;
    }

    private Button createDangerButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", 13));
        btn.setStyle("-fx-background-color: rgba(239, 68, 68, 0.15); -fx-text-fill: #FCA5A5; -fx-border-color: #EF4444; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 7 14; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-border-color: #EF4444; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 7 14; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: rgba(239, 68, 68, 0.15); -fx-text-fill: #FCA5A5; -fx-border-color: #EF4444; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 7 14; -fx-cursor: hand;"));
        return btn;
    }
}
