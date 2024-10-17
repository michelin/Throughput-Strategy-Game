package com.michelin.throughputfxproject.controllers;

import com.michelin.throughputfxproject.Board;
import com.michelin.throughputfxproject.Color;
import com.michelin.throughputfxproject.entities.Workstation;
import com.michelin.throughputfxproject.services.WorkstationService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import lombok.Getter;

import java.util.Objects;

public class AddedCapacityController {

    @FXML
    public TextArea addCapacityText;
    @FXML
    private Button capacityButton;
    @FXML
    @Getter
    private ComboBox<Color> workstationToAddCapacity;

    public void addCapacity(ActionEvent actionEvent) {

        Workstation workstation = WorkstationService.getWorkstation(workstationToAddCapacity.getSelectionModel().getSelectedItem());
        Objects.requireNonNull(workstation);
        if (addCapacityText.getText().contains("double")) {
            workstation.setCapacity(Math.min(Board.SIX_SIDES, (workstation.getCapacity() * 2)));
        } else {
            workstation.setCapacity(Math.min(Board.SIX_SIDES, (workstation.getCapacity() + 1)));
        }

        ((Stage) capacityButton.getParent().getScene().getWindow()).close();
    }
}
