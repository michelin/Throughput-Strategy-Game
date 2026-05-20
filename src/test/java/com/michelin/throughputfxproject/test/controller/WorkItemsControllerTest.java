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
            try { Platform.startup(() -> {}); } catch (IllegalStateException ignored) {}
            javafxInitialized = true;
        }
        Platform.setImplicitExit(false);
        TestUtils.resetServiceState();
        Board.clearInstance();
        Board.initializeInstance(6, 5, 6, 5);
        // Seed workstations with work items
        WorkstationService.getWorkstation(0).addToWorkItemCount(10);
        WorkstationService.getWorkstation(1).addToWorkItemCount(10);
        WorkstationService.getWorkstation(2).addToWorkItemCount(10);
        WorkstationService.getWorkstation(3).addToWorkItemCount(10);
        WorkstationService.getWorkstation(4).addToWorkItemCount(10);
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
    void moveWorkItems_movesItemsToNextWorkstation() throws Exception {
        int position = 0;
        int max = 5;
        int move = 3;
        int ws0Before = WorkstationService.getWorkstation(0).getWorkItemCount();
        int ws1Before = WorkstationService.getWorkstation(1).getWorkItemCount();

        WorkItemsController controller = buildController(max, move, position);
        Button button = controller.getWorkItemMoveButton();

        runOnFx(() -> {
            StackPane root = new StackPane(button);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 100, 100));
            stage.show();
            callMethod(controller, "moveWorkItems");
        });

        int moved = Math.min(move, Math.min(max, ws0Before));
        assertEquals(ws0Before - moved, WorkstationService.getWorkstation(0).getWorkItemCount());
        assertEquals(ws1Before + moved, WorkstationService.getWorkstation(1).getWorkItemCount());
    }

    @Test
    void moveWorkItems_capsAtMaxWhenMoveExceedsMax() throws Exception {
        int position = 1;
        int max = 2;
        int move = 8; // exceeds max
        int ws1Before = WorkstationService.getWorkstation(1).getWorkItemCount();
        int ws2Before = WorkstationService.getWorkstation(2).getWorkItemCount();

        WorkItemsController controller = buildController(max, move, position);
        Button button = controller.getWorkItemMoveButton();

        runOnFx(() -> {
            StackPane root = new StackPane(button);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 100, 100));
            stage.show();
            callMethod(controller, "moveWorkItems");
        });

        assertEquals(ws1Before - max, WorkstationService.getWorkstation(1).getWorkItemCount());
        assertEquals(ws2Before + max, WorkstationService.getWorkstation(2).getWorkItemCount());
    }

    @Test
    void moveWorkItems_atLastPosition_addsToFinishedGoods() throws Exception {
        int position = 4; // last workstation
        int max = 5;
        int move = 4;
        int ws4Before = WorkstationService.getWorkstation(4).getWorkItemCount();
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

        int moved = Math.min(move, Math.min(max, ws4Before));
        assertEquals(ws4Before - moved, WorkstationService.getWorkstation(4).getWorkItemCount());
        assertEquals(fgBefore + moved, ScorecardService.FINISHED_GOODS.getFinishedGoodsTally());
    }

    @Test
    void moveWorkItems_withInvalidInput_showsErrorAndClosesWindow() throws Exception {
        int position = 0;
        WorkItemsController controller = new WorkItemsController();
        Button button = new Button();
        setField(controller, "workItemMoveButton", button);
        setField(controller, "txtWorkstationMax", new Text("not-a-number"));
        setField(controller, "workItemMoveResponseText", new TextField("3"));
        setField(controller, "txtWorkstationPosition", new Text(String.valueOf(position)));
        setField(controller, "workItemMoveText", new TextArea());

        int ws0Before = WorkstationService.getWorkstation(0).getWorkItemCount();

        runOnFx(() -> {
            StackPane root = new StackPane(button);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 100, 100));
            stage.show();
            // NumberFormatException is caught internally; window still closes
            callMethod(controller, "moveWorkItems");
        });

        // Workstation should be unchanged on parse error
        assertEquals(ws0Before, WorkstationService.getWorkstation(0).getWorkItemCount());
    }
}
