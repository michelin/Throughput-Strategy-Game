package com.michelin.throughputfxproject;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Collections;

public class ThroughputApplication extends Application {


    public ThroughputApplication() {

        super();
    }


    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ThroughputApplication.class.getResource("board.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 700, 500);
        stage.setTitle("Throughput");
        stage.setScene(scene);
        stage.show();

       // Board.getInstance().runGame(stage);
    }

    public void start1(Stage stage) throws IOException {

        // FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));

        FXMLLoader fxmlLoader = new FXMLLoader(ThroughputApplication.class.getResource("ascii-art-2.fxml"));
//        Label secondaryLabel = new Label("::  A Strategy Game  ::");
//        secondaryLabel.setFont(new Font("Arial", 18));
//        Label welcomeLabel = new Label("Throughput",secondaryLabel);
//        welcomeLabel.setFont(new Font("Arial", 48));
//        Scene scene = new Scene(welcomeLabel, 500, 500);

        Scene scene = new Scene(fxmlLoader.load(), 320, 240);

        stage.setScene(scene);
        stage.show();

        new Background(
                Collections.singletonList(new BackgroundFill(
                        Color.WHITE,
                        new CornerRadii(500),
                        new Insets(10))),
                Collections.singletonList(new BackgroundImage(
                        new Image("Cover.jpg", 100, 100, false, true),
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        BackgroundSize.DEFAULT)));


    }

    public void start2(Stage stage) {
        //Creating a dialog
        Dialog<String> dialog = new Dialog<>();
        //Setting the title
        dialog.setTitle("Dialog");
        ButtonType type = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
        //Setting the content of the dialog
        dialog.setContentText("This is a sample dialog");
        //Adding buttons to the dialog pane
        dialog.getDialogPane().getButtonTypes().add(type);
        //Setting the label
        Text txt = new Text("Click the button to show the dialog");
        Font font = Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 12);
        txt.setFont(font);
        //Creating a button
        Button button = new Button("Show Dialog");
        //Showing the dialog on clicking the button
        button.setOnAction(e -> {
            dialog.showAndWait();
        });
        //Creating a vbox to hold the button and the label
        HBox pane = new HBox(15);
        //Setting the space between the nodes of a HBox pane
        pane.setPadding(new Insets(50, 150, 50, 60));
        pane.getChildren().addAll(txt, button);
        //Creating a scene object
        Scene scene = new Scene(new Group(pane), 595, 250, Color.BEIGE);
        stage.setTitle("Dialog");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }


}