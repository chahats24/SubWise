
package com.subwise.customer;

import com.subwise.dao.SubscriptionDAO;
import com.subwise.model.Subscription;
import com.subwise.utils.SubscriptionStore;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;
;


public class ManageSubscriptionsController {

    private static final String BG = "#0f172a";
    private static final String CARD = "#1e293b";
    private static final String PURPLE = "#6366f1";
    private static final String MUTED = "#94a3b8";

    private ListView<Subscription> subscriptionList;
    private ObservableList<Subscription> allSubscriptions;
    private FilteredList<Subscription> filteredSubscriptions;

    private TextField searchField;
    private ComboBox<String> categoryFilter;
    private ComboBox<String> statusFilter;
    private Label resultLabel;

    public void show(Stage stage) {

        Label title = new Label("Manage Subscriptions");
        title.setFont(Font.font("Arial", 28));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label(
            "Search, filter, edit or remove your subscriptions."
        );
        subtitle.setTextFill(Color.web(MUTED));
        subtitle.setFont(Font.font("Arial", 13));

        searchField = new TextField();
        searchField.setPromptText("Search by service name...");
        searchField.setStyle(
            "-fx-background-color: " + BG + ";" +
            "-fx-text-fill: white;" +
            "-fx-prompt-text-fill: " + MUTED + ";" +
            "-fx-border-color: #475569;" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;" +
            "-fx-padding: 10;"
        );

        categoryFilter = new ComboBox<>();
        categoryFilter.getItems().addAll(
            "All Categories", "Entertainment", "Music", "Streaming",
            "Productivity", "Gaming", "Utilities", "Education",
            "Health & Fitness", "Shopping", "Other"
        );
        categoryFilter.setValue("All Categories");
        styleCombo(categoryFilter);

        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll(
            "All Statuses", "Active", "Cancelled", "Expired"
        );
        statusFilter.setValue("All Statuses");
        styleCombo(statusFilter);

        Button resetButton = new Button("Reset Filters");
        styleButton(resetButton, "#475569");

        HBox filterRow = new HBox(
            10, searchField, categoryFilter, statusFilter, resetButton
        );
        filterRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        categoryFilter.setPrefWidth(170);
        statusFilter.setPrefWidth(145);

        // Load the current user's subscriptions from MySQL.
        allSubscriptions = FXCollections.observableArrayList(
            SubscriptionStore.getSubscriptions()
        );
        filteredSubscriptions = new FilteredList<>(
            allSubscriptions, subscription -> true
        );

        subscriptionList = new ListView<>(filteredSubscriptions);
        subscriptionList.setPlaceholder(
            new Label("No subscriptions match your filters.")
        );
        subscriptionList.setStyle(
            "-fx-background-color: " + CARD + ";" +
            "-fx-control-inner-background: " + CARD + ";" +
            "-fx-border-color: #334155;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;"
        );

        subscriptionList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Subscription subscription,
                                      boolean empty) {
                super.updateItem(subscription, empty);

                if (empty || subscription == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: " + CARD + ";");
                    return;
                }

                Label name = new Label(subscription.getServiceName());
                name.setTextFill(Color.WHITE);
                name.setFont(Font.font("Arial", 16));

                Label details = new Label(
                    subscription.getCategory() + "  •  "
                    + subscription.getBillingCycle() + "  •  ₹"
                    + String.format(
                        Locale.ROOT, "%.2f", subscription.getPrice()
                    )
                );
                details.setTextFill(Color.web(MUTED));
                details.setFont(Font.font("Arial", 12));

                Label status = new Label(
                    subscription.getStatus() == null
                        ? "Unknown" : subscription.getStatus()
                );
                status.setTextFill(Color.web("#c4b5fd"));
                status.setFont(Font.font("Arial", 12));

                VBox text = new VBox(6, name, details, status);
                text.setPadding(new Insets(8));

                setGraphic(text);
                setText(null);
                setStyle(
                    "-fx-background-color: " + CARD + ";" +
                    "-fx-padding: 6;"
                );
            }
        });

        resultLabel = new Label();
        resultLabel.setTextFill(Color.web(MUTED));
        resultLabel.setFont(Font.font("Arial", 12));

        Button editButton = new Button("Edit Selected");
        styleButton(editButton, PURPLE);

        Button deleteButton = new Button("Delete Selected");
        styleButton(deleteButton, "#dc2626");

        Button backButton = new Button("Back to Dashboard");
        styleButton(backButton, "#475569");

        HBox actionRow = new HBox(
            12, editButton, deleteButton, backButton
        );
        actionRow.setAlignment(Pos.CENTER_LEFT);

        searchField.textProperty().addListener(
            (observable, oldValue, newValue) -> applyFilters()
        );
        categoryFilter.valueProperty().addListener(
            (observable, oldValue, newValue) -> applyFilters()
        );
        statusFilter.valueProperty().addListener(
            (observable, oldValue, newValue) -> applyFilters()
        );

        resetButton.setOnAction(event -> {
            searchField.clear();
            categoryFilter.setValue("All Categories");
            statusFilter.setValue("All Statuses");
            applyFilters();
        });

        editButton.setOnAction(event -> {
            Subscription selected = subscriptionList
                .getSelectionModel().getSelectedItem();

            if (selected == null) {
                showMessage(
                    Alert.AlertType.WARNING,
                    "Select a Subscription",
                    "Please select a subscription to edit."
                );
                return;
            }

            if (showEditDialog(stage, selected)) {
                try {
                    boolean updated =
                        new SubscriptionDAO().updateSubscription(selected);

                    if (updated) {
                        refreshSubscriptions();
                        showMessage(
                            Alert.AlertType.INFORMATION,
                            "Updated",
                            "Subscription updated successfully."
                        );
                    } else {
                        showMessage(
                            Alert.AlertType.ERROR,
                            "Update Failed",
                            "The subscription could not be updated."
                        );
                        refreshSubscriptions();
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    showMessage(
                        Alert.AlertType.ERROR,
                        "Database Error",
                        "Could not update the subscription."
                    );
                    refreshSubscriptions();
                }
            }
        });

        deleteButton.setOnAction(event -> {
            Subscription selected = subscriptionList
                .getSelectionModel().getSelectedItem();

            if (selected == null) {
                showMessage(
                    Alert.AlertType.WARNING,
                    "Select a Subscription",
                    "Please select a subscription to delete."
                );
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.initOwner(stage);
            confirm.initModality(Modality.APPLICATION_MODAL);
            confirm.setTitle("Delete Subscription");
            confirm.setHeaderText(
                "Delete " + selected.getServiceName() + "?"
            );
            confirm.setContentText(
                "This action cannot be undone."
            );

            Optional<ButtonType> answer = confirm.showAndWait();

            if (answer.isPresent()
                    && answer.get() == ButtonType.OK) {
                try {
                    boolean deleted = new SubscriptionDAO()
                        .deleteSubscription(
                            selected.getSubscriptionId()
                        );

                    if (deleted) {
                        refreshSubscriptions();
                        showMessage(
                            Alert.AlertType.INFORMATION,
                            "Deleted",
                            "Subscription deleted successfully."
                        );
                    } else {
                        showMessage(
                            Alert.AlertType.ERROR,
                            "Delete Failed",
                            "Subscription was not deleted."
                        );
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    showMessage(
                        Alert.AlertType.ERROR,
                        "Database Error",
                        "Could not delete the subscription."
                    );
                }
            }
        });

        backButton.setOnAction(event ->
            new DashboardController().show(stage)
        );

        VBox card = new VBox(
            16, title, subtitle, filterRow, resultLabel,
            subscriptionList, actionRow
        );
        card.setPadding(new Insets(28));
        card.setStyle(
            "-fx-background-color: " + CARD + ";" +
            "-fx-background-radius: 14;"
        );
        VBox.setVgrow(subscriptionList, Priority.ALWAYS);

        StackPane root = new StackPane(card);
        root.setPadding(new Insets(28));
        root.setStyle("-fx-background-color: " + BG + ";");

        Scene scene = new Scene(root, 1000, 680);
        stage.setTitle("Subwise - Manage Subscriptions");
        stage.setScene(scene);
        stage.show();

        applyFilters();
    }

    private void refreshSubscriptions() {
        allSubscriptions.setAll(SubscriptionStore.getSubscriptions());
        applyFilters();
    }

    private void applyFilters() {
        if (filteredSubscriptions == null) {
            return;
        }

        String search = searchField.getText() == null
            ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);

        String category = categoryFilter.getValue();
        String status = statusFilter.getValue();

        filteredSubscriptions.setPredicate(subscription -> {
            boolean matchesSearch =
                subscription.getServiceName() != null
                && subscription.getServiceName()
                    .toLowerCase(Locale.ROOT).contains(search);

            boolean matchesCategory =
                category == null
                || category.equals("All Categories")
                || (subscription.getCategory() != null
                && subscription.getCategory().equalsIgnoreCase(category));

            boolean matchesStatus =
                status == null
                || status.equals("All Statuses")
                || (subscription.getStatus() != null
                && subscription.getStatus().equalsIgnoreCase(status));

            return matchesSearch && matchesCategory && matchesStatus;
        });

        resultLabel.setText(
            "Showing " + filteredSubscriptions.size()
            + " of " + allSubscriptions.size() + " subscriptions"
        );
    }

    private boolean showEditDialog(Stage owner,
                                   Subscription subscription) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Edit Subscription");
        dialog.setHeaderText("Update subscription details");

        TextField nameField = new TextField(subscription.getServiceName());

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll(
            "Entertainment", "Music", "Streaming", "Productivity",
            "Gaming", "Utilities", "Education", "Health & Fitness",
            "Shopping", "Other"
        );
        categoryBox.setValue(subscription.getCategory());
        styleCombo(categoryBox);

        TextField priceField = new TextField(
            String.valueOf(subscription.getPrice())
        );

        ComboBox<String> billingBox = new ComboBox<>();
        billingBox.getItems().addAll("Monthly", "Yearly");
        billingBox.setValue(
            subscription.getBillingCycle() == null ? null
                : subscription.getBillingCycle().substring(0, 1)
                    .toUpperCase(Locale.ROOT)
                + subscription.getBillingCycle().substring(1).toLowerCase(Locale.ROOT)
        );
        styleCombo(billingBox);

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("Active", "Cancelled", "Expired");
        statusBox.setValue(
            subscription.getStatus() == null ? "Active"
                : subscription.getStatus().substring(0, 1)
                    .toUpperCase(Locale.ROOT)
                + subscription.getStatus().substring(1).toLowerCase(Locale.ROOT)
        );
        styleCombo(statusBox);

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.web("#f87171"));
        errorLabel.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(18));

        grid.add(new Label("Service Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(categoryBox, 1, 1);
        grid.add(new Label("Price (₹):"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("Billing Cycle:"), 0, 3);
        grid.add(billingBox, 1, 3);
        grid.add(new Label("Status:"), 0, 4);
        grid.add(statusBox, 1, 4);
        grid.add(errorLabel, 0, 5, 2, 1);

        for (javafx.scene.Node node : grid.getChildren()) {
            if (node instanceof Label label && label != errorLabel) {
                label.setTextFill(Color.WHITE);
            }
            if (node instanceof TextField field) {
                field.setStyle(
                    "-fx-control-inner-background: " + BG + ";" +
                    "-fx-text-fill: white;" +
                    "-fx-prompt-text-fill: " + MUTED + ";"
                );
            }
        }

        DialogPane pane = dialog.getDialogPane();
        pane.setContent(grid);
        pane.getButtonTypes().addAll(
            ButtonType.CANCEL,
            new ButtonType("Save Changes", ButtonBar.ButtonData.OK_DONE)
        );
        pane.setStyle(
            "-fx-background-color: " + CARD + ";" +
            "-fx-border-color: #334155;"
        );

        ButtonType saveType = pane.getButtonTypes().get(1);
        Button saveButton = (Button) pane.lookupButton(saveType);

        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            String name = nameField.getText().trim();
            String priceText = priceField.getText().trim();

            if (name.isEmpty() || categoryBox.getValue() == null
                    || billingBox.getValue() == null
                    || statusBox.getValue() == null
                    || priceText.isEmpty()) {
                errorLabel.setText("Please fill in all fields.");
                event.consume();
                return;
            }

            try {
                double price = Double.parseDouble(priceText);
                if (!Double.isFinite(price) || price <= 0) {
                    errorLabel.setText("Price must be a positive number.");
                    event.consume();
                }
            } catch (NumberFormatException ex) {
                errorLabel.setText("Enter a valid price, for example 199.");
                event.consume();
            }
        });

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == saveType) {
            subscription.setServiceName(nameField.getText().trim());
            subscription.setCategory(categoryBox.getValue());
            subscription.setPrice(
                Double.parseDouble(priceField.getText().trim())
            );
            subscription.setBillingCycle(billingBox.getValue());
            subscription.setStatus(statusBox.getValue());
            return true;
        }

        return false;
    }

    private void styleCombo(ComboBox<String> combo) {
        combo.setStyle(
            "-fx-background-color: " + BG + ";" +
            "-fx-mark-color: white;" +
            "-fx-border-color: #475569;" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;"
        );

        combo.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setTextFill(Color.WHITE);
                setStyle("-fx-background-color: " + CARD + ";");
            }
        });

        combo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setTextFill(Color.WHITE);
                setStyle("-fx-background-color: " + BG + ";");
            }
        });
    }

    private void styleButton(Button button, String color) {
        button.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 7;" +
            "-fx-padding: 10 16 10 16;" +
            "-fx-cursor: hand;"
        );
    }

    private void showMessage(Alert.AlertType type,
                             String title,
                             String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
