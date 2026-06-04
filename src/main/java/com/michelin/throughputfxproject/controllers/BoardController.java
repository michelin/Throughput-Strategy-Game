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

package com.michelin.throughputfxproject.controllers;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.michelin.throughputfxproject.ThroughputApplication;
import com.michelin.throughputfxproject.entities.Color;
import com.michelin.throughputfxproject.entities.actions.*;
import com.michelin.throughputfxproject.entities.cards.BitCard;
import com.michelin.throughputfxproject.entities.servers.*;
import com.michelin.throughputfxproject.entities.state.*;
import com.michelin.throughputfxproject.exceptions.ThroughputRuntimeException;
import com.michelin.throughputfxproject.services.ScorecardService;
import com.michelin.throughputfxproject.services.WorkstationService;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.michelin.throughputfxproject.ThroughputApplication.*;
import static com.michelin.throughputfxproject.entities.state.Board.*;

@Slf4j
@Getter
@Setter
public class BoardController {

    public static final String ACTION_EVENT = "Action Event: {}";
    public static final int SERVER_RETRY_DELAY = 5000;
    private static final int BACKLOG_HIGHLIGHT = -1;
    private static final int FINISHED_GOODS_HIGHLIGHT = 100;
    private static final String COLUMN_WK = "Wk";
    private static final String COLUMN_EST = "Est";
    private static final String COLUMN_WIP = "WIP";
    private static final String COLUMN_FIN = "FIN";
    private static final String COLUMN_PTS = "Pts";
    private static final String PROPERTY_PERIOD = "period";
    private static final String PROPERTY_ESTIMATE = "estimate";
    private static final String PROPERTY_WORK_IN_PROCESS = "workInProcess";
    private static final String PROPERTY_FINISHED_GOODS = "finishedGoods";
    private static final String PROPERTY_SCORE = "score";
    private static final String STYLE_SCORE_BOX_LABEL = "score-box-label";
    private static final String STYLE_WORKSTATION_CARD_ROW = "workstation-card-row";
    @FXML
    private Button buttonLoadGame;
    @FXML
    private Label periodLabel;
    @FXML
    private Label runLabel;
    @FXML
    private Button buttonSaveGame;
    @FXML
    private LineChart<Integer, Integer> scoreLineChart;
    @FXML
    private Label countdownTimer;
    @FXML
    private TextArea gameBoardLog;
    @FXML
    private Pane inTrainingBox;
    @FXML
    private Pane holdCardBox;
    @FXML
    private ButtonBar turnButtonBar;
    @FXML
    private ButtonBar periodButtonBar;
    @FXML
    private ButtonBar gameButtonBar;
    @FXML
    private Button buttonServerMoves;
    @FXML
    private Button buttonRunTurn;
    @FXML
    private Button buttonAddSkills;
    @FXML
    private Button buttonRunPeriod;
    @FXML
    private Button buttonRunGame;
    @FXML
    private Pane gameDialogPane;
    @FXML
    private TableView<ScoreCard> scoreTableView;
    @FXML
    private Label totalScore;
    @FXML
    private Label runNumber;
    @FXML
    private Label periodNumber;
    @FXML
    private Label backlogCount;
    @FXML
    private Label backlogHeader;
    @FXML
    private Label finishedGoodsHeader;
    @FXML
    private Label finishedGoodsCount;
    @FXML
    private HBox workstationContainer;
    @FXML
    private HBox outerScoreBox;
    @FXML
    private Button buttonEndGame;
    @FXML
    private CheckBox timedRun;

    private boolean runTimed = false;
    private Timeline periodTimeline;
    private Timeline runTurnTimeline;
    private final List<Pane> workstationServerPanes = new ArrayList<>();
    private final List<VBox> workstationCards = new ArrayList<>();
    private final List<Label> workstationLabels = new ArrayList<>();
    private final List<Label> workstationInputLabels = new ArrayList<>();
    private final List<Label> workstationWorkersLabels = new ArrayList<>();
    private final List<Label> workstationUpgradeLabels = new ArrayList<>();
    private final List<Label> workstationAttemptsLabels = new ArrayList<>();
    private final List<Label> workstationCapacityLabels = new ArrayList<>();
    private final List<Label> workstationStatusLabels = new ArrayList<>();
    private final List<Label> workstationOutputLabels = new ArrayList<>();
    private final List<Label> queueCountLabels = new ArrayList<>();
    private final List<Label> queueCapacityLabels = new ArrayList<>();
    private final List<Label> queueDotLabels = new ArrayList<>();


    public BoardController() {
        try {
            Board.getInstance();
        } catch (IllegalStateException e) {
            log.info(e.getMessage());
            Board.initializeInstance(DEFAULT_DIE_SIDES, DEFAULT_RUN_STATIONS, DEFAULT_RUN_PERIODS, DEFAULT_RUN_TURNS);
        } catch (Exception e) {
            log.error("Error initializing Board instance", e);
            throw new ThroughputRuntimeException(e);
        }
    }

    /**
     * Activates a trap and applies its effects based on the trap's type and duration.
     *
     * @param trap               The trap to be activated.
     * @param bitCard            The BitCard associated with the trap, used to determine if the trap is mitigated.
     * @param currentWorkstation The index of the current workstation where the trap is being applied.
     */
    private void activateTrap(Trap trap, BitCard bitCard, int currentWorkstation) {
        boolean trapMitigated = Board.getInstance().isTrapMitigated(bitCard);
        Prompts.promptForAppliedTrap(trap, trapMitigated, gameBoardLog);

        if (trapMitigated) {
            if (trap.effected().equals(TEAM) && trap.mitigatedDuration().equals(RUN)) {
                Board.getInstance().augmentRunTurn();
            }
            return;
        }

        switch (trap.effected()) {
            case TEAM -> {
                if (trap.duration().equals(PERIOD)) {
                    Board.getInstance().augmentRunTurn(Board.getInstance().getRunTurns());
                } else if (trap.duration().equals(RUN)) {
                    Board.getInstance().augmentRunTurn();
                }
            }
            case ANY_SERVER -> {
                if (trap.duration().equals(RUN)) { // NOSONAR java:S6916 - 'when' guards require type patterns, not enum constants
                    int nextWorkstation = (currentWorkstation + 1) % Board.getInstance().getStationCount();
                    WorkstationService.getWorkstation(nextWorkstation).setActive(false);
                }
            }
            default -> log.debug("Trap effected mismatch {}", trap.effected());
        }
    }

    /**
     * Adds or removes skills for servers based on the current game state and user prompts.
     *
     * @param actionEvent The action event triggered by the user.
     * @throws IOException If an I/O error occurs during the process.
     */
    @FXML
    public void addOrRemoveSkillsForServers(ActionEvent actionEvent) throws IOException {
        // Log the action event for debugging purposes
        log.debug("Remove skill - Action Event: {}", actionEvent);

        // Check if the game is not in vanilla mode and prompt the user to draw a skills card
        if (!isVanilla() && Prompts.promptToDrawSkillsCard(gameDialogPane)) {
            // Prompt the user to add a skill
            Prompts.promptToAddSkill(gameDialogPane);
        } else if (!isVanilla()) {
            // Prompt the user to remove a skill
            Prompts.promptToRemoveSkill();
        }

        // Disable the "Add Skills" button after the operation
        buttonAddSkills.setDisable(true);

        // Redraw the game board to reflect the changes
        redrawBoard();
    }

    private boolean isVanilla() {
        return Board.getInstance().getCurrentPeriod() == 1;
    }

