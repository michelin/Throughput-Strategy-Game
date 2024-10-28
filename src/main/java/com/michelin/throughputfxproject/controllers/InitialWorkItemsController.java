package com.michelin.throughputfxproject.controllers;

import com.michelin.throughputfxproject.entities.Workstation;
import com.michelin.throughputfxproject.services.ScorecardService;
import com.michelin.throughputfxproject.services.WorkstationService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Getter;

@Getter
public class InitialWorkItemsController {

    @FXML
    private TextArea workItemMoveText;
    @FXML
    private Button workItemMoveButton;
    @FXML
    private TextField workItemMoveResponseText;

    public void moveInitialWorkItems(ActionEvent actionEvent) {

        try {
            String moveValueText = workItemMoveResponseText.getText();
            int initialMoveFromBacklog = Integer.parseInt(moveValueText);
            final Workstation workstationZero = WorkstationService.getWorkstation(0);
            workstationZero.addToWorkItemCount(initialMoveFromBacklog);
            ScorecardService.getBacklog().subtractFromBacklog(initialMoveFromBacklog);
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Must enter a valid number");
            alert.setContentText("Please enter a valid number");
            alert.showAndWait();
        }
        ((Stage) workItemMoveButton.getParent().getScene().getWindow()).close();


    }
}
