package com.michelin.throughputfxproject.controllers;

import com.michelin.throughputfxproject.Board;
import com.michelin.throughputfxproject.Color;
import com.michelin.throughputfxproject.entities.servers.ServerMove;
import com.michelin.throughputfxproject.exceptions.ThroughputRuntimeException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import lombok.Getter;

import java.util.Objects;


public class ServerMovesController {

    @FXML
    private Button moveButton;
    @FXML
    @Getter
    private ComboBox<Color> workstationToMoveTo;
    @FXML
    @Getter
    private ComboBox<Color> serverToMove;

    public void moveToServer(ActionEvent actionEvent) {

        Color serverColor = serverToMove.getSelectionModel().getSelectedItem();
        Color workstationColor = workstationToMoveTo.getSelectionModel().getSelectedItem();
        ServerMove move = new ServerMove(Objects.requireNonNull(serverColor), Objects.requireNonNull(workstationColor) );
        try {
            Board.getInstance().startDay(move);
        } catch (IllegalArgumentException e) {
            ((Stage)moveButton.getParent().getScene().getWindow()).close();
            throw new ThroughputRuntimeException(e);
        }

        ((Stage)moveButton.getParent().getScene().getWindow()).close();
    }
}
