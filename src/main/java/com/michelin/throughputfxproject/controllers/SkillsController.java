package com.michelin.throughputfxproject.controllers;

import com.michelin.throughputfxproject.entities.Server;
import com.michelin.throughputfxproject.services.ServerService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;

public class SkillsController {

    @FXML
    private TextArea skillAddText;
    @FXML
    private ComboBox<Color> serverToAddSkills;
    @FXML
    private ComboBox<Color> skillsToAddToServer;


    @FXML
    protected void addSkillsToServer(ActionEvent actionEvent) {
        Server server = ServerService.getHumanServer(com.michelin.throughputfxproject.Color.lookupByFXColor(serverToAddSkills.getSelectionModel().getSelectedItem()));
        server.getSkills().add(com.michelin.throughputfxproject.Color.lookupByFXColor(skillsToAddToServer.getSelectionModel().getSelectedItem()));

    }
}