    /**
     * Redraws the game board by updating the UI elements to reflect the current state of the game.
     * This includes updating server cards, workstation counts, labels, and other game statistics.
     */
    public void redrawBoard() {
        // Retrieve the list of workstations
        Workstation[] workstations = WorkstationService.getWorkstations();
        Board.getInstance().ensureQueueStateInitialized();
        ensureWorkstationUi(workstations.length);

        try {
            // Update the server cards for each workstation
            for (int i = 0; i < workstations.length; i++) {
                buildServerCards(workstations[i].getServers(), workstationServerPanes.get(i));
            }
            // Update the in-training server card
            buildInTrainingCard(Board.getInstance().getInTrainingServer(), inTrainingBox);
        } catch (IOException e) {
            // Handle any I/O exceptions by wrapping them in a runtime exception
            throw new ThroughputRuntimeException(e);
        }

        // Update workstation labels and queue labels
        for (int i = 0; i < workstations.length; i++) {
            updateWorkstationLabel(workstations, i);
            if (i < queueCountLabels.size()) {
                int queueCount = Board.getInstance().getQueueCount(i);
                queueCountLabels.get(i).setText(String.valueOf(queueCount));
                queueCapacityLabels.get(i).setText(String.valueOf(Board.getInstance().getQueueCapacity(i)));
                if (i < queueDotLabels.size()) {
                    queueDotLabels.get(i).setText(buildQueueDots(queueCount));
                }
            }
        }

        // Update the backlog and finished goods counts
        backlogCount.setText(StringUtils.leftPad(String.valueOf(ScorecardService.BACKLOG.getBacklogItemCount()), 3, '0'));
        finishedGoodsCount.setText(StringUtils.leftPad(String.valueOf(ScorecardService.FINISHED_GOODS.getFinishedGoodsTally()), 3, '0'));

        // Update the current run number, period number, and total score
        runNumber.setText(String.valueOf(Board.getInstance().getCurrentRunTurn()));
        periodNumber.setText(String.valueOf(Board.getInstance().getCurrentPeriod()));
        totalScore.setText(String.valueOf(ScorecardService.getTotalScore()));

        // Refresh the scorecard table and hold card box
        updateScorecardTable();
        updateHoldCardBox();

        log.info("Board redraw complete: stations={}, workstationNodes={}, backlog={}, finishedGoods={}",
            workstations.length,
            workstationContainer != null ? workstationContainer.getChildren().size() : -1,
            backlogCount != null ? backlogCount.getText() : "n/a",
            finishedGoodsCount != null ? finishedGoodsCount.getText() : "n/a");
    }

    /**
     * Builds and configures server cards for a given set of servers and adds them to the specified pane.
     *
     * @param serverSet    The set of servers to be displayed as cards.
     * @param serverHolder The pane where the server cards will be added.
     * @throws IOException If an error occurs while loading server images or tooltips.
     */
    private void buildServerCards(Set<Server> serverSet, Pane serverHolder) throws IOException {
        // Clear any existing children in the server holder pane
        serverHolder.getChildren().clear();

        // Iterate through each server in the set
        for (Server server : serverSet) {
            // Create and configure the server image
            ImageView imageView = createImageView(server.getImage(), 60, 53);

            // Create and configure the tooltip with the server's back image
            Tooltip tooltip = createTooltip(server.getBackImage(), 60, 60);

            // Build the skills box for human servers
            VBox vBox = new VBox();
            if (Server.TYPE_HUMAN.equals(server.getType())) {
                buildServerSkillsBox(server, vBox, 60.0);
            } else {
                // Log a debug message for non-human servers
                log.debug("Non Human server of color {}", server.getColor());
            }
            // Set a unique ID for the VBox based on the server holder and server color
            vBox.setId("v_box_" + serverHolder.getId() + "_" + server.getColor().name());

            // Create and configure the HBox containing the image and skills box
            HBox hBox = new HBox(imageView, vBox);
            hBox.setPrefSize(63, 60);
            hBox.setId("h_box_" + serverHolder.getId() + "_" + server.getColor().name());
            Tooltip.install(hBox, tooltip);

            // Add the HBox to the server holder pane
            serverHolder.getChildren().add(hBox);
        }
    }

    /**
     * Builds and configures the in-training server card and adds it to the specified pane.
     *
     * @param inTrainingServer The human server currently in training. If null, the method exits without action.
     * @param inTrainingBox    The pane where the in-training server card will be added.
     * @throws IOException If an error occurs while loading server images or tooltips.
     */
    private void buildInTrainingCard(HumanServer inTrainingServer, Pane inTrainingBox) throws IOException {
        // Exit early if there is no in-training server
        if (inTrainingServer == null) {
            return;
        }
        // Clear any existing children in the in-training box
        inTrainingBox.getChildren().clear();

        // Create and configure the server image
        ImageView imageView = createImageView(inTrainingServer.getImage(), 100, 100);

        // Create and configure the tooltip with the server's back image
        Tooltip tooltip = createTooltip(inTrainingServer.getBackImage(), 100, 100);

        // Build the skills box for the in-training server
        VBox vBox = new VBox();
        buildServerSkillsBox(inTrainingServer, vBox, 100.0);
        vBox.setId("v_box_" + inTrainingBox.getId() + "_" + inTrainingServer.getColor().name());

        // Create and configure the HBox containing the image and skills box
        HBox hBox = new HBox(imageView, vBox);
        hBox.setPrefSize(90, 100);
        hBox.setId("h_box_" + inTrainingBox.getId() + "_" + inTrainingServer.getColor().name());
        Tooltip.install(hBox, tooltip);

        // Add the HBox to the in-training box
        inTrainingBox.getChildren().add(hBox);

        // Add a margin to the in-training box
        VBox.setMargin(inTrainingBox, new Insets(0, 0, 0, 10));
    }

    public void toggleTimedRun(ActionEvent actionEvent) {
        // Log the action event for debugging purposes
        log.debug("Toggle Timed Run - Action Event: {}", actionEvent);

        // Check if the timed run checkbox is selected
        // If selected, disable the run button and enable the period button
        // If not selected, enable the run button and disable the period button
        runTimed = timedRun.isSelected();
        Prompts.setTimedRun(runTimed);
    }

    /**
     * Updates the scorecard table by retrieving scorecards, defining table columns,
     * and refreshing the table view to reflect the current data.
     */
    private void updateScorecardTable() {
        // Retrieve scorecards and set them as table items
        ObservableList<ScoreCard> scoreCards = FXCollections.observableArrayList(ScorecardService.getSCORECARDS());
        scoreTableView.setItems(scoreCards);

        // Define table columns with their respective property mappings
        List<TableColumn<ScoreCard, ?>> columns = List.of(
                createTableColumn(COLUMN_WK, PROPERTY_PERIOD), // Column for the period
                createTableColumn(COLUMN_EST, PROPERTY_ESTIMATE), // Column for the estimate
                createTableColumn(COLUMN_WIP, PROPERTY_WORK_IN_PROCESS), // Column for work in process
                createTableColumn(COLUMN_FIN, PROPERTY_FINISHED_GOODS), // Column for finished goods
                createTableColumn(COLUMN_PTS, PROPERTY_SCORE) // Column for the score
        );

        // Set columns to the table and refresh
        scoreTableView.getColumns().setAll(columns);
        scoreTableView.refresh();
    }

    /**
     * Updates the hold card box by clearing its contents and populating it with
     * period and game hold cards. Each card is built and added to the box.
     */
    private void updateHoldCardBox() {
        // Clear any existing children in the hold card box
        holdCardBox.getChildren().clear();

        // Populate the hold card box with period hold cards
        AtomicInteger periodIndex = new AtomicInteger(0);
        Board.getInstance().getPeriodHoldCards().forEach(ignored ->
                buildHoldCards("2nd Chance", periodIndex.getAndIncrement(), 0)
        );

        // Populate the hold card box with game hold cards
        AtomicInteger gameIndex = new AtomicInteger(0);
        Board.getInstance().getGameHoldCards().forEach(card ->
                buildHoldCards(card.getInstructions(), gameIndex.getAndIncrement(), 1)
        );
    }

    /**
     * Creates and configures an ImageView for a given image path.
     *
     * @param imagePath The path to the image resource.
     * @param height    The height of the ImageView.
     * @param width     The width of the ImageView.
     * @return The configured ImageView.
     * @throws IOException If the image resource cannot be loaded.
     */
    private ImageView createImageView(String imagePath, double height, double width) throws IOException {
        ImageView imageView = new ImageView(new Image(Objects.requireNonNull(ThroughputApplication.class.getResource(imagePath)).openStream()));
        imageView.setFitHeight(height);
        imageView.setFitWidth(width);
        return imageView;
    }

    /**
     * Creates and configures a Tooltip with an image for a given back image path.
     *
     * @param backImagePath The path to the back image resource.
     * @param height        The height of the tooltip image.
     * @param width         The width of the tooltip image.
     * @return The configured Tooltip.
     */
    private Tooltip createTooltip(String backImagePath, double height, double width) {
        Tooltip tooltip = new Tooltip();
        Image tooltipImage = new Image(Objects.requireNonNull(ThroughputApplication.class.getResourceAsStream(backImagePath)));
        ImageView tooltipImageView = new ImageView(tooltipImage);
        tooltipImageView.setFitHeight(height);
        tooltipImageView.setFitWidth(width);
        tooltip.setGraphic(tooltipImageView);
        return tooltip;
    }

