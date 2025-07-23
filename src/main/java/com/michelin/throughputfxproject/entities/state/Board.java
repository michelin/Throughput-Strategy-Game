/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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
import javafx.animation.*;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Label;
import javafx.util.Duration;
import lombok.*;
import lombok.extern.slf4j.Slf4j;


import java.util.*;


@Getter
@EqualsAndHashCode
@ToString
@Builder
@Slf4j
public class Board implements Savable {

    public static final String NONE = "NONE";
    public static final String RUN = "RUN";
    public static final String TEAM = "TEAM";
    public static final String PERIOD = "PERIOD";
    public static final String ANY_SERVER = "ANY_SERVER";
    private static final String HUMAN_SERVER = "HUMAN_SERVER";

    private static Board instance;
    //Play containers - Holders of game status
    @Builder.Default
    private final List<BitCard> periodHoldCards= new ArrayList<>(10);
    @Builder.Default
    private final List<BitCard> gameHoldCards= new ArrayList<>(10);
    private final int dieFaces;
    private final int stationCount;
    private final int runPeriods;
    private final int runTurns;
    @Builder.Default
    private Integer runTime = 20;
    @Builder.Default
    private Integer currentRunTurn = 1;
    @Builder.Default
    private Integer currentPeriod = 1;
    private HumanServer inTrainingServer;




    /**
     * Retrieves the singleton instance of the Board.
     *
     * @return The singleton instance of the Board.
     * @throws IllegalStateException If the Board has not been initialized.
     */
    public static Board getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Board has not been initialized");
        }
        return instance;
    }

    /**
     * Clears the singleton instance of the Board.
     * This is primarily intended for testing or resetting the game state.
     * After calling this method, getInstance() will throw an IllegalStateException
     * until initializeInstance() is called again.
     */
    public static void clearInstance() {
        instance = null;
    }

    /**
     * Initializes the singleton instance of the Board with the specified parameters.
     *
     * @param dieFaces The number of faces on the die used in the game.
     * @param stationCount The number of stations in the game.
     * @param runPeriods The number of periods in a game run.
     * @param runTurns The number of turns in a game run.
     */
    public static Board initializeInstance(int dieFaces, int stationCount, int runPeriods, int runTurns) {
        instance = Board.builder().dieFaces(dieFaces).stationCount(stationCount).runPeriods(runPeriods).runTurns(runTurns).build();
        return instance;
    }

    /**
     * Reloads the state of the Board instance from a provided JSON-like map.
     * This method extracts various game parameters and services from the map
     * and reinitialized the Board instance with the extracted data.
     *
     * @param reloadGameJson A map containing the serialized state of the game.
     *                       Expected keys include:
     *                       - "dieFaces" (Integer): Number of die faces.
     *                       - "stationCount" (Integer): Number of stations.
     *                       - "runPeriods" (Integer): Number of run periods.
     *                       - "runTurns" (Integer): Number of run turns.
     *                       - "inTrainingServer" (Map<String, Object>): Serialized in-training server.
     *                       - "currentRunTurn" (Integer): Current run turn.
     *                       - "currentPeriod" (Integer): Current period.
     *                       - "periodHoldCards" (List<Object>): Serialized period hold cards.
     *                       - "gameHoldCards" (List<Object>): Serialized game hold cards.
     *                       - "workstationService" (Map<String, Object>): Serialized workstation service.
     *                       - "scorecardService" (Map<String, Object>): Serialized scorecard service.
     *                       - "bitDeck" (List<Object>): Serialized bit deck.
     * @throws ClassCastException If any of the map values cannot be cast to the expected type.
     */
    @SuppressWarnings({"unchecked"})
    public static void reloadInstance(Map<String, Object> reloadGameJson) {

        Board.BoardBuilder builder = Board.builder();
        builder.dieFaces((Integer) reloadGameJson.get("dieFaces"));
        builder.stationCount((Integer) reloadGameJson.get("stationCount"));
        builder.runPeriods((Integer) reloadGameJson.get("runPeriods"));
        builder.runTurns((Integer) reloadGameJson.get("runTurns"));
        builder.inTrainingServer((HumanServer) ServerService.recreateServerFromMap((Map<String, Object>) reloadGameJson.get("inTrainingServer")));
        builder.currentRunTurn((Integer) reloadGameJson.get("currentRunTurn"));
        builder.currentPeriod((Integer) reloadGameJson.get("currentPeriod"));
        builder.periodHoldCards(CardService.reloadHoldCards((List<Object>) reloadGameJson.get("periodHoldCards")));
        builder.gameHoldCards(CardService.reloadHoldCards((List<Object>) reloadGameJson.get("gameHoldCards")));

        // Reload services
        WorkstationService.reloadWorkstations((Map<String, Object>) reloadGameJson.get("workstationService"));
        ScorecardService.reloadScorecards((Map<String, Object>) reloadGameJson.get("scorecardService"));
        CardService.reloadCards((List<Object>) reloadGameJson.get("bitDeck"));

        // Reload board
        instance = builder.build();
    }

  /**
   * Increments the current run turn by the specified number of turns.
   * Calls the single-turn increment method for each turn.
   *
   * @param turns The number of turns to increment.
   */
  public void augmentRunTurn(int turns) {
      for (int ndx = 0; ndx < turns; ndx++) {
          augmentRunTurn();
      }
  }

  /**
   * Increments the current run turn by one.
   * If the current run turn exceeds the total number of run turns, it clears the period hold cards,
   * updates the scorecard, increments the run period, and resets the run turns.
   */
  public void augmentRunTurn() {
      currentRunTurn++;
      if (currentRunTurn > runTurns) {
          this.getPeriodHoldCards().clear();

          // Update Scorecard
          ScoreCard scoreCard = ScorecardService.getScorecardForCurrentWeek();
          scoreCard.setWorkInProcess(WorkstationService.tallyWorkInProcess());
          scoreCard.setFinishedGoods(ScorecardService.FINISHED_GOODS.getFinishedGoodsTally());
          scoreCard.setScore(ScorecardService.currentWeekRunningScore());

          // Remove finished goods
          ScorecardService.FINISHED_GOODS.setFinishedGoodsTally(0);

          augmentRunPeriod();
          resetRunTurns();
      }
  }

  /**
   * Increments the current run period by one.
   */
  private void augmentRunPeriod() {
      currentPeriod++;
  }

  /**
   * Resets the current run turn to the initial value of 1.
   */
  public void resetRunTurns() {
      currentRunTurn = 1;
  }

