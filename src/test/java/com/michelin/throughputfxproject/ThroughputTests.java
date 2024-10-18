package com.michelin.throughputfxproject;

import javafx.application.Application;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

public class ThroughputTests extends Application {

    private Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
    }

    @Test
    public void testThroughput() {
        Prompts.drawBit(stage.);
    }
}
