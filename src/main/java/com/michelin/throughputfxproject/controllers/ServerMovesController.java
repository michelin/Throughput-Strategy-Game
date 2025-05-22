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
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@Getter
public class ServerMovesController {
    public static final Logger LOGGER = LoggerFactory.getLogger(ServerMovesController.class.getName());

    @FXML
    private TextArea serverMovesText;

    @FXML
    private Button moveButton;

    @FXML
    private ComboBox<Color> workstationToMoveTo;

    @FXML
    private ComboBox<Color> serverToMove;

    @Setter
    private BoardController boardController;

    @FXML
    protected void moveToServer(ActionEvent actionEvent) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(actionEvent.toString());
        }
        Color serverColor = serverToMove.getSelectionModel().getSelectedItem();
        Color workstationColor = workstationToMoveTo.getSelectionModel().getSelectedItem();
        ServerMove move = new ServerMove(Objects.requireNonNull(serverColor), Objects.requireNonNull(workstationColor));
        try {
            Board.getInstance().startDay(move);
        } catch (RuntimeException _) {
            //do nothing
        } finally {
            ((Stage) moveButton.getParent().getScene().getWindow()).close();
            boardController.redrawBoard();
        }


    }
}
