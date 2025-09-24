/*
 * Copyright 2025 Manufacture Française des Pneumatiques Michelin
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
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
    protected void moveInitialWorkItems(ActionEvent ignoredActionEvent) {
        workItemMoveButton.setDisable(true);
        try {
            String maxValueText = txtWorkstationMax.getText();
            int workstationMaxMovesInt = Integer.parseInt(maxValueText);

            String moveValueText = workItemMoveResponseText.getText();
            int initialMoveFromBacklog = Integer.parseInt(moveValueText);

            final Workstation workstationZero = WorkstationService.getWorkstation(0);
            workstationZero.addToWorkItemCount(Math.min(workstationMaxMovesInt,initialMoveFromBacklog));

            ScorecardService.BACKLOG.subtractFromBacklog(Math.min(workstationMaxMovesInt,initialMoveFromBacklog));

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
