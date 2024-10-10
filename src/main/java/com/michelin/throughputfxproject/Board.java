package com.michelin.throughputfxproject;

import com.michelin.throughputfxproject.entities.Card;
import com.michelin.throughputfxproject.entities.Server;
import com.michelin.throughputfxproject.entities.Trap;
import com.michelin.throughputfxproject.entities.Workstation;
import com.michelin.throughputfxproject.entities.cards.BitCard;
import com.michelin.throughputfxproject.entities.servers.HumanServer;
import com.michelin.throughputfxproject.entities.servers.ServerMove;
import com.michelin.throughputfxproject.services.ScorecardService;
import com.michelin.throughputfxproject.services.WorkstationService;
import lombok.Getter;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private final List<BitCard> weekHoldCards = new ArrayList<>(10);
    private final List<BitCard> gameHoldCards = new ArrayList<>(10);
    private Integer dayOfTheWeek = 0;
    private Integer gameWeek = 0;
    private static Board board;

    private Board() {

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


    public Trap discoverBitActions(BitCard bitCard, int runDay, int runWeek) throws IOException {

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
                Prompts.promptToAddOneToWorkstationCapacity(SIX_SIDES);
                break;
            case 3:
                Prompts.promptToDoubleWorkstationCapacity(SIX_SIDES);
                break;
            case 4:
                Prompts.promptToAutomateWorkstation();
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


    public HumanServer findAndRemoveServer(@NonNull Color serverColor) {
        for (Workstation workstation : WorkstationService.getWorkstations()) {
            HumanServer serverToMove = (HumanServer) workstation.getServers().stream().filter(server -> server.getColor().equals(serverColor) && server.getType().equals(Server.TYPE_HUMAN)).findAny().orElse(null);
            if (serverToMove != null && workstation.getServers().remove(serverToMove)) {
                return serverToMove;
            }
        }
        return null;
    }

    private void finishedGoodsAreNowFourPoints() {
        ScorecardService.getInstance().getFinishedGoods().setValue(4);
    }

    public int getDayOfTheWeek() {
        return dayOfTheWeek;
    }

    public int getGameWeek() {
        return gameWeek;
    }

    public void augmentDayOfTheWeek() {
         dayOfTheWeek++;
    }

    public void augmentGameWeek() {
         gameWeek++;
    }

    public void handleChanceCardAndServerMovements(Server server, Workstation workstation, int i) throws InterruptedException, IOException {
        boolean success = Prompts.serverChanceCardPlay(server, workstation);
        workItemMoves(server, success, workstation, i);
    }


    public boolean isTrapMitigated(BitCard bitCard) {
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
            ScorecardService.getInstance().getBacklog().addToBacklog(offendingWorkstation.getWorkItemCount());
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
        ScorecardService.getInstance().getBacklog().addToBacklog(ScorecardService.getInstance().getFinishedGoods().getFinishedGoods());
        ScorecardService.getInstance().getFinishedGoods().setFinishedGoods(0);
    }

    public void returnServerToWorkstation(@NonNull HumanServer serverToMove) {
        Workstation workstation = WorkstationService.getWorkstation(serverToMove.getColor());
        if (workstation != null) {
            workstation.getServers().add(serverToMove);
        }
    }




    public void startDay(List<ServerMove> moves) {
        moves.forEach(move -> {
            HumanServer serverToMove = findAndRemoveServer(move.getServerColor());
            assert serverToMove != null;
            addServer(serverToMove, move.getWorkstationColor());
        });
    }


    private void workItemMoves(Server server, boolean success, Workstation workstation, int i) throws InterruptedException, IOException {
        if (success) {
            //Move available work items to next workstation
            int workstationMovesInt = Prompts.promptForWorkItemWorkstationMoves(workstation, i);
            if (i == 4) {
                ScorecardService.getInstance().getFinishedGoods().addToFinishedGoods(workstationMovesInt);
            } else {
                WorkstationService.getWorkstation(i + 1).addToWorkItemCount(workstationMovesInt);
            }
            workstation.subtractFromWorkItemCount(workstationMovesInt);
            //Prompts.asciiArt3(backlog, finishedGoods);
        } else {
            if (weekHoldCards.isEmpty()) {
                TimeUnit.SECONDS.sleep(3);
            } else {
                //Prompt for do over
                boolean retry = Prompts.promptForServerRetry(server);
                if (retry) {
                    weekHoldCards.remove(0);
                    boolean secondChanceSuccess = Prompts.serverChanceCardPlay(server, workstation);
                    if (secondChanceSuccess) {
                        workItemMoves(server, true, workstation, i);
                    }

                }
            }
        }
    }

}
