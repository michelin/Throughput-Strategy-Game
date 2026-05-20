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
import com.michelin.throughputfxproject.controllers.AddAutomationController;
import com.michelin.throughputfxproject.services.WorkstationService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class AddAutomationControllerTest {

    private static boolean javafxInitialized = false;

    @BeforeEach
    void setUp() {
        if (!javafxInitialized) {
            try {
                Platform.startup(() -> {});
            } catch (IllegalStateException ignored) {
                // already started
            }
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

    /** Runs a block on the FX thread and waits up to 5 seconds for it to complete. */
    private void runOnFx(Runnable action) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "FX action timed out");
    }

    @Test
    void addAutomation_withSelectedColor_automatesChosenWorkstation() throws Exception {
        AddAutomationController controller = new AddAutomationController();
        Button button = new Button();
        ComboBox<Color> combo = new ComboBox<>();
        Color targetColor = WorkstationService.getWorkstation(0).getColor();
        combo.getItems().add(targetColor);
        combo.getSelectionModel().select(targetColor);
        setField(controller, "automationButton", button);
        setField(controller, "workstationToAddAutomation", combo);

        int before = WorkstationService.findDeployedAutomatedServers().size();

        runOnFx(() -> {
            StackPane root = new StackPane(button);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 100, 100));
            stage.show();
            controller.addAutomation(new ActionEvent());
        });

        assertEquals(before + 1, WorkstationService.findDeployedAutomatedServers().size());
    }

    @Test
    void addAutomation_withNoColorSelected_usesFirstLeftoverAutomatedColor() throws Exception {
        AddAutomationController controller = new AddAutomationController();
        Button button = new Button();
        ComboBox<Color> combo = new ComboBox<>();
        // deliberately leave selection empty
        setField(controller, "automationButton", button);
        setField(controller, "workstationToAddAutomation", combo);

        int before = WorkstationService.findDeployedAutomatedServers().size();

        runOnFx(() -> {
            StackPane root = new StackPane(button);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 100, 100));
            stage.show();
            controller.addAutomation(new ActionEvent());
        });

        // A leftover automated color should have been deployed
        assertEquals(before + 1, WorkstationService.findDeployedAutomatedServers().size());
    }

    @Test
    void addAutomation_whenAllAutomatedColorsDeployed_doesNotAddMore() throws Exception {
        // Deploy every possible automated color first
        for (Color c : Color.automatedColorValues()) {
            WorkstationService.automateWorkstation(c);
        }
        int allDeployed = WorkstationService.findDeployedAutomatedServers().size();

        AddAutomationController controller = new AddAutomationController();
        Button button = new Button();
        ComboBox<Color> combo = new ComboBox<>();
        setField(controller, "automationButton", button);
        setField(controller, "workstationToAddAutomation", combo);

        runOnFx(() -> {
            StackPane root = new StackPane(button);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 100, 100));
            stage.show();
            controller.addAutomation(new ActionEvent());
        });

        assertEquals(allDeployed, WorkstationService.findDeployedAutomatedServers().size());
    }
}
