package com.michelin.throughputfxproject.controllers;

import com.michelin.throughputfxproject.entities.state.Board;
import com.michelin.throughputfxproject.entities.Color;
import com.michelin.throughputfxproject.entities.state.Workstation;
import com.michelin.throughputfxproject.services.WorkstationService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@Getter
public class AddedCapacityController {
    public static final Logger LOGGER = LoggerFactory.getLogger(AddedCapacityController.class.getName());

    @FXML
    public TextArea addCapacityText;
    @FXML
    private Button capacityButton;
    @FXML
    private ComboBox<Color> workstationToAddCapacity;

    public void addCapacity(ActionEvent actionEvent) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(actionEvent.toString());
        }

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
