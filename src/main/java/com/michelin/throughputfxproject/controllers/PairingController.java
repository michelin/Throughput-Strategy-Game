package com.michelin.throughputfxproject.controllers;

import com.michelin.throughputfxproject.entities.Color;
import com.michelin.throughputfxproject.services.WorkstationService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import lombok.Getter;

@Getter
public class PairingController {

    @FXML
    private TextArea implementPairsText;
    @FXML
    private Button pairButton;
    @FXML
    private ComboBox<Color> workstationToPairWith;

    @FXML
    protected void moveToServer(ActionEvent actionEvent) {

        Color workstationColor = workstationToPairWith.getSelectionModel().getSelectedItem();
        WorkstationService.pairWorkstation(workstationColor);

        ((Stage)pairButton.getParent().getScene().getWindow()).close();
    }
}
