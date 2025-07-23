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
package com.michelin.throughputfxproject.test.service;

import com.michelin.throughputfxproject.entities.Color;
import com.michelin.throughputfxproject.entities.servers.HumanServer;
import com.michelin.throughputfxproject.entities.state.Board;
import com.michelin.throughputfxproject.entities.state.Workstation;
import com.michelin.throughputfxproject.services.WorkstationService;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class WorkstationServiceTest {
    @BeforeEach
    void setup() {
        // Reset Board and WorkstationService state if possible
        Board.initializeInstance(6,3,6,5);
        // Reflection or direct reset may be needed for static workstations
    }

    @Test
    void getWorkstations_initializesAndReturnsArray() {
        Workstation[] workstations = WorkstationService.getWorkstations();
        assertNotNull(workstations);
        assertEquals(Board.getInstance().getStationCount(), workstations.length);
    }

    @Test
    void getWorkstation_returnsCorrectWorkstationByColor() {
        Workstation[] workstations = WorkstationService.getWorkstations();
        Color color = workstations[0].getColor();
        Workstation found = WorkstationService.getWorkstation(color);
        assertNotNull(found);
        assertEquals(color, found.getColor());
    }

    @Test
    void automateWorkstation_addsRobotServer() {
        Workstation[] workstations = WorkstationService.getWorkstations();
        Color color = workstations[0].getColor();
        int initialSize = workstations[0].getServers().size();
        WorkstationService.automateWorkstation(color);
        assertTrue(workstations[0].getServers().size() > initialSize);
        boolean hasRobot = workstations[0].getServers().stream().anyMatch(s -> !(s instanceof HumanServer));
        assertTrue(hasRobot, "Workstation should have a robot server after automation");
    }

    @Test
    void getWorkstation_returnsNullForUnknownColor() {
        Color unknown = Color.valueOf("RED"); // Assuming RED is always present, use a non-existent if possible
        Workstation[] workstations = WorkstationService.getWorkstations();
        // Find a color not used by any workstation
        for (Color c : Color.values()) {
            boolean used = false;
            for (Workstation w : workstations) {
                if (w.getColor() == c) used = true;
            }
            if (!used) {
                unknown = c;
                break;
            }
        }
        assertNull(WorkstationService.getWorkstation(unknown));
    }
}

