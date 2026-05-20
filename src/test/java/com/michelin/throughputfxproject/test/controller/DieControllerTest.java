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

import com.michelin.throughputfxproject.controllers.DieController;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class DieControllerTest {

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
        assertDoesNotThrow(DieController::new);
    }

    @Test
    void getters_returnNullForUninitializedFxmlFields() {
        DieController controller = new DieController();
        assertNull(controller.getDieImage());
        assertNull(controller.getDieHeaderText());
        assertNull(controller.getDieText());
    }

    @Test
    void dieHeaderText_canBeSetViaReflection() throws Exception {
        DieController controller = new DieController();
        Label label = new Label("Die Result");
        Field field = DieController.class.getDeclaredField("dieHeaderText");
        field.setAccessible(true);
        field.set(controller, label);
        assertEquals("Die Result", controller.getDieHeaderText().getText());
    }

    @Test
    void dieImage_canBeSetViaReflection() throws Exception {
        DieController controller = new DieController();
        ImageView imageView = new ImageView();
        Field field = DieController.class.getDeclaredField("dieImage");
        field.setAccessible(true);
        field.set(controller, imageView);
        assertNotNull(controller.getDieImage());
    }
}
