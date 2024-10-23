package com.michelin.throughputfxproject.controllers;

import com.michelin.throughputfxproject.Board;
import com.michelin.throughputfxproject.Color;
import com.michelin.throughputfxproject.entities.servers.HumanServer;
import com.michelin.throughputfxproject.services.ServerService;
import com.michelin.throughputfxproject.services.WorkstationService;
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

        HumanServer server = ServerService.getHumanServer(Objects.requireNonNull(serverToAddSkills.getSelectionModel().getSelectedItem()));
        Objects.requireNonNull(server);
        server.getSkills().add(Objects.requireNonNull(skillsToAddToServer.getSelectionModel().getSelectedItem()));
        WorkstationService.removeInTrainingServerFromWorkstation(server);
        Board.setInTrainingServer(server);

        ((Stage)skillAddButton.getParent().getScene().getWindow()).close();

    }
}
