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
import com.michelin.throughputfxproject.controllers.InitialWorkItemsController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class InitialWorkItemsControllerTest {

    private static boolean javafxInitialized = false;

    @BeforeEach
    void setUp() {
        if (!javafxInitialized) {
            try { Platform.startup(() -> {}); } catch (IllegalStateException _) {
                //It's a test, so we can ignore this exception which just means FX is already running.
            }
            javafxInitialized = true;
        }
        Platform.setImplicitExit(false);
        TestUtils.resetServiceState();
        Board.clearInstance();
        Board.initializeInstance(6, 5, 6, 5);
        // Seed backlog with items
        ScorecardService.BACKLOG.addToBacklog(10);
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
    void moveInitialWorkItems_movesItemsToFirstQueue() throws Exception {
        int max = 3;
        int move = 2;
        int queueBefore = Board.getInstance().getQueueCount(0);
        int backlogBefore = ScorecardService.BACKLOG.getBacklogItemCount();

        InitialWorkItemsController controller = new InitialWorkItemsController();
        Button button = new Button();
        Text txtMax = new Text(String.valueOf(max));
        TextField txtMove = new TextField(String.valueOf(move));
        TextArea textArea = new TextArea();
        setField(controller, "workItemMoveButton", button);
        setField(controller, "txtWorkstationMax", txtMax);
        setField(controller, "workItemMoveResponseText", txtMove);
        setField(controller, "workItemMoveText", textArea);

        runOnFx(() -> {
            StackPane root = new StackPane(button);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 100, 100));
            stage.show();
            callMethod(controller, "moveInitialWorkItems");
        });

        int expected = Math.min(max, move);
        assertEquals(queueBefore + expected, Board.getInstance().getQueueCount(0));
        assertEquals(backlogBefore - expected, ScorecardService.BACKLOG.getBacklogItemCount());
    }

    @Test
    void moveInitialWorkItems_capsAtMaxWhenMoveExceedsMax() throws Exception {
        int max = 2;
        int move = 10; // exceeds max
        int queueBefore = Board.getInstance().getQueueCount(0);

        InitialWorkItemsController controller = new InitialWorkItemsController();
        Button button = new Button();
        Text txtMax = new Text(String.valueOf(max));
        TextField txtMove = new TextField(String.valueOf(move));
        TextArea textArea = new TextArea();
        setField(controller, "workItemMoveButton", button);
        setField(controller, "txtWorkstationMax", txtMax);
        setField(controller, "workItemMoveResponseText", txtMove);
        setField(controller, "workItemMoveText", textArea);

        runOnFx(() -> {
            StackPane root = new StackPane(button);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 100, 100));
            stage.show();
            callMethod(controller, "moveInitialWorkItems");
        });

        assertEquals(queueBefore + max, Board.getInstance().getQueueCount(0));
    }

    @Test
    void moveInitialWorkItems_withInvalidInput_doesNotChangeQueue() throws Exception {
        int queueBefore = Board.getInstance().getQueueCount(0);

        InitialWorkItemsController controller = new InitialWorkItemsController();
        Button button = new Button();
        Text txtMax = new Text("not-a-number");
        TextField txtMove = new TextField("2");
        TextArea textArea = new TextArea();
        setField(controller, "workItemMoveButton", button);
        setField(controller, "txtWorkstationMax", txtMax);
        setField(controller, "workItemMoveResponseText", txtMove);
        setField(controller, "workItemMoveText", textArea);

        runOnFx(() -> {
            StackPane root = new StackPane(button);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 100, 100));
            stage.show();
            callMethod(controller, "moveInitialWorkItems");
        });

        assertEquals(queueBefore, Board.getInstance().getQueueCount(0));
    }
}
