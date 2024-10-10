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
        Scene scene = new Scene(fxmlLoader.load(), 925, 1000);
        stage.setTitle("Throughput");
        stage.setScene(scene);
        stage.show();
    }



    public static void main(String[] args) {
        launch();
    }


}