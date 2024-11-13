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
    protected void applyEstimate(ActionEvent actionEvent) {
        try {
            String startValueText = estimateResponseText.getText();
            int startValue = Integer.parseInt(startValueText);

            ScoreCard scoreCard = ScorecardService.getScorecardForCurrentWeek();
            scoreCard.setEstimate(startValue);

            ScorecardService.getBacklog().addToBacklog(startValue);
            estimateButton.setDisable(true);
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Must enter a valid number");
            alert.setContentText("Please enter a valid number");
            alert.showAndWait();
        }
        ((Stage)estimateButton.getParent().getScene().getWindow()).close();
    }

}
