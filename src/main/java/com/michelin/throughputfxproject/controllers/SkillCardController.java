package com.michelin.throughputfxproject.controllers;


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import lombok.Getter;

@Getter
public class SkillCardController {

    @FXML
    private Text isSuccessful;
    @FXML
    private Button skillChangeButton;
    @FXML
    private Label cardSkill;
    @FXML
    private Label cardInstructions;
    @FXML
    private Label cardInstructionsExtended;

    public void closeCardWindow(MouseEvent mouseEvent) {
        ((HBox) mouseEvent.getSource()).getScene().getWindow().hide();
    }
}
