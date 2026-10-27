package com.subwise.member3;

import com.subwise.App;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
//import javafx.scene.layout.HBox;
//import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class AdminLoginController {
    private static final String BG = "#090e1d";
    private static final String CARD = "#151e31";
    private static final String PURPLE = "#7838f5";
    private static final String BORDER = "#293650";

    public void show(Stage stage) {
        Label logo = new Label("SUBWISE");
        logo.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 38));
        logo.setTextFill(Color.web("#9257ff"));

        Label title = new Label("Admin Login");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.WHITE);

        TextField email = new TextField();
        email.setPromptText("Email");
        styleInput(email);

        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        styleInput(password);

        Label message = new Label();
        message.setWrapText(true);
        message.setMaxWidth(Double.MAX_VALUE);
        message.setStyle("-fx-text-fill: #f87171; -fx-font-size: 12px;");

        Button login = new Button("Login");
        stylePrimaryButton(login);
        login.setOnAction(event -> {
            if (email.getText().trim().equalsIgnoreCase("admin@subwise.com")
                    && password.getText().equals("admin123")) {
                new AdminDashboardController().show(stage);
            } else {
                message.setText("Invalid email or password.");
            }
        });

        Label demoInfo = new Label(
            "Demo admin login: admin@subwise.com  |  Password: admin123"
        );
        demoInfo.setWrapText(true);
        demoInfo.setMaxWidth(390);
        demoInfo.setStyle("-fx-text-fill: #9aa9c4; -fx-font-size: 11px;");

        Button back = new Button("←  Back");
        back.setFont(Font.font("Arial", 13));
        back.setTextFill(Color.web("#a5b4d8"));
        back.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #a5b4d8;" +
            "-fx-cursor: hand;"
        );
        back.setOnAction(event -> App.showWelcome(stage));

        VBox card = new VBox(13, title, email, password, login, message, demoInfo, back);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(22, 26, 22, 26));
        card.setMaxWidth(400);
        card.setPrefWidth(400);
        card.setStyle(
            "-fx-background-color: " + CARD + ";" +
            "-fx-background-radius: 17;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 17;"
        );

        VBox root = new VBox(10, logo, card);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(14, 24, 14, 24));
        root.setStyle("-fx-background-color: " + BG + ";");

        stage.setTitle("SubWise - Admin Login");
        stage.setScene(new Scene(root, 720, 510));
        stage.setMinWidth(600);
        stage.setMinHeight(460);
        stage.show();
    }

    private void styleInput(TextField field) {
        field.setMaxWidth(Double.MAX_VALUE);
        field.setPrefHeight(42);
        field.setFont(Font.font("Arial", 13));
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
        button.setPrefHeight(44);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        String normal = "-fx-background-color: " + PURPLE + ";" +
            "-fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand;";
        String hover = "-fx-background-color: #9257ff;" +
            "-fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand;";
        button.setStyle(normal);
        button.setOnMouseEntered(event -> button.setStyle(hover));
        button.setOnMouseExited(event -> button.setStyle(normal));
    }
}
