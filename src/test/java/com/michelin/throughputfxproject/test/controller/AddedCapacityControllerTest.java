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
import com.michelin.throughputfxproject.controllers.AddedCapacityController;
import com.michelin.throughputfxproject.entities.state.Workstation;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class AddedCapacityControllerTest {

    private static boolean javafxInitialized = false;

    @BeforeEach
    void setUp() {
        if (!javafxInitialized) {
            try {
                Platform.startup(() -> {});
            } catch (IllegalStateException ignored) {}
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

    private void runOnFx(Runnable action) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try { action.run(); } finally { latch.countDown(); }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "FX action timed out");
    }

    @Test
    void addCapacity_withSelectedColor_incrementsCapacityByOne() throws Exception {
        Workstation ws = WorkstationService.getWorkstation(0);
        Color color = ws.getColor();
        int initialCapacity = ws.getCapacity();

        AddedCapacityController controller = new AddedCapacityController();
        Button button = new Button();
        ComboBox<Color> combo = new ComboBox<>();
        combo.getItems().add(color);
        combo.getSelectionModel().select(color);
        TextArea text = new TextArea("add one capacity");
        setField(controller, "capacityButton", button);
        setField(controller, "workstationToAddCapacity", combo);
        setField(controller, "addCapacityText", text);

        runOnFx(() -> {
            StackPane root = new StackPane(button);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 100, 100));
            stage.show();
            controller.addCapacity(new ActionEvent());
        });

        assertEquals(Math.min(6, initialCapacity + 1), ws.getCapacity());
    }

    @Test
    void addCapacity_withDoubleText_doublesCapacity() throws Exception {
        Workstation ws = WorkstationService.getWorkstation(0);
        Color color = ws.getColor();
        int initialCapacity = ws.getCapacity();

        AddedCapacityController controller = new AddedCapacityController();
        Button button = new Button();
        ComboBox<Color> combo = new ComboBox<>();
        combo.getItems().add(color);
        combo.getSelectionModel().select(color);
        TextArea text = new TextArea("double the capacity");
        setField(controller, "capacityButton", button);
        setField(controller, "workstationToAddCapacity", combo);
        setField(controller, "addCapacityText", text);

        runOnFx(() -> {
            StackPane root = new StackPane(button);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 100, 100));
            stage.show();
            controller.addCapacity(new ActionEvent());
        });

        assertEquals(Math.min(6, initialCapacity * 2), ws.getCapacity());
    }

    @Test
    void addCapacity_withNoColorSelected_usesLowestCapacityWorkstation() throws Exception {
        // Find which workstation has the lowest capacity
        Workstation lowest = WorkstationService.findLowestCapacityWorkstation();
        int initialCapacity = lowest.getCapacity();

        AddedCapacityController controller = new AddedCapacityController();
        Button button = new Button();
        ComboBox<Color> combo = new ComboBox<>();
        // leave selection empty
        TextArea text = new TextArea("add one");
        setField(controller, "capacityButton", button);
        setField(controller, "workstationToAddCapacity", combo);
        setField(controller, "addCapacityText", text);

        runOnFx(() -> {
            StackPane root = new StackPane(button);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 100, 100));
            stage.show();
            controller.addCapacity(new ActionEvent());
        });

        assertEquals(Math.min(6, initialCapacity + 1), lowest.getCapacity());
    }
}
