package com.michelin.throughputfxproject.services;

import com.michelin.throughputfxproject.Board;
import com.michelin.throughputfxproject.Color;
import com.michelin.throughputfxproject.entities.Die;
import com.michelin.throughputfxproject.entities.Server;
import com.michelin.throughputfxproject.entities.Workstation;
import com.michelin.throughputfxproject.entities.servers.HumanServer;
import lombok.NonNull;
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

    private static void createWorkstations(int workstationCount, int maxCapacity) {

        Die[] capacities = DiceService.getDice(maxCapacity, workstationCount);
        DiceService.rollDice(capacities);

        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Workstation capacity: {}", Arrays.toString(capacities));
        }

        Workstation[] localWorkstations = new Workstation[workstationCount];
        for (int i = 0; i < workstationCount; i++) {
            localWorkstations[i] = getNewWorkstation(ServerService.getHumanServer(Color.values()[i]), Color.values()[i], capacities[i].getValue());
        }
        WorkstationService.workstations = localWorkstations;
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Workstations: {}", Arrays.toString(WorkstationService.workstations));
        }
    }

    public static Workstation getWorkstation(Color color) {
        for (Workstation workstation : getWorkstations()) {
            if (workstation.getColor() == color) {
                return workstation;
            }
        }
        return null;
    }

    public static void automateWorkstation(@NonNull Color color) {
        Server server = ServerService.getRobotServer(color);
        Objects.requireNonNull(server);
        Workstation workstation = WorkstationService.getWorkstation(color);
        Objects.requireNonNull(workstation);
        workstation.getServers().add(server);
    }

    public static void pairWorkstation(Color color) {
        Objects.requireNonNull(getWorkstation(color)).getServers().add(ServerService.getPairPartnerInstance());
    }

    public static Workstation getWorkstation(int index) {
        if (index > getWorkstations().length - 1) {
            throw new IndexOutOfBoundsException(index + " > " + getWorkstations().length);
        }

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

    public static int tallyWorkInProcess() {
        return Arrays.stream(getWorkstations()).mapToInt(Workstation::getWorkItemCount).sum();
    }

    public static Workstation[] getWorkstations() {
        if (workstations == null) {
            createWorkstations(Board.FIVE_STATIONS, Board.SIX_SIDES);
        }
        return workstations;
    }
}
