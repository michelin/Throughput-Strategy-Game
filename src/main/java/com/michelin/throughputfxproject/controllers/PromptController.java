package com.michelin.throughputfxproject.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class PromptController {



    @FXML
    private Label gameWelcomeText;

    @FXML
    protected void onHelloButtonClick() {
        gameWelcomeText.setText("Welcome to JavaFX Application!");
    }
}