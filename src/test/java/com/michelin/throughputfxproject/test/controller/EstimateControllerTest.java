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
import com.michelin.throughputfxproject.entities.state.Board;
import com.michelin.throughputfxproject.services.ScorecardService;
import com.michelin.throughputfxproject.controllers.EstimateController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class EstimateControllerTest {

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
    void applyEstimate_withValidNumber_setsEstimateAndAddsToBacklog() throws Exception {
        int estimateValue = 5;
        int backlogBefore = ScorecardService.BACKLOG.getBacklogItemCount();

        EstimateController controller = new EstimateController();
        Button button = new Button();
        TextField textField = new TextField(String.valueOf(estimateValue));
        setField(controller, "estimateButton", button);
        setField(controller, "estimateResponseText", textField);

        runOnFx(() -> {
            StackPane root = new StackPane(button);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 100, 100));
            stage.show();
            callMethod(controller, "applyEstimate");
        });

        assertEquals(estimateValue, ScorecardService.getScorecardForCurrentWeek().getEstimate());
        assertEquals(backlogBefore + estimateValue, ScorecardService.BACKLOG.getBacklogItemCount());
        assertTrue(controller.getEstimateButton().isDisabled());
    }

    @Test
    void applyEstimate_withZero_setsZeroEstimate() throws Exception {
        EstimateController controller = new EstimateController();
        Button button = new Button();
        TextField textField = new TextField("0");
        setField(controller, "estimateButton", button);
        setField(controller, "estimateResponseText", textField);

        runOnFx(() -> {
            StackPane root = new StackPane(button);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 100, 100));
            stage.show();
            callMethod(controller, "applyEstimate");
        });

        assertEquals(0, ScorecardService.getScorecardForCurrentWeek().getEstimate());
    }
}
