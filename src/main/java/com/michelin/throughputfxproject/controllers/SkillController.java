package com.michelin.throughputfxproject.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import lombok.Getter;

@Getter
public class SkillController {

    @FXML
    private Button skillAddButton;
    @FXML
    private Label cardSkill;
    @FXML
    private Label cardInstructions;
    @FXML
    private Label cardInstructionsExtended;


}
