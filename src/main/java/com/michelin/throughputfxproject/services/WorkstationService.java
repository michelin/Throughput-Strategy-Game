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

package com.michelin.throughputfxproject.services;

import com.michelin.throughputfxproject.entities.Color;
import com.michelin.throughputfxproject.entities.servers.*;
import com.michelin.throughputfxproject.entities.state.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Service class for managing workstations in the application.
 * This class provides methods for creating, retrieving, and manipulating workstations,
 * as well as handling their associated servers and states.
 * It integrates with other services such as `ServerService` and `DiceService`
 * to perform its operations.
 * <p>
 * The class is designed as a utility with static methods and maintains a static
 * array of workstations. It uses Lombok annotations for boilerplate code reduction
 * and SLF4J for logging.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
@Slf4j
public class WorkstationService {

    private static Workstation[] workstations;

    /**
     * Automates a workstation by adding a robot server to its list of servers.
     * The workstation is identified by its color. If the workstation or robot server
     * is not found, a `NullPointerException` is thrown.
     *
     * @param color The color of the workstation to automate.
     * @throws NullPointerException If the workstation or robot server is not found.
     */
    public static void automateWorkstation(@NonNull Color color) {
        Objects.requireNonNull(getWorkstation(color)).getServers().add(Objects.requireNonNull(ServerService.getRobotServer(color)));
    }

    public static Workstation findLowestCapacityWorkstation() {
        Workstation lowest = null;
        int minCapacity = Integer.MAX_VALUE;
        for (Workstation workstation : getWorkstations()) {
            int capacity = workstation.getCapacity();
            if (lowest == null || capacity < minCapacity) {
                lowest = workstation;
                minCapacity = capacity;
            }
        }
        return lowest;
    }

    /**
     * Retrieves a workstation based on its color.
     *
     * @param color The color of the workstation to retrieve.
     * @return The workstation with the specified color, or null if no such workstation exists.
     */
    public static Workstation getWorkstation(Color color) {
        for (Workstation workstation : getWorkstations()) {
            if (workstation.getColor() == color) {
                return workstation;
            }
        }
        return null;
    }

    /**
     * Retrieves the array of all workstations. If the workstations have not been
     * initialized, they are created based on the station count and die faces from the Board.
     *
     * @return An array of all workstations.
     */
    public static Workstation[] getWorkstations() {
        if (workstations == null) {
            createWorkstations(Board.getInstance().getStationCount(), Board.getInstance().getDieFaces());
        }
        return workstations;
    }

    /**
     * Creates and initializes an array of workstations with specified count and maximum capacity.
     * The method generates workstation capacities using dice rolls, shuffles the workstation indices,
     * and assigns each workstation a human server, color, and capacity.
     * The created workstations are stored in the static `workstations` array.
     *
     * @param workstationCount The number of workstations to create.
     * @param maxCapacity      The maximum capacity for each workstation.
     */
    private static void createWorkstations(int workstationCount, int maxCapacity) {

        // Generate workstation capacities using dice rolls
        Die[] capacities = DiceService.getDice(maxCapacity, workstationCount);
        DiceService.rollDice(capacities);

        log.debug("Workstation capacity: {}", Arrays.toString(capacities));

        // Initialize the workstation array and shuffle indices
        Workstation[] localWorkstations = new Workstation[workstationCount];
        List<Integer> numbers = new ArrayList<>();
        IntStream.range(0, workstationCount).forEach(numbers::add);

        // Shuffle the list using the Collections.shuffle() method
        Collections.shuffle(numbers);
        AtomicInteger index = new AtomicInteger(0);

        // Assign each workstation a human server, color, and capacity
        numbers.forEach(workstationIndex -> localWorkstations[index.getAndIncrement()] =
                getNewWorkstation(
                        ServerService.getHumanServer(Color.values()[workstationIndex]),
                        Color.values()[workstationIndex],
                        capacities[workstationIndex].getValue()
                )
        );
        WorkstationService.workstations = localWorkstations;

        log.debug("Workstations: {}", Arrays.toString(WorkstationService.workstations));
    }

    /**
     * Creates a new workstation with the specified human server, color, and capacity.
     *
     * @param humanServer The human server to assign to the workstation.
     * @param color       The color representing the workstation.
     * @param capacity    The capacity of the workstation.
     * @return A new `Workstation` instance with the specified attributes.
     */
    private static Workstation getNewWorkstation(HumanServer humanServer, Color color, int capacity) {
        return new Workstation(humanServer, capacity, color);
    }

    /**
     * Finds and retrieves a list of all deployed automated servers across all workstations.
     * The method filters servers of type `TYPE_AUTOMATED` and casts them to `AutomatedServer`.
     *
     * @return A list of deployed `AutomatedServer` instances.
     */
    public static List<AutomatedServer> findDeployedAutomatedServers() {
        List<AutomatedServer> automatedServers = new ArrayList<>();
        Arrays.stream(getWorkstations()).forEach(workstation -> automatedServers.addAll(workstation.getServers().stream().filter(server -> server.getType().equals(Server.TYPE_AUTOMATED)).map(AutomatedServer.class::cast).toList()));
        return automatedServers;
    }

