package com.michelin.throughputfxproject.controllers;

import com.michelin.throughputfxproject.Color;
import com.michelin.throughputfxproject.services.WorkstationService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import lombok.Getter;

public class PairingController {

    @FXML
    private Button pairButton;
    @FXML
    @Getter
    private ComboBox<Color> workstationToPairWith;

    public void moveToServer(ActionEvent actionEvent) {

        Color workstationColor = workstationToPairWith.getSelectionModel().getSelectedItem();
        WorkstationService.pairWorkstation(workstationColor);

        ((Stage)pairButton.getParent().getScene().getWindow()).close();
    }
}
