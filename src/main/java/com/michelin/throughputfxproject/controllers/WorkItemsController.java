/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
    protected void moveWorkItems(ActionEvent ignoredActionEvent) {
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
                ScorecardService.FINISHED_GOODS.addToFinishedGoods(Math.min(workstationMovesInt,workstationMaxMovesInt));
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
