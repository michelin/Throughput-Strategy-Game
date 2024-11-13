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
public class InitialWorkItemsController {

    public static final Logger LOGGER = LoggerFactory.getLogger(InitialWorkItemsController.class.getName());

    @FXML
    private Text txtWorkstationMax;
    @FXML
    private TextArea workItemMoveText;
    @FXML
    private Button workItemMoveButton;
    @FXML
    private TextField workItemMoveResponseText;

    @FXML
    protected void moveInitialWorkItems(ActionEvent actionEvent) {
        workItemMoveButton.setDisable(true);
        try {
            String maxValueText = txtWorkstationMax.getText();
            int workstationMaxMovesInt = Integer.parseInt(maxValueText);

            String moveValueText = workItemMoveResponseText.getText();
            int initialMoveFromBacklog = Integer.parseInt(moveValueText);

            final Workstation workstationZero = WorkstationService.getWorkstation(0);
            workstationZero.addToWorkItemCount(Math.min(workstationMaxMovesInt,initialMoveFromBacklog));

            ScorecardService.getBacklog().subtractFromBacklog(Math.min(workstationMaxMovesInt,initialMoveFromBacklog));

        } catch (NumberFormatException e) {
            LOGGER.error("InitialWorkItems", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Must enter a valid number");
            alert.setContentText("Please enter a valid number");
            alert.show();
        }
        ((Stage) workItemMoveButton.getParent().getScene().getWindow()).close();
    }
}