    /**
     * Checks if a pair partner server is already assigned to any workstation.
     * The method iterates through all workstations and their servers to find a server of type `TYPE_PARTNER`.
     *
     * @return `true` if a pair partner server is already assigned, otherwise `false`.
     */
    public static boolean findIfPairPartnerIsAlreadyAssigned() {
        AtomicBoolean assigned = new AtomicBoolean(false);
        Arrays.stream(getWorkstations()).forEach(workstation -> assigned.set(workstation.getServers().stream().anyMatch(server -> server.getType().equals(Server.TYPE_PARTNER))));
        return assigned.get();
    }

    /**
     * Retrieves a workstation by its index in the workstation array.
     *
     * @param index The index of the workstation to retrieve.
     * @return The `Workstation` instance at the specified index.
     */
    public static Workstation getWorkstation(int index) {
        return getWorkstations()[index];
    }

    /**
     * Finds the index of a workstation based on its color.
     *
     * @param color The color of the workstation to locate.
     * @return The index of the workstation with the specified color, or -1 if not found.
     */
    public static int getWorkstationIndex(Color color) {
        for (int i = 0; i < getWorkstations().length; i++) {
            Workstation workstation = getWorkstations()[i];
            if (workstation.getColor() == color) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Pairs a workstation by adding a pair partner server to its list of servers.
     * The workstation is identified by its color. If the workstation is not found,
     * a `NullPointerException` is thrown.
     *
     * @param color The color of the workstation to pair.
     * @throws NullPointerException If the workstation is not found.
     */
    public static void pairWorkstation(Color color) {
        Objects.requireNonNull(getWorkstation(color)).getServers().add(ServerService.getPairPartnerInstance());
    }

    /**
     * Reloads the workstations from a JSON-like map structure.
     * This method reconstructs the workstations, including their attributes and associated servers,
     * and stores them in the static `workstations` array.
     *
     * @param workstationServiceJson A map containing the JSON-like representation of the workstations.
     * @throws ClassCastException If the map structure does not match the expected format.
     */
    @SuppressWarnings("unchecked")
    public static void reloadWorkstations(Map<String, Object> workstationServiceJson) {
        List<Object> workstationsJson = (List<Object>) workstationServiceJson.get("workstations");
        workstations = new Workstation[workstationsJson.size()];
        AtomicInteger index = new AtomicInteger(0);
        workstationsJson.forEach(workstation -> {
            // Rebuild workstation attributes
            Color color = Color.valueOf((String) ((Map<String, Object>) workstation).get("color"));
            Integer capacity = (Integer) ((Map<String, Object>) workstation).get("capacity");
            Integer workItemCount = (Integer) ((Map<String, Object>) workstation).get("workItemCount");
            Boolean active = (Boolean) ((Map<String, Object>) workstation).get("active");
            // Rebuild servers
            List<Object> serversJson = (List<Object>) ((Map<String, Object>) workstation).get("servers");
            List<Server> newServers = new ArrayList<>();
            serversJson.forEach(serverMap -> newServers.add(ServerService.recreateServerFromMap((Map<String, Object>) serverMap)));
            int ndx = index.getAndIncrement();
            workstations[ndx] = new Workstation(newServers, capacity, color, workItemCount, active);
        });
    }

    /**
     * Removes a human server from all workstations where it is currently assigned.
     * The method iterates through all workstations and removes the specified server
     * from the list of servers for each workstation that contains it.
     *
     * @param server The `HumanServer` instance to be removed from the workstations.
     */
    public static void removeHumanServerFromWorkstation(HumanServer server) {
        Arrays.stream(getWorkstations()).filter(workstation -> workstation.getServers().contains(server)).forEach(workstation -> workstation.getServers().remove(server));
    }

    /**
     * Calculates the total number of work items currently in process across all workstations.
     * The method sums up the `workItemCount` of all workstations.
     *
     * @return The total number of work items in process.
     */
    public static int tallyWorkInProcess() {
        return Arrays.stream(getWorkstations()).mapToInt(Workstation::getWorkItemCount).sum();
    }

    /**
     * Calculates the total work-in-process score across all workstations.
     * The score is computed by multiplying the `workItemCount` of each workstation
     * by a weight based on its position and the number of die faces on the board.
     *
     * @return The total work-in-process score as a float.
     */
    public static float tallyWorkInProcessScore() {
        float totalScore = 0;
        for (int i = 0; i < getWorkstations().length; i++) {
            Workstation workstation = getWorkstations()[i];
            totalScore = totalScore + workstation.getWorkItemCount() * ((float) (i + 2) / Board.getInstance().getDieFaces());
        }
        return totalScore;
    }

    /**
     * Converts the workstation service data to a JSON-like string representation.
     * The method serializes all workstations into a JSON format and wraps them
     * in a JSON object representing the workstation service.
     *
     * @return A JSON-like string representation of the workstation service.
     */
    public static String toJSON() {
        List<String> stringList = Arrays.stream(getWorkstations()).map(Workstation::toJSON).toList();
        return "\"workstationService\": {\"workstations\": [" + String.join(",", stringList) + "]}";
    }
}
