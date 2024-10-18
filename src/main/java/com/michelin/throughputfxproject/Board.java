package com.michelin.throughputfxproject;

import com.michelin.throughputfxproject.entities.*;
import com.michelin.throughputfxproject.entities.cards.BitCard;
import com.michelin.throughputfxproject.entities.servers.HumanServer;
import com.michelin.throughputfxproject.entities.servers.ServerMove;
import com.michelin.throughputfxproject.services.ScorecardService;
import com.michelin.throughputfxproject.services.WorkstationService;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Board {
    public static final Logger LOGGER = LoggerFactory.getLogger(Board.class.getName());
    public static final int SIX_SIDES = 6;
    public static final int FIVE_STATIONS = 5;
    public static final int RUN_WEEKS = 5;
    public static final int RUN_DAYS = 5;
    public static final String NONE = "NONE";
    public static final String DAY = "DAY";
    public static final String TEAM = "TEAM";
    public static final String ANY_SERVER = "ANY_SERVER";
    public static final String HUMAN_SERVER = "HUMAN_SERVER";
    public static final String WEEK = "WEEK";

    //Play containers - Holders of game status
    @Getter
    private static final List<BitCard> weekHoldCards = new ArrayList<>(10);
    @Getter
    private static final List<BitCard> gameHoldCards = new ArrayList<>(10);
    @Getter
    private static Integer dayOfTheWeek = 0;
    @Getter
    private static Integer gameWeek = 0;
    @Getter
    @Setter
    private static HumanServer inTrainingServer = null;


    private Board() {

    }


    private static void addServer(@NonNull Server serverToMove, @NonNull Color color) {

        if (!serverToMove.getSkills().contains(color)) {
            throw new IllegalArgumentException("Server must match workstation or have a skill that matches workstation");
        }
        Objects.requireNonNull(WorkstationService.getWorkstation(color)).getServers().add(serverToMove);
    }


    public static BoardAction discoverBitActions(BitCard bitCard, int runDay, int runWeek)  {

        //Reduce complexity of calling method by passing along null
        if (bitCard == null) {
            return null;
        }

        LOGGER.debug("Executing bit actions {}", bitCard);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("{} card: {}", Card.BOOSTER_INOCULATE_TRAP, bitCard);
        }
        switch (bitCard.getAction()) {
            case 1:
                weekHoldCards.add(bitCard);
                return null;
            case 2:
                return new HelpAction(HelpAction.HelpActionType.ADD_ONE);
            case 3:
                return new HelpAction(HelpAction.HelpActionType.DOUBLE);
            case 4:
                return new HelpAction(HelpAction.HelpActionType.AUTOMATE);
            case 5:
            case 6:
            case 7:
                gameHoldCards.add(bitCard);
                return null;
            case 8:
                return new HelpAction(HelpAction.HelpActionType.AUGMENT);
            case 9:
                return new HelpAction(HelpAction.HelpActionType.PAIR);
            case 10:
                //Team loses the day
                return new Trap(TEAM, DAY, runWeek, runDay, NONE);
            case 11:
                //Skip the next server
                return new Trap(ANY_SERVER, DAY, runWeek, runDay, NONE);
            case 12:
                returnFinishedGoodsToBacklog();
                return null;
            case 13:
                moveWorkItemsFromColorWorkstationToPrevious(bitCard);
                return null;
            case 14:
                //Skip the next human server
                return new Trap(HUMAN_SERVER, DAY, runWeek, runDay, NONE);
            case 15:
                //Team loses the week
                return new Trap(TEAM, WEEK, runWeek, runDay, DAY);
            default:
                return null;
        }
    }


    public static HumanServer findAndRemoveServer(@NonNull Color serverColor) {
        for (Workstation workstation : WorkstationService.getWorkstations()) {
            HumanServer serverToMove = (HumanServer) workstation.getServers().stream().filter(server -> server.getColor().equals(serverColor) && server.getType().equals(Server.TYPE_HUMAN)).findAny().orElse(null);
            if (serverToMove != null && workstation.getServers().remove(serverToMove)) {
                return serverToMove;
            }
        }
        return null;
    }


    public static void augmentDayOfTheWeek() {
        dayOfTheWeek++;
    }

    public static void augmentGameWeek() {
        gameWeek++;
    }


    public static boolean isTrapMitigated(BitCard bitCard) {
        return gameHoldCards.removeIf(card -> card.getId() == bitCard.getCounterCard());
    }

    private static void moveWorkItemsFromColorWorkstationToPrevious(BitCard bitCard) {
        Color color = Color.BLUE;
        try {
            color = Color.valueOf(bitCard.getDescription());
        } catch (IllegalArgumentException e) {
            LOGGER.error("Couldn't find the color {}", bitCard.getDescription(), e);
        }
        int offendingWorkstationIndex = WorkstationService.getWorkstationIndex(color);
        Workstation offendingWorkstation = WorkstationService.getWorkstation(offendingWorkstationIndex);
        if (offendingWorkstationIndex == 0) {
            ScorecardService.getBacklog().addToBacklog(offendingWorkstation.getWorkItemCount());
            offendingWorkstation.setWorkItemCount(0);
        } else if (offendingWorkstationIndex < 0) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Couldn't find workstation for color {}", color.name());
            }
        } else {
            Workstation receivingWorkstation = WorkstationService.getWorkstation(offendingWorkstationIndex - 1);
            receivingWorkstation.addToWorkItemCount(offendingWorkstation.getWorkItemCount());
            offendingWorkstation.setWorkItemCount(0);
        }
    }


    private static void returnFinishedGoodsToBacklog() {
        ScorecardService.getBacklog().addToBacklog(ScorecardService.getFinishedGoods().getFinishedGoodsTally());
        ScorecardService.getFinishedGoods().setFinishedGoodsTally(0);
    }

    public static void returnServerToWorkstation() {
        if (inTrainingServer == null) {
            return;
        }
        Workstation workstation = WorkstationService.getWorkstation(inTrainingServer.getColor());
        if (workstation != null) {
            workstation.getServers().add(inTrainingServer);
        }
        inTrainingServer = null;
    }


    public static void startDay(@NonNull ServerMove move) throws IllegalArgumentException{
        HumanServer serverToMove = findAndRemoveServer(move.getServerColor());
        addServer(Objects.requireNonNull(serverToMove), move.getWorkstationColor());
    }


}
