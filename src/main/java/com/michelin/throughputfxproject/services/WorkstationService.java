package com.michelin.throughputfxproject.services;

import com.michelin.throughputfxproject.entities.Color;
import com.michelin.throughputfxproject.entities.servers.AutomatedServer;
import com.michelin.throughputfxproject.entities.servers.HumanServer;
import com.michelin.throughputfxproject.entities.servers.Server;
import com.michelin.throughputfxproject.entities.state.Board;
import com.michelin.throughputfxproject.entities.state.Die;
import com.michelin.throughputfxproject.entities.state.Workstation;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;


@SuppressWarnings("SameParameterValue")
public class WorkstationService {
    public static final Logger LOGGER = LoggerFactory.getLogger(WorkstationService.class.getName());
    private static Workstation[] workstations;

    private WorkstationService() {
        super();
    }

    public static void automateWorkstation(@NonNull Color color) {

        Objects.requireNonNull(getWorkstation(color)).getServers().add(Objects.requireNonNull(ServerService.getRobotServer(color)));
    }

    public static Workstation getWorkstation(Color color) {
        for (Workstation workstation : getWorkstations()) {
            if (workstation.getColor() == color) {
                return workstation;
            }
        }
        return null;
    }

    public static Workstation[] getWorkstations() {
        if (workstations == null) {
            createWorkstations(Board.getInstance().getStationCount(), Board.getInstance().getDieFaces());
        }
        return workstations;
    }

    private static void createWorkstations(int workstationCount, int maxCapacity) {

        Die[] capacities = DiceService.getDice(maxCapacity, workstationCount);
        DiceService.rollDice(capacities);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Workstation capacity: {}", Arrays.toString(capacities));
        }

        Workstation[] localWorkstations = new Workstation[workstationCount];
        List<Integer> numbers = new ArrayList<>();
        IntStream.range(0, workstationCount).forEach(numbers::add);

        // Shuffle the list using the Collections.shuffle() method
        Collections.shuffle(numbers);
        AtomicInteger index = new AtomicInteger(0);
        numbers.forEach(workstationIndex -> localWorkstations[index.getAndIncrement()] = getNewWorkstation(ServerService.getHumanServer(Color.values()[workstationIndex]), Color.values()[workstationIndex], capacities[workstationIndex].getValue()));
        WorkstationService.workstations = localWorkstations;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Workstations: {}", Arrays.toString(WorkstationService.workstations));
        }
    }

    private static Workstation getNewWorkstation(HumanServer humanServer, Color color, int capacity) {
        return new Workstation(humanServer, capacity, color);
    }

    public static List<AutomatedServer> findDeployedAutomatedServers() {
        List<AutomatedServer> automatedServers = new ArrayList<>();
        Arrays.stream(getWorkstations()).forEach(workstation -> automatedServers.addAll(workstation.getServers().stream().filter(server -> server.getType().equals(Server.TYPE_AUTOMATED)).map(AutomatedServer.class::cast).toList()));
        return automatedServers;
    }

    public static boolean findIfPairPartnerIsAlreadyAssigned() {
        AtomicBoolean assigned = new AtomicBoolean(false);
        Arrays.stream(getWorkstations()).forEach(workstation -> assigned.set(workstation.getServers().stream().anyMatch(server -> server.getType().equals(Server.TYPE_PARTNER))));
        return assigned.get();
    }

    public static Workstation getWorkstation(int index) {
        return getWorkstations()[index];
    }

    public static int getWorkstationIndex(Color color) {
        for (int i = 0; i < getWorkstations().length; i++) {
            Workstation workstation = getWorkstations()[i];
            if (workstation.getColor() == color) {
                return i;
            }
        }
        return -1;
    }

    public static void pairWorkstation(Color color) {
        Objects.requireNonNull(getWorkstation(color)).getServers().add(ServerService.getPairPartnerInstance());
    }

    @SuppressWarnings("unchecked")
    public static void reloadWorkstations(Map<String, Object> workstationServiceJson) {
        List<Object> workstationsJson = (List<Object>) workstationServiceJson.get("workstations");
        workstations = new Workstation[workstationsJson.size()];
        AtomicInteger index = new AtomicInteger(0);
        workstationsJson.forEach(workstation -> {
            //rebuild workstation
            Color color = Color.valueOf((String) ((Map<String, Object>) workstation).get("color"));
            Integer capacity = (Integer) ((Map<String, Object>) workstation).get("capacity");
            Integer workItemCount = (Integer) ((Map<String, Object>) workstation).get("workItemCount");
            Boolean active = (Boolean) ((Map<String, Object>) workstation).get("active");
            //Rebuild servers
            List<Object> serversJson = (List<Object>) ((Map<String, Object>) workstation).get("servers");
            List<Server> newServers = new ArrayList<>();
            serversJson.forEach(serverMap -> newServers.add(ServerService.recreateServerFromMap((Map<String, Object>) serverMap)));
            int ndx = index.getAndIncrement();
            workstations[ndx] = new Workstation(newServers,capacity, color,  workItemCount, active);
        });
    }

    public static void removeHumanServerFromWorkstation(HumanServer server) {
        Arrays.stream(getWorkstations()).filter(workstation -> workstation.getServers().contains(server)).forEach(workstation -> workstation.getServers().remove(server));
    }

    public static int tallyWorkInProcess() {
        return Arrays.stream(workstations).mapToInt(Workstation::getWorkItemCount).sum();
    }

    public static float tallyWorkInProcessScore() {
        float totalScore = 0;
        for (int i = 0; i < getWorkstations().length; i++) {
            Workstation workstation = getWorkstations()[i];
            totalScore = totalScore + workstation.getWorkItemCount() * ((float) (i + 2) / Board.getInstance().getDieFaces());
        }
        return totalScore;
    }

    public static String toJSON() {

        List<String> stringList = Arrays.stream(getWorkstations()).map(Workstation::toJSON).toList();
        return "\"workstationService\": {\"workstations\": [" + String.join(",", stringList) + "]}";

    }
}
