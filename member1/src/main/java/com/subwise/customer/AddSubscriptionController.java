
package com.subwise.customer;

import com.subwise.dao.SubscriptionDAO;
import com.subwise.model.Subscription;
import com.subwise.utils.UserStore;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;

public class AddSubscriptionController {

    private static final String BG = "#0f172a";
    private static final String CARD = "#1e293b";
    private static final String MUTED = "#94a3b8";
    private static final String PURPLE = "#6366f1";

    public void show(Stage stage) {

        Label title = new Label("Add Subscription");
        title.setFont(Font.font("Arial", 28));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label(
            "Add a service to keep track of your spending"
        );
        subtitle.setTextFill(Color.web(MUTED));

        TextField nameField = new TextField();
        nameField.setPromptText("e.g. Netflix");
        styleTextField(nameField);

        Label categoryLabel = createLabel("Subscription Category");

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll(
            "Entertainment",
            "Music",
            "Streaming",
            "Productivity",
            "Gaming",
            "Utilities",
            "Education",
            "Health & Fitness",
            "Shopping",
            "Other"
        );
        categoryBox.setPromptText("Choose a category");
        styleCombo(categoryBox);

        Label priceLabel = createLabel("Price (₹ INR)");

        TextField priceField = new TextField();
        priceField.setPromptText("Enter price in rupees");
        styleTextField(priceField);

        Label currencyLabel = new Label(
            "All prices are in Indian Rupees (₹)"
        );
        currencyLabel.setTextFill(Color.web(MUTED));
        currencyLabel.setFont(Font.font(12));

        Label billingLabel = createLabel("Billing Cycle");

        ComboBox<String> billingBox = new ComboBox<>();
        billingBox.getItems().addAll("Monthly", "Yearly");
        billingBox.setPromptText("Choose billing cycle");
        styleCombo(billingBox);

        Label dateLabel = createLabel("Subscription Start Date");

        DatePicker startDatePicker = new DatePicker(LocalDate.now());
        startDatePicker.setMaxWidth(Double.MAX_VALUE);
        styleDatePicker(startDatePicker);

        Label endDateLabel = createLabel("End Date (Optional)");

        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setPromptText("Select end date (optional)");
        endDatePicker.setMaxWidth(Double.MAX_VALUE);
        styleDatePicker(endDatePicker);

        Label message = new Label();
        message.setWrapText(true);
        message.setMaxWidth(Double.MAX_VALUE);

        Button saveButton = new Button("Save Subscription");
        saveButton.setMaxWidth(Double.MAX_VALUE);
        saveButton.setStyle(
            "-fx-background-color: " + PURPLE + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 13;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );

        Button backButton = new Button("Back to Dashboard");
        backButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setStyle(
            "-fx-background-color: #334155;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 12;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );

        nameField.setMaxWidth(Double.MAX_VALUE);
        priceField.setMaxWidth(Double.MAX_VALUE);

        VBox form = new VBox(
            10,
            title,
            subtitle,
            spacer(8),
            createLabel("Service Name"),
            nameField,
            categoryLabel,
            categoryBox,
            priceLabel,
            priceField,
            currencyLabel,
            billingLabel,
            billingBox,
            dateLabel,
            startDatePicker,
            endDateLabel,
            endDatePicker,
            spacer(5),
            saveButton,
            backButton,
            message
        );

        form.setAlignment(Pos.CENTER_LEFT);
        form.setPadding(new Insets(30));
        form.setMaxWidth(460);
        form.setStyle(
            "-fx-background-color: " + CARD + ";" +
            "-fx-background-radius: 18;"
        );

        StackPane root = new StackPane(form);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: " + BG + ";");

        saveButton.setOnAction(event -> {

            String name = nameField.getText().trim();
            String category = categoryBox.getValue();
            String priceText = priceField.getText().trim();
            String billing = billingBox.getValue();

            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            message.setTextFill(Color.SALMON);

            // Check required fields
            if (name.isEmpty()
                    || category == null
                    || priceText.isEmpty()
                    || billing == null
                    || startDate == null) {

                message.setText(
                    "Please fill in all required fields."
                );
                return;
            }

            // Validate dates
            if (endDate != null && endDate.isBefore(startDate)) {
                message.setText(
                    "End date cannot be before the start date."
                );
                return;
            }

            // Ensure a user is logged in
            if (UserStore.getCurrentUser() == null) {
                message.setText(
                    "Please log in before adding a subscription."
                );
                return;
            }

            try {
                double price = Double.parseDouble(priceText);

                if (!Double.isFinite(price) || price <= 0) {
                    message.setText(
                        "Enter a price greater than zero."
                    );
                    return;
                }

                // Create subscription for the logged-in user
                Subscription subscription = new Subscription(
                    0,
                    (int) UserStore.getCurrentUser().getUserId(),
                    name,
                    category,
                    price,
                    billing,
                    startDate,
                    endDate,
                    "ACTIVE"
                );

                // Save subscription to MySQL
                boolean saved =
                    new SubscriptionDAO().addSubscription(subscription);

                if (saved) {
                    message.setTextFill(Color.LIGHTGREEN);
                    message.setText(
                        "Subscription added successfully!"
                    );

                    // Clear fields after successful save
                    nameField.clear();
                    categoryBox.setValue(null);
                    priceField.clear();
                    billingBox.setValue(null);
                    startDatePicker.setValue(LocalDate.now());
                    endDatePicker.setValue(null);
                } else {
                    message.setText(
                        "Subscription could not be saved."
                    );
                }

            } catch (NumberFormatException ex) {
                message.setTextFill(Color.SALMON);
                message.setText(
                    "Enter a valid price using numbers only."
                );

            } catch (SQLException ex) {
                ex.printStackTrace();
                message.setTextFill(Color.SALMON);
                message.setText(
                    "Database error. Please check your connection and category."
                );
            }
        });

        backButton.setOnAction(event ->
            new DashboardController().show(stage)
        );

        stage.setTitle("Subwise - Add Subscription");
        stage.setScene(new Scene(root, 900, 760));
        stage.show();
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.WHITE);
        label.setFont(Font.font("Arial", 14));
        return label;
    }