/**
 * Processes a given BitCard and determines the appropriate action to take based on its type.
 * Depending on the action, the method may modify the state of the board, such as adding cards
 * to hold lists, creating traps, or performing other game-related actions.
 *
 * @param bitCard The BitCard to process. If null, the method returns null.
 * @param runDay The current day of the run, used for creating traps.
 * @param runWeek The current week of the run, used for creating traps.
 * @return A BoardAction representing the result of the BitCard's action, or null if no action is required.
 */
public BoardAction discoverBitActions(BitCard bitCard, int runDay, int runWeek) {

    // Reduce complexity of calling method by passing along null
    if (bitCard == null) {
        return null;
    }

    log.debug("Executing bit actions {}", bitCard);
    log.debug("{} card: {}", Card.BOOSTER_INOCULATE_TRAP, bitCard);

    return switch (bitCard.getAction()) {
        case 1 -> {
            // Add the BitCard to the period hold cards list
            periodHoldCards.add(bitCard);
            yield null;
        }
        case 2 ->
            // Create a HelpAction to add one to a value
            new HelpAction(HelpAction.HelpActionType.ADD_ONE);
        case 3 ->
            // Create a HelpAction to double a value
            new HelpAction(HelpAction.HelpActionType.DOUBLE);
        case 4 ->
            // Create a HelpAction to automate a process
            new HelpAction(HelpAction.HelpActionType.AUTOMATE);
        case 5, 6, 7 -> {
            // Add the BitCard to the game hold cards list
            gameHoldCards.add(bitCard);
            yield null;
        }
        case 8 ->
            // Create a HelpAction to augment a value
            new HelpAction(HelpAction.HelpActionType.AUGMENT);
        case 9 ->
            // Create a HelpAction to pair items
            new HelpAction(HelpAction.HelpActionType.PAIR);
        case 10 ->
            // Create a Trap indicating the team loses the day
            new Trap(TEAM, RUN, runWeek, runDay, NONE);
        case 11 ->
            // Create a Trap to skip the next workstation
            new Trap(ANY_SERVER, RUN, runWeek, runDay, NONE);
        case 12 -> {
            // Return finished goods to the backlog
            returnFinishedGoodsToBacklog();
            yield null;
        }
        case 13 -> {
            // Move work items from a workstation of a specific color to the previous workstation
            moveWorkItemsFromColorWorkstationToPrevious(bitCard);
            yield null;
        }
        case 14 ->
            // Create a Trap to skip the next human server
            new Trap(HUMAN_SERVER, RUN, runWeek, runDay, NONE);
        case 15 ->
            // Create a Trap indicating the team loses the period
            new Trap(TEAM, PERIOD, runWeek, runDay, RUN);
        default ->
            // Return null for unrecognized actions
            null;
    };
}
    /**
     * Returns all finished goods to the backlog and resets the finished goods tally to zero.
     * This method updates the backlog with the current tally of finished goods and clears the tally.
     */
    private void returnFinishedGoodsToBacklog() {
        ScorecardService.BACKLOG.addToBacklog(ScorecardService.FINISHED_GOODS.getFinishedGoodsTally());
        ScorecardService.FINISHED_GOODS.setFinishedGoodsTally(0);
    }
    
    /**
     * Moves work items from a workstation of a specific color to the previous workstation.
     * If the workstation is the first one, the work items are added to the backlog.
     * Logs a warning if the workstation for the specified color cannot be found.
     *
     * @param bitCard The BitCard containing the description of the workstation color.
     * @throws ThroughputRuntimeException If the color in the BitCard is invalid.
     */
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
            ScorecardService.BACKLOG.addToBacklog(offendingWorkstation.getWorkItemCount());
            offendingWorkstation.setWorkItemCount(0);
        } else if (offendingWorkstationIndex < 0) {
            log.warn("Couldn't find workstation for color {}", color.name());
        } else {
            Workstation receivingWorkstation = WorkstationService.getWorkstation(offendingWorkstationIndex - 1);
            receivingWorkstation.addToWorkItemCount(offendingWorkstation.getWorkItemCount());
            offendingWorkstation.setWorkItemCount(0);
        }
    }
    
    /**
     * Checks if the game is over by comparing the current period with the total run periods.
     *
     * @return true if the current period exceeds the total run periods, false otherwise.
     */
    public boolean gameIsOver() {
        return currentPeriod > runPeriods;
    }
    
    /**
     * Checks if a trap associated with a given BitCard has been mitigated.
     * Removes the counter card from the game hold cards if it exists.
     *
     * @param bitCard The BitCard to check for trap mitigation.
     * @return true if the trap was mitigated, false otherwise.
     */
    public boolean isTrapMitigated(BitCard bitCard) {
        return gameHoldCards.removeIf(card -> card.getId() == bitCard.getCounterCard());
    }

 /**
  * Returns the in-training server to its original workstation.
  * If no server is currently in training, the method does nothing.
  * If the workstation corresponding to the server's color exists, 
  * the server is added back to that workstation.
  */
 public void returnServerToOriginalWorkstation() {
     if (inTrainingServer == null) {
         return;
     }
     Workstation workstation = WorkstationService.getWorkstation(inTrainingServer.getColor());
     if (workstation != null) workstation.getServers().add(inTrainingServer);
 
     inTrainingServer = null;
 }
 
 /**
  * Returns the specified human server to its original workstation.
  * Removes the server from its current workstation and adds it back 
  * to the workstation corresponding to its color, if such a workstation exists.
  *
  * @param humanServer The human server to return to its original workstation.
  */
 public void returnServerToOriginalWorkstation(HumanServer humanServer) {
     WorkstationService.removeHumanServerFromWorkstation(humanServer);
     Workstation workstation = WorkstationService.getWorkstation(humanServer.getColor());
     if (workstation != null) workstation.getServers().add(humanServer);
 }
 
 /**
  * Puts a server into training by assigning it a new skill and removing it 
  * from its current workstation. The server is then marked as the in-training server.
  *
  * @param serverColor The color of the server to put into training.
  * @param skillColor The color of the skill to assign to the server.
  * @throws ThroughputRuntimeException If the server cannot be found.
  */
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

    /**
     * Converts the current state of the Board into a JSON string representation.
     * The JSON includes details about the die faces, station count, run periods, 
     * run turns, current run turn, current period, in-training server, period hold cards, 
     * game hold cards, and serialized data from various services.
     *
     * @return A JSON string representing the current state of the Board.
     */
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
    
    /**
     * Converts a collection of Savable objects into a JSON array string representation.
     * Each object in the collection is serialized using its `toJSON` method.
     *
     * @param collection The collection of Savable objects to convert to JSON.
     * @return A JSON array string representing the collection.
     */
    public String collectionToJson(Collection<Savable> collection) {
        var json = new StringBuilder();
        json.append("[");
        List<String> stringList = collection.stream().map(Savable::toJSON).toList();
        json.append(String.join(",", stringList));
        json.append("]");
        return json.toString();
    }

    public Timeline getFreshTimeline(int divisor, Label timerLabel){

        if(divisor <= 0) throw new ThroughputRuntimeException(new IllegalArgumentException("divisor must be > 0"));
        // Create a property to track the remaining time
        IntegerProperty timeSeconds = new SimpleIntegerProperty(runTime / divisor);

        timerLabel.textProperty().bind(timeSeconds.asString());

        // Initialize the timeline for the countdown
        Timeline timeline = new Timeline();

        // Add a keyframe to decrement the time to zero over the specified duration
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(runTime + 1.0), new KeyValue(timeSeconds, 0)));

        return timeline;
    }

}
