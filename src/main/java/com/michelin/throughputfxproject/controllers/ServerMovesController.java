package com.michelin.throughputfxproject.controllers;

import com.michelin.throughputfxproject.Board;
import com.michelin.throughputfxproject.entities.servers.ServerMove;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;

public class ServerMovesController {

    @FXML
    private ComboBox<Color> workstationToMoveTo;
    @FXML
    private ComboBox<Color> serverToMove;

    public void moveToServer(ActionEvent actionEvent) {
        ServerMove move = new ServerMove(com.michelin.throughputfxproject.Color.lookupByFXColor(serverToMove.getSelectionModel().getSelectedItem()), com.michelin.throughputfxproject.Color.lookupByFXColor(workstationToMoveTo.getSelectionModel().getSelectedItem()));
        Board.getInstance().startDay(move);
    }
}
