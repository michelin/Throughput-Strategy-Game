package com.michelin.throughputfxproject.controllers;

import com.michelin.throughputfxproject.Color;
import com.michelin.throughputfxproject.services.WorkstationService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import lombok.Getter;


public class AddAutomationController {
    @FXML
    private Button automationButton;
    @FXML
    @Getter
    private ComboBox<Color> workstationToAddAutomation;

    public void addAutomation(ActionEvent actionEvent) {

        Color selectedColor = workstationToAddAutomation.getSelectionModel().getSelectedItem();
        WorkstationService.automateWorkstation(selectedColor);

        ((Stage)automationButton.getParent().getScene().getWindow()).close();
    }
}
