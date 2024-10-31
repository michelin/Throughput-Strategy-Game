package com.michelin.throughputfxproject.controllers;

import com.michelin.throughputfxproject.entities.Color;
import com.michelin.throughputfxproject.entities.servers.ServerMove;
import com.michelin.throughputfxproject.entities.state.Board;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import lombok.Getter;

import java.util.Objects;

@Getter
public class ServerMovesController {

    @FXML
    private TextArea serverMovesText;

    @FXML
    private Button moveButton;

    @FXML
    private ComboBox<Color> workstationToMoveTo;

    @FXML
    private ComboBox<Color> serverToMove;

    public void moveToServer(ActionEvent actionEvent) {

        Color serverColor = serverToMove.getSelectionModel().getSelectedItem();
        Color workstationColor = workstationToMoveTo.getSelectionModel().getSelectedItem();
        ServerMove move = new ServerMove(Objects.requireNonNull(serverColor), Objects.requireNonNull(workstationColor));
        try {
            Board.startDay(move);
        } catch (RuntimeException e) {
            //do nothing
        } finally {
            ((Stage) moveButton.getParent().getScene().getWindow()).close();
        }


    }
}
