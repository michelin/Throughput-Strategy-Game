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

import com.michelin.throughputfxproject.controllers.BITController;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class BITControllerTest {

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
        assertDoesNotThrow(BITController::new);
    }

    @Test
    void getters_returnNullForUninitializedFxmlFields() {
        BITController controller = new BITController();
        assertNull(controller.getCardTitle());
        assertNull(controller.getCardSubtitle());
        assertNull(controller.getCardReason());
        assertNull(controller.getCardDescriptionTitle());
        assertNull(controller.getDescriptionImage());
        assertNull(controller.getCardDescription());
        assertNull(controller.getCardInstructions());
    }

    @Test
    void fxmlFields_canBeSetViaReflection() throws Exception {
        BITController controller = new BITController();
        Label label = new Label("test");
        Field field = BITController.class.getDeclaredField("cardTitle");
        field.setAccessible(true);
        field.set(controller, label);
        assertEquals("test", controller.getCardTitle().getText());
    }

    @Test
    void descriptionImageField_canBeSetViaReflection() throws Exception {
        BITController controller = new BITController();
        ImageView imageView = new ImageView();
        Field field = BITController.class.getDeclaredField("descriptionImage");
        field.setAccessible(true);
        field.set(controller, imageView);
        assertNotNull(controller.getDescriptionImage());
    }
}
