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

import com.michelin.throughputfxproject.controllers.SkillCardController;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class SkillCardControllerTest {

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
        assertDoesNotThrow(SkillCardController::new);
    }

    @Test
    void getters_returnNullForUninitializedFxmlFields() {
        SkillCardController controller = new SkillCardController();
        assertNull(controller.getIsSuccessful());
        assertNull(controller.getSkillChangeButton());
        assertNull(controller.getCardSkill());
        assertNull(controller.getCardInstructions());
        assertNull(controller.getCardInstructionsExtended());
    }

    @Test
    void cardSkill_canBeSetViaReflection() throws Exception {
        SkillCardController controller = new SkillCardController();
        Label label = new Label("Java");
        Field field = SkillCardController.class.getDeclaredField("cardSkill");
        field.setAccessible(true);
        field.set(controller, label);
        assertEquals("Java", controller.getCardSkill().getText());
    }

    @Test
    void isSuccessful_canBeSetViaReflection() throws Exception {
        SkillCardController controller = new SkillCardController();
        Text text = new Text("Success");
        Field field = SkillCardController.class.getDeclaredField("isSuccessful");
        field.setAccessible(true);
        field.set(controller, text);
        assertEquals("Success", controller.getIsSuccessful().getText());
    }
}
