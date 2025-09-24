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

package com.michelin.throughputfxproject.entities.state;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.michelin.throughputfxproject.entities.Color;
import com.michelin.throughputfxproject.entities.servers.HumanServer;
import com.michelin.throughputfxproject.entities.servers.Server;
import com.michelin.throughputfxproject.exceptions.ThroughputRuntimeException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Represents a `Workstation` entity that implements the `Savable` interface.
 * This class models a workstation with attributes such as color, servers, capacity,
 * work item count, and active status. It provides methods to manage its state
 * and serialize it to a JSON format.
 */
@Getter
@Setter
@Slf4j
@EqualsAndHashCode
@ToString
public class Workstation implements Savable {

    private Color color;
    private Set<Server> servers;
    private int capacity;
    private int workItemCount;
    private boolean active = true;


    /**
     * Constructs a `Workstation` with a single human server, capacity, and color.
     *
     * @param server   A non-null `HumanServer` to be added to the workstation.
     * @param capacity The maximum capacity of the workstation.
     * @param color    A non-null `Color` representing the workstation's color.
     */
    public Workstation(@NonNull HumanServer server, int capacity, @NonNull Color color) {
        this.servers = new HashSet<>();
        this.servers.add(server);
        this.capacity = capacity;
        this.color = color;
    }

    /**
     * Constructs a `Workstation` with a collection of servers, capacity, color,
     * initial work item count, and active status.
     *
     * @param serverList    A collection of `Server` objects to initialize the workstation.
     * @param capacity      The maximum capacity of the workstation.
     * @param color         A non-null `Color` representing the workstation's color.
     * @param workItemCount The initial number of work items in the workstation.
     * @param active        A boolean indicating whether the workstation is active.
     */
    public Workstation(Collection<Server> serverList, int capacity, @NonNull Color color, int workItemCount, boolean active) {
        this.servers = new HashSet<>(serverList);
        this.capacity = capacity;
        this.color = color;
        this.active = active;
        this.workItemCount = workItemCount;
    }

    /**
     * Increases the work item count by the specified amount.
     * Throws an assertion error if the amount is negative.
     *
     * @param amount The number of work items to add. Must be non-negative.
     */
    public void addToWorkItemCount(int amount) {
        if (amount < 0) throw new AssertionError();
        workItemCount += amount;
    }

    /**
     * Retrieves the color of the workstation.
     * If the workstation is inactive, returns `Color.GRAY`.
     *
     * @return The current color of the workstation, or `Color.GRAY` if inactive.
     */
    public @NonNull Color getColor() {
        if (!active) return Color.GRAY;
        return color;
    }

    /**
     * Checks if the workstation has any human servers.
     *
     * @return `true` if the workstation contains at least one human server, otherwise `false`.
     */
    public boolean hasHumanServers() {
        return servers.stream().anyMatch(server -> server.getType().equals(Server.TYPE_HUMAN));
    }

    /**
     * Decreases the work item count by the specified amount.
     * Throws an assertion error if the amount is negative or greater than the current work item count.
     *
     * @param amount The number of work items to subtract. Must be non-negative and not exceed the current work item count.
     */
    public void subtractFromWorkItemCount(int amount) {
        if (amount < 0) throw new AssertionError();
        if (amount > workItemCount) throw new AssertionError();
        workItemCount -= amount;
    }

    /**
     * Converts the current `Workstation` object to its JSON representation.
     * Utilizes the Jackson library to serialize the object with pretty printing.
     * Logs the generated JSON string at the info level.
     *
     * @return A `String` containing the JSON representation of the `Workstation` object.
     * @throws ThroughputRuntimeException if a `JsonProcessingException` occurs during serialization.
     */
    public String toJSON() {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json;
        try {
            json = ow.writeValueAsString(this);
            log.info("Workstation {}", json);
        } catch (JsonProcessingException e) {
            throw new ThroughputRuntimeException(e);
        }
        return json;
    }
}
