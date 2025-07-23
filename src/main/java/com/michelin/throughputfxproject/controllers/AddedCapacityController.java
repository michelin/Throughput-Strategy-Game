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

import com.michelin.throughputfxproject.entities.state.Board;
import com.michelin.throughputfxproject.entities.Color;
import com.michelin.throughputfxproject.entities.state.Workstation;
import com.michelin.throughputfxproject.services.WorkstationService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@Getter
public class AddedCapacityController {
    public static final Logger LOGGER = LoggerFactory.getLogger(AddedCapacityController.class.getName());

    @FXML
    public TextArea addCapacityText;
    @FXML
    private Button capacityButton;
    @FXML
    private ComboBox<Color> workstationToAddCapacity;


    public void addCapacity(ActionEvent actionEvent) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(actionEvent.toString());
        }

        Workstation workstation = WorkstationService.getWorkstation(workstationToAddCapacity.getSelectionModel().getSelectedItem());
        Objects.requireNonNull(workstation);
        if (addCapacityText.getText().contains("double")) {
            workstation.setCapacity(Math.min( Board.getInstance().getDieFaces(), (workstation.getCapacity() * 2)));
        } else {
            workstation.setCapacity(Math.min( Board.getInstance().getDieFaces(), (workstation.getCapacity() + 1)));
        }

        ((Stage) capacityButton.getParent().getScene().getWindow()).close();
    }
}
