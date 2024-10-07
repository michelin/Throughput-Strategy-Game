package com.michelin.throughputfxproject;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;

public class CardPopup extends Application {
    public static void run(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Create an image view with the desired image
        Image image = new Image("./cards/BIT.jpg"); // Replace with your image path
        ImageView imageView = new ImageView(image);

        // Set the image view's dimensions to 3.5 inches x 2.5 inches
        double width = 3.5 * 72; // Convert inches to pixels (assuming 72 DPI)
        double height = 2.5 * 72;
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(true);

        // Create a stack pane to hold the image view
        StackPane stackPane = new StackPane(imageView);

        // Create a popup and add the stack pane to it
        Popup popup = new Popup();
        popup.getContent().add(stackPane);

        // Set the popup's size to match the image view
        popup.setWidth(width);
        popup.setHeight(height);

        // Show the popup
        popup.show(primaryStage);

        // Close the popup when it loses focus
        popup.setOnHidden(event -> {
            // Perform any cleanup actions here
            System.out.println("Popup closed");
        });
    }
}
