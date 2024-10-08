package com.michelin.throughputfxproject;

import com.michelin.throughputfxproject.entities.*;
import com.michelin.throughputfxproject.entities.cards.BitCard;
import com.michelin.throughputfxproject.entities.servers.HumanServer;
import com.michelin.throughputfxproject.entities.servers.ServerMove;
import com.michelin.throughputfxproject.exceptions.ThroughputRuntimeException;
import com.michelin.throughputfxproject.services.WorkstationService;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;


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
    @Getter
    private final Backlog backlog = new Backlog();
    @Getter
    private final FinishedGoods finishedGoods = new FinishedGoods();
    @Getter
    private final List<ScoreCard> scoreCards = new ArrayList<>(RUN_WEEKS);
    private final List<BitCard> weekHoldCards = new ArrayList<>(10);
    private final List<BitCard> gameHoldCards = new ArrayList<>(10);
    private Integer dayOfTheWeek = 0;
    private Integer gameWeek = 0;
    private static Board board;

    private Board() {
        for(int i =0;i < RUN_WEEKS;i++){
            scoreCards.add(new ScoreCard(i+1,0,0,0,0));
        }
    }

    public static Board getInstance() {
        if (board == null) {
            board = new Board();
        }
        return board;
    }


    private void addServer(@NonNull Server serverToMove, @NonNull Color color) {

        if (!serverToMove.getSkills().contains(color)) {
            throw new IllegalArgumentException("Server must match workstation or have a skill that matches workstation");
        }
        Objects.requireNonNull(WorkstationService.getWorkstation(color)).getServers().add(serverToMove);
    }


    private Trap discoverBitActions(@NonNull Scanner scanner, BitCard bitCard, int runDay, int runWeek) throws IOException {

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
                break;
            case 2:
                Prompts.promptToAddOneToWorkstationCapacity(scanner, SIX_SIDES);
                break;
            case 3:
                Prompts.promptToDoubleWorkstationCapacity(scanner, SIX_SIDES);
                break;
            case 4:
                Prompts.promptToAutomateWorkstation(scanner);
                break;
            case 5:
            case 6:
            case 7:
                gameHoldCards.add(bitCard);
                break;
            case 8:
                finishedGoodsAreNowFourPoints();
                break;
            case 9:
                Prompts.implementPairedProgramming();
                break;
            case 10:
                //Team loses the day
                return new Trap(TEAM, DAY, runWeek, runDay, NONE);

            case 11:
                //Skip the next server
                return new Trap(ANY_SERVER, DAY, runWeek, runDay, NONE);

            case 12:
                returnFinishedGoodsToBacklog();
                break;
            case 13:
                moveWorkItemsFromColorWorkstationToPrevious(bitCard);
                break;
            case 14:
                //Skip the next human server
                return new Trap(HUMAN_SERVER, DAY, runWeek, runDay, NONE);

            case 15:
                //Team loses the week
                return new Trap(TEAM, WEEK, runWeek, runDay, DAY);

            default:
                return null;
        }
        return null;
    }


    private HumanServer findAndRemoveServer(@NonNull Color serverColor) {
        for (Workstation workstation : WorkstationService.getWorkstations()) {
            HumanServer serverToMove = (HumanServer) workstation.getServers().stream().filter(server -> server.getColor().equals(serverColor) && server.getType().equals(Server.TYPE_HUMAN)).findAny().orElse(null);
            if (serverToMove != null && workstation.getServers().remove(serverToMove)) {
                return serverToMove;
            }
        }
        return null;
    }

    private void finishedGoodsAreNowFourPoints() {
        finishedGoods.setValue(4);
    }

    public int getDayOfTheWeek() {
        return dayOfTheWeek;
    }

    public int getGameWeek() {
        return gameWeek;
    }

    private void handleChanceCardAndServerMovements(Scanner scanner, Server server, Workstation workstation, int i) throws InterruptedException, IOException {
        boolean success = Prompts.serverChanceCardPlay(server, workstation);
        workItemMoves(scanner, server, success, workstation, i);
    }


    private boolean isTrapMitigated(BitCard bitCard) {
        return gameHoldCards.removeIf(card -> card.getId() == bitCard.getCounterCard());
    }

    private void moveWorkItemsFromColorWorkstationToPrevious(BitCard bitCard) {
        Color color = Color.BLUE;
        try {
            color = Color.valueOf(bitCard.getDescription());
        } catch (IllegalArgumentException e) {
            LOGGER.error("Couldn't find the color {}", bitCard.getDescription(), e);
        }
        int offendingWorkstationIndex = WorkstationService.getWorkstationIndex(color);
        Workstation offendingWorkstation = WorkstationService.getWorkstation(offendingWorkstationIndex);
        if (offendingWorkstationIndex == 0) {
            backlog.addToBacklog(offendingWorkstation.getWorkItemCount());
            offendingWorkstation.setWorkItemCount(0);
        } else if (offendingWorkstationIndex < 0) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Couldn't find workstation for color {}", color.nameWithColor());
            }
        } else {
            Workstation receivingWorkstation = WorkstationService.getWorkstation(offendingWorkstationIndex - 1);
            receivingWorkstation.addToWorkItemCount(offendingWorkstation.getWorkItemCount());
            offendingWorkstation.setWorkItemCount(0);
        }
    }


    private void returnFinishedGoodsToBacklog() {
        backlog.addToBacklog(finishedGoods.getFinishedGoods());
        finishedGoods.setFinishedGoods(0);
    }

    private void returnServerToWorkstation(@NonNull HumanServer serverToMove) {
        Workstation workstation = WorkstationService.getWorkstation(serverToMove.getColor());
        if (workstation != null) {
            workstation.getServers().add(serverToMove);
        }
    }

    @SneakyThrows
    private void runDay(boolean vanilla,  HumanServer inTraining) {
        LOGGER.debug("Run Day {}", dayOfTheWeek + 1);

        Prompts.publishDayStart(dayOfTheWeek, gameWeek);
        Prompts.asciiArt3(backlog, finishedGoods);

        //If not Week 1 ask about moving servers
        if (!vanilla) {
            //Server Moves
            List<ServerMove> moves = Prompts.promptForServerMoves(inTraining);
            startDay(moves);
        }

        //Get Team mood and start moving work items
        final int startValue = Prompts.teamMood(SIX_SIDES, backlog);
        final Workstation workstationZero = WorkstationService.getWorkstation(0);
        int initialMoveFromBacklog = Prompts.promptForWorkItemInitialMoves(startValue, backlog);
        workstationZero.addToWorkItemCount(initialMoveFromBacklog);
        backlog.subtractFromBacklog(initialMoveFromBacklog);
        Prompts.asciiArt3(backlog, finishedGoods);

        for (int i = 0; i < FIVE_STATIONS; i++) {
            runWorkstationDay(vanilla, i);
        }
        if (inTraining != null) {
            returnServerToWorkstation(inTraining);
        }
    }

    public void runGame(Stage primaryStage) {

             WorkstationService.createWorkstations(FIVE_STATIONS, SIX_SIDES);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Created workstations {}", Arrays.toString(WorkstationService.getWorkstations()));
        }

       try{
            for (gameWeek = 0; gameWeek < RUN_WEEKS; gameWeek++) {
                runWeek();
            }
        } catch (IOException e) {
            throw new ThroughputRuntimeException(e);
        }
    }

    private void runWeek() throws IOException {

        ScoreCard scoreCard = scoreCards.stream().filter(innerScoreCard-> innerScoreCard.getWeek() == gameWeek).findFirst().orElse(null);
        Objects.requireNonNull(scoreCard);
        Prompts.publishStartWeek(gameWeek);
        Prompts.asciiArt3(backlog, finishedGoods);
        //Estimate Work Items
        int startValue = Prompts.promptForWorkItemEstimates(gameWeek, scoreCard, backlog);
        scoreCard.setEstimate(startValue);

        HumanServer inTraining = null;
        if (gameWeek > 0) {
            //Get skills
            inTraining = Prompts.promptToAddSkill();
            if (inTraining != null) {
                findAndRemoveServer(inTraining.getColor());
            }
        }
        for (dayOfTheWeek = 0; dayOfTheWeek < RUN_DAYS; dayOfTheWeek++) {
            runDay( gameWeek == 0, inTraining);
        }

        weekHoldCards.clear();
        //Tally board
        scoreCard.setWorkInProcess(WorkstationService.tallyWorkInProcess());
        scoreCard.setFinishedGoods(finishedGoods.getFinishedGoods());
        scoreCard.setScore(finishedGoods.calculateScore() - (backlog.getBacklogItemCount() + scoreCard.getWorkInProcess()));
        //Remove finished Goods
        finishedGoods.setFinishedGoods(0);

        Prompts.publishEndWeek(gameWeek, scoreCard);
        Prompts.asciiArt3(backlog, finishedGoods);
    }


    private void runWorkstationDay(boolean vanilla, int i) throws InterruptedException, IOException {
        //For each server
        Workstation workstation = WorkstationService.getWorkstation(i);
        for (Server server : workstation.getServers()) {

            handleChanceCardAndServerMovements( server, workstation, i);

            if (!vanilla) {
                BitCard bitCard = Prompts.drawBit(SIX_SIDES);
                //Discover bit actions handles a null bit card
                Trap trap = discoverBitActions(bitCard, dayOfTheWeek, gameWeek);
                if (bitCard != null && trap != null) {
                    boolean trapMitigated = isTrapMitigated(bitCard);
                    if (!trapMitigated) {
                        Prompts.promptForAppliedTrap(trap);
                        if (trap.getEffected().equals(TEAM) && trap.getDuration().equals(WEEK)) {
                            gameWeek++;
                        } else if (trap.getEffected().equals(TEAM) && trap.getDuration().equals(DAY)) {
                            dayOfTheWeek++;
                        }
                    } else {
                        Prompts.promptForMitigatedTrap(trap);
                        if (trap.getEffected().equals(TEAM) && trap.getMitigatedDuration().equals(DAY)) {
                            dayOfTheWeek++;
                        }
                    }
                }
            }
        }
    }


    private void startDay(List<ServerMove> moves) {
        moves.forEach(move -> {
            HumanServer serverToMove = findAndRemoveServer(move.getServerColor());
            assert serverToMove != null;
            addServer(serverToMove, move.getWorkstationColor());
        });
    }


    private void workItemMoves(Scanner scanner, Server server, boolean success, Workstation workstation, int i) throws InterruptedException, IOException {
        if (success) {
            //Move available work items to next workstation
            int workstationMovesInt = Prompts.promptForWorkItemWorkstationMoves(scanner, workstation, i);
            if (i == 4) {
                finishedGoods.addToFinishedGoods(workstationMovesInt);
            } else {
                WorkstationService.getWorkstation(i + 1).addToWorkItemCount(workstationMovesInt);
            }
            workstation.subtractFromWorkItemCount(workstationMovesInt);
            Prompts.asciiArt3(backlog, finishedGoods);
        } else {
            if (weekHoldCards.isEmpty()) {
                TimeUnit.SECONDS.sleep(3);
            } else {
                //Prompt for do over
                boolean retry = Prompts.promptForServerRetry(scanner, server);
                if (retry) {
                    weekHoldCards.remove(0);
                    boolean secondChanceSuccess = Prompts.serverChanceCardPlay(server, workstation);
                    if (secondChanceSuccess) {
                        workItemMoves(scanner, server, true, workstation, i);
                    }

                }
            }
        }
    }

}
