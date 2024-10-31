package com.michelin.throughputfxproject.entities.state;

import com.michelin.throughputfxproject.entities.Color;
import com.michelin.throughputfxproject.entities.actions.BoardAction;
import com.michelin.throughputfxproject.entities.actions.HelpAction;
import com.michelin.throughputfxproject.entities.actions.Trap;
import com.michelin.throughputfxproject.entities.cards.BitCard;
import com.michelin.throughputfxproject.entities.cards.Card;
import com.michelin.throughputfxproject.entities.servers.HumanServer;
import com.michelin.throughputfxproject.entities.servers.Server;
import com.michelin.throughputfxproject.entities.servers.ServerMove;
import com.michelin.throughputfxproject.exceptions.ThroughputRuntimeException;
import com.michelin.throughputfxproject.services.ScorecardService;
import com.michelin.throughputfxproject.services.ServerService;
import com.michelin.throughputfxproject.services.WorkstationService;
import lombok.Getter;
import lombok.NonNull;
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
    public static final String WEEK = "WEEK";
    private static final String ANY_SERVER = "ANY_SERVER";
    private static final String HUMAN_SERVER = "HUMAN_SERVER";
    //Play containers - Holders of game status
    @Getter
    private static final List<BitCard> weekHoldCards = new ArrayList<>(10);
    @Getter
    private static final List<BitCard> gameHoldCards = new ArrayList<>(10);
    @Getter
    private static Integer dayOfTheWeek = 1;
    @Getter
    private static Integer gameWeek = 1;
    @Getter
    private static HumanServer inTrainingServer = null;


    private Board() {

    }

    public static void augmentDayOfTheWeek() {
        dayOfTheWeek++;
        if (dayOfTheWeek > RUN_DAYS) {
            resetDayOfTheWeek();
        }
    }

    public static void resetDayOfTheWeek() {
        dayOfTheWeek = 1;
    }

    public static void augmentGameWeek() {
        gameWeek++;
    }

    public static BoardAction discoverBitActions(BitCard bitCard, int runDay, int runWeek) {

        //Reduce complexity of calling method by passing along null
        if (bitCard == null) {
            return null;
        }

        LOGGER.debug("Executing bit actions {}", bitCard);
        LOGGER.debug("{} card: {}", Card.BOOSTER_INOCULATE_TRAP, bitCard);

        return switch (bitCard.getAction()) {
            case 1 -> {
                weekHoldCards.add(bitCard);
                yield null;
            }
            case 2 -> new HelpAction(HelpAction.HelpActionType.ADD_ONE);
            case 3 -> new HelpAction(HelpAction.HelpActionType.DOUBLE);
            case 4 -> new HelpAction(HelpAction.HelpActionType.AUTOMATE);
            case 5, 6, 7 -> {
                gameHoldCards.add(bitCard);
                yield null;
            }
            case 8 -> new HelpAction(HelpAction.HelpActionType.AUGMENT);
            case 9 -> new HelpAction(HelpAction.HelpActionType.PAIR);
            case 10 ->
                //Team loses the day
                    new Trap(TEAM, DAY, runWeek, runDay, NONE);
            case 11 ->
                //Skip the next server
                    new Trap(ANY_SERVER, DAY, runWeek, runDay, NONE);
            case 12 -> {
                returnFinishedGoodsToBacklog();
                yield null;
            }
            case 13 -> {
                moveWorkItemsFromColorWorkstationToPrevious(bitCard);
                yield null;
            }
            case 14 ->
                //Skip the next human server
                    new Trap(HUMAN_SERVER, DAY, runWeek, runDay, NONE);
            case 15 ->
                //Team loses the week
                    new Trap(TEAM, WEEK, runWeek, runDay, DAY);
            default -> null;
        };
    }

    private static void returnFinishedGoodsToBacklog() {
        ScorecardService.getBacklog().addToBacklog(ScorecardService.getFinishedGoods().getFinishedGoodsTally());
        ScorecardService.getFinishedGoods().setFinishedGoodsTally(0);
    }

    private static void moveWorkItemsFromColorWorkstationToPrevious(BitCard bitCard) {
        Color color;
        try {
            color = Color.valueOf(bitCard.getDescription());
        } catch (IllegalArgumentException e) {
            throw new ThroughputRuntimeException(e);
        }
        int offendingWorkstationIndex = WorkstationService.getWorkstationIndex(color);
        Workstation offendingWorkstation = WorkstationService.getWorkstation(offendingWorkstationIndex);
        if (offendingWorkstationIndex == 0) {
            ScorecardService.getBacklog().addToBacklog(offendingWorkstation.getWorkItemCount());
            offendingWorkstation.setWorkItemCount(0);
        } else if (offendingWorkstationIndex < 0) {
            LOGGER.warn("Couldn't find workstation for color {}", color.name());
        } else {
            Workstation receivingWorkstation = WorkstationService.getWorkstation(offendingWorkstationIndex - 1);
            receivingWorkstation.addToWorkItemCount(offendingWorkstation.getWorkItemCount());
            offendingWorkstation.setWorkItemCount(0);
        }
    }

    public static boolean isTrapMitigated(BitCard bitCard) {
        return gameHoldCards.removeIf(card -> card.getId() == bitCard.getCounterCard());
    }

    public static void returnServerToWorkstation() {
        if (inTrainingServer == null) {
            return;
        }
        Workstation workstation = WorkstationService.getWorkstation(inTrainingServer.getColor());
        if (workstation != null) workstation.getServers().add(inTrainingServer);

        inTrainingServer = null;
    }

    public static void setInTrainingServer(@NonNull Color serverColor, @NonNull Color skillColor) {
        HumanServer server = ServerService.getHumanServer(serverColor);
        server.getSkills().add(skillColor);
        WorkstationService.removeInTrainingServerFromWorkstation(server);
        inTrainingServer = server;
    }

    public static void startDay(@NonNull ServerMove move) {
        HumanServer serverToMove = findServer(move.getServerColor());
        if (addServer(Objects.requireNonNull(serverToMove), move.getWorkstationColor())) {
            removeServer(serverToMove);
        } else {
            throw new ThroughputRuntimeException(new IllegalArgumentException("Server must match workstation or have a skill that matches workstation"));
        }

    }

    private static HumanServer findServer(@NonNull Color serverColor) {
        for (Workstation workstation : WorkstationService.getWorkstations()) {
            HumanServer serverToMove = (HumanServer) workstation.getServers().stream().filter(server -> server.getColor().equals(serverColor) && server.getType().equals(Server.TYPE_HUMAN)).findAny().orElse(null);
            if (serverToMove != null) {
                return serverToMove;
            }
        }
        return null;
    }

    private static boolean addServer(@NonNull Server serverToMove, @NonNull Color color) {

        if (!serverToMove.getSkills().contains(color)) {
            return false;
        }
        Objects.requireNonNull(WorkstationService.getWorkstation(color)).getServers().add(serverToMove);
        return true;
    }

    private static void removeServer(@NonNull HumanServer serverToMove) {
        for (Workstation workstation : WorkstationService.getWorkstations()) {
            HumanServer serverToRemove = (HumanServer) workstation.getServers().stream().filter(server -> server.equals(serverToMove)).findAny().orElse(null);
            if (serverToRemove != null) {
                workstation.getServers().remove(serverToMove);
            }
        }


    }


}
