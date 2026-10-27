
package com.subwise.customer;

import com.subwise.dao.PaymentDAO;
import com.subwise.model.Payment;
import com.subwise.model.Subscription;
import com.subwise.utils.SubscriptionStore;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.StringConverter;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class PaymentHistoryController {

    private static final String BG = "#0f172a";
    private static final String CARD = "#1e293b";
    private static final String MUTED = "#94a3b8";
    private static final String PURPLE = "#6366f1";
    private static final String GREEN = "#86efac";
    private static final String RED = "#fca5a5";
    private static final String WHITE = "#f8fafc";

    private final PaymentDAO paymentDAO = new PaymentDAO();

    private final ObservableList<Payment> paymentList =
            FXCollections.observableArrayList();

    private final TableView<Payment> table = new TableView<>();
    private final ComboBox<Subscription> subscriptionBox =
            new ComboBox<>();
    private final Label messageLabel = new Label();
    private final Label countLabel = new Label();

    public void show(Stage stage) {

        // Page heading
        Label title = new Label("Payment History");
        title.setFont(Font.font("Arial", 28));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label(
                "View and record your subscription payments");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setTextFill(Color.web(MUTED));

        VBox header = new VBox(7, title, subtitle);

        // Payment count
        countLabel.setFont(Font.font("Arial", 13));
        countLabel.setTextFill(Color.web(MUTED));

        // Table columns
        TableColumn<Payment, Integer> idColumn =
                new TableColumn<>("Payment ID");
        idColumn.setCellValueFactory(
                new PropertyValueFactory<>("paymentId"));
        idColumn.setPrefWidth(100);

        TableColumn<Payment, String> subscriptionColumn =
                new TableColumn<>("Subscription");
        subscriptionColumn.setCellValueFactory(
                new PropertyValueFactory<>("serviceName"));
        subscriptionColumn.setPrefWidth(180);

        TableColumn<Payment, Double> amountColumn =
                new TableColumn<>("Amount (₹)");
        amountColumn.setCellValueFactory(
                new PropertyValueFactory<>("amount"));
        amountColumn.setPrefWidth(140);

        // Display amounts with two decimal places
        amountColumn.setCellFactory(column ->
                new javafx.scene.control.TableCell<Payment, Double>() {
                    @Override
                    protected void updateItem(Double amount,
                                               boolean empty) {
                        super.updateItem(amount, empty);

                        if (empty || amount == null) {
                            setText(null);
                        } else {
                            setText(String.format(
                                    Locale.US, "%.2f", amount));
                        }
                    }
                });

        TableColumn<Payment, LocalDate> dateColumn =
                new TableColumn<>("Payment Date");
        dateColumn.setCellValueFactory(
                new PropertyValueFactory<>("paymentDate"));
        dateColumn.setPrefWidth(150);

        TableColumn<Payment, String> statusColumn =
                new TableColumn<>("Status");
        statusColumn.setCellValueFactory(
                new PropertyValueFactory<>("status"));
        statusColumn.setPrefWidth(130);

        table.getColumns().setAll(java.util.List.of(
                idColumn,
                subscriptionColumn,
                amountColumn,
                dateColumn,
                statusColumn
        ));

        table.setItems(paymentList);
        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        // Show a message instead of blank rows when there are no records
        Label emptyLabel = new Label("No payment records found.");
        emptyLabel.setTextFill(Color.web(MUTED));
        emptyLabel.setFont(Font.font("Arial", 14));
        table.setPlaceholder(emptyLabel);

        table.setFixedCellSize(42);
        table.setPrefHeight(330);
        table.setMinHeight(220);

        table.setStyle(
                "-fx-background-color: " + CARD + ";" +
                "-fx-control-inner-background: " + CARD + ";" +
                "-fx-text-background-color: " + WHITE + ";" +
                "-fx-border-color: #334155;" +
                "-fx-selection-bar: #334155;" +
                "-fx-selection-bar-non-focused: #334155;"
        );

        VBox tableCard = new VBox(12, countLabel, table);
        tableCard.setPadding(new Insets(18));
        tableCard.setStyle(
                "-fx-background-color: " + CARD + ";" +
                "-fx-background-radius: 12;"
        );

        VBox.setVgrow(table, Priority.ALWAYS);

        // Record payment section
        Label recordTitle = new Label("Record a Payment");
        recordTitle.setFont(Font.font("Arial", 20));
        recordTitle.setTextFill(Color.WHITE);

        Label recordSubtitle = new Label(
                "Choose a subscription to create a payment record.");
        recordSubtitle.setFont(Font.font("Arial", 13));
        recordSubtitle.setTextFill(Color.web(MUTED));

        subscriptionBox.setPromptText("Choose a subscription");
        subscriptionBox.setPrefWidth(320);
        subscriptionBox.setMaxWidth(400);

        subscriptionBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Subscription subscription) {
                if (subscription == null) {
                    return "";
                }

                return subscription.getServiceName()
                        + " (₹"
                        + String.format(
                                Locale.US, "%.2f",
                                subscription.getPrice())
                        + ")";
            }

            @Override
            public Subscription fromString(String text) {
                return null;
            }
        });

        Button recordButton = new Button("Record Payment");
        recordButton.setStyle(
                "-fx-background-color: " + PURPLE + ";" +
                "-fx-text-fill: white;" +
                "-fx-padding: 12 20;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;"
        );

        Button refreshButton = new Button("Refresh");
        refreshButton.setStyle(
                "-fx-background-color: #334155;" +
                "-fx-text-fill: white;" +
                "-fx-padding: 12 20;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;"
        );

        messageLabel.setWrapText(true);
        messageLabel.setFont(Font.font("Arial", 13));

        recordButton.setOnAction(event -> recordPayment());

        refreshButton.setOnAction(event -> {
            loadSubscriptions();
            loadPayments();
        });

        HBox recordActions = new HBox(
                12, subscriptionBox, recordButton, refreshButton);
        recordActions.setAlignment(Pos.CENTER_LEFT);
        recordActions.setPadding(new Insets(4, 0, 4, 0));

        VBox recordCard = new VBox(
                12, recordTitle, recordSubtitle,
                recordActions, messageLabel);
        recordCard.setPadding(new Insets(22));
        recordCard.setStyle(
                "-fx-background-color: " + CARD + ";" +
                "-fx-background-radius: 12;"
        );

        // Back button
        Button backButton = new Button("Back to Dashboard");
        backButton.setStyle(
                "-fx-background-color: #334155;" +
                "-fx-text-fill: white;" +
                "-fx-padding: 12 20;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;"
        );

        backButton.setOnAction(event ->
                new DashboardController().show(stage));

        VBox content = new VBox(
                22, header, tableCard, recordCard, backButton);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + BG + ";");

        Scene scene = new Scene(content, 1000, 760);

        stage.setTitle("Subwise - Payment History");
        stage.setScene(scene);
        stage.show();

        loadSubscriptions();
        loadPayments();
    }

    private void loadPayments() {
        try {
                List<Payment> payments =
                        paymentDAO.getPaymentsForCurrentUser();

                paymentList.clear();
                paymentList.addAll(payments);

                int count = paymentList.size();

                countLabel.setText(
                        count + (count == 1
                                ? " payment record"
                                : " payment records"));

                if (count == 0) {
                setMessage("No payment records yet.", MUTED);
                } else {
                setMessage("Payment history loaded successfully.",
                        GREEN);
                }

        } catch (SQLException e) {
                setMessage("Could not load payment history.", RED);
                showError("Could not load payments: "
                        + e.getMessage());
        }
        }

    private void loadSubscriptions() {
        try {
            List<Subscription> subscriptions =
                    SubscriptionStore.getSubscriptions();

            subscriptionBox.setItems(
                    FXCollections.observableArrayList(subscriptions));

            if (subscriptions.isEmpty()) {
                subscriptionBox.setPromptText(
                        "No subscriptions available");
                setMessage(
                        "Add a subscription before recording a payment.",
                        MUTED);
            } else {
                subscriptionBox.setPromptText(
                        "Choose a subscription");
            }

        } catch (RuntimeException e) {
            showError("Could not load subscriptions: "
                    + e.getMessage());
        }
    }

    private void recordPayment() {
        Subscription selected = subscriptionBox.getValue();

        if (selected == null) {
            showError("Please select a subscription first.");
            return;
        }

        Alert confirmation =
                new Alert(Alert.AlertType.CONFIRMATION);

        confirmation.setTitle("Confirm Simulated Payment");
        confirmation.setHeaderText(
                "Record payment for "
                        + selected.getServiceName() + "?");

        confirmation.setContentText(
                "Amount: ₹"
                        + String.format(
                                Locale.US, "%.2f",
                                selected.getPrice())
                        + "\n\nThis creates a simulated SUCCESS "
                        + "payment record. No real money will be charged.");

        Optional<ButtonType> result =
                confirmation.showAndWait();

        if (result.isEmpty()
                || result.get() != ButtonType.OK) {
            return;
        }

        try {
            Payment payment = new Payment(
                    0,
                    selected.getSubscriptionId(),
                    selected.getPrice(),
                    LocalDate.now(),
                    "SUCCESS"
            );

            boolean saved = paymentDAO.addPayment(payment);

            if (saved) {
                setMessage(
                        "Payment record saved successfully.",
                        GREEN);

                subscriptionBox.getSelectionModel().clearSelection();
                loadPayments();

            } else {
                showError(
                        "Payment was not saved. "
                                + "Check the selected subscription.");
            }

        } catch (SQLException e) {
            showError("Could not record payment: "
                    + e.getMessage());
        }
    }

    private void setMessage(String message, String color) {
        messageLabel.setText(message);
        messageLabel.setTextFill(Color.web(color));
    }

    private void showError(String message) {
        setMessage(message, RED);

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Payment Error");
        alert.setHeaderText("Something went wrong");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
