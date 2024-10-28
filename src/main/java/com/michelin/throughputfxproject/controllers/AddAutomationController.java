package com.michelin.throughputfxproject.controllers;

import com.michelin.throughputfxproject.Color;
import com.michelin.throughputfxproject.services.WorkstationService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import lombok.Getter;


public class AddAutomationController {
    @FXML
    @Getter
    private TextArea addAutomationText;
    @FXML
    private Button automationButton;
    @FXML
    @Getter
    private ComboBox<Color> workstationToAddAutomation;

    public void addAutomation(ActionEvent actionEvent) {

        Color selectedColor = workstationToAddAutomation.getSelectionModel().getSelectedItem();
        if (selectedColor != null) {
            WorkstationService.automateWorkstation(selectedColor);
        }

        ((Stage)automationButton.getParent().getScene().getWindow()).close();
    }
}
