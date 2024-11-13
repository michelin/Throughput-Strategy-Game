package com.michelin.throughputfxproject.controllers;

import com.michelin.throughputfxproject.entities.state.Board;
import com.michelin.throughputfxproject.entities.Color;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import lombok.Getter;

import java.util.Objects;

@Getter
public class SkillsController {

    @FXML
    private TextArea skillAddText;
    @FXML
    private Button skillAddButton;
    @FXML
    private ComboBox<Color> serverToAddSkills;
    @FXML
    private ComboBox<Color> skillsToAddToServer;


    @FXML
    protected void addSkillsToServer(ActionEvent actionEvent) {

        Color serverToAdd = serverToAddSkills.getSelectionModel().getSelectedItem();
        Objects.requireNonNull(serverToAdd);
        Color skillsToAdd = skillsToAddToServer.getSelectionModel().getSelectedItem();
        Objects.requireNonNull(skillsToAdd);
        Board.getInstance().putServerInTraining(serverToAdd, skillsToAdd);

        ((Stage)skillAddButton.getParent().getScene().getWindow()).close();

    }
}
