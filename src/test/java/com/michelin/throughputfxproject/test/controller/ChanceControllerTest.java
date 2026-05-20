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

import com.michelin.throughputfxproject.controllers.ChanceController;
import javafx.application.Platform;
import javafx.scene.control.Label;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class ChanceControllerTest {

    private static boolean javafxInitialized = false;

    @BeforeEach
    void setUp() {
        if (!javafxInitialized) {
            try { Platform.startup(() -> {}); } catch (IllegalStateException ignored) {}
            javafxInitialized = true;
        }
    }

    @Test
    void controller_canBeInstantiated() {
        assertDoesNotThrow(ChanceController::new);
    }

    @Test
    void getters_returnNullForUninitializedFxmlFields() {
        ChanceController controller = new ChanceController();
        assertNull(controller.getCardChance());
        assertNull(controller.getCardInstructions());
    }

    @Test
    void cardChance_canBeSetViaReflection() throws Exception {
        ChanceController controller = new ChanceController();
        Label label = new Label("Roll the die!");
        Field field = ChanceController.class.getDeclaredField("cardChance");
        field.setAccessible(true);
        field.set(controller, label);
        assertEquals("Roll the die!", controller.getCardChance().getText());
    }

    @Test
    void cardInstructions_canBeSetViaReflection() throws Exception {
        ChanceController controller = new ChanceController();
        Label label = new Label("Follow these instructions");
        Field field = ChanceController.class.getDeclaredField("cardInstructions");
        field.setAccessible(true);
        field.set(controller, label);
        assertEquals("Follow these instructions", controller.getCardInstructions().getText());
    }
}