    private Region spacer(double height) {
        Region region = new Region();
        region.setMinHeight(height);
        region.setPrefHeight(height);
        return region;
    }

    private void styleTextField(TextField field) {
        field.setStyle(
            "-fx-background-color: " + BG + ";" +
            "-fx-text-fill: white;" +
            "-fx-prompt-text-fill: " + MUTED + ";" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 12;"
        );
    }

    private void styleDatePicker(DatePicker picker) {
        picker.setStyle(
            "-fx-background-color: " + BG + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: #334155;" +
            "-fx-border-radius: 8;"
        );
    }

    private void styleCombo(ComboBox<String> combo) {
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setPrefHeight(42);

        combo.setStyle(
            "-fx-background-color: " + BG + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: #334155;" +
            "-fx-border-radius: 8;"
        );

        combo.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                setText(
                    empty || item == null
                        ? combo.getPromptText()
                        : item
                );

                setTextFill(
                    empty || item == null
                        ? Color.web(MUTED)
                        : Color.WHITE
                );

                setStyle(
                    "-fx-background-color: " + BG + ";" +
                    "-fx-padding: 6;"
                );
            }
        });

        combo.setCellFactory(list -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                setText(empty ? null : item);
                setTextFill(Color.WHITE);

                setStyle(
                    "-fx-background-color: " + CARD + ";" +
                    "-fx-padding: 10;"
                );

                if (!empty) {
                    setOnMouseEntered(e ->
                        setStyle(
                            "-fx-background-color: " + PURPLE + ";" +
                            "-fx-text-fill: white;" +
                            "-fx-padding: 10;"
                        )
                    );

                    setOnMouseExited(e ->
                        setStyle(
                            "-fx-background-color: " + CARD + ";" +
                            "-fx-text-fill: white;" +
                            "-fx-padding: 10;"
                        )
                    );
                }
            }
        });
    }
}
