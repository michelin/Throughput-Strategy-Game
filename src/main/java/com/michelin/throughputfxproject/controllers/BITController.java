package com.michelin.throughputfxproject.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
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
    @FXML
    private ImageView cardBackImage;


}
