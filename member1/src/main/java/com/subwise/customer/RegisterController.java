package com.subwise.customer;

import com.subwise.dao.UserDAO;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.SQLException;

public class RegisterController {
    private static final String BG = "#090e1d";
    private static final String CARD = "#151e31";
    private static final String PURPLE = "#7838f5";
    private static final String BORDER = "#293650";

    public void show(Stage stage) {
        Label logo = new Label("SUBWISE");
        logo.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 44));
        logo.setTextFill(Color.web("#9257ff"));

        Label title = new Label("Create Your Account");
        title.setFont(Font.font("Arial", FontWeight.NORMAL, 25));
        title.setTextFill(Color.WHITE);

        TextField nameField = new TextField();
        nameField.setPromptText("Username");
        styleInput(nameField);

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        styleInput(emailField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        styleInput(passwordField);

        Label message = new Label();
        message.setWrapText(true);
        message.setMaxWidth(Double.MAX_VALUE);
        message.setStyle("-fx-text-fill: #f87171; -fx-font-size: 12px;");

        Button registerButton = new Button("Register");
        stylePrimaryButton(registerButton);

        registerButton.setOnAction(event -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText();
            message.setStyle("-fx-text-fill: #f87171; -fx-font-size: 12px;");

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                message.setText("Please fill in all fields.");
                return;
            }
            if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                message.setText("Please enter a valid email address.");
                return;
            }
            if (password.length() < 6) {
                message.setText("Password must be at least 6 characters.");
                return;
            }

            try {
                UserDAO userDAO = new UserDAO();
                if (userDAO.emailExists(email)) {
                    message.setText("This email is already registered. Please log in.");
                    return;
                }

                if (userDAO.registerCustomer(name, email, password)) {
                    message.setStyle("-fx-text-fill: #4ade80; -fx-font-size: 12px;");
                    message.setText("Account created! You can now log in.");
                } else {
                    message.setText("Registration failed. Please try again.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                message.setText("Database error. Please try again.");
            }
        });

        Label orText = new Label("or");
        orText.setStyle("-fx-text-fill: #8795b5; -fx-font-size: 13px;");
        Label line1 = dividerLine();
        Label line2 = dividerLine();
        HBox divider = new HBox(12, line1, orText, line2);
        divider.setAlignment(Pos.CENTER);
        HBox.setHgrow(line1, Priority.ALWAYS);
        HBox.setHgrow(line2, Priority.ALWAYS);

        Button backButton = new Button("Back to Login");
        backButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setPrefHeight(48);
        backButton.setFont(Font.font("Arial", 15));
        backButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: white;" +
            "-fx-border-color: #58678c;" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        );
        backButton.setOnAction(event -> new LoginController().show(stage));

        VBox card = new VBox(15, title, nameField, emailField,
            passwordField, registerButton, message, divider, backButton);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(26, 32, 26, 32));
        card.setMaxWidth(460);
        card.setPrefWidth(460);
        card.setStyle(
            "-fx-background-color: " + CARD + ";" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 20;"
        );

        VBox root = new VBox(10, logo, card);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(18, 30, 18, 30));
        root.setStyle("-fx-background-color: " + BG + ";");

        stage.setTitle("SubWise - Create Account");
        stage.setScene(new Scene(root, 800, 560));
        stage.setMinWidth(650);
        stage.setMinHeight(500);
        stage.show();
    }

    private Label dividerLine() {
        Label line = new Label();
        line.setMaxWidth(Double.MAX_VALUE);
        line.setPrefHeight(1);
        line.setStyle("-fx-background-color: #394660;");
        return line;
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
        button.setPrefHeight(50);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        String normal = "-fx-background-color: " + PURPLE + ";" +
            "-fx-text-fill: white; -fx-background-radius: 12; -fx-cursor: hand;";
        String hover = "-fx-background-color: #9257ff;" +
            "-fx-text-fill: white; -fx-background-radius: 12; -fx-cursor: hand;";
        button.setStyle(normal);
        button.setOnMouseEntered(event -> button.setStyle(hover));
        button.setOnMouseExited(event -> button.setStyle(normal));
    }
}
