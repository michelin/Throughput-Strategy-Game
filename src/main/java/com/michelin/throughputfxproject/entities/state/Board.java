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
import com.michelin.throughputfxproject.services.CardService;
import com.michelin.throughputfxproject.services.ScorecardService;
import com.michelin.throughputfxproject.services.ServerService;
import com.michelin.throughputfxproject.services.WorkstationService;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Board implements Savable {

    public static final Logger LOGGER = LoggerFactory.getLogger(Board.class.getName());

    public static final String NONE = "NONE";
    public static final String RUN = "RUN";
    public static final String TEAM = "TEAM";
    public static final String PERIOD = "PERIOD";
    public static final String ANY_SERVER = "ANY_SERVER";
    private static final String HUMAN_SERVER = "HUMAN_SERVER";
    private static Board instance;
    //Play containers - Holders of game status
    private final List<BitCard> periodHoldCards;
    private final List<BitCard> gameHoldCards;
    private final int dieFaces;
    private final int stationCount;
    private final int runPeriods;
    private final int runTurns;
    private Integer currentRunTurn = 1;
    private Integer currentPeriod = 1;
    private HumanServer inTrainingServer = null;


    private Board(int dieFaces, int stationCount, int runPeriods, int runTurns) {
        this.dieFaces = dieFaces;
        this.stationCount = stationCount;
        this.runPeriods = runPeriods;
        this.runTurns = runTurns;
        this.periodHoldCards = new ArrayList<>(10);
        this.gameHoldCards = new ArrayList<>(10);
    }


    public static Board getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Board has not been initialized");
        }
        return instance;
    }

    public static void initializeInstance(int dieFaces, int stationCount, int runPeriods, int runTurns) {
        if (instance != null) {
            return;
        }
        instance = new Board(dieFaces, stationCount, runPeriods, runTurns);
    }

    @SuppressWarnings("unchecked")
    public static void reloadInstance(Map<String, Object> reloadGameJson) {
        var reDieFaces = (Integer) reloadGameJson.get("dieFaces");
        var reStationCount = (Integer) reloadGameJson.get("stationCount");
        var reRunPeriods = (Integer) reloadGameJson.get("runPeriods");
        var reRunTurns = (Integer) reloadGameJson.get("runTurns");
        var reInTrainingServer = (HumanServer) ServerService.recreateServerFromMap((Map<String, Object>) reloadGameJson.get("inTrainingServer"));
        var reCurrentRunTurn = (Integer) reloadGameJson.get("currentRunTurn");
        var reCurrentPeriod = (Integer) reloadGameJson.get("currentPeriod");

        var periodHoldCards= CardService.reloadHoldCards((List<Object>) reloadGameJson.get("periodHoldCards"));
        var gameHoldCards= CardService.reloadHoldCards( (List<Object>) reloadGameJson.get("gameHoldCards"));


        //Reload services
        WorkstationService.reloadWorkstations((Map<String,Object>) reloadGameJson.get("workstationService"));
        ScorecardService.reloadScorecards((Map<String,Object>) reloadGameJson.get("scorecardService"));
        CardService.reloadCards((List<Object>) reloadGameJson.get("bitDeck"));

        //Reload board
        instance = new Board(periodHoldCards, gameHoldCards, reDieFaces, reStationCount, reRunPeriods, reRunTurns,reCurrentRunTurn,reCurrentPeriod,reInTrainingServer);

    }

    public void augmentRunTurn(int turns) {
        for (int ndx = 0; ndx < turns; ndx++) {
            augmentRunTurn();
        }
    }

    public void augmentRunTurn() {
        currentRunTurn++;
        if (currentRunTurn > runTurns) {
            this.getPeriodHoldCards().clear();

            //Update Scorecard
            ScoreCard scoreCard = ScorecardService.getScorecardForCurrentWeek();
            scoreCard.setWorkInProcess(WorkstationService.tallyWorkInProcess());
            scoreCard.setFinishedGoods(ScorecardService.getFinishedGoods().getFinishedGoodsTally());
            scoreCard.setScore(ScorecardService.currentWeekRunningScore());
            //Remove finished Goods
            ScorecardService.getFinishedGoods().setFinishedGoodsTally(0);

            augmentRunPeriod();
            resetRunTurns();
        }
    }

    private void augmentRunPeriod() {
        currentPeriod++;
    }

    public void resetRunTurns() {

        currentRunTurn = 1;
    }

    public BoardAction discoverBitActions(BitCard bitCard, int runDay, int runWeek) {

        //Reduce complexity of calling method by passing along null
        if (bitCard == null) {
            return null;
        }

        LOGGER.debug("Executing bit actions {}", bitCard);
        LOGGER.debug("{} card: {}", Card.BOOSTER_INOCULATE_TRAP, bitCard);

        return switch (bitCard.getAction()) {
            case 1 -> {
                periodHoldCards.add(bitCard);
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
                    new Trap(TEAM, RUN, runWeek, runDay, NONE);
            case 11 ->
                //Skip the next workstation
                    new Trap(ANY_SERVER, RUN, runWeek, runDay, NONE);
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
                    new Trap(HUMAN_SERVER, RUN, runWeek, runDay, NONE);
            case 15 ->
                //Team loses the period
                    new Trap(TEAM, PERIOD, runWeek, runDay, RUN);
            default -> null;
        };
    }

    private void returnFinishedGoodsToBacklog() {
        ScorecardService.getBacklog().addToBacklog(ScorecardService.getFinishedGoods().getFinishedGoodsTally());
        ScorecardService.getFinishedGoods().setFinishedGoodsTally(0);
    }

    private void moveWorkItemsFromColorWorkstationToPrevious(BitCard bitCard) {
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

    public boolean gameIsOver() {
        return currentPeriod > runPeriods;
    }

    public boolean isTrapMitigated(BitCard bitCard) {
        return gameHoldCards.removeIf(card -> card.getId() == bitCard.getCounterCard());
    }

    public void returnServerToOriginalWorkstation() {
        if (inTrainingServer == null) {
            return;
        }
        Workstation workstation = WorkstationService.getWorkstation(inTrainingServer.getColor());
        if (workstation != null) workstation.getServers().add(inTrainingServer);

        inTrainingServer = null;
    }

    public void returnServerToOriginalWorkstation(HumanServer humanServer) {
        WorkstationService.removeHumanServerFromWorkstation(humanServer);
        Workstation workstation = WorkstationService.getWorkstation(humanServer.getColor());
        if (workstation != null) workstation.getServers().add(humanServer);

    }

    public void putServerInTraining(@NonNull Color serverColor, @NonNull Color skillColor) {
        HumanServer server = ServerService.getHumanServer(serverColor);
        server.getSkills().add(skillColor);
        WorkstationService.removeHumanServerFromWorkstation(server);
        inTrainingServer = server;
    }

    public void startDay(@NonNull ServerMove move) {
        Color workstationColor = move.workstationColor();
        Color serverColor = move.serverColor();
        for (Workstation serversCurrentWorkstation : WorkstationService.getWorkstations()) {
            HumanServer serverToMove = (HumanServer) serversCurrentWorkstation.getServers().stream().filter(server -> server.getColor().equals(serverColor) && server.getType().equals(Server.TYPE_HUMAN)).findAny().orElse(null);
            //Test if serverToMove is null and continue
            if (serverToMove == null) continue;

            if (!serverToMove.getSkills().contains(workstationColor)) {
                throw new ThroughputRuntimeException(new IllegalArgumentException("Server must match workstation or have a skill that matches workstation"));
            } else {
                Workstation moveToWorkstation = WorkstationService.getWorkstation(workstationColor);
                Objects.requireNonNull(moveToWorkstation).getServers().add(serverToMove);
                serversCurrentWorkstation.getServers().remove(serverToMove);
                return;
            }
        }
    }

    public String toJSON() {
        return "{" +
                "\"dieFaces\":" + dieFaces +
                ",\"stationCount\":" + stationCount +
                ",\"runPeriods\":" + runPeriods +
                ",\"runTurns\":" + runTurns +
                ",\"currentRunTurn\":" + currentRunTurn +
                ",\"currentPeriod\":" + currentPeriod +
                ",\"inTrainingServer\":" + inTrainingServer +
                ",\"periodHoldCards\":" + collectionToJson(Collections.unmodifiableCollection(periodHoldCards)) +
                ",\"gameHoldCards\":" + collectionToJson(Collections.unmodifiableCollection(gameHoldCards)) +
                "," +
                WorkstationService.toJSON() +
                "," +
                ScorecardService.toJSON() +
                "," +
                CardService.toJSON() +
                "}";
    }

    public String collectionToJson(Collection<Savable> collection) {
        var json = new StringBuilder();
        json.append("[");
        List<String> stringList = collection.stream().map(Savable::toJSON).toList();
        json.append(String.join(",", stringList));
        json.append("]");
        return json.toString();
    }
}


