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
