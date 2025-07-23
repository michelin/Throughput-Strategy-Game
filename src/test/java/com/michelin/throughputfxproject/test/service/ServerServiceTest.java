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
import com.michelin.throughputfxproject.entities.servers.*;

import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static com.michelin.throughputfxproject.services.ServerService.*;

class ServerServiceTest {
    @Test
    void getHumanServer_createsAndRetrievesByColor() {
        Color color = Color.RED;
        HumanServer server1 = getHumanServer(color);
        HumanServer server2 = getHumanServer(color);
        assertNotNull(server1);
        assertSame(server1, server2, "Should return the same instance for the same color");
        assertEquals(color, server1.getColor());
    }

    @Test
    void getRobotServer_createsAndRetrievesByColor() {
        Color color = Color.BLUE;
        AutomatedServer server1 = getRobotServer(color);
        AutomatedServer server2 = getRobotServer(color);
        assertNotNull(server1);
        assertSame(server1, server2, "Should return the same instance for the same color");
        assertEquals(color, server1.getColor());
    }

    @Test
    void getPairPartnerInstance_returnsSingleton() {
        PairPartner p1 = getPairPartnerInstance();
        PairPartner p2 = getPairPartnerInstance();
        assertNotNull(p1);
        assertSame(p1, p2, "Should always return the same PairPartner instance");
    }

    @Test
    void recreateServerFromMap_createsCorrectServer() {
        Map<String, Object> map = new HashMap<>();
        map.put("color", "GREEN");
        map.put("type", "HUMAN");
        map.put("skills", List.of("RED", "BLUE"));
        Server server = recreateServerFromMap(map);
        assertNotNull(server);
        assertEquals(Color.GREEN, server.getColor());
        assertInstanceOf(HumanServer.class, server);
        assertEquals(2, server.getSkills().size());
        assertTrue(server.getSkills().contains(Color.RED));
        assertTrue(server.getSkills().contains(Color.BLUE));
    }

    @Test
    void recreateServerFromMap_nullMap_returnsNull() {
        assertNull(recreateServerFromMap(null));
    }
}

