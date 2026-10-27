
package com.subwise.customer;

import com.subwise.App;
import com.subwise.dao.UserDAO;
import com.subwise.utils.UserStore;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.SQLException;

public class LoginController {

    private static final String BG = "#090e1d";
    private static final String CARD = "#151e31";
    private static final String PURPLE = "#7838f5";
    private static final String BORDER = "#293650";

    public void show(Stage stage) {

        Label logo = new Label("SUBWISE");
        logo.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 48));
        logo.setTextFill(Color.web("#9257ff"));

        Label heading = new Label("Manage it all.");
        heading.setFont(Font.font("Arial", FontWeight.BOLD, 23));
        heading.setTextFill(Color.WHITE);

        Label title = new Label("Customer Login");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);

        // Email field
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        styleInput(emailField);

        // Password field
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        styleInput(passwordField);

        Label message = new Label();
        message.setWrapText(true);
        message.setMaxWidth(400);
        message.setStyle(
            "-fx-text-fill: #f87171;" +
            "-fx-font-size: 12px;"
        );

        // Login button
        Button loginButton = new Button("Login");
        stylePrimaryButton(loginButton);

        loginButton.setOnAction(event -> {
            String email = emailField.getText().trim();
            String password = passwordField.getText();

            if (email.isEmpty() || password.isEmpty()) {
                message.setText("Please enter email and password.");
                return;
            }

            try {
                UserDAO userDAO = new UserDAO();
                var customer = userDAO.loginCustomer(email, password);

                if (customer != null) {
                    UserStore.setCurrentUser(customer);
                    new DashboardController().show(stage);
                } else {
                    message.setText("Invalid email or password.");
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
                message.setText("Database error. Please try again.");
            }
        });

        // Divider
        Label orText = new Label("or");
        orText.setStyle(
            "-fx-text-fill: #8795b5;" +
            "-fx-font-size: 13px;"
        );

        Label line1 = new Label();
        line1.setMaxWidth(Double.MAX_VALUE);
        line1.setStyle("-fx-background-color: #394660;");
        line1.setPrefHeight(1);

        Label line2 = new Label();
        line2.setMaxWidth(Double.MAX_VALUE);
        line2.setStyle("-fx-background-color: #394660;");
        line2.setPrefHeight(1);

        HBox divider = new HBox(12, line1, orText, line2);
        divider.setAlignment(Pos.CENTER);
        HBox.setHgrow(line1, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(line2, javafx.scene.layout.Priority.ALWAYS);

        // Create account button
        Button registerButton = new Button("Create Account");
        registerButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setPrefHeight(48);
        registerButton.setFont(Font.font("Arial", 15));
        registerButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: white;" +
            "-fx-border-color: #58678c;" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        );
        registerButton.setOnAction(
            event -> new RegisterController().show(stage)
        );

        // Login card
        VBox card = new VBox(
            19,
            title,
            emailField,
            passwordField,
            loginButton,
            divider,
            registerButton,
            message
        );

        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30, 34, 30, 34));
        card.setMaxWidth(460);
        card.setPrefWidth(460);
        card.setStyle(
            "-fx-background-color: " + CARD + ";" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 20;"
        );

        // Back button
        Button backButton = new Button("←  Back");
        backButton.setFont(Font.font("Arial", 14));
        backButton.setTextFill(Color.web("#a5b4d8"));
        backButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #a5b4d8;" +
            "-fx-cursor: hand;"
        );
        backButton.setOnAction(event -> App.showWelcome(stage));

        VBox root = new VBox(
            8,
            logo,
            heading,
            card,
            backButton
        );

        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20, 30, 20, 30));
        root.setStyle("-fx-background-color: " + BG + ";");

        VBox.setMargin(card, new Insets(24, 0, 8, 0));

        stage.setTitle("SubWise - Login");
        stage.setScene(new Scene(root, 800, 560));
        stage.setMinWidth(650);
        stage.setMinHeight(500);
        stage.show();
    }

    private void styleInput(TextField field) {
        field.setMaxWidth(Double.MAX_VALUE);
        field.setPrefHeight(48);
        field.setFont(Font.font("Arial", 14));
        field.setStyle(
            "-fx-background-color: #1a263b;" +
            "-fx-text-fill: white;" +
            "-fx-prompt-text-fill: #8c9ab4;" +
            "-fx-border-color: #394963;" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 0 14;"
        );
    }

    private void stylePrimaryButton(Button button) {
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(52);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        String normal =
            "-fx-background-color: " + PURPLE + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;";

        String hover =
            "-fx-background-color: #9257ff;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;";

        button.setStyle(normal);
        button.setOnMouseEntered(event -> button.setStyle(hover));
        button.setOnMouseExited(event -> button.setStyle(normal));
    }
}
