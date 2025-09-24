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

import com.michelin.throughputfxproject.entities.state.ScoreCard;
import com.michelin.throughputfxproject.services.ScorecardService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Getter;

@Getter
public class EstimateController {

    @FXML
    private Button estimateButton;
    @FXML
    private TextField estimateResponseText;

    @FXML
    protected void applyEstimate(ActionEvent ignoredActionEvent) {
        try {
            String startValueText = estimateResponseText.getText();
            int startValue = Integer.parseInt(startValueText);

            ScoreCard scoreCard = ScorecardService.getScorecardForCurrentWeek();
            scoreCard.setEstimate(startValue);

            ScorecardService.BACKLOG.addToBacklog(startValue);
            estimateButton.setDisable(true);
        } catch (NumberFormatException _) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Must enter a valid number");
            alert.setContentText("Please enter a valid number");
            alert.showAndWait();
        }
        ((Stage)estimateButton.getParent().getScene().getWindow()).close();
    }

}
