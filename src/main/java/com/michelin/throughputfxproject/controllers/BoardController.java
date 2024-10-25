package com.michelin.throughputfxproject.controllers;

import com.michelin.throughputfxproject.ChanceResult;
import com.michelin.throughputfxproject.Color;
import com.michelin.throughputfxproject.Prompts;
import com.michelin.throughputfxproject.ThroughputApplication;
import com.michelin.throughputfxproject.entities.*;
import com.michelin.throughputfxproject.entities.cards.BitCard;
import com.michelin.throughputfxproject.entities.servers.HumanServer;
import com.michelin.throughputfxproject.entities.servers.PairPartner;
import com.michelin.throughputfxproject.exceptions.ThroughputRuntimeException;
import com.michelin.throughputfxproject.services.ScorecardService;
import com.michelin.throughputfxproject.services.WorkstationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.michelin.throughputfxproject.Board.*;


public class BoardController {

    public static final Logger LOGGER = LoggerFactory.getLogger(BoardController.class.getName());

    @FXML
    private TextArea gameBoardLog;
    @FXML
    private Pane inTrainingBox;
    @FXML
    private Pane holdCardBox;
    @FXML
    private ButtonBar dailyButtonBar;
    @FXML
    private ButtonBar weeklyButtonBar;
    @FXML
    private ButtonBar gameButtonBar;
    @FXML
    private Button buttonServerMoves;
    @FXML
    private Button buttonRunDay;
    @FXML
    private Button buttonAddSkills;
    @FXML
    private Button buttonRunWeek;
    @FXML
    private Button buttonRunGame;
    @FXML
    private Pane gameDialogPane;
    @FXML
    private TableView<ScoreCard> scoreTableView;
    @FXML
    private Label totalScore;
    @FXML
    private Label dayNumber;
    @FXML
    private Label weekNumber;
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

    private  void activateTrap(Trap trap, BitCard bitCard) {

        boolean trapMitigated = isTrapMitigated(bitCard);
        Prompts.promptForAppliedTrap(trap, trapMitigated,gameBoardLog);
        if (!trapMitigated) {
            if (trap.getEffected().equals(TEAM) && trap.getDuration().equals(WEEK)) {
                augmentGameWeek();
            } else if (trap.getEffected().equals(TEAM) && trap.getDuration().equals(DAY)) {
                augmentDayOfTheWeek();
            }
        } else {
            if (trap.getEffected().equals(TEAM) && trap.getMitigatedDuration().equals(DAY)) {
                augmentDayOfTheWeek();
            }
        }
    }

    public void addSkillsToServer(ActionEvent actionEvent) {

        try {
            if (!isVanilla() && Prompts.promptToDrawSkillsCard(gameDialogPane)) {
                Prompts.promptToAddSkill(gameDialogPane);
            }
        } catch (IOException e) {
            throw new ThroughputRuntimeException(e);
        }
        buttonAddSkills.setDisable(true);
        redrawBoard();
    }

    private static boolean isVanilla() {
        return getGameWeek() == 1;
    }

    private void redrawBoard() {

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
            buildInTrainingCard(getInTrainingServer(), inTrainingBox);
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

        dayNumber.setText(String.valueOf(getDayOfTheWeek()));
        weekNumber.setText(String.valueOf(getGameWeek()));

        totalScore.setText(String.valueOf(ScorecardService.getTotalScore()));

        updateScorecardTable();
        updateHoldCardBox();
    }

    private void buildServerCards(Set<Server> serverSet, Pane serverHolder) throws IOException {
        serverHolder.getChildren().clear();
        for (Server server : serverSet) {
            String serverImageFile = getImageStringForServer(server);

            ImageView imageView = new ImageView(new Image(Objects.requireNonNull(ThroughputApplication.class.getResource(serverImageFile)).openStream()));
            imageView.setFitHeight(60);
            imageView.setFitWidth(53);

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

            serverHolder.getChildren().add(hBox);
        }
    }

    private void buildInTrainingCard(HumanServer inTrainingServer, Pane inTrainingBox) throws IOException {
        if (inTrainingServer == null) {
            return;
        }
        inTrainingBox.getChildren().clear();
        String serverImageFile = getImageStringForServer(inTrainingServer);

        ImageView imageView = new ImageView(new Image(Objects.requireNonNull(ThroughputApplication.class.getResource(serverImageFile)).openStream()));
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);

        //loop through skills
        VBox vBox = new VBox();
        buildServerSkillsBox(inTrainingServer, vBox, 100.0);

