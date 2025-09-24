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

import com.michelin.throughputfxproject.entities.state.Board;
import com.michelin.throughputfxproject.entities.Color;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import lombok.Getter;

import java.util.Objects;

@Getter
public class SkillsController {

    @FXML
    private TextArea skillAddText;
    @FXML
    private Button skillAddButton;
    @FXML
    private ComboBox<Color> serverToAddSkills;
    @FXML
    private ComboBox<Color> skillsToAddToServer;


    @FXML
    protected void addSkillsToServer(ActionEvent ignoredActionEvent) {

        Color serverToAdd = serverToAddSkills.getSelectionModel().getSelectedItem();
        Objects.requireNonNull(serverToAdd);
        Color skillsToAdd = skillsToAddToServer.getSelectionModel().getSelectedItem();
        Objects.requireNonNull(skillsToAdd);
        Board.getInstance().putServerInTraining(serverToAdd, skillsToAdd);

        ((Stage)skillAddButton.getParent().getScene().getWindow()).close();

    }
}
