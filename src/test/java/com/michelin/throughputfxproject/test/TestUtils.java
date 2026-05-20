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
package com.michelin.throughputfxproject.test;

import com.michelin.throughputfxproject.services.ServerService;
import com.michelin.throughputfxproject.services.WorkstationService;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * Test utility class for resetting static service state between tests.
 */
public final class TestUtils {

    private TestUtils() {}

    /**
     * Resets the static workstation and server state so that each test gets a clean slate.
     * Call this before Board.initializeInstance() in @BeforeEach setUp methods.
     */
    public static void resetServiceState() {
        try {
            // Reset WorkstationService.workstations
            Field wField = WorkstationService.class.getDeclaredField("workstations");
            wField.setAccessible(true);
            wField.set(null, null);

            // Clear ServerService.humanServers
            Field hField = ServerService.class.getDeclaredField("humanServers");
            hField.setAccessible(true);
            ((Collection<?>) hField.get(null)).clear();

            // Clear ServerService.automatedServers
            Field aField = ServerService.class.getDeclaredField("automatedServers");
            aField.setAccessible(true);
            ((Collection<?>) aField.get(null)).clear();

        } catch (Exception e) {
            throw new RuntimeException("Failed to reset service state", e);
        }
    }
}
