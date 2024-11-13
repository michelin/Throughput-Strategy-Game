package com.michelin.throughputfxproject.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import lombok.Getter;


@Getter
public class BITController {

    @FXML
    private Label cardTitle;
    @FXML
    private Label cardSubtitle;
    @FXML
    private Label cardReason;
    @FXML
    private Label cardDescriptionTitle;
    @FXML
    private ImageView descriptionImage;
    @FXML
    private Label cardDescription;
    @FXML
    private Label cardInstructions;


    public void closeCardWindow(MouseEvent mouseEvent) {
        ((HBox) mouseEvent.getSource()).getScene().getWindow().hide();
    }
}
