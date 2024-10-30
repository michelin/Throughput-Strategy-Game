package com.michelin.throughputfxproject;

import javafx.application.Application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public class ThroughputApplication extends Application {


    public ThroughputApplication() {
        super();
    }


    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ThroughputApplication.class.getResource("board.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 925, 1000);
        scene.getStylesheets().add(Objects.requireNonNull(ThroughputApplication.class.getResource("css/throughput.css")).toExternalForm());
        stage.setTitle("Throughput");
        stage.setScene(scene);
        stage.show();
    }


    public static void main(String[] args) {
        launch();
    }


}