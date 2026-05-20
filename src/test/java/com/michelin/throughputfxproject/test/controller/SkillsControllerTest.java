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
package com.michelin.throughputfxproject.test.controller;

import com.michelin.throughputfxproject.test.TestUtils;
import com.michelin.throughputfxproject.entities.Color;
import com.michelin.throughputfxproject.entities.state.Board;
import com.michelin.throughputfxproject.controllers.SkillsController;
import com.michelin.throughputfxproject.services.WorkstationService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class SkillsControllerTest {

    private static boolean javafxInitialized = false;

    @BeforeEach
    void setUp() {
        if (!javafxInitialized) {
            try { Platform.startup(() -> {}); } catch (IllegalStateException ignored) {}
            javafxInitialized = true;
        }
        Platform.setImplicitExit(false);
        TestUtils.resetServiceState();
        Board.clearInstance();
        Board.initializeInstance(6, 5, 6, 5);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    /** Invokes a protected @FXML method by reflection, wrapping any checked exception. */
    private static void callMethod(Object target, String methodName) {
        try {
            Method m = target.getClass().getDeclaredMethod(methodName, ActionEvent.class);
            m.setAccessible(true);
            m.invoke(target, new ActionEvent());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void runOnFx(Runnable action) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try { action.run(); } finally { latch.countDown(); }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "FX action timed out");
    }

    @Test
    void addSkillsToServer_withNullServerSelection_throwsNPE() throws Exception {
        SkillsController controller = new SkillsController();
        Button button = new Button();
        ComboBox<Color> serverCombo = new ComboBox<>();  // nothing selected
        ComboBox<Color> skillCombo = new ComboBox<>();
        setField(controller, "skillAddButton", button);
        setField(controller, "serverToAddSkills", serverCombo);
        setField(controller, "skillsToAddToServer", skillCombo);
        setField(controller, "skillAddText", new TextArea());

        AtomicReference<Throwable> caughtEx = new AtomicReference<>();
        runOnFx(() -> {
            StackPane root = new StackPane(button);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 100, 100));
            stage.show();
            try {
                callMethod(controller, "addSkillsToServer");
            } catch (RuntimeException e) {
                caughtEx.set(e.getCause() != null ? e.getCause() : e);
            }
        });

        assertNotNull(caughtEx.get(), "Expected NullPointerException when server is not selected");
    }

    @Test
    void addSkillsToServer_withValidColors_putsServerInTraining() throws Exception {
        // Use the first workstation's color as the server to train
        Color serverColor = WorkstationService.getWorkstation(0).getColor();
        // Use a different color as the skill
        Color skillColor = WorkstationService.getWorkstation(1).getColor();

        SkillsController controller = new SkillsController();
        Button button = new Button();
        ComboBox<Color> serverCombo = new ComboBox<>();
        serverCombo.getItems().add(serverColor);
        serverCombo.getSelectionModel().select(serverColor);
        ComboBox<Color> skillCombo = new ComboBox<>();
        skillCombo.getItems().add(skillColor);
        skillCombo.getSelectionModel().select(skillColor);
        setField(controller, "skillAddButton", button);
        setField(controller, "serverToAddSkills", serverCombo);
        setField(controller, "skillsToAddToServer", skillCombo);
        setField(controller, "skillAddText", new TextArea());

        runOnFx(() -> {
            StackPane root = new StackPane(button);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 100, 100));
            stage.show();
            callMethod(controller, "addSkillsToServer");
        });

        // The board should now have an in-training server
        assertNotNull(Board.getInstance().getInTrainingServer());
    }
}
