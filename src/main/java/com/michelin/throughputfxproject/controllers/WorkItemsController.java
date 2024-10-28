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
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.Getter;

@Getter
public class WorkItemsController {

    @FXML
    private TextArea workItemMoveText;
    @FXML
    private Text txtWorkstationPosition;
    @FXML
    private TextField workItemMoveResponseText;
    @FXML
    private Button workItemMoveButton;



    public void moveWorkItems(ActionEvent actionEvent) {
        try {
            String moveValueText = workItemMoveResponseText.getText();
            int workstationMovesInt = Integer.parseInt(moveValueText);
            String activePositionText = txtWorkstationPosition.getText();
            int workstationPosition = Integer.parseInt(activePositionText);

           Workstation workstation =  WorkstationService.getWorkstations()[workstationPosition];

            if (workstationPosition == 4) {
                ScorecardService.getFinishedGoods().addToFinishedGoods(workstationMovesInt);
            } else {
                WorkstationService.getWorkstation(workstationPosition+ 1).addToWorkItemCount(workstationMovesInt);
            }
            workstation.subtractFromWorkItemCount(workstationMovesInt);

        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Must enter a valid number");
            alert.setContentText("Please enter a valid number");
            alert.showAndWait();
        }
        ((Stage)workItemMoveButton.getParent().getScene().getWindow()).close();
    }
}
