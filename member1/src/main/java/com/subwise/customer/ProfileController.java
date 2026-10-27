
package com.subwise.customer;

import com.subwise.model.User;
import com.subwise.utils.UserStore;
import com.subwise.utils.SubscriptionStore;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class ProfileController {

    private static final String BG = "#0f172a";
    private static final String CARD = "#1e293b";
    private static final String MUTED = "#94a3b8";
    private static final String PURPLE = "#6366f1";
    private static final String GREEN = "#86efac";

    public void show(Stage stage) {

        Label title = new Label("My Profile");
        title.setFont(Font.font("Arial", 28));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Manage your account details");
        subtitle.setTextFill(Color.web(MUTED));

        Label nameLabel = createLabel("Full Name");
        TextField nameField = createField("Enter your full name");

        Label emailLabel = createLabel("Email Address");
        TextField emailField = createField("Enter your email");

        Label message = new Label();
        message.setWrapText(true);
        message.setTextFill(Color.web(GREEN));

        // Load current user's details
        User user = UserStore.getCurrentUser();

        if (user != null) {
            nameField.setText(user.getName());
            emailField.setText(user.getEmail());
        } else {
            message.setText(
                    "No logged-in user found. Log in to load your profile."
            );
        }

        Button saveButton = new Button("Save Changes");
        saveButton.setMaxWidth(Double.MAX_VALUE);
        saveButton.setStyle(
                "-fx-background-color: " + PURPLE + ";" +
                "-fx-text-fill: white;" +
                "-fx-padding: 12;" +
                "-fx-background-radius: 8;"
        );

        saveButton.setOnAction(event -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();

            if (name.isEmpty() || email.isEmpty()) {
                message.setText("Please fill in both fields.");
                message.setTextFill(Color.web("#fca5a5"));
                return;
            }

            if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                message.setText("Please enter a valid email address.");
                message.setTextFill(Color.web("#fca5a5"));
                return;
            }

            boolean updated =
                    UserStore.updateCurrentUser(name, email);

            if (updated) {
                message.setText("Profile updated successfully!");
                message.setTextFill(Color.web(GREEN));
            } else {
                message.setText(
                        "Could not update profile. Check login or email."
                );
                message.setTextFill(Color.web("#fca5a5"));
            }
        });

        Label summaryTitle = createLabel("Subscription Summary");

        Label activeLabel = new Label(
                "Active subscriptions: "
                        + SubscriptionStore.getActiveCount()
        );
        activeLabel.setTextFill(Color.WHITE);

        Label spendingLabel = new Label(
                String.format(
                        java.util.Locale.ROOT,
                        "Monthly spending: ₹%.2f",
                        SubscriptionStore.getMonthlySpending()
                )
        );
        spendingLabel.setTextFill(Color.web(GREEN));
        spendingLabel.setFont(Font.font("Arial", 17));

        Button backButton = new Button("Back to Dashboard");
        backButton.setStyle(
                "-fx-background-color: #334155;" +
                "-fx-text-fill: white;" +
                "-fx-padding: 12 20;" +
                "-fx-background-radius: 8;"
        );

        backButton.setOnAction(event ->
                new DashboardController().show(stage)
        );

        VBox detailsCard = new VBox(
                12,
                nameLabel, nameField,
                emailLabel, emailField,
                saveButton, message
        );
        styleCard(detailsCard);

        VBox summaryCard = new VBox(
                14, summaryTitle, activeLabel, spendingLabel
        );
        styleCard(summaryCard);

        VBox content = new VBox(
                22, title, subtitle, detailsCard,
                summaryCard, backButton
        );
        content.setPadding(new Insets(30));
        content.setMaxWidth(600);

        VBox root = new VBox(content);
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: " + BG + ";");

        Scene scene = new Scene(root, 900, 700);
        stage.setTitle("Subwise - My Profile");
        stage.setScene(scene);
        stage.show();
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.WHITE);
        label.setFont(Font.font("Arial", 14));
        return label;
    }

    private TextField createField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setMaxWidth(Double.MAX_VALUE);
        field.setStyle(
                "-fx-background-color: #334155;" +
                "-fx-text-fill: white;" +
                "-fx-prompt-text-fill: #94a3b8;" +
                "-fx-background-radius: 7;" +
                "-fx-padding: 12;"
        );
        return field;
    }

    private void styleCard(VBox card) {
        card.setPadding(new Insets(24));
        card.setStyle(
                "-fx-background-color: " + CARD + ";" +
                "-fx-background-radius: 12;"
        );
    }
}
