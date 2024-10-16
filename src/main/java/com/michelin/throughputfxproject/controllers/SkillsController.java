package com.michelin.throughputfxproject.controllers;

import com.michelin.throughputfxproject.Color;
import com.michelin.throughputfxproject.entities.Server;
import com.michelin.throughputfxproject.services.ServerService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.util.Objects;


public class SkillsController {

    @FXML
    private Button skillAddButton;
    @FXML
    private ComboBox<Color> serverToAddSkills;
    @FXML
    private ComboBox<Color> skillsToAddToServer;


    @FXML
    protected void addSkillsToServer(ActionEvent actionEvent) {
        Server server = ServerService.getHumanServer(serverToAddSkills.getSelectionModel().getSelectedItem());
        Objects.requireNonNull(server).getSkills().add(skillsToAddToServer.getSelectionModel().getSelectedItem());

        ((Stage)skillAddButton.getParent().getScene().getWindow()).close();

    }
}
