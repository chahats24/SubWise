
package com.subwise;

import com.subwise.customer.LoginController;
import com.subwise.member3.AdminLoginController;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class App extends Application {

    private static final String BG = "#090e1d";
    private static final String CARD = "#151e31";
    private static final String PURPLE = "#8950f5";
    private static final String BORDER = "#26334d";

    @Override
    public void start(Stage stage) {
        showWelcome(stage);
    }

    public static void showWelcome(Stage stage) {

        Label logo = new Label("SUBWISE");
        logo.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 48));
        logo.setTextFill(Color.web("#9560ff"));

        Label title = new Label("Manage it all.");
        title.setFont(Font.font("Arial", 25));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Your subscriptions, one place.");
        subtitle.setFont(Font.font("Arial", 15));
        subtitle.setTextFill(Color.web("#91a5c2"));

        Button customerButton = new Button("Login");
        styleLoginButton(customerButton);
        customerButton.setOnAction(event ->
            new LoginController().show(stage)
        );

        Button adminButton = new Button("Login");
        styleLoginButton(adminButton);
        adminButton.setOnAction(event ->
            new AdminLoginController().show(stage)
        );

        VBox customerCard = createCard("Customer", customerButton);
        VBox adminCard = createCard("Administrator", adminButton);

        HBox cards = new HBox(16, customerCard, adminCard);
        cards.setAlignment(Pos.CENTER);

        Label footer = new Label(
            "SUBWISE   •   SUBSCRIPTION MANAGER"
        );
        footer.setFont(Font.font("Arial", 11));
        footer.setTextFill(Color.web("#7185a4"));

        VBox root = new VBox(8, logo, title, subtitle, cards, footer);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: " + BG + ";");

        VBox.setMargin(cards, new Insets(22, 0, 8, 0));

        stage.setTitle("SubWise");
        stage.setScene(new Scene(root, 800, 560));
        stage.setMinWidth(700);
        stage.setMinHeight(500);
        stage.setResizable(true);
        stage.show();
    }

    private static VBox createCard(String heading, Button button) {

        Label title = new Label(heading);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.WHITE);

        VBox card = new VBox(28, title, button);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(26, 28, 26, 28));
        card.setPrefWidth(285);
        card.setMinWidth(240);
        card.setPrefHeight(175);
        card.setStyle(
            "-fx-background-color: " + CARD + ";" +
            "-fx-background-radius: 22;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 22;"
        );

        return card;
    }

    private static void styleLoginButton(Button button) {
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefWidth(230);
        button.setPrefHeight(48);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        button.setTextFill(Color.WHITE);

        String normalStyle =
            "-fx-background-color: " + PURPLE + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 12;" +
            "-fx-border-radius: 12;" +
            "-fx-padding: 10 18;" +
            "-fx-cursor: hand;";

        String hoverStyle =
            "-fx-background-color: #9b6aff;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 12;" +
            "-fx-border-radius: 12;" +
            "-fx-padding: 10 18;" +
            "-fx-cursor: hand;";

        button.setStyle(normalStyle);
        button.setOnMouseEntered(event -> button.setStyle(hoverStyle));
        button.setOnMouseExited(event -> button.setStyle(normalStyle));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
