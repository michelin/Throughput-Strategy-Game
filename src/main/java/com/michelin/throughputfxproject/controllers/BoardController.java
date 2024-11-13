package com.michelin.throughputfxproject.controllers;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.michelin.throughputfxproject.ThroughputApplication;
import com.michelin.throughputfxproject.entities.Color;
import com.michelin.throughputfxproject.entities.actions.BoardAction;
import com.michelin.throughputfxproject.entities.actions.HelpAction;
import com.michelin.throughputfxproject.entities.actions.Trap;
import com.michelin.throughputfxproject.entities.cards.BitCard;
import com.michelin.throughputfxproject.entities.servers.HumanServer;
import com.michelin.throughputfxproject.entities.servers.PairPartner;
import com.michelin.throughputfxproject.entities.servers.Server;
import com.michelin.throughputfxproject.entities.state.Board;
import com.michelin.throughputfxproject.entities.state.ScoreCard;
import com.michelin.throughputfxproject.entities.state.Workstation;
import com.michelin.throughputfxproject.exceptions.ThroughputRuntimeException;
import com.michelin.throughputfxproject.services.ScorecardService;
import com.michelin.throughputfxproject.services.WorkstationService;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.michelin.throughputfxproject.ThroughputApplication.*;
import static com.michelin.throughputfxproject.entities.state.Board.*;


public class BoardController {

    public static final Logger LOGGER = LoggerFactory.getLogger(BoardController.class.getName());
    //Timer label implementation
    private static final Integer START_TIME = 60;

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
    private Label finishedGoodsCount;
    @FXML
    private VBox servers00;
    @FXML
    private VBox servers10;
    @FXML
    private VBox servers20;
    @FXML
    private VBox servers30;
    @FXML
    private VBox servers40;
    @FXML
    private Label workstationCount0;
    @FXML
    private Label workstationCount1;
    @FXML
    private Label workstationCount2;
    @FXML
    private Label workstationCount3;
    @FXML
    private Label workstationCount4;
    @FXML
    private Label workstationLabel0;
    @FXML
    private Label workstationLabel1;
    @FXML
    private Label workstationLabel2;
    @FXML
    private Label workstationLabel3;
    @FXML
    private Label workstationLabel4;
    @FXML
    private Button buttonEndGame;

    private Timeline timeline;


    public BoardController() {
        try {
            var board = Board.getInstance();
            if (board == null) {
                Board.initializeInstance(DEFAULT_DIE_SIDES, DEFAULT_RUN_STATIONS, DEFAULT_RUN_PERIODS, DEFAULT_RUN_TURNS);
            }
        } catch (IllegalStateException e) {
            LOGGER.info(e.getMessage());
        }
    }

    private void activateTrap(Trap trap, BitCard bitCard, int currentWorkstation) {

        boolean trapMitigated = Board.getInstance().isTrapMitigated(bitCard);
        Prompts.promptForAppliedTrap(trap, trapMitigated, gameBoardLog);
        if (!trapMitigated) {
            if (trap.effected().equals(TEAM) && trap.duration().equals(PERIOD)) {
                Board.getInstance().augmentRunTurn(Board.getInstance().getRunTurns());
            } else if (trap.effected().equals(TEAM) && trap.duration().equals(RUN)) {
                Board.getInstance().augmentRunTurn();
            } else if (trap.effected().equals(ANY_SERVER) && trap.duration().equals(RUN)) {
                int nextWorkstationLocation = currentWorkstation + 1;
                if (nextWorkstationLocation >= Board.getInstance().getStationCount())
                    nextWorkstationLocation = 0;
                Workstation nextWorkstation = WorkstationService.getWorkstation(nextWorkstationLocation);
                nextWorkstation.setActive(false);
            }
        } else {
            if (trap.effected().equals(TEAM) && trap.mitigatedDuration().equals(RUN)) {
                Board.getInstance().augmentRunTurn();
            }
        }
    }