        vBox.setId("v_box_" + inTrainingBox.getId() + "_" + inTrainingServer.getColor().name());

        HBox hBox = new HBox(imageView, vBox);
        hBox.setPrefHeight(100);
        hBox.setPrefWidth(90);
        hBox.setId("h_box_" + inTrainingBox.getId() + "_" + inTrainingServer.getColor().name());

        inTrainingBox.getChildren().add(hBox);
        VBox.setMargin(inTrainingBox, new Insets(0, 0, 0, 10));
    }

    private void updateScorecardTable() {
        ObservableList<ScoreCard> scoreCards = FXCollections.observableArrayList(ScorecardService.getScorecards());

        TableColumn<ScoreCard, Integer> weekCol = new TableColumn<>("Wk");
        weekCol.setCellValueFactory(new PropertyValueFactory<>("week"));

        TableColumn<ScoreCard, Integer> estimatedCol = new TableColumn<>("Est");
        estimatedCol.setCellValueFactory(new PropertyValueFactory<>("estimate"));

        TableColumn<ScoreCard, Integer> wipCol = new TableColumn<>("WIP");
        wipCol.setCellValueFactory(new PropertyValueFactory<>("workInProcess"));

        TableColumn<ScoreCard, Integer> finishedGoodsCol = new TableColumn<>("FIN");
        finishedGoodsCol.setCellValueFactory(new PropertyValueFactory<>("finishedGoods"));

        TableColumn<ScoreCard, Integer> scoreCol = new TableColumn<>("Pts");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));

        ObservableList<TableColumn<ScoreCard, ?>> columns = scoreTableView.getColumns();
        columns.setAll(weekCol, estimatedCol, wipCol, finishedGoodsCol, scoreCol);
        scoreTableView.setItems(scoreCards);
        scoreTableView.refresh();
    }

    private void updateHoldCardBox() {
        holdCardBox.getChildren().clear();
        AtomicInteger weeklyIndex = new AtomicInteger(0);
        getWeekHoldCards().forEach((card -> buildHoldCards("2nd Chance", weeklyIndex.getAndIncrement(), 0)));
        AtomicInteger gameIndex = new AtomicInteger(0);
        getGameHoldCards().forEach(card -> buildHoldCards(card.getInstructions(), gameIndex.getAndIncrement(), 1));

    }

    private static String getImageStringForServer(Server server) {
        String serverImageFile;
        switch (server.getColor()) {
            case BLUE:
                serverImageFile = "servers/server_blue.jpg";
                break;
            case GREEN:
                if (server.getType().equals(Server.TYPE_AUTOMATED)) {
                    serverImageFile = "servers/robot_green.jpg";
                } else {
                    serverImageFile = "servers/server_green.jpg";
                }
                break;
            case YELLOW:
                if (server.getType().equals(Server.TYPE_AUTOMATED)) {
                    serverImageFile = "servers/robot_yellow.jpg";
                } else {
                    serverImageFile = "servers/server_yellow.jpg";
                }
                break;
            case VIOLET:
                serverImageFile = "servers/server_violet.jpg";
                break;
            case ROSE:
                if (server.getType().equals(Server.TYPE_AUTOMATED)) {
                    serverImageFile = "servers/robot_rose.jpg";
                } else {
                    serverImageFile = "servers/server_rose.jpg";
                }
                break;
            case GRAY:
            default:
                serverImageFile = "servers/server_pair.jpg";
        }
        return serverImageFile;
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

        Rectangle rectangle = new Rectangle(110, row == 0 ? 60 : 90, javafx.scene.paint.Color.LIGHTGRAY);
        rectangle.setStroke(javafx.scene.paint.Color.YELLOW);
        rectangle.setStrokeWidth(2);
        Label label = new Label(text);
        label.setLayoutX(5);
        label.setLayoutY(15);
        label.setWrapText(true);
        label.setPrefWidth(105);
        label.setCenterShape(true);
        AnchorPane anchorPane = new AnchorPane(rectangle, label);
        HBox.setMargin(anchorPane, new Insets(5));
        ((GridPane) holdCardBox).add(anchorPane, column, row);
    }

    private void bitActionsDetermined() throws IOException {
        if (isVanilla()) {
            return;
        }
        BitCard bitCard = Prompts.drawBit(gameDialogPane, SIX_SIDES,gameBoardLog);
        //Discover bit actions handles a null bit card
        BoardAction boardAction = discoverBitActions(bitCard, getDayOfTheWeek(), getGameWeek());
        if (boardAction == null) {
            return;
        }

        if (boardAction instanceof Trap) {
            activateTrap((Trap) boardAction, bitCard);
        } else if (boardAction instanceof HelpAction) {
            switch (((HelpAction) boardAction).getType()) {
                case ADD_ONE:
                    Prompts.promptToAugmentWorkstationCapacity(gameDialogPane, false);
                    return;
                case DOUBLE:
                    Prompts.promptToAugmentWorkstationCapacity(gameDialogPane, true);
                    return;
                case AUTOMATE:
                    Prompts.promptToAutomateWorkstation(gameDialogPane,gameBoardLog);
                    return;
                case PAIR:
                    Prompts.implementPairedProgramming(gameDialogPane,gameBoardLog);
                    return;
                case AUGMENT:
                    Prompts.promptForFinishedGoodsAreNowFourPoints(gameBoardLog);
            }
        }
    }

    public void endGame(ActionEvent ignoredActionEvent) {
        Prompts.publishEndOfGame(gameBoardLog);
    }

    public void executeGame() {

        //Start the game by highlighting the Run Week button
        redrawBoard();
        buttonRunGame.setDisable(true);
        weeklyButtonBar.setVisible(true);
        buttonRunWeek.setVisible(true);
        buttonRunWeek.setDisable(false);
        buttonAddSkills.setVisible(false);
        Prompts.publishStartWeek(gameBoardLog);

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

        dayNumber.setText(String.valueOf(getDayOfTheWeek()));
        weekNumber.setText(String.valueOf(getGameWeek()));

        totalScore.setText("000");

        updateScorecardTable();
    }

    private void retryWithCard(int position, Server server) throws IOException {
        //Prompt for do over
        boolean retry = Prompts.promptForServerRetry(server);
        if (retry) {
            getWeekHoldCards().remove(0);
            updateHoldCardBox();
            ChanceResult result = Prompts.serverChanceCardPlay(gameDialogPane, server, position,gameBoardLog);
            if (ChanceResult.SUCCESS.equals(result)) {
                Prompts.promptForWorkItemWorkstationMoves(gameDialogPane, position,gameBoardLog);
            }
        }
    }

    private void retryWithPartner(int position, Server server) throws IOException {
        //Prompt for do over
        Prompts.promptForPairRetry(server,gameBoardLog);
        ChanceResult result = Prompts.serverChanceCardPlay(gameDialogPane, server, position,gameBoardLog);
        if (ChanceResult.SUCCESS.equals(result)) {
            Prompts.promptForWorkItemWorkstationMoves(gameDialogPane, position,gameBoardLog);
        }

    }


    private void runDay() throws IOException, InterruptedException {
        LOGGER.debug("Run Day {}", getDayOfTheWeek());

        //During run day activities week activities are hidden and run day and server moves disabled
        buttonRunDay.setDisable(true);
        buttonServerMoves.setDisable(true);

        highlightActiveWorkstation(-1);

        //Get Team mood and start moving work items

        int localBacklogCount = ScorecardService.getBacklog().getBacklogItemCount();
        int startValue = localBacklogCount > 0 ? Prompts.teamMood(gameDialogPane, SIX_SIDES) : 0;

        Prompts.promptForWorkItemInitialMoves(gameDialogPane, startValue, localBacklogCount,gameBoardLog);
        redrawBoard();

        for (int stationIndex = 0; stationIndex < FIVE_STATIONS; stationIndex++) {
            highlightActiveWorkstation(stationIndex);
            //go through server workstations - redraws at end of each server turn
            runWorkstationDay(stationIndex);
            TimeUnit.MILLISECONDS.sleep(1500);
        }


        highlightActiveWorkstation(100);

        //Clear in training box
        returnServerToWorkstation();
        inTrainingBox.getChildren().clear();
        redrawBoard();

        //If not last day of the week re-enable run day buttons
        if (getDayOfTheWeek() < RUN_DAYS) {
            //re-enable run day buttons
            buttonRunDay.setDisable(false);
            buttonServerMoves.setDisable(false);
            Prompts.publishDayStart(gameBoardLog);
        } else {
            //hide day buttons
            dailyButtonBar.setVisible(false);
            buttonRunDay.setVisible(false);
            buttonServerMoves.setVisible(false);
        }

    }

    public void runDay(ActionEvent actionEvent) throws InterruptedException {

        //Run day
        try {
            runDay();
        } catch (IOException e) {
            throw new ThroughputRuntimeException(e);
        }

        //Run End of Week Activities
        if (getDayOfTheWeek() == RUN_DAYS) {
            if (getGameWeek() < RUN_WEEKS) {
                weeklyButtonBar.setVisible(true);
                buttonRunWeek.setVisible(true);
                buttonRunWeek.setDisable(false);
                buttonAddSkills.setVisible(true);
                buttonAddSkills.setDisable(false);
                augmentGameWeek();
                redrawBoard();
            } else {
                //End of game
                gameButtonBar.setVisible(true);
                buttonEndGame.setVisible(true);
            }

            ScoreCard scoreCard = ScorecardService.getScorecardForCurrentWeek();
            getWeekHoldCards().clear();
            //Tally board
            scoreCard.setWorkInProcess(WorkstationService.tallyWorkInProcess());
            scoreCard.setFinishedGoods(ScorecardService.getFinishedGoods().getFinishedGoodsTally());
            scoreCard.setScore(ScorecardService.getFinishedGoods().calculateScore() - (ScorecardService.getBacklog().getBacklogItemCount() + scoreCard.getWorkInProcess()));
            //Remove finished Goods
            ScorecardService.getFinishedGoods().setFinishedGoodsTally(0);
            Prompts.publishEndWeek(gameBoardLog);
            resetDayOfTheWeek();

            TimeUnit.MILLISECONDS.sleep(2000);

            //Start Week
            Prompts.publishStartWeek(gameBoardLog);

        } else {
            augmentDayOfTheWeek();
            redrawBoard();
        }


    }

    @FXML
    protected void runGame(ActionEvent actionEvent) {
        String gettingStarted = "Game Starting.....";
        gameBoardLog.setText(gettingStarted);

        executeGame();
    }

    public void runWeek(ActionEvent actionEvent)  {
        try {
            runWeek();
        } catch (IOException e) {
            throw new ThroughputRuntimeException(e);
        }
        Prompts.publishDayStart(gameBoardLog);
    }

    private void runWeek() throws IOException {

        //Hide Week Buttons
        weeklyButtonBar.setVisible(false);
        buttonRunWeek.setVisible(false);
        buttonAddSkills.setVisible(false);

        //Estimate Work Items
        Prompts.promptForWorkItemEstimates(gameDialogPane);
        redrawBoard();
        highlightActiveWorkstation(-1);

        //Skills add is triggered by button push

        //Show buttons to run day
        dailyButtonBar.setVisible(true);
        buttonRunDay.setVisible(true);
        buttonRunDay.setDisable(false);
        if (isVanilla()) {
            buttonServerMoves.setVisible(false);
            buttonServerMoves.setDisable(true);
        } else {
            buttonServerMoves.setVisible(true);
            buttonServerMoves.setDisable(false);
        }
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

    private void runWorkstationDay(int position) throws InterruptedException, IOException {
        //For each server
        Workstation workstation = WorkstationService.getWorkstation(position);
        Objects.requireNonNull(workstation);
        Set<Server> servers = workstation.getServers();
        List<Server> serverList = new ArrayList<>(servers);
        for (Server server : serverList) {
            LOGGER.info("Now serving server {}", server);
            //Don't roll for a Partner
            if (server instanceof PairPartner) continue;
            ChanceResult result = Prompts.serverChanceCardPlay(gameDialogPane, server, position,gameBoardLog);
            switch (result) {
                case SUCCESS:
                    Prompts.promptForWorkItemWorkstationMoves(gameDialogPane, position,gameBoardLog);
                    break;
                case FAILED:
                    if (!getWeekHoldCards().isEmpty()) {
                        retryWithCard(position, server);
                    } else if (workstation.getServers().stream().anyMatch(PairPartner.class::isInstance)) {
                        retryWithPartner(position, server);
                    } else {
                        TimeUnit.MILLISECONDS.sleep(500);
                    }
                    break;
                case EMPTY:
                    break;
            }
            bitActionsDetermined();
            redrawBoard();
        }
    }

    public void serverMoves(ActionEvent actionEvent) {

        try {
            Prompts.promptForServerMoves(gameDialogPane, getInTrainingServer());
        } catch (IOException e) {
            throw new ThroughputRuntimeException(e);
        }
        redrawBoard();
    }

    public void showInfo(ActionEvent actionEvent) {
        try {
            Prompts.showInfoCard(gameDialogPane);
        } catch (IOException e) {
            throw new ThroughputRuntimeException(e);
        }
    }

    public void showRules(ActionEvent actionEvent) {
        try {
            Prompts.showRulesCard(gameDialogPane);
        } catch (IOException e) {
            throw new ThroughputRuntimeException(e);
        }
    }

}
