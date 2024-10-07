package com.michelin.throughputfxproject.services;

import com.michelin.throughputfxproject.Color;
import com.michelin.throughputfxproject.entities.Die;
import com.michelin.throughputfxproject.entities.Workstation;
import com.michelin.throughputfxproject.entities.servers.HumanServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Objects;


public class WorkstationService {
    public static final Logger LOGGER = LoggerFactory.getLogger(WorkstationService.class.getName());
    private static com.michelin.throughputfxproject.entities.Workstation[] workstations;

    private WorkstationService() {
        super();
    }

    private static Workstation getNewWorkstation(HumanServer humanServer, Color color, int capacity) {
        return new Workstation(humanServer, capacity, color);
    }

    public static void createWorkstations(int workstationCount, int maxCapacity) {

        Die[] capacities = DiceService.getDice(maxCapacity, workstationCount);
        DiceService.rollDice(capacities);
        LOGGER.debug("Workstation capacity: {}", Arrays.toString(capacities));

        Workstation[] workstations = new Workstation[workstationCount];
        for (int i = 0; i < workstationCount; i++) {
            workstations[i] = getNewWorkstation(ServerService.getHumanServer(Color.values()[i]), Color.values()[i], capacities[i].getValue());
        }
        WorkstationService.workstations = workstations;
        LOGGER.debug("Workstations: {}", Arrays.toString(workstations));
    }

    public static Workstation getWorkstation(Color color) {
        for (Workstation workstation : workstations) {
            if (workstation.getColor() == color) {
                return workstation;
            }
        }
        return null;
    }

    public static void automateWorkstation(Color color){
        Objects.requireNonNull(getWorkstation(color)).getServers().add(ServerService.getRobotServer(color));
    }

    public static void pairWorkstation(Color color){
        Objects.requireNonNull(getWorkstation(color)).getServers().add(ServerService.getPairPartnerInstance());
    }

    public static Workstation getWorkstation(int index) {
            if (index > workstations.length-1) {
                throw new IndexOutOfBoundsException(index + " > " + workstations.length);
            }

        return workstations[index];
    }

    public static int getWorkstationIndex(Color color) {
        for (int i = 0; i < workstations.length; i++) {
            Workstation workstation = workstations[i];
            if (workstation.getColor() == color) {
                return i;
            }
        }
        return -1;
    }

    public static int tallyWorkInProcess(){
        return Arrays.stream(workstations).mapToInt(Workstation::getWorkItemCount).sum();
    }

    public static Workstation[] getWorkstations() {
        return workstations;
    }
}
