package com.michelin.throughputfxproject.controllers;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

public class DieController {
    public void closeCardWindow(MouseEvent mouseEvent) {
        ((BorderPane) mouseEvent.getSource()).getScene().getWindow().hide();
    }
}
