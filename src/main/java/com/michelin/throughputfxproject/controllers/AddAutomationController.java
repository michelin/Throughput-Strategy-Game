package com.michelin.throughputfxproject.controllers;

import com.michelin.throughputfxproject.entities.Color;
import com.michelin.throughputfxproject.services.WorkstationService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller class for handling the automation functionality in the application.
 * This class is responsible for managing the UI components and actions related
 * to adding automation to a workstation. It uses JavaFX for UI interaction and
 * integrates with the WorkstationService for business logic.
 */
@Getter
@ToString
@EqualsAndHashCode
public class AddAutomationController {
    public static final Logger LOGGER = LoggerFactory.getLogger(AddAutomationController.class.getName());

    @FXML
    private TextArea addAutomationText;
    @FXML
    private Button automationButton;
    @FXML
    private ComboBox<Color> workstationToAddAutomation;

    /**
     * Handles the action event triggered by the automation button.
     * Logs the event if debug logging is enabled, retrieves the selected workstation color
     * from the combo box, and automates the workstation if a color is selected.
     * Finally, closes the current window.
     *
     * @param actionEvent The action event triggered by the automation button.
     */
    public void addAutomation(ActionEvent actionEvent) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(actionEvent.toString());
        }
        Color selectedColor = workstationToAddAutomation.getSelectionModel().getSelectedItem();
        if (selectedColor != null) {
            WorkstationService.automateWorkstation(selectedColor);
        }
        ((Stage)automationButton.getParent().getScene().getWindow()).close();
    }
}
