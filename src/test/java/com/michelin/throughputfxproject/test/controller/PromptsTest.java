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
package com.michelin.throughputfxproject.test.controller;

import com.michelin.throughputfxproject.controllers.BoardController;
import com.michelin.throughputfxproject.controllers.Prompts;
import com.michelin.throughputfxproject.entities.Color;
import com.michelin.throughputfxproject.entities.actions.Trap;
import com.michelin.throughputfxproject.entities.cards.BitCard;
import com.michelin.throughputfxproject.entities.servers.HumanServer;
import com.michelin.throughputfxproject.entities.servers.Server;
import com.michelin.throughputfxproject.entities.state.Die;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;


class PromptsTest {

    private static boolean javafxInitialized = false;
    CountDownLatch latch;

    @BeforeAll
    static void initToolkit() {
        if (!javafxInitialized) {
            Platform.startup(() -> {}); // This will start the JavaFX runtime
            javafxInitialized = true;
        }
    }

    @Test
    void alertWithoutBoardUpdate_doesNotThrow() throws InterruptedException {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> Prompts.alertWithoutBoardUpdate("Test Title", "Test Message", 10));
            latch.countDown();
        });
        assertTrue(latch.await(5, java.util.concurrent.TimeUnit.SECONDS));
    }

    @Test
    void drawBit_returnsNullOrBitCard() throws InterruptedException {
        Platform.runLater(() -> {
            Pane pane = new Pane();
            TextArea area = new TextArea();
            try (MockedStatic<com.michelin.throughputfxproject.services.DiceService> diceService = Mockito.mockStatic(com.michelin.throughputfxproject.services.DiceService.class)) {
                diceService.when(() -> com.michelin.throughputfxproject.services.DiceService.rollDie(Mockito.any())).thenReturn(Die.builder().sides(6).build());
                BitCard card = Prompts.drawBit(pane, 6, area, 7); // Should return null since 6 < 7
                assertNull(card);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            latch.countDown();
        });
        assertTrue(latch.await(5, java.util.concurrent.TimeUnit.SECONDS));
    }

    // The following methods require FXML and controller mocks, so we just check for no exceptions
    @Test
    void implementPairedProgramming_doesNotThrow() throws InterruptedException {
        Platform.runLater(() -> {
            Pane pane = new Pane();
            TextArea area = new TextArea();
            assertDoesNotThrow(() -> Prompts.implementPairedProgramming(pane, area));
            latch.countDown();
        });
        assertTrue(latch.await(5, java.util.concurrent.TimeUnit.SECONDS));
    }

    @Test
    void promptForAppliedTrap_setsExpectedText() throws InterruptedException {
        Platform.runLater(() -> {

            Trap trap = Mockito.mock(Trap.class);
            Mockito.when(trap.effected()).thenReturn("ServerX");
            Mockito.when(trap.mitigatedDuration()).thenReturn(String.valueOf(2));
            Mockito.when(trap.duration()).thenReturn(String.valueOf(3));
            TextArea area = new TextArea();
            Prompts.promptForAppliedTrap(trap, true, area);
            assertTrue(area.getText().contains("Mitigation"));
            Prompts.promptForAppliedTrap(trap, false, area);
            assertTrue(area.getText().contains("No Mitigation"));
            latch.countDown();
        });
        assertTrue(latch.await(5, java.util.concurrent.TimeUnit.SECONDS));
    }

    @Test
    void promptForFinishedGoodsAreNowFourPoints_doesNotThrow() throws InterruptedException {
        Platform.runLater(() -> {

            TextArea area = new TextArea();
            assertDoesNotThrow(() -> Prompts.promptForFinishedGoodsAreNowFourPoints(area));
            latch.countDown();
        });
        assertTrue(latch.await(5, java.util.concurrent.TimeUnit.SECONDS));
    }

    @Test
    void promptForPairRetry_doesNotThrow() throws InterruptedException {
        Platform.runLater(() -> {
            Server server = Mockito.mock(Server.class);
            Mockito.when(server.getColor()).thenReturn(Color.BLUE);
            TextArea area = new TextArea();
            assertDoesNotThrow(() -> Prompts.promptForPairRetry(server, area));
            latch.countDown();
        });
        assertTrue(latch.await(5, java.util.concurrent.TimeUnit.SECONDS));
    }

    @Test
    void promptForServerMoves_doesNotThrow() throws InterruptedException {
        Platform.runLater(() -> {
            Pane pane = new Pane();
            HumanServer server = Mockito.mock(HumanServer.class);
            BoardController controller = Mockito.mock(BoardController.class);
            assertDoesNotThrow(() -> Prompts.promptForServerMoves(pane, server, controller));
            latch.countDown();
        });
        assertTrue(latch.await(5, java.util.concurrent.TimeUnit.SECONDS));
    }

    @Test
    void promptForServerRetry_returnsBoolean() throws InterruptedException {
        Platform.runLater(() -> {

            Server server = Mockito.mock(Server.class);
            Mockito.when(server.getColor()).thenReturn(Color.BLUE);
            assertDoesNotThrow(() -> Prompts.promptForServerRetry(server));
            latch.countDown();
        });
        assertTrue(latch.await(5, java.util.concurrent.TimeUnit.SECONDS));
    }

    @Test
    void promptForWorkItemEstimates_doesNotThrow() throws InterruptedException {
        Platform.runLater(() -> {
            Pane pane = new Pane();
            assertDoesNotThrow(() -> Prompts.promptForWorkItemEstimates(pane));
            latch.countDown();
        });
        assertTrue(latch.await(5, java.util.concurrent.TimeUnit.SECONDS));
    }

    @BeforeEach
    void setUp() {
        // Reset the latch before each test
        latch = new CountDownLatch(1);
    }
}

