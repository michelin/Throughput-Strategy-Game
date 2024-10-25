package com.michelin.throughputfxproject.controllers;

import com.michelin.throughputfxproject.Board;
import com.michelin.throughputfxproject.Color;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import lombok.Getter;

import java.util.Objects;


public class SkillsController {

    @FXML
    private Button skillAddButton;
    @FXML
    @Getter
    private ComboBox<Color> serverToAddSkills;
    @FXML
    @Getter
    private ComboBox<Color> skillsToAddToServer;


    @FXML
    protected void addSkillsToServer(ActionEvent actionEvent) {

        Color serverToAdd = serverToAddSkills.getSelectionModel().getSelectedItem();
        Objects.requireNonNull(serverToAdd);
        Color skillsToAdd = skillsToAddToServer.getSelectionModel().getSelectedItem();
        Objects.requireNonNull(skillsToAdd);
        Board.setInTrainingServer(serverToAdd, skillsToAdd);

        ((Stage)skillAddButton.getParent().getScene().getWindow()).close();

    }
}