    @FXML
    protected void addOrRemoveSkillsForServers(ActionEvent actionEvent) throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(actionEvent.toString());
        }
        if (!isVanilla()) {
            if (Prompts.promptToDrawSkillsCard(gameDialogPane)) {
                Prompts.promptToAddSkill(gameDialogPane);
            } else {
                Prompts.promptToRemoveSkill();
            }
        }
        buttonAddSkills.setDisable(true);
        redrawBoard();
    }

    private boolean isVanilla() {
        return Board.getInstance().getCurrentPeriod() == 1;
    }


    protected void redrawBoard() {

        Workstation workstation0 = WorkstationService.getWorkstations()[0];
        Workstation workstation1 = WorkstationService.getWorkstations()[1];
        Workstation workstation2 = WorkstationService.getWorkstations()[2];
        Workstation workstation3 = WorkstationService.getWorkstations()[3];
        Workstation workstation4 = WorkstationService.getWorkstations()[4];

        try {
            buildServerCards(workstation0.getServers(), servers00);
            buildServerCards(workstation1.getServers(), servers10);
            buildServerCards(workstation2.getServers(), servers20);
            buildServerCards(workstation3.getServers(), servers30);
            buildServerCards(workstation4.getServers(), servers40);
            buildInTrainingCard(Board.getInstance().getInTrainingServer(), inTrainingBox);
        } catch (IOException e) {
            throw new ThroughputRuntimeException(e);
        }

        workstationCount0.setText(StringUtils.leftPad(String.valueOf(workstation0.getWorkItemCount()), 3, '0'));
        workstationCount1.setText(StringUtils.leftPad(String.valueOf(workstation1.getWorkItemCount()), 3, '0'));
        workstationCount2.setText(StringUtils.leftPad(String.valueOf(workstation2.getWorkItemCount()), 3, '0'));
        workstationCount3.setText(StringUtils.leftPad(String.valueOf(workstation3.getWorkItemCount()), 3, '0'));
        workstationCount4.setText(StringUtils.leftPad(String.valueOf(workstation4.getWorkItemCount()), 3, '0'));

        backlogCount.setText(StringUtils.leftPad(String.valueOf(ScorecardService.getBacklog().getBacklogItemCount()), 3, '0'));
        finishedGoodsCount.setText(StringUtils.leftPad(String.valueOf(ScorecardService.getFinishedGoods().getFinishedGoodsTally()), 3, '0'));

        workstationLabel0.setText(workstation0.getColor().name() + ": " + workstation0.getCapacity());
        workstationLabel1.setText(workstation1.getColor().name() + ": " + workstation1.getCapacity());
        workstationLabel2.setText(workstation2.getColor().name() + ": " + workstation2.getCapacity());
        workstationLabel3.setText(workstation3.getColor().name() + ": " + workstation3.getCapacity());
        workstationLabel4.setText(workstation4.getColor().name() + ": " + workstation4.getCapacity());

        runNumber.setText(String.valueOf(Board.getInstance().getCurrentRunTurn()));
        periodNumber.setText(String.valueOf(Board.getInstance().getCurrentPeriod()));

        totalScore.setText(String.valueOf(ScorecardService.getTotalScore()));

        updateScorecardTable();
        updateHoldCardBox();
    }

    private void buildServerCards(Set<Server> serverSet, Pane serverHolder) throws IOException {
        serverHolder.getChildren().clear();
        for (Server server : serverSet) {

            ImageView imageView = new ImageView(new Image(Objects.requireNonNull(Objects.requireNonNull(ThroughputApplication.class.getResource(server.getImage())).openStream())));
            imageView.setFitHeight(60);
            imageView.setFitWidth(53);

            Tooltip tooltip = new Tooltip();
            Image tooltipImage = new Image(Objects.requireNonNull(ThroughputApplication.class.getResourceAsStream(server.getBackImage())));
            ImageView tooltipImageView = new ImageView(tooltipImage);
            tooltipImageView.setFitHeight(60);
            tooltipImageView.setFitWidth(60);
            tooltip.setGraphic(tooltipImageView);

            //loop through skills
            VBox vBox = new VBox();
            if (server.getType().equals(Server.TYPE_HUMAN)) {
                buildServerSkillsBox(server, vBox, 60.0);
            } else {
                LOGGER.debug("Non Human server of color {}", server.getColor());
            }
            vBox.setId("v_box_" + serverHolder.getId() + "_" + server.getColor().name());

            HBox hBox = new HBox(imageView, vBox);
            hBox.setPrefHeight(60);
            hBox.setPrefWidth(63);
            hBox.setId("h_box_" + serverHolder.getId() + "_" + server.getColor().name());
            Tooltip.install(hBox, tooltip);

            serverHolder.getChildren().add(hBox);
        }
    }

    private void buildInTrainingCard(HumanServer inTrainingServer, Pane inTrainingBox) throws IOException {
        if (inTrainingServer == null) {
            return;
        }
        inTrainingBox.getChildren().clear();

        ImageView imageView = new ImageView(new Image(Objects.requireNonNull(ThroughputApplication.class.getResource(inTrainingServer.getImage())).openStream()));
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);

        Tooltip tooltip = new Tooltip();
        Image tooltipImage = new Image(Objects.requireNonNull(ThroughputApplication.class.getResourceAsStream(inTrainingServer.getBackImage())));
        ImageView tooltipImageView = new ImageView(tooltipImage);
        tooltipImageView.setFitHeight(100);
        tooltipImageView.setFitWidth(100);
        tooltip.setGraphic(tooltipImageView);

        //loop through skills
        VBox vBox = new VBox();
        buildServerSkillsBox(inTrainingServer, vBox, 100.0);
        vBox.setId("v_box_" + inTrainingBox.getId() + "_" + inTrainingServer.getColor().name());

        HBox hBox = new HBox(imageView, vBox);
        hBox.setPrefHeight(100);
        hBox.setPrefWidth(90);
        hBox.setId("h_box_" + inTrainingBox.getId() + "_" + inTrainingServer.getColor().name());
        Tooltip.install(hBox, tooltip);


        inTrainingBox.getChildren().add(hBox);
        VBox.setMargin(inTrainingBox, new Insets(0, 0, 0, 10));
    }

    private void updateScorecardTable() {
        ObservableList<ScoreCard> scoreCards = FXCollections.observableArrayList(ScorecardService.getScorecards());

        TableColumn<ScoreCard, Integer> periodCol = new TableColumn<>("Wk");
        periodCol.setCellValueFactory(new PropertyValueFactory<>("period"));

        TableColumn<ScoreCard, Integer> estimatedCol = new TableColumn<>("Est");
        estimatedCol.setCellValueFactory(new PropertyValueFactory<>("estimate"));

        TableColumn<ScoreCard, Integer> wipCol = new TableColumn<>("WIP");
        wipCol.setCellValueFactory(new PropertyValueFactory<>("workInProcess"));

        TableColumn<ScoreCard, Integer> finishedGoodsCol = new TableColumn<>("FIN");
        finishedGoodsCol.setCellValueFactory(new PropertyValueFactory<>("finishedGoods"));

        TableColumn<ScoreCard, Integer> scoreCol = new TableColumn<>("Pts");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));

        ObservableList<TableColumn<ScoreCard, ?>> columns = scoreTableView.getColumns();
        columns.setAll(Arrays.asList(periodCol, estimatedCol, wipCol, finishedGoodsCol, scoreCol));
        scoreTableView.setItems(scoreCards);
        scoreTableView.refresh();
    }

    @java.lang.SuppressWarnings({"java:S1190", "java:S117"})
    private void updateHoldCardBox() {
        holdCardBox.getChildren().clear();
        AtomicInteger periodIndex = new AtomicInteger(0);
        Board.getInstance().getPeriodHoldCards().forEach(_ -> buildHoldCards("2nd Chance", periodIndex.getAndIncrement(), 0));
        AtomicInteger gameIndex = new AtomicInteger(0);
        Board.getInstance().getGameHoldCards().forEach(card -> buildHoldCards(card.getInstructions(), gameIndex.getAndIncrement(), 1));

    }

    private void buildServerSkillsBox(Server server, VBox vBox, double boxHeight) {
        int skillsCount = server.getSkills().size();
        server.getSkills().forEach(color -> {
            Rectangle rectangle = new Rectangle(10, (boxHeight / skillsCount), color.lookupFXColor());
            vBox.getChildren().add(rectangle);
        });
        LOGGER.debug("Human server of color {}", server.getColor());
    }

    private void buildHoldCards(String text, int column, int row) {

        InnerShadow innerShadow = new InnerShadow();
        innerShadow.setOffsetX(3.0);
        innerShadow.setOffsetY(3.0);
        innerShadow.setColor(javafx.scene.paint.Color.BLACK);


        Rectangle rectangle = new Rectangle(110, row == 0 ? 60 : 110, javafx.scene.paint.Color.web("#e4fbff"));
        rectangle.setEffect(innerShadow);

        Label label = new Label(text);
        label.setLayoutX(5);
        label.setLayoutY(15);
        label.setWrapText(true);
        label.setPrefWidth(105);
        label.setCenterShape(true);
        label.setPadding(new Insets(5, 5, 5, 5));
        label.setFont(Font.font("Michelin", FontWeight.BOLD, FontPosture.ITALIC, 11.0));


        AnchorPane anchorPane = new AnchorPane(rectangle, label);

        HBox.setMargin(anchorPane, new Insets(5));
        ((GridPane) holdCardBox).add(anchorPane, column, row);
    }

    @java.lang.SuppressWarnings("java:S6878")
    private void bitActionsDetermined(int currentWorkstation) throws IOException {
        if (isVanilla()) {
            return;
        }
        BitCard bitCard = Prompts.drawBit(gameDialogPane, Board.getInstance().getDieFaces(), gameBoardLog, Board.getInstance().getDieFaces());
        //Discover bit actions handles a null bit card
        BoardAction boardAction = Board.getInstance().discoverBitActions(bitCard, Board.getInstance().getCurrentRunTurn(), Board.getInstance().getCurrentPeriod());
        switch (boardAction) {
            case Trap trap -> activateTrap(trap, bitCard, currentWorkstation);
            case HelpAction helpAction -> {
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
            case null, default -> {
                //do nothing
            }
        }

    }

    @FXML
    protected void doRunTurn(ActionEvent actionEvent) throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(actionEvent.toString());
        }
        //Stop timer
        if (timeline != null) timeline.stop();

        LOGGER.debug("Run Turn {}", Board.getInstance().getCurrentRunTurn());

        //During run activities period activities are hidden and run and server moves disabled
        buttonRunTurn.setDisable(true);
        buttonServerMoves.setDisable(true);

        highlightActiveWorkstation(-1);

        //Get Team mood and start moving work items

        int localBacklogCount = ScorecardService.getBacklog().getBacklogItemCount();
        int startValue = localBacklogCount > 0 ? Prompts.teamMood(gameDialogPane, Board.getInstance().getDieFaces()) : 0;

        Prompts.promptForWorkItemInitialMoves(gameDialogPane, startValue, localBacklogCount, gameBoardLog);
        redrawBoard();

        for (int stationIndex = 0; stationIndex < Board.getInstance().getStationCount(); stationIndex++) {
            highlightActiveWorkstation(stationIndex);
            //go through server workstations - redraws at end of each server turn
            runWorkstations(stationIndex);
        }

        if (Board.getInstance().getCurrentRunTurn() == Board.getInstance().getRunTurns()) {
            //hide run buttons
            turnButtonBar.setVisible(false);
            buttonRunTurn.setVisible(false);
            buttonServerMoves.setVisible(false);
            //End of period
            Prompts.publishEndPeriod(gameBoardLog);
        }

        //Next Run
        Board.getInstance().augmentRunTurn();
        highlightActiveWorkstation(100);

        //Clear in training box
        Board.getInstance().returnServerToOriginalWorkstation();
        inTrainingBox.getChildren().clear();

        if (Board.getInstance().gameIsOver()) {
            //Draw chart
            updateScorecardChart();
            //End of game
            gameButtonBar.setVisible(true);
            buttonEndGame.setVisible(true);
            redrawBoard();
            return;
        }

        if (Board.getInstance().getCurrentRunTurn() == 1) {

            //Make period buttons visible
            periodButtonBar.setVisible(true);
            buttonRunPeriod.setVisible(true);
            buttonRunPeriod.setDisable(false);
            buttonAddSkills.setVisible(true);
            buttonAddSkills.setDisable(false);

            redrawBoard();
            //Start Period
            Prompts.publishStartPeriod(gameBoardLog, Board.getInstance().getCurrentPeriod());

            //Timer for start of period
            buildTimer(START_TIME * 2);
            return;

        }

        //If not last run of the period re-enable run buttons
        if (Board.getInstance().getCurrentRunTurn() <= Board.getInstance().getRunTurns()) {
            //re-enable run  buttons
            buttonRunTurn.setDisable(false);
            buttonServerMoves.setDisable(isVanilla());
            redrawBoard();
            Prompts.publishTurnStart(gameBoardLog, Board.getInstance().getCurrentPeriod(), Board.getInstance().getCurrentRunTurn());

            //Timer for start of turn
            buildTimer(START_TIME);
            return;

        }
        throw new IllegalStateException("Run: " + Board.getInstance().getCurrentRunTurn() + " Period: " + Board.getInstance().getCurrentPeriod() + " Is not allowed!!");


    }

    @FXML
    protected void endGame(ActionEvent ignoredActionEvent) {
        Prompts.publishEndOfGame(gameBoardLog);
        buttonEndGame.setDisable(true);
    }

    @FXML
    protected void initialize() {

        workstationCount0.setText("000");
        workstationCount1.setText("000");
        workstationCount2.setText("000");
        workstationCount3.setText("000");
        workstationCount4.setText("000");

        backlogCount.setText("000");
        backlogCount.setBackground(new Background(new BackgroundFill(javafx.scene.paint.Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        finishedGoodsCount.setText("000");


        workstationLabel0.setText(Color.BLUE.name());
        workstationLabel1.setText(Color.GREEN.name());
        workstationLabel2.setText(Color.ROSE.name());
        workstationLabel3.setText(Color.YELLOW.name());
        workstationLabel4.setText(Color.VIOLET.name());

        runLabel.setText(System.getProperty("run.label", "Day"));
        runNumber.setText(String.valueOf(Board.getInstance().getCurrentRunTurn()));
        periodLabel.setText(System.getProperty("period.label", "Week"));
        periodNumber.setText(String.valueOf(Board.getInstance().getCurrentPeriod()));

        totalScore.setText("000");

        updateScorecardTable();
    }

    @FXML
    protected void loadGame(ActionEvent actionEvent) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(actionEvent.toString());
        }
        // Create an ObjectMapper for JSON deserialization
        ObjectMapper objectMapper = new ObjectMapper();

        // Get the resources directory within the project structure
        File gameFile = Prompts.promptToUploadPreviousGame(gameDialogPane, new File(getProjectResourcesPath()));

        try {
            // Read the JSON data from the file and deserialize it
            String jsonData = new String(Files.readAllBytes(gameFile.toPath()));
            TypeReference<HashMap<String, Object>> typeRef= new TypeReference<>() {};
            HashMap<String, Object> parsedJson = objectMapper.readValue(jsonData,typeRef);
            if(LOGGER.isDebugEnabled()) parsedJson.forEach((k, v) -> LOGGER.debug("{} : {}", k, v));
            Board.reloadInstance(parsedJson);

        } catch (IOException e) {
            throw new ThroughputRuntimeException(e);
        }


    }

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


    private void retryWithCard(Workstation workstation, int position, Server server) throws IOException {
        //Prompt for do over
        boolean retry = Prompts.promptForServerRetry(server);
        if (retry) {
            Board.getInstance().getPeriodHoldCards().removeFirst();
            updateHoldCardBox();
            ChanceResult result = Prompts.serverChanceCardPlay(gameDialogPane, server, workstation, gameBoardLog);
            if (ChanceResult.SUCCESS.equals(result)) {
                Prompts.promptForWorkItemWorkstationMoves(gameDialogPane, workstation, position);
            }
        }
    }

    private void retryWithPartner(Workstation workstation, int position, Server server) throws IOException {
        //Prompt for do over
        Prompts.promptForPairRetry(server, gameBoardLog);
        ChanceResult result = Prompts.serverChanceCardPlay(gameDialogPane, server, workstation, gameBoardLog);
        if (ChanceResult.SUCCESS.equals(result)) {
            Prompts.promptForWorkItemWorkstationMoves(gameDialogPane, workstation, position);
        }

    }

    @FXML
    protected void runGame(ActionEvent actionEvent) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(actionEvent.toString());
        }

        Prompts.publishStartPeriod(gameBoardLog, Board.getInstance().getCurrentPeriod());

        //Start the game by highlighting the Run Period button
        redrawBoard();
        buttonRunGame.setDisable(true);
        periodButtonBar.setVisible(true);
        buttonRunPeriod.setVisible(true);
        buttonRunPeriod.setDisable(false);
        buttonAddSkills.setVisible(true);
        buttonAddSkills.setDisable(true);

        buttonSaveGame.setDisable(false);
        buttonLoadGame.setDisable(true);

        //Start the 15-Second timer.
        //Timer label implementation
        buildTimer(15);
    }

    @java.lang.SuppressWarnings({"java:S1190", "java:S117"})
    private void buildTimer(Integer startTime) {

        countdownTimer.setTextFill(javafx.scene.paint.Color.DARKBLUE);
        //Start timer
        IntegerProperty timeSeconds =
                new SimpleIntegerProperty(startTime);
        timeline = new Timeline();
        countdownTimer.textProperty().bind(timeSeconds.asString());
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(startTime + 1.0),
                        new KeyValue(timeSeconds, 0)));
        timeline.setOnFinished(_ -> {
            countdownTimer.textProperty().unbind();
            countdownTimer.setText("X");
            countdownTimer.setTextFill(javafx.scene.paint.Color.RED);
        });
        timeline.playFromStart();

    }

    @FXML
    protected void runPeriod(ActionEvent actionEvent) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(actionEvent.toString());
        }
        //Stop timer
        if (timeline != null) timeline.stop();
        try {
            //Hide Period Buttons
            periodButtonBar.setVisible(false);
            buttonRunPeriod.setVisible(false);
            buttonAddSkills.setVisible(false);

            //Estimate Work Items
            Prompts.promptForWorkItemEstimates(gameDialogPane);
            redrawBoard();
            highlightActiveWorkstation(-1);

            //Skills add is triggered by button push

            //Show buttons to run turn
            turnButtonBar.setVisible(true);
            buttonRunTurn.setVisible(true);
            buttonRunTurn.setDisable(false);
            buttonServerMoves.setVisible(true);
            buttonServerMoves.setDisable(isVanilla());
            Prompts.publishTurnStart(gameBoardLog, Board.getInstance().getCurrentPeriod(), Board.getInstance().getCurrentRunTurn());
        } catch (IOException e) {
            throw new ThroughputRuntimeException(e);
        }


        //Start timer for the turn
        buildTimer(START_TIME);

    }

    private void highlightActiveWorkstation(int activeWorkstation) {
        //RESET colors
        Background whiteBackground = new Background(new BackgroundFill(javafx.scene.paint.Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY));
        backlogCount.setBackground(whiteBackground);
        workstationCount0.setBackground(whiteBackground);
        workstationCount1.setBackground(whiteBackground);
        workstationCount2.setBackground(whiteBackground);
        workstationCount3.setBackground(whiteBackground);
        workstationCount4.setBackground(whiteBackground);
        finishedGoodsCount.setBackground(whiteBackground);

        workstationCount0.setTextFill(javafx.scene.paint.Color.BLACK);
        workstationCount1.setTextFill(javafx.scene.paint.Color.BLACK);
        workstationCount2.setTextFill(javafx.scene.paint.Color.BLACK);
        workstationCount3.setTextFill(javafx.scene.paint.Color.BLACK);
        workstationCount4.setTextFill(javafx.scene.paint.Color.BLACK);

        switch (activeWorkstation) {
            case -1:
                backlogCount.setBackground(new Background(new BackgroundFill(javafx.scene.paint.Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
                break;
            case 0:
                workstationCount0.setBackground(new Background(new BackgroundFill(WorkstationService.getWorkstations()[0].getColor().lookupFXColor(), CornerRadii.EMPTY, Insets.EMPTY)));
                workstationCount0.setTextFill(WorkstationService.getWorkstations()[0].getColor().lookupFontColor());
                break;
            case 1:
                workstationCount1.setBackground(new Background(new BackgroundFill(WorkstationService.getWorkstations()[1].getColor().lookupFXColor(), CornerRadii.EMPTY, Insets.EMPTY)));
                workstationCount1.setTextFill(WorkstationService.getWorkstations()[1].getColor().lookupFontColor());
                break;
            case 2:
                workstationCount2.setBackground(new Background(new BackgroundFill(WorkstationService.getWorkstations()[2].getColor().lookupFXColor(), CornerRadii.EMPTY, Insets.EMPTY)));
                workstationCount2.setTextFill(WorkstationService.getWorkstations()[2].getColor().lookupFontColor());
                break;
            case 3:
                workstationCount3.setBackground(new Background(new BackgroundFill(WorkstationService.getWorkstations()[3].getColor().lookupFXColor(), CornerRadii.EMPTY, Insets.EMPTY)));
                workstationCount3.setTextFill(WorkstationService.getWorkstations()[3].getColor().lookupFontColor());
                break;
            case 4:
                workstationCount4.setBackground(new Background(new BackgroundFill(WorkstationService.getWorkstations()[4].getColor().lookupFXColor(), CornerRadii.EMPTY, Insets.EMPTY)));
                workstationCount4.setTextFill(WorkstationService.getWorkstations()[4].getColor().lookupFontColor());
                break;
            case 100:
                finishedGoodsCount.setBackground(new Background(new BackgroundFill(javafx.scene.paint.Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
                break;
            default:
                //do nothing
        }
    }

    private void runWorkstations(int position) throws IOException {
        //For each server
        Workstation workstation = WorkstationService.getWorkstation(position);
        Objects.requireNonNull(workstation);

        //Check if skip workstation has been triggered
        if (!workstation.isActive()) {
            workstation.setActive(true);
            redrawBoard();
            return;
        }

        //Don't roll for a Partner
        List<Server> serverList = workstation.getServers().stream().filter(server -> !(server instanceof PairPartner)).toList();
        for (Server server : serverList) {
            if (Board.getInstance().gameIsOver()) break;
            LOGGER.debug("Now serving server {}", server);

            ChanceResult result = Prompts.serverChanceCardPlay(gameDialogPane, server, workstation, gameBoardLog);
            switch (result) {
                case SUCCESS:
                    Prompts.promptForWorkItemWorkstationMoves(gameDialogPane, workstation, position);
                    break;
                case FAILED:
                    if (!Board.getInstance().getPeriodHoldCards().isEmpty()) {
                        retryWithCard(workstation, position, server);
                    } else if (workstation.getServers().stream().anyMatch(PairPartner.class::isInstance)) {
                        retryWithPartner(workstation, position, server);
                    }
                    break;
                case EMPTY:
                    break;
            }
            bitActionsDetermined(position);
            redrawBoard();
        }
    }

    @FXML
    protected void saveGame(ActionEvent actionEvent) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(actionEvent.toString());
        }

        String stateOfTheGame = Board.getInstance().toJSON();
        LOGGER.info(stateOfTheGame);
        try {
            // Get the resources directory within the project structure
            String resourcesPath = getProjectResourcesPath();

            // Create the resources directory if it doesn't exist
            File resourcesDirectory = new File(resourcesPath + "/savedGames");
            if (!resourcesDirectory.exists()) resourcesDirectory.mkdirs();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            JsonNode node = objectMapper.readTree(stateOfTheGame);

            // Create the file path
            String filePath = resourcesPath + "/savedGames/" + "game_" + System.currentTimeMillis() + ".json";
            LOGGER.info("Data saved to: {}", filePath);
            objectMapper.writer().withDefaultPrettyPrinter().writeValue(new File(filePath), node);

        } catch (IOException e) {
            throw new ThroughputRuntimeException(e);
        }

    }

    @FXML
    protected void serverMoves(ActionEvent actionEvent) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(actionEvent.toString());
        }
        try {
            Prompts.promptForServerMoves(gameDialogPane, Board.getInstance().getInTrainingServer(), this);
        } catch (IOException e) {
            throw new ThroughputRuntimeException(e);
        }
    }

    @FXML
    protected void showInfo(ActionEvent actionEvent) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(actionEvent.toString());
        }
        try {
            Prompts.showInfoCard(gameDialogPane);
        } catch (IOException e) {
            throw new ThroughputRuntimeException(e);
        }
    }

    @FXML
    protected void showRules(ActionEvent actionEvent) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(actionEvent.toString());
        }
        try {
            Prompts.showRulesCard(gameDialogPane);
        } catch (IOException e) {
            throw new ThroughputRuntimeException(e);
        }
    }

    private void updateScorecardChart() {
        scoreLineChart.getData().clear();

        ScoreCard[] scorecards = ScorecardService.getScorecards();
        //defining a series
        XYChart.Series<Integer, Integer> series = new XYChart.Series<>();

        for (int scorecardIndex = 0; scorecardIndex < scorecards.length; scorecardIndex++) {
            //populating the series with data
            series.getData().add(new XYChart.Data<>(scorecardIndex + 1, scorecards[scorecardIndex].getScore()));
        }
        scoreLineChart.setCreateSymbols(false);
        scoreLineChart.setAnimated(false);
        scoreLineChart.setLegendVisible(false);
        scoreLineChart.getData().add(series);
    }

}