    /**
     * Builds and configures a skills box for a given server by creating rectangles
     * representing each skill and adding them to the specified VBox.
     *
     * @param server    The server whose skills are to be displayed.
     * @param vBox      The VBox where the skill rectangles will be added.
     * @param boxHeight The height of the VBox, used to calculate the height of each skill rectangle.
     */
    private void buildServerSkillsBox(Server server, VBox vBox, double boxHeight) {
        // Get the number of skills the server has
        int skillsCount = server.getSkills().size();

        // Create a rectangle for each skill and add it to the VBox
        server.getSkills().forEach(color -> {
            Rectangle rectangle = new Rectangle(10, (boxHeight / skillsCount), color.lookupFXColor());
            rectangle.getStyleClass().add("server-skill-chip");
            if (server.getColor().equals(color)) {
                rectangle.getStyleClass().add("server-skill-required");
            }
            vBox.getChildren().add(rectangle);
        });

        // Log the color of the human server for debugging purposes
        log.debug("Human server of color {}", server.getColor());
    }

    /**
     * Creates a table column for a `TableView` with the specified title and property binding.
     *
     * @param <T>      The type of the property value in the column.
     * @param title    The title of the column to be displayed in the table header.
     * @param property The name of the property to bind to the column's cells.
     * @return A configured `TableColumn` instance with the specified title and property binding.
     */
    private <T> TableColumn<ScoreCard, T> createTableColumn(String title, String property) {
        TableColumn<ScoreCard, T> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        return column;
    }

    /**
     * Builds and configures a hold card by creating a rectangle with a shadow effect
     * and a label with the specified text. The card is then added to the hold card box
     * at the specified column and row.
     *
     * @param text   The text to display on the card.
     * @param column The column index in the GridPane where the card will be placed.
     * @param row    The row index in the GridPane where the card will be placed.
     */
    private void buildHoldCards(String text, int column, int row) {
        // Create a rectangle with a shadow effect
        Rectangle rectangle = new Rectangle(110, row == 0 ? 60 : 110, javafx.scene.paint.Color.rgb(228, 251, 255));
        rectangle.setEffect(new InnerShadow(10, 3.0, 3.0, javafx.scene.paint.Color.BLACK));

        // Create a label with the specified text and styling
        Label label = new Label(text);
        label.setLayoutX(5);
        label.setLayoutY(15);
        label.setWrapText(true);
        label.setPrefWidth(105);
        label.setPadding(new Insets(5));
        label.setFont(Font.font("Michelin", FontWeight.BOLD, FontPosture.ITALIC, 11.0));

        // Combine the rectangle and label into an AnchorPane
        AnchorPane anchorPane = new AnchorPane(rectangle, label);

        // Add the AnchorPane to the GridPane with a margin
        HBox.setMargin(anchorPane, new Insets(5));
        ((GridPane) holdCardBox).add(anchorPane, column, row);
    }

    /**
     * Determines and executes the appropriate actions based on the drawn BitCard
     * and the current game state. This includes handling traps or help actions.
     *
     * @param currentWorkstation The index of the current workstation where the action is being applied.
     * @throws IOException If an I/O error occurs during the process.
     */
    private void bitActionsDetermined(int currentWorkstation) throws IOException {
        // Exit early if the game is in vanilla mode
        if (isVanilla()) return;

        // Draw a BitCard and determine the corresponding board action
        BitCard bitCard = Prompts.drawBit(gameDialogPane, Board.getInstance().getDieFaces(), gameBoardLog, Board.getInstance().getDieFaces());
        BoardAction boardAction = Board.getInstance().discoverBitActions(bitCard, Board.getInstance().getCurrentRunTurn(), Board.getInstance().getCurrentPeriod());

        // Handle the board action based on its type (Trap or HelpAction)
        if (boardAction instanceof Trap trap) {
            activateTrap(trap, bitCard, currentWorkstation);
        } else if (boardAction instanceof HelpAction helpAction) {
            handleHelpAction(helpAction);
        }
    }

    private void buildPeriodTimer(ActionEvent actionEvent) {

        // Set the text color of the countdown timer to dark blue
        countdownTimer.setTextFill(javafx.scene.paint.Color.DARKBLUE);
        setCountdownGraphicVisible(true);

        // Initialize the timeline for the countdown
        periodTimeline = Board.getInstance().getFreshTimeline(2, countdownTimer);

        // Define the action to perform when the timer finishes
        periodTimeline.setOnFinished(_ -> Platform.runLater(() -> {
            countdownTimer.textProperty().unbind(); // Unbind the text property
            countdownTimer.setText("X"); // Display "X" when the timer ends
            countdownTimer.setTextFill(javafx.scene.paint.Color.RED); // Change text color to red
            setCountdownGraphicVisible(false);
            runPeriod(actionEvent, periodTimeline);
            buildRunTurnTimer(actionEvent);

        })); // Automatically call runPeriod

        periodTimeline.playFromStart();


    }

    private void buildRunTurnTimer(ActionEvent actionEvent) {

        // Set the text color of the countdown timer to dark blue
        countdownTimer.setTextFill(javafx.scene.paint.Color.DARKBLUE);
        setCountdownGraphicVisible(true);

        // Initialize the timeline for the countdown
        runTurnTimeline = Board.getInstance().getFreshTimeline(3, countdownTimer);
        int currentPeriod = Board.getInstance().getCurrentPeriod();

        // Define the action to perform when the timer finishes
        runTurnTimeline.setOnFinished(_ -> Platform.runLater(() -> {
            countdownTimer.textProperty().unbind(); // Unbind the text property
            countdownTimer.setText("X"); // Display "X" when the timer ends
            countdownTimer.setTextFill(javafx.scene.paint.Color.RED); // Change text color to red
            setCountdownGraphicVisible(false);
            runTurn(actionEvent, runTurnTimeline, () -> {
                if ((currentPeriod == Board.getInstance().getCurrentPeriod()) && (Board.getInstance().getCurrentPeriod() <= Board.getInstance().getRunPeriods())) {
                    buildRunTurnTimer(actionEvent); // Rebuild timer for the next turn in same period
                } else if (Board.getInstance().getCurrentPeriod() <= Board.getInstance().getRunPeriods()) {
                    buildPeriodTimer(actionEvent); // Start period timer for the next period
                }
            });
        }));

        // Start the timer from the beginning
        runTurnTimeline.playFromStart();
    }


    /**
     * Enables or disables the main game buttons.
     *
     * @param disable A boolean indicating whether to disable (true) or enable (false) the buttons.
     */
    private void disableButtons(boolean disable) {
        buttonRunTurn.setDisable(disable);
        buttonServerMoves.setDisable(disable);
        buttonSaveGame.setDisable(disable);
        buttonEndGame.setDisable(disable);
    }

    /**
     * Executes the logic for running a turn in the game. This includes stopping the timer,
     * processing workstations, updating the game state, and handling the transition to the next turn or period.
     *
     * @param actionEvent The action event triggered by the user.
     * @throws IOException If an I/O error occurs during the process.
     */
    @FXML
    protected void doRunTurn(ActionEvent actionEvent) {
        runTurn(actionEvent, runTurnTimeline);
    }


    /**
     * Ends the game by publishing the end-of-game message, disabling the "End Game" button,
     * and prompting the user to save the game. If the user chooses to save, the game state
     * is saved to a file.
     *
     * @param ignoredActionEvent The action event triggered by the user (not used in this method).
     */
    @FXML
    protected void endGame(ActionEvent ignoredActionEvent) {
        // Publish the end-of-game message to the game log
        Prompts.publishEndOfGame(gameBoardLog);

        // Disable the "End Game" button to prevent further interaction
        buttonEndGame.setDisable(true);

        // Create a confirmation alert to ask the user if they want to save the game
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION, "Save Game?", ButtonType.YES, ButtonType.NO);
        confirmationAlert.setTitle("Throughput");

