package com.michelin.throughputfxproject.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import lombok.Getter;

@Getter
public class DieController {

    @FXML
    private ImageView dieImage;
    @FXML
    private Label dieHeaderText;
    @FXML
    private Label dieText;

    public void closeCardWindow(MouseEvent mouseEvent) {
        ((BorderPane) mouseEvent.getSource()).getScene().getWindow().hide();
    }
}
