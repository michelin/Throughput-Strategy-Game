package com.michelin.throughputfxproject;

import com.michelin.throughputfxproject.entities.cards.BitCard;
import javafx.application.Application;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.util.Objects;

public class CardPopup extends Application {
    public static void run(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        BitCard card = BitCard.builder().description("description").descriptionTitle("description title").descriptionImg("").instructions("Instructions").reason("reason").subtitle("subtitle").title("title").build();
        // Create an image view with the desired image
        Image image = new Image(Objects.requireNonNull(CardPopup.class.getResource("cards/BIT.jpg")).openStream());
        ImageView imageView = new ImageView(image);

        // Set the image view's dimensions to 3.5 inches x 2.5 inches
        double width = 2.5 * 72; // Convert inches to pixels (assuming 72 DPI)
        double height = 3.5 * 72;
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(false);

        card.getDescriptionImg();
        VBox cardText = new VBox();
        cardText.getChildren().addAll(
                new Label(card.getTitle()),
                new Label(card.getSubtitle()),
                new Label(card.getReason()),
                new Label(card.getInstructions()),
                new Label(card.getDescriptionTitle()),
                new Label(card.getDescription()));

        Pane pane = new Pane(cardText);
        pane.setPrefSize(width, height);
        // Create a stack pane to hold the image view
        HBox hBox = new HBox(pane, imageView);

        // Create a popup and add the stack pane to it
        Popup popup = new Popup();
        popup.getContent().add(hBox);

        // Set the popup's size to match the image view
        popup.setWidth(width);
        popup.setHeight(height);

//        primaryStage.setTitle("Skills Add Window");
//        primaryStage.setWidth((width*2)+50);
//        primaryStage.setHeight(height + 50);
//        primaryStage.show();

        // Show the popup
        popup.show(primaryStage);

        // Close the popup when it loses focus
        popup.setOnHidden(event -> {
            // Perform any cleanup actions here
            System.out.println("Popup closed");
        });

    }
}
