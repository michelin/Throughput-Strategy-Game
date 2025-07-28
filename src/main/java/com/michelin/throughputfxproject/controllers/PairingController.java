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
import com.michelin.throughputfxproject.services.WorkstationService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import lombok.Getter;

@Getter
public class PairingController {

    @FXML
    private TextArea implementPairsText;
    @FXML
    private Button pairButton;
    @FXML
    private ComboBox<Color> workstationToPairWith;

    private static final java.util.Random RANDOM = new java.util.Random();

    @FXML
    protected void moveToServer(ActionEvent ignoredActionEvent) {

        Color workstationColor = workstationToPairWith.getSelectionModel().getSelectedItem();
        if (workstationColor == null) {
            Color[] humanColors = Color.humanColorValues();
            workstationColor = humanColors[RANDOM.nextInt(humanColors.length)];
        }
        WorkstationService.pairWorkstation(workstationColor);

        ((Stage)pairButton.getParent().getScene().getWindow()).close();
    }
}
