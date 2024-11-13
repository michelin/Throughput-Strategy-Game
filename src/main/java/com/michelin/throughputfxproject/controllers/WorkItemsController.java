package com.michelin.throughputfxproject.controllers;

import com.michelin.throughputfxproject.entities.state.Workstation;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public class WorkItemsController {
    public static final Logger LOGGER = LoggerFactory.getLogger(WorkItemsController.class.getName());

    @FXML
    private Text txtWorkstationMax;
    @FXML
    private TextArea workItemMoveText;
    @FXML
    private Text txtWorkstationPosition;
    @FXML
    private TextField workItemMoveResponseText;
    @FXML
    private Button workItemMoveButton;



    @FXML
    protected void moveWorkItems(ActionEvent actionEvent) {
        workItemMoveButton.setDisable(true);
        try {
            String maxValueText = txtWorkstationMax.getText();
            int workstationMaxMovesInt = Integer.parseInt(maxValueText);

            String moveValueText = workItemMoveResponseText.getText();
            int workstationMovesInt = Integer.parseInt(moveValueText);

            String activePositionText = txtWorkstationPosition.getText();
            int workstationPosition = Integer.parseInt(activePositionText);

           Workstation workstation =  WorkstationService.getWorkstations()[workstationPosition];

            if (workstationPosition == 4) {
                ScorecardService.getFinishedGoods().addToFinishedGoods(Math.min(workstationMovesInt,workstationMaxMovesInt));
            } else {
                WorkstationService.getWorkstation(workstationPosition+ 1).addToWorkItemCount(Math.min(workstationMovesInt,workstationMaxMovesInt));
            }
            workstation.subtractFromWorkItemCount(Math.min(workstationMovesInt,workstationMaxMovesInt));

        } catch (NumberFormatException e) {
            LOGGER.error("WorkItems", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Must enter a valid number");
            alert.setContentText("Please enter a valid number");
            alert.show();
        }
        ((Stage)workItemMoveButton.getParent().getScene().getWindow()).close();
    }
}
