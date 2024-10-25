package com.michelin.throughputfxproject.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import lombok.Getter;

@Getter
public class ChanceController {

    @FXML
    private Label cardChance;
    @FXML
    private Label cardInstructions;

    public void closeCardWindow(MouseEvent mouseEvent) {
        ((HBox) mouseEvent.getSource()).getScene().getWindow().hide();
    }
}
