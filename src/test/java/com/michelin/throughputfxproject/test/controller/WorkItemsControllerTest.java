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
import com.michelin.throughputfxproject.entities.state.Workstation;
import com.michelin.throughputfxproject.services.ScorecardService;
import com.michelin.throughputfxproject.controllers.WorkItemsController;
import com.michelin.throughputfxproject.services.WorkstationService;
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

class WorkItemsControllerTest {

    private static boolean javafxInitialized = false;

    @BeforeEach
    void setUp() {
        if (!javafxInitialized) {
            try { Platform.startup(() -> {}); } catch (IllegalStateException _) {//It's a test, so we can ignore this exception which just means FX is already running.
            }
            javafxInitialized = true;
        }
        Platform.setImplicitExit(false);
        TestUtils.resetServiceState();
        Board.clearInstance();
        Board.initializeInstance(6, 5, 6, 5);

        // Keep capacities deterministic for queue-flow assertions.
        for (Workstation workstation : WorkstationService.getWorkstations()) {
            workstation.setCapacity(20);
        }

        // Seed source queues for workstation positions 1..4.
        Board.getInstance().addToQueueCount(0, 10);
        Board.getInstance().addToQueueCount(1, 10);
        Board.getInstance().addToQueueCount(2, 10);
        Board.getInstance().addToQueueCount(3, 10);
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

    private WorkItemsController buildController(int max, int move, int position) throws Exception {
        WorkItemsController controller = new WorkItemsController();
        Button button = new Button();
        setField(controller, "workItemMoveButton", button);
        setField(controller, "txtWorkstationMax", new Text(String.valueOf(max)));
        setField(controller, "workItemMoveResponseText", new TextField(String.valueOf(move)));
        setField(controller, "txtWorkstationPosition", new Text(String.valueOf(position)));
        setField(controller, "workItemMoveText", new TextArea());
        return controller;
    }

    @Test
    void moveWorkItems_movesItemsToNextQueue() throws Exception {
        int position = 1;
        int max = 5;
        int move = 3;
        int sourceBefore = Board.getInstance().getQueueCount(position - 1);
        int destinationBefore = Board.getInstance().getQueueCount(position);

        WorkItemsController controller = buildController(max, move, position);
        Button button = controller.getWorkItemMoveButton();

        runOnFx(() -> {
            StackPane root = new StackPane(button);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 100, 100));
            stage.show();
            callMethod(controller, "moveWorkItems");
        });

        int moved = Math.min(move, Math.min(max, sourceBefore));
        assertEquals(sourceBefore - moved, Board.getInstance().getQueueCount(position - 1));
        assertEquals(destinationBefore + moved, Board.getInstance().getQueueCount(position));
    }

    @Test
    void moveWorkItems_capsAtMaxWhenMoveExceedsMax() throws Exception {
        int position = 2;
        int max = 2;
        int move = 8; // exceeds max
        int sourceBefore = Board.getInstance().getQueueCount(position - 1);
        int destinationBefore = Board.getInstance().getQueueCount(position);

        WorkItemsController controller = buildController(max, move, position);
        Button button = controller.getWorkItemMoveButton();

        runOnFx(() -> {
            StackPane root = new StackPane(button);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 100, 100));
            stage.show();
            callMethod(controller, "moveWorkItems");
        });

        assertEquals(sourceBefore - max, Board.getInstance().getQueueCount(position - 1));
        assertEquals(destinationBefore + max, Board.getInstance().getQueueCount(position));
    }

    @Test
    void moveWorkItems_atLastPosition_addsToFinishedGoods() throws Exception {
        int position = 4; // last workstation
        int max = 5;
        int move = 4;
        int sourceBefore = Board.getInstance().getQueueCount(position - 1);
        int fgBefore = ScorecardService.FINISHED_GOODS.getFinishedGoodsTally();

        WorkItemsController controller = buildController(max, move, position);
        Button button = controller.getWorkItemMoveButton();

        runOnFx(() -> {
            StackPane root = new StackPane(button);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 100, 100));
            stage.show();
            callMethod(controller, "moveWorkItems");
        });

        int moved = Math.min(move, Math.min(max, sourceBefore));
        assertEquals(sourceBefore - moved, Board.getInstance().getQueueCount(position - 1));
        assertEquals(fgBefore + moved, ScorecardService.FINISHED_GOODS.getFinishedGoodsTally());
    }

    @Test
    void moveWorkItems_withInvalidInput_showsErrorAndClosesWindow() throws Exception {
        int position = 1;
        WorkItemsController controller = new WorkItemsController();
        Button button = new Button();
        setField(controller, "workItemMoveButton", button);
        setField(controller, "txtWorkstationMax", new Text("not-a-number"));
        setField(controller, "workItemMoveResponseText", new TextField("3"));
        setField(controller, "txtWorkstationPosition", new Text(String.valueOf(position)));
        setField(controller, "workItemMoveText", new TextArea());

        int sourceBefore = Board.getInstance().getQueueCount(position - 1);

        runOnFx(() -> {
            StackPane root = new StackPane(button);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 100, 100));
            stage.show();
            // NumberFormatException is caught internally; window still closes
            callMethod(controller, "moveWorkItems");
        });

        // Source queue should be unchanged on parse error.
        assertEquals(sourceBefore, Board.getInstance().getQueueCount(position - 1));
    }
}