        // Show the alert and handle the user's response
        confirmationAlert.showAndWait().ifPresent(answer -> {
            // If the user chooses "YES", save the game state
            if (answer.equals(ButtonType.YES)) saveGame(ignoredActionEvent);
        });
    }

    /**
     * Saves the current state of the game to a JSON file.
     *
     * @param actionEvent The action event triggered by the user.
     * @throws ThroughputRuntimeException If an I/O error occurs or the directory for saved games cannot be created.
     */
    @FXML
    protected void saveGame(ActionEvent actionEvent) {
        log.debug("Save Game - Action Event: {}", actionEvent);

        try {
            String gameState = Board.getInstance().toJSON();
            log.debug("Game State: {}", gameState);

            File saveDir = new File(getProjectResourcesPath() + "/savedGames");
            if (!saveDir.exists() && !saveDir.mkdirs()) {
                throw new IllegalStateException("Failed to create saved games directory");
            }

            String filePath = saveDir + "/game_" + System.currentTimeMillis() + ".json";
            ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(new File(filePath), mapper.readTree(gameState));

            log.debug("Data saved to: {}", filePath);
            Prompts.alertWithoutBoardUpdate("Game Saved", filePath, 10);

        } catch (IOException e) {
            throw new ThroughputRuntimeException(e);
        }
    }

    /**
     * Retrieves the absolute path to the project's resources directory.
     * This method determines the location of the current class file, processes the path
     * to remove unnecessary parts, and constructs the path to the `main/resources` directory.
     *
     * @return A string representing the absolute path to the `main/resources` directory.
     */
    private String getProjectResourcesPath() {
        // Get the absolute path of the current class file
        String currentClassPath = ThroughputApplication.class.getProtectionDomain().getCodeSource().getLocation().getPath();

        // Remove the "file:" prefix and the class file name from the path
        String currentClassDirectory = currentClassPath.replaceFirst("file:", "").replaceFirst(ThroughputApplication.class.getSimpleName() + ".class", "");

        // Remove any trailing slashes or backslashes
        currentClassDirectory = currentClassDirectory.replaceAll("[/\\\\]$", "");

        // Construct the path to the resources directory
        return currentClassDirectory + "/main/resources";
    }

    /**
     * Handles a HelpAction by executing the appropriate prompt based on the action type.
     *
     * @param helpAction The HelpAction to be handled.
     * @throws IOException If an I/O error occurs during the process.
     */
    private void handleHelpAction(HelpAction helpAction) throws IOException {
        // Execute the corresponding prompt based on the type of help action
        switch (helpAction.type()) {
            case ADD_ONE ->
                    Prompts.promptToAugmentWorkstationCapacity(gameDialogPane, false, Board.getInstance().getDieFaces());
            case DOUBLE ->
                    Prompts.promptToAugmentWorkstationCapacity(gameDialogPane, true, Board.getInstance().getDieFaces());
            case AUTOMATE -> Prompts.promptToAutomateWorkstation(gameDialogPane, gameBoardLog);
            case PAIR -> Prompts.implementPairedProgramming(gameDialogPane, gameBoardLog);
            case AUGMENT -> Prompts.promptForFinishedGoodsAreNowFourPoints(gameBoardLog);
        }
    }

    /**
     * Handles the transition to the next run or period. This includes updating the UI,
     * publishing prompts, and starting the timer for the next phase.
     */
    private void handleNextRunOrPeriod() {
        // Check if the current run turn is the first one
        if (Board.getInstance().getCurrentRunTurn() == 1) {
            // Show period-related buttons and prompts
            periodButtonBar.setVisible(true);
            buttonRunPeriod.setVisible(true);
            buttonRunPeriod.setDisable(false);
            buttonAddSkills.setVisible(true);
            buttonAddSkills.setDisable(false);
            redrawBoard();
            Prompts.publishStartPeriod(gameBoardLog, Board.getInstance().getCurrentPeriod());
        } else if (Board.getInstance().getCurrentRunTurn() <= Board.getInstance().getRunTurns()) {
            // Enable turn-related buttons and publish the start of the turn
            buttonRunTurn.setDisable(false);
            buttonServerMoves.setDisable(isVanilla());
            redrawBoard();
            Prompts.publishTurnStart(gameBoardLog, Board.getInstance().getCurrentPeriod(), Board.getInstance().getCurrentRunTurn());
        } else {
            // Throw an exception if the run turn is invalid
            throw new IllegalStateException("Run: " + Board.getInstance().getCurrentRunTurn() +
                    " Period: " + Board.getInstance().getCurrentPeriod() + " Is not allowed!!");
        }
    }

    /**
     * Handles the end-of-turn state by either showing the final scorecard
     * when the game is over, or transitioning to the next run or period.
     */
    private void handleEndOfTurnState() {
        if (Board.getInstance().gameIsOver()) {
            updateScorecardChart();
            redrawBoard();
            endGame(null);
            ((Stage) gameDialogPane.getScene().getWindow()).close();
        } else {
            handleNextRunOrPeriod();
        }
    }

    /**
     * Hides the buttons related to running a turn.
     */
    private void hideRunButtons() {
        turnButtonBar.setVisible(false);
        buttonRunTurn.setVisible(false);
        buttonServerMoves.setVisible(false);
    }

    /**
     * Initializes the game board by setting up workstation counts, labels,
     * backlog and finished goods counts, run and period labels, total score,
     * and the scorecard table.
     */
    @FXML
    protected void initialize() {
        log.info("BoardController.initialize start: stationCount={}, period={}, runTurn={}",
            Board.getInstance().getStationCount(),
            Board.getInstance().getCurrentPeriod(),
            Board.getInstance().getCurrentRunTurn());

        // Build workstation UI from the configured station count.
        initializeWorkstationUi();

        // Initialize backlog and finished goods counts
        setEndpointHeaderText(backlogHeader, "BACKLOG");
        backlogCount.setText("000");
        backlogCount.setBackground(new Background(new BackgroundFill(javafx.scene.paint.Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        setEndpointHeaderText(finishedGoodsHeader, "FINISHED GOODS");
        finishedGoodsCount.setText("000");

        // Initialize run and period labels
        runLabel.setText(System.getProperty("run.label", "Day"));
        runNumber.setText(String.valueOf(Board.getInstance().getCurrentRunTurn()));
        periodLabel.setText(System.getProperty("period.label", "Week"));
        periodNumber.setText(String.valueOf(Board.getInstance().getCurrentPeriod()));

        // Initialize total score
        totalScore.setText("000");

        // Apply column policy in code; FXML string coercion for callback policies is brittle across JavaFX versions.
        scoreTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Update the scorecard table
        updateScorecardTable();

        // Render the initial board state immediately so workstation UI is visible on startup.
        redrawBoard();

        // Ensure prompts start in manual (non-timed) mode and know about the countdown label
        Prompts.setTimedRun(false);
        Prompts.setCountdownTimer(countdownTimer);
        setCountdownGraphicVisible(false);

        log.info("BoardController.initialize complete: workstationNodes={}, holdCards={}, inTrainingNodes={}",
            workstationContainer != null ? workstationContainer.getChildren().size() : -1,
            holdCardBox != null ? holdCardBox.getChildren().size() : -1,
            inTrainingBox != null ? inTrainingBox.getChildren().size() : -1);
    }

    /**
     * Initializes the workstation counts by setting their text values to "000".
     */
    private void initializeWorkstationCounts() {
        // No-op: queue labels are initialized dynamically per queue.
    }

    /**
     * Initializes the workstation labels by setting their text values to the names
     * of their respective colors.
     */
    private void initializeWorkstationLabels() {
        Workstation[] workstations = WorkstationService.getWorkstations();
        for (int i = 0; i < workstations.length; i++) {
            updateWorkstationLabel(workstations, i);
        }
    }

    private void initializeQueueLabels() {
        Board.getInstance().ensureQueueStateInitialized();
        for (int i = 0; i < queueCountLabels.size(); i++) {
            int queueCount = Board.getInstance().getQueueCount(i);
            queueCountLabels.get(i).setText(String.valueOf(queueCount));
            queueCapacityLabels.get(i).setText(String.valueOf(Board.getInstance().getQueueCapacity(i)));
            if (i < queueDotLabels.size()) {
                queueDotLabels.get(i).setText(buildQueueDots(queueCount));
            }
        }
    }

    /**
     * Loads a previously saved game by prompting the user to select a game file,
     * parsing its JSON content, and updating the game state and UI accordingly.
     *
     * @param actionEvent The action event triggered by the user.
     * @throws ThroughputRuntimeException If an I/O error occurs during the loading process.
     */
    @FXML
    protected void loadGame(ActionEvent actionEvent) {
        // Log the action event for debugging purposes
        log.debug("Load Game - Action Event: {}", actionEvent);

        try {
            // Prompt the user to upload a previously saved game file
            File gameFile = Prompts.promptToUploadPreviousGame(gameDialogPane, new File(getProjectResourcesPath()));

            // Read the content of the selected game file as a string
            String jsonData = Files.readString(gameFile.toPath());

            // Parse the JSON content into a HashMap
            HashMap<String, Object> parsedJson = new ObjectMapper().readValue(jsonData, new TypeReference<>() {
            });

            // Log the parsed JSON content for debugging purposes
            parsedJson.forEach((k, v) -> log.debug("{} : {}", k, v));

            // Reload the game state using the parsed JSON data
            Board.reloadInstance(parsedJson);

            // Update the states of various buttons
            buttonSaveGame.setDisable(false);
            buttonLoadGame.setDisable(true);
            buttonRunGame.setDisable(true);
            buttonEndGame.setDisable(false);

            // Redraw the game board to reflect the loaded state
            redrawBoard();

            // Update the UI based on the current run turn
            if (Board.getInstance().getCurrentRunTurn() == 1) {
                // Show period-related buttons and enable the "Add Skills" button if in vanilla mode
                periodButtonBar.setVisible(true);
                buttonRunPeriod.setVisible(true);
                buttonAddSkills.setVisible(isVanilla());
            } else {
                // Show turn-related buttons and update their states
                periodButtonBar.setVisible(false);
                buttonRunPeriod.setVisible(false);
                buttonAddSkills.setVisible(false);
                turnButtonBar.setVisible(true);
                buttonRunTurn.setVisible(true);
                buttonRunTurn.setDisable(false);
                buttonServerMoves.setVisible(true);
                buttonServerMoves.setDisable(isVanilla());

                // Publish the start of the current turn
                Prompts.publishTurnStart(gameBoardLog, Board.getInstance().getCurrentPeriod(), Board.getInstance().getCurrentRunTurn());
            }
        } catch (IOException e) {
            // Wrap and rethrow any I/O exceptions as a runtime exception
            throw new ThroughputRuntimeException(e);
        }
    }

    /**
     * Attempts to retry a server action using a period hold card. If the retry is successful,
     * the server performs its action on the workstation. The method also updates the hold card box
     * and handles the result of the retry action.
     *
     * @param workstation The workstation where the server is located.
     * @param position    The position of the workstation in the sequence.
     * @param server      The server attempting the retry action.
     * @throws IOException If an I/O error occurs during the retry process.
     */
    private void retryWithCard(Workstation workstation, int position, Server server) throws IOException {
        // Prompt the user to decide whether to retry the server action
        boolean retry = Prompts.promptForServerRetry(server);
        if (retry) {
            // Remove the first period hold card and update the hold card box
            if (Board.getInstance().getPeriodHoldCards().isEmpty()) return;
            Board.getInstance().getPeriodHoldCards().removeFirst();
            runFx(this::updateHoldCardBox);

            // Attempt the server action and handle the result
            ChanceResult result = Prompts.serverChanceCardPlay(gameDialogPane, server, workstation, gameBoardLog);
            if (ChanceResult.SUCCESS.equals(result)) {
                // If the action is successful, prompt for workstation moves
                Prompts.promptForWorkItemWorkstationMoves(gameDialogPane, workstation, position);
            }
        }
    }

    /**
     * Attempts to retry a server action using a pair retry mechanism. If the retry is successful,
     * the server performs its action on the workstation. This method handles the retry prompt,
     * executes the server action, and processes workstation moves if the action succeeds.
     *
     * @param workstation The workstation where the server is located.
     * @param position    The position of the workstation in the sequence.
     * @param server      The server attempting the retry action.
     * @throws IOException If an I/O error occurs during the retry process.
     */
    private boolean retryWithPartner(Workstation workstation, int position, Server server) throws IOException {
        // Prompt the user for a pair retry action
        Prompts.promptForPairRetry(server, gameBoardLog);

        // Execute the server action and retrieve the result
        ChanceResult result = Prompts.serverChanceCardPlay(gameDialogPane, server, workstation, gameBoardLog);

        // If the action is successful, prompt for workstation moves
        if (ChanceResult.SUCCESS.equals(result)) {
            Prompts.promptForWorkItemWorkstationMoves(gameDialogPane, workstation, position);
            return true;
        }
        return false;
    }

    /**
     * Starts the game by initializing the UI and enabling/disabling relevant buttons.
     * This method also publishes the start of the current period and sets up a timer.
     *
     * @param actionEvent The action event triggered by the user.
     */
    @FXML
    protected void runGame(ActionEvent actionEvent) {
        // Log the action event if debugging is enabled
        if (log.isDebugEnabled()) {
            log.debug(actionEvent.toString());
        }

        // Publish the start of the current period to the game log
        Prompts.publishStartPeriod(gameBoardLog, Board.getInstance().getCurrentPeriod());

        // Redraw the game board and update button states
        redrawBoard();
        buttonRunGame.setDisable(true);
        periodButtonBar.setVisible(true);
        buttonRunPeriod.setVisible(true);
        buttonRunPeriod.setDisable(false);
        buttonAddSkills.setVisible(true);
        buttonAddSkills.setDisable(true);
        buttonSaveGame.setDisable(false);
        buttonLoadGame.setDisable(true);
        buttonEndGame.setDisable(false);

        if(runTimed) {
            // Build the period timer
            buildPeriodTimer(actionEvent);
        }
    }

    /**
     * Handles the logic for running a period in the game. This includes stopping the timer,
     * hiding period-related buttons, estimating work items, updating the game board, and
     * preparing the UI for the next turn. Finally, it starts a timer for the turn.
     *
     * @param actionEvent The action event triggered by the user.
     */
    @FXML
    protected void runPeriod(ActionEvent actionEvent) {
        runPeriod(actionEvent, periodTimeline);
    }

    private void runPeriod(ActionEvent actionEvent, Timeline timeline) {
        // Log the action event if debugging is enabled
        if (log.isDebugEnabled()) {
            log.debug(actionEvent.toString());
        }
        // Stop the timer if it is active
        if (timeline != null) timeline.stop();
        setCountdownGraphicVisible(false);
        try {
            // Hide period-related buttons
            periodButtonBar.setVisible(false);
            buttonRunPeriod.setVisible(false);
            buttonAddSkills.setVisible(false);

            // Prompt the user to estimate work items
            Prompts.promptForWorkItemEstimates(gameDialogPane);
            // Redraw the game board to reflect the updated state
            redrawBoard();
            // Highlight the backlog as the active workstation
            highlightActiveWorkstation(BACKLOG_HIGHLIGHT);

            // Skills addition is triggered by a button push


            // Show buttons to run the turn
            turnButtonBar.setVisible(true);
            buttonRunTurn.setVisible(true);
            buttonRunTurn.setDisable(false);
            buttonServerMoves.setVisible(true);
            buttonServerMoves.setDisable(isVanilla());
            // Publish the start of the turn to the game log
            Prompts.publishTurnStart(gameBoardLog, Board.getInstance().getCurrentPeriod(), Board.getInstance().getCurrentRunTurn());
        } catch (IOException e) {
            // Wrap and rethrow any I/O exceptions as a runtime exception
            throw new ThroughputRuntimeException(e);
        }

        log.info("Run TURN timer from period: {} Run: {}", Board.getInstance().getCurrentPeriod(), Board.getInstance().getCurrentRunTurn());
    }

    /**
     * Highlights the active workstation or count by updating the background and text colors
     * of the corresponding UI elements. Resets all other workstations and counts to their default state.
     *
     * @param activeWorkstation The index of the active workstation to highlight.
     *                          Use -1 to highlight the backlog count,
     *                          100 to highlight the finished goods count,
     *                          or a valid workstation index (0-4) to highlight a specific workstation.
     */
    private void highlightActiveWorkstation(int activeWorkstation) {
        // Reset active card state and count backgrounds
        workstationCards.forEach(card -> card.getStyleClass().remove("workstation-card-active"));
        Background whiteBackground = new Background(new BackgroundFill(javafx.scene.paint.Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY));
        backlogCount.setBackground(whiteBackground);
        finishedGoodsCount.setBackground(whiteBackground);

        // Highlight the active workstation or count
        if (activeWorkstation == BACKLOG_HIGHLIGHT) {
            // Highlight the backlog count
            backlogCount.setBackground(new Background(new BackgroundFill(javafx.scene.paint.Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        } else if (activeWorkstation >= 0 && activeWorkstation < workstationLabels.size()) {
            workstationCards.get(activeWorkstation).getStyleClass().add("workstation-card-active");
        } else if (activeWorkstation == FINISHED_GOODS_HIGHLIGHT) {
            // Highlight the finished goods count
            finishedGoodsCount.setBackground(new Background(new BackgroundFill(javafx.scene.paint.Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        }
    }

    private void runTurn(ActionEvent actionEvent, Timeline timeline) {
        runTurn(actionEvent, timeline, null);
    }

    private void runTurn(ActionEvent actionEvent, Timeline timeline, Runnable afterTurn) {
        // Log the action event if debugging is enabled
        if (log.isDebugEnabled()) log.debug(actionEvent.toString());

        // Stop the timer if it is active
        if (timeline != null) {
            log.info("Timeline status {}", timeline.getStatus().name());
            timeline.stop();
        }
        setCountdownGraphicVisible(false);

        // Log the current run turn
        log.debug("Run Turn {}", Board.getInstance().getCurrentRunTurn());

        // Disable buttons during the run
        disableButtons(true);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws IOException {
                // Highlight the backlog as the active workstation
                runFx(() -> highlightActiveWorkstation(BACKLOG_HIGHLIGHT));

                // Process each workstation
                for (int stationIndex = 0; stationIndex < Board.getInstance().getStationCount(); stationIndex++) {
                    if (Board.getInstance().gameIsOver()) break;
                    final int idx = stationIndex;
                    runFx(() -> highlightActiveWorkstation(idx));
                    runWorkstations(idx);
                }

                // Re-enable buttons after all workstations are processed
                runFx(() -> disableButtons(false));

                // Check if the current run turn is the last one
                if (Board.getInstance().getCurrentRunTurn() >= Board.getInstance().getRunTurns()) {
                    runFx(BoardController.this::hideRunButtons);
                    Prompts.publishEndPeriod(gameBoardLog);
                }

                // Move to the next run turn
                Board.getInstance().augmentRunTurn();
                runFx(() -> highlightActiveWorkstation(FINISHED_GOODS_HIGHLIGHT));
                Board.getInstance().returnServerToOriginalWorkstation();
                runFx(() -> inTrainingBox.getChildren().clear());

                // Check if the game is over
                runFx(BoardController.this::handleEndOfTurnState);

                return null;
            }
        };

        task.setOnSucceeded(_ -> {
            if (afterTurn != null) afterTurn.run();
        });
        task.setOnFailed(_ -> {
            disableButtons(false);
            log.error("Game turn failed", task.getException());
        });

        Thread taskThread = new Thread(task, "game-turn-thread");
        taskThread.setDaemon(true);
        taskThread.start();
    }

    private void setCountdownGraphicVisible(boolean visible) {
        if (countdownTimer == null || countdownTimer.getGraphic() == null) {
            return;
        }
        countdownTimer.getGraphic().setVisible(visible);
        countdownTimer.getGraphic().setManaged(visible);
    }

    /**
     * Runs a Runnable on the JavaFX Application Thread.
     * If already on the FX thread, runs immediately; otherwise schedules via Platform.runLater.
     */
    private void runFx(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }

    /**
     * Processes the servers in a workstation, handling their actions and updating the game state.
     * If the workstation is inactive, it waits for a specified duration, reactivates it, and redraws the board.
     * For each server, it performs the server's action and handles success, failure, or empty results.
     * Additionally, it determines and executes any bit actions and redraws the board after each server's action.
     *
     * @param position The index of the workstation to process.
     * @throws IOException If an I/O error occurs during server actions or prompts.
     */
    private void runWorkstations(int position) throws IOException {
        // Retrieve the workstation at the specified position
        Workstation workstation = WorkstationService.getWorkstation(position);
        Objects.requireNonNull(workstation);

        // Check if the workstation is inactive
        if (!workstation.isActive()) {
            // Show a timed alert (background-thread safe; waits for full visual close before returning)
            Prompts.alertWithoutBoardUpdate(
                    workstation.getColor() + " Workstation Inactive",
                    workstation.getColor() + " workstation was inactive. It has been reactivated.",
                    SERVER_RETRY_DELAY);
            workstation.setActive(true);
            runFx(this::redrawBoard);
            return;
        }

        // Filter out PairPartner servers from the workstation's server list
        List<Server> serverList = workstation.getServers().stream().filter(server -> !(server instanceof PairPartner)).toList();
        for (Server server : serverList) {
            // Exit the loop if the game is over - If days are lost due to BIT actions, the game may end
            if (Board.getInstance().gameIsOver()) break;

            // Log the current server being processed
            log.debug("Now serving server {}", server);

            // Perform the server's action and handle the result
            ChanceResult result = Prompts.serverChanceCardPlay(gameDialogPane, server, workstation, gameBoardLog);
            switch (result) {
                case SUCCESS:
                    if (position == 0) {
                        // Workstation 0 pulls from backlog into queue 0 only after a successful chance roll.
                        int backlogItemCount = ScorecardService.BACKLOG.getBacklogItemCount();
                        int queueMaxMoves = Board.getInstance().getStationCount() > 1
                                ? Math.min(workstation.getCapacity(), Board.getInstance().getQueueSpaceRemaining(0))
                                : Math.min(workstation.getCapacity(), backlogItemCount);
                        Prompts.promptForWorkItemInitialMoves(gameDialogPane, queueMaxMoves, backlogItemCount, gameBoardLog);
                    } else {
                        // Prompt for moving work items within the workstation
                        Prompts.promptForWorkItemWorkstationMoves(gameDialogPane, workstation, position);
                    }
                    break;
                case FAILED:
                    // Retry the action using a period hold card or a partner if available
                    boolean pairRetryResult = workstation.getServers().stream()
                            .anyMatch(PairPartner.class::isInstance) &&
                            retryWithPartner(workstation, position, server);
                    if (!Board.getInstance().getPeriodHoldCards().isEmpty() && !pairRetryResult) {
                        retryWithCard(workstation, position, server);
                    }
                    break;
                case EMPTY:
                    // No action is taken for an empty result
                    break;
            }

            // Determine and execute any bit actions for the current position
            bitActionsDetermined(position);

            // Redraw the board to reflect the updated state
            runFx(this::redrawBoard);
        }
    }

    /**
     * Handles the server moves action by prompting the user to move servers
     * and updating the game state accordingly.
     *
     * @param actionEvent The action event triggered by the user.
     * @throws ThroughputRuntimeException If an I/O error occurs during the process.
     */
    @FXML
    protected void serverMoves(ActionEvent actionEvent) {
        if (log.isDebugEnabled()) {
            log.debug(actionEvent.toString());
        }
        try {
            Prompts.promptForServerMoves(gameDialogPane, Board.getInstance().getInTrainingServer(), this);
        } catch (IOException e) {
            throw new ThroughputRuntimeException(e);
        }
    }

    /**
     * Displays the information card to the user by invoking the appropriate prompt.
     *
     * @param actionEvent The action event triggered by the user.
     * @throws ThroughputRuntimeException If an I/O error occurs during the process.
     */
    @FXML
    protected void showInfo(ActionEvent actionEvent) {
        if (log.isDebugEnabled()) {
            log.debug(actionEvent.toString());
        }
        try {
            Prompts.showInfoCard(gameDialogPane);
        } catch (IOException e) {
            throw new ThroughputRuntimeException(e);
        }
    }

    /**
     * Displays the rules card to the user by invoking the appropriate prompt.
     *
     * @param actionEvent The action event triggered by the user.
     * @throws ThroughputRuntimeException If an I/O error occurs during the process.
     */
    @FXML
    protected void showRules(ActionEvent actionEvent) {
        if (log.isDebugEnabled()) {
            log.debug(actionEvent.toString());
        }
        try {
            Prompts.showRulesCard(gameDialogPane);
        } catch (IOException e) {
            throw new ThroughputRuntimeException(e);
        }
    }

    /**
     * Updates the scorecard chart by clearing existing data, retrieving the latest scorecards,
     * and populating the chart with the scores. Configures the chart to disable symbols,
     * animations, and legends for a cleaner display.
     */
    private void updateScorecardChart() {
        // Clear existing data from the score line chart
        scoreLineChart.getData().clear();

        // Retrieve the array of scorecards
        ScoreCard[] scorecards = ScorecardService.getSCORECARDS();

        // Define a new series to hold the score data
        XYChart.Series<Integer, Integer> series = new XYChart.Series<>();

        // Populate the series with data from the scorecards
        for (int scorecardIndex = 0; scorecardIndex < scorecards.length; scorecardIndex++) {
            series.getData().add(new XYChart.Data<>(scorecardIndex + 1, scorecards[scorecardIndex].getScore()));
        }

        // Configure the chart settings
        scoreLineChart.setCreateSymbols(false); // Disable symbols on the chart
        scoreLineChart.setAnimated(false); // Disable animations for the chart
        scoreLineChart.setLegendVisible(false); // Hide the legend

        // Add the populated series to the chart
        scoreLineChart.getData().add(series);
    }

    private void initializeWorkstationUi() {
        Workstation[] workstations = WorkstationService.getWorkstations();
        Board.getInstance().ensureQueueStateInitialized();
        ensureWorkstationUi(workstations.length);
        initializeWorkstationCounts();
        initializeWorkstationLabels();
        initializeQueueLabels();
    }

    private void ensureWorkstationUi(int workstationCount) {
        if (workstationContainer == null) {
            throw new ThroughputRuntimeException(new IllegalStateException("workstationContainer is not wired from FXML"));
        }
        if (workstationServerPanes.size() == workstationCount
                && workstationLabels.size() == workstationCount) {
            return;
        }

        workstationContainer.getChildren().clear();
        workstationContainer.setSpacing(14.0);
        workstationServerPanes.clear();
        workstationCards.clear();
        workstationLabels.clear();
        workstationInputLabels.clear();
        workstationWorkersLabels.clear();
        workstationUpgradeLabels.clear();
        workstationAttemptsLabels.clear();
        workstationCapacityLabels.clear();
        workstationStatusLabels.clear();
        workstationOutputLabels.clear();
        queueCountLabels.clear();
        queueCapacityLabels.clear();
        queueDotLabels.clear();

        for (int i = 0; i < workstationCount; i++) {
            final int queueIndex = i;
            VBox servers = new VBox();
            servers.setId("servers" + i + "0");
                servers.getStyleClass().add("workstation-workers-pane");

            Label workstationNameLabel = new Label();
                workstationNameLabel.getStyleClass().add("workstation-card-title");
            workstationNameLabel.setWrapText(true);
                workstationNameLabel.setMaxWidth(Double.MAX_VALUE);

                Label inputLabel = new Label();
                inputLabel.getStyleClass().addAll(STYLE_SCORE_BOX_LABEL, STYLE_WORKSTATION_CARD_ROW, "workstation-card-input");

                Label workersLabel = new Label();
                workersLabel.getStyleClass().addAll(STYLE_SCORE_BOX_LABEL, STYLE_WORKSTATION_CARD_ROW);

                Label upgradesLabel = new Label();
                upgradesLabel.getStyleClass().addAll(STYLE_SCORE_BOX_LABEL, STYLE_WORKSTATION_CARD_ROW);

                Label attemptsLabel = new Label();
                attemptsLabel.getStyleClass().addAll(STYLE_SCORE_BOX_LABEL, STYLE_WORKSTATION_CARD_ROW);

                Label capacityLabel = new Label();
                capacityLabel.getStyleClass().addAll(STYLE_SCORE_BOX_LABEL, STYLE_WORKSTATION_CARD_ROW);

                Label statusLabel = new Label();
                statusLabel.getStyleClass().addAll(STYLE_SCORE_BOX_LABEL, STYLE_WORKSTATION_CARD_ROW, "workstation-card-status");

                Label outputLabel = new Label();
                outputLabel.getStyleClass().addAll(STYLE_SCORE_BOX_LABEL, STYLE_WORKSTATION_CARD_ROW, "workstation-card-output");

                Separator dividerTop = new Separator();
                dividerTop.getStyleClass().add("workstation-card-divider");

                Separator dividerBottom = new Separator();
                dividerBottom.getStyleClass().add("workstation-card-divider");

                VBox workstationCard = new VBox(
                    workstationNameLabel,
                    inputLabel,
                    dividerTop,
                    workersLabel,
                    servers,
                    upgradesLabel,
                    attemptsLabel,
                    capacityLabel,
                    statusLabel,
                    dividerBottom,
                    outputLabel
                );
                workstationCard.setAlignment(javafx.geometry.Pos.TOP_LEFT);
                workstationCard.setId("workstation" + i);
                workstationCard.getStyleClass().add("workstation-card");

            if (i < workstationCount - 1) {
                Label queueTitleLabel = new Label();
                queueTitleLabel.getStyleClass().add("queue-side-title");
                queueTitleLabel.setMaxWidth(Double.MAX_VALUE);
                queueTitleLabel.setAlignment(javafx.geometry.Pos.CENTER);
                queueTitleLabel.setTextAlignment(TextAlignment.CENTER);
                queueTitleLabel.setWrapText(false);
                queueTitleLabel.setTextOverrun(OverrunStyle.CLIP);

                Label queueCountLabel = new Label();
                queueCountLabel.getStyleClass().add("queue-side-count");
                queueCountLabel.setMaxWidth(Double.MAX_VALUE);
                queueCountLabel.setAlignment(javafx.geometry.Pos.CENTER);

                Label queueDotsLabel = new Label();
                queueDotsLabel.getStyleClass().add("queue-side-dots");
                queueDotsLabel.setWrapText(true);
                queueDotsLabel.setMaxWidth(Double.MAX_VALUE);
                queueDotsLabel.setAlignment(javafx.geometry.Pos.CENTER);

                Label queueCapacityLabel = new Label();
                queueCapacityLabel.getStyleClass().add("capacity-box-label");
                queueCapacityLabel.setMinWidth(22.0);
                queueCapacityLabel.setPrefWidth(22.0);
                queueCapacityLabel.setMaxWidth(22.0);
                queueCapacityLabel.setMinHeight(16.0);
                queueCapacityLabel.setPrefHeight(16.0);
                queueCapacityLabel.setMaxHeight(16.0);
                queueCapacityLabel.setAlignment(javafx.geometry.Pos.CENTER);

                Button decrementButton = new Button();
                Button incrementButton = new Button();
                decrementButton.getStyleClass().add("button-increment-default");
                incrementButton.getStyleClass().add("button-increment-default");
                decrementButton.setGraphic(new FontIcon("fas-minus"));
                incrementButton.setGraphic(new FontIcon("fas-plus"));
                decrementButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                incrementButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                decrementButton.setGraphicTextGap(0);
                incrementButton.setGraphicTextGap(0);
                decrementButton.setPadding(Insets.EMPTY);
                incrementButton.setPadding(Insets.EMPTY);
                decrementButton.setFocusTraversable(false);
                incrementButton.setFocusTraversable(false);
                decrementButton.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-border-color: transparent; -fx-border-width: 0; -fx-padding: 0;");
                incrementButton.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-border-color: transparent; -fx-border-width: 0; -fx-padding: 0;");
                decrementButton.setMinSize(6.0, 6.0);
                decrementButton.setPrefSize(10.0, 10.0);
                decrementButton.setMaxSize(14.0, 14.0);
                incrementButton.setMinSize(6.0, 6.0);
                incrementButton.setPrefSize(10.0, 10.0);
                incrementButton.setMaxSize(14.0, 14.0);
                decrementButton.setOnAction(event -> adjustQueueCapacity(queueIndex, -1));
                incrementButton.setOnAction(event -> adjustQueueCapacity(queueIndex, 1));

                HBox queueButtons = new HBox(1, decrementButton, queueCapacityLabel, incrementButton);
                queueButtons.setAlignment(javafx.geometry.Pos.CENTER);
                queueButtons.getStyleClass().add("queue-side-controls");

                Separator queueDivider = new Separator();
                queueDivider.getStyleClass().add("queue-side-divider");

                VBox queueCenter = new VBox(1, queueCountLabel, queueDotsLabel);
                queueCenter.setAlignment(javafx.geometry.Pos.CENTER);

                Region upperSpacer = new Region();
                Region lowerSpacer = new Region();
                VBox.setVgrow(upperSpacer, Priority.ALWAYS);
                VBox.setVgrow(lowerSpacer, Priority.ALWAYS);

                VBox queueVBox = new VBox(2, queueTitleLabel, upperSpacer, queueCenter, lowerSpacer, queueDivider, queueButtons);
                queueVBox.setAlignment(javafx.geometry.Pos.TOP_CENTER);
                queueVBox.setFillWidth(true);
                queueVBox.setId("queue" + i);
                queueVBox.getStyleClass().add("queue-side-box");

                Color queueColor = WorkstationService.getWorkstation(i).getColor();
                setQueueHeaderText(queueTitleLabel, queueColor.name() + " QUEUE");
                applyQueueBoxColor(queueVBox, queueColor);

                Label queueFlowArrow = new Label();
                queueFlowArrow.getStyleClass().add("queue-flow-arrow");
                queueFlowArrow.setGraphic(new FontIcon("fas-arrow-right"));
                queueFlowArrow.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                queueFlowArrow.setAlignment(javafx.geometry.Pos.CENTER);
                applyQueueFlowArrowColor(queueFlowArrow, queueColor);

                Label queueOutboundArrow = new Label();
                queueOutboundArrow.getStyleClass().add("queue-flow-arrow");
                queueOutboundArrow.setGraphic(new FontIcon("fas-arrow-right"));
                queueOutboundArrow.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                queueOutboundArrow.setAlignment(javafx.geometry.Pos.CENTER);
                applyQueueFlowArrowColor(queueOutboundArrow, queueColor);

                HBox queueBridge = new HBox(10, queueFlowArrow, queueVBox, queueOutboundArrow);
                queueBridge.setAlignment(javafx.geometry.Pos.CENTER);
                queueBridge.getStyleClass().add("queue-bridge");

                workstationContainer.getChildren().add(workstationCard);
                workstationContainer.getChildren().add(queueBridge);
                queueCountLabels.add(queueCountLabel);
                queueCapacityLabels.add(queueCapacityLabel);
                queueDotLabels.add(queueDotsLabel);
            } else {
                workstationContainer.getChildren().add(workstationCard);
            }

            workstationServerPanes.add(servers);
            workstationCards.add(workstationCard);
            workstationLabels.add(workstationNameLabel);
            workstationInputLabels.add(inputLabel);
            workstationWorkersLabels.add(workersLabel);
            workstationUpgradeLabels.add(upgradesLabel);
            workstationAttemptsLabels.add(attemptsLabel);
            workstationCapacityLabels.add(capacityLabel);
            workstationStatusLabels.add(statusLabel);
            workstationOutputLabels.add(outputLabel);
        }
    }

    private void updateWorkstationLabel(Workstation[] workstations, int index) {
        Workstation workstation = workstations[index];
        Label workstationLabel = workstationLabels.get(index);
        Label inputLabel = workstationInputLabels.get(index);
        Label workersLabel = workstationWorkersLabels.get(index);
        Label upgradesLabel = workstationUpgradeLabels.get(index);
        Label attemptsLabel = workstationAttemptsLabels.get(index);
        Label capacityLabel = workstationCapacityLabels.get(index);
        Label statusLabel = workstationStatusLabels.get(index);
        Label outputLabel = workstationOutputLabels.get(index);
        VBox workstationCard = workstationCards.get(index);

        boolean hasAutoUpgrade = workstation.getServers().stream().anyMatch(AutomatedServer.class::isInstance);
        boolean hasPairUpgrade = workstation.getServers().stream().anyMatch(PairPartner.class::isInstance);
        long visibleWorkersCount = workstation.getServers().stream().filter(server -> !(server instanceof PairPartner)).count();

        workstationLabel.setText(workstation.getColor().name() + " WORKSTATION");
        inputLabel.setText(resolveWorkstationInput(workstations, index));
        workersLabel.setText("Workers (" + visibleWorkersCount + ")");
        upgradesLabel.setText("Upgrades:"
                + (hasAutoUpgrade ? " AUTO" : "")
                + (hasPairUpgrade ? " PAIR" : "")
                + (!hasAutoUpgrade && !hasPairUpgrade ? " NONE" : ""));
        attemptsLabel.setText("Attempts: " + (hasPairUpgrade ? 2 : 1));
            capacityLabel.setText("Cap: " + workstation.getCapacity());
        statusLabel.setText("Status: " + (workstation.isActive() ? "Working" : "Paused"));
        outputLabel.setText(resolveWorkstationOutput(workstations, index));

        applyWorkstationCardColor(workstationCard, workstation.getColor());
    }

    private String resolveWorkstationInput(Workstation[] workstations, int index) {
        if (index == 0) {
            return "Input: Backlog";
        }
        return "Input: " + workstations[index - 1].getColor().name() + " Queue";
    }

    private String resolveWorkstationOutput(Workstation[] workstations, int index) {
        if (index == workstations.length - 1) {
            return "Output: Finished Goods";
        }
        return "Output: " + workstations[index].getColor().name() + " Queue";
    }

    private void applyWorkstationCardColor(VBox workstationCard, Color color) {
        workstationCard.getStyleClass().removeIf(styleClass -> styleClass.startsWith("workstation-card-"));
        workstationCard.getStyleClass().add("workstation-card-" + color.name().toLowerCase());
    }

    private void applyQueueBoxColor(VBox queueBox, Color color) {
        queueBox.getStyleClass().removeIf(styleClass -> styleClass.startsWith("queue-side-box-"));
        queueBox.getStyleClass().add("queue-side-box-" + color.name().toLowerCase());
    }

    private void applyQueueFlowArrowColor(Label queueFlowArrow, Color color) {
        queueFlowArrow.getStyleClass().removeIf(styleClass -> styleClass.startsWith("queue-flow-arrow-"));
        queueFlowArrow.getStyleClass().add("queue-flow-arrow-" + color.name().toLowerCase());
    }

    private void setQueueHeaderText(Label queueTitleLabel, String text) {
        queueTitleLabel.setText(text);

        // Keep the queue name on one line by shrinking font size to fit header width.
        final double maxWidth = 96.0;
        final double maxFontSize = 10.0;
        final double minFontSize = 7.0;

        double selectedFontSize = minFontSize;
        for (double fontSize = maxFontSize; fontSize >= minFontSize; fontSize -= 0.25) {
            Text measure = new Text(text);
            measure.setFont(Font.font("Michelin", FontWeight.BOLD, fontSize));
            if (measure.getLayoutBounds().getWidth() <= maxWidth) {
                selectedFontSize = fontSize;
                break;
            }
        }
        queueTitleLabel.setFont(Font.font("Michelin", FontWeight.BOLD, selectedFontSize));
    }

    private void setEndpointHeaderText(Label endpointHeaderLabel, String text) {
        if (endpointHeaderLabel == null) {
            return;
        }

        endpointHeaderLabel.setText(text);
        endpointHeaderLabel.setAlignment(javafx.geometry.Pos.CENTER);
        endpointHeaderLabel.setTextAlignment(TextAlignment.CENTER);

        // Keep endpoint title on one centered line by shrinking the font to fit card width.
        final double maxWidth = 120.0;
        final double maxFontSize = 11.0;
        final double minFontSize = 7.5;

        double selectedFontSize = minFontSize;
        for (double fontSize = maxFontSize; fontSize >= minFontSize; fontSize -= 0.25) {
            Text measured = new Text(text);
            measured.setFont(Font.font("Michelin", FontWeight.BOLD, fontSize));
            if (measured.getLayoutBounds().getWidth() <= maxWidth) {
                selectedFontSize = fontSize;
                break;
            }
        }

        endpointHeaderLabel.setFont(Font.font("Michelin", FontWeight.BOLD, selectedFontSize));
    }

    private String buildQueueDots(int queueCount) {
        if (queueCount <= 0) {
            return "";
        }
        int visibleDots = Math.min(queueCount, 6);
        String dots = "● ".repeat(visibleDots).trim();
        if (queueCount > visibleDots) {
            return dots + " +";
        }
        return dots;
    }

    private void adjustQueueCapacity(int queueIndex, int delta) {
        Board.getInstance().adjustQueueCapacity(queueIndex, delta);
        redrawBoard();
    }

    /**
     * Resizes the primary stage so the score row content (backlog, workstations/queues,
     * and finished goods) fits without horizontal scrolling when possible.
     *
     * If the required width exceeds the available screen width, the stage is capped
     * at the visual bounds and the existing ScrollPane provides overflow behavior.
     */
    public void resizeStageToFitOuterScoreBox() {
        if (outerScoreBox == null || gameDialogPane == null || gameDialogPane.getScene() == null) {
            return;
        }

        Stage stage = (Stage) gameDialogPane.getScene().getWindow();
        if (stage == null) {
            return;
        }

        Region root = gameDialogPane.getScene().getRoot() instanceof Region region ? region : null;
        if (root != null) {
            root.applyCss();
            root.layout();
        }

        double requiredSceneWidth = outerScoreBox.prefWidth(-1) + 24;
        double currentSceneWidth = gameDialogPane.getScene().getWidth();
        double targetSceneWidth = Math.max(requiredSceneWidth, currentSceneWidth);

        double currentSceneHeight = gameDialogPane.getScene().getHeight();
        double requiredSceneHeight = root != null ? root.prefHeight(targetSceneWidth) + 24 : currentSceneHeight;
        double targetSceneHeight = Math.max(requiredSceneHeight, currentSceneHeight);

        double chromeWidth = stage.getWidth() - currentSceneWidth;
        double chromeHeight = stage.getHeight() - currentSceneHeight;
        double maxStageWidth = Screen.getPrimary().getVisualBounds().getWidth();
        double maxStageHeight = Screen.getPrimary().getVisualBounds().getHeight();
        double targetStageWidth = Math.min(targetSceneWidth + chromeWidth, maxStageWidth);
        double targetStageHeight = Math.min(targetSceneHeight + chromeHeight, maxStageHeight);

        if (targetStageWidth > stage.getWidth()) {
            stage.setWidth(targetStageWidth);
        }
        if (targetStageHeight > stage.getHeight()) {
            stage.setHeight(targetStageHeight);
        }
    }

}
