package com.michelin.throughputfxproject.controllers;

import com.michelin.throughputfxproject.*;
import com.michelin.throughputfxproject.entities.*;
import com.michelin.throughputfxproject.entities.cards.BitCard;
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
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.michelin.throughputfxproject.Board.SIX_SIDES;


public class BoardController {

    public static final Logger LOGGER = LoggerFactory.getLogger(BoardController.class.getName());
    @FXML
    private HBox holdCardBox;
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

        dayNumber.setText(String.valueOf(Board.getDayOfTheWeek() + 1));
        weekNumber.setText(String.valueOf(Board.getGameWeek() + 1));

        totalScore.setText("000");

        updateScorecardTable();
    }

    private void redrawBoard(int activeWorkstation) {

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
        } catch (IOException e) {
            throw new ThroughputRuntimeException(e);
        }

        workstationCount0.setText(StringUtils.leftPad(String.valueOf(workstation0.getWorkItemCount()), 3, '0'));
        workstationCount1.setText(StringUtils.leftPad(String.valueOf(workstation1.getWorkItemCount()), 3, '0'));
        workstationCount2.setText(StringUtils.leftPad(String.valueOf(workstation2.getWorkItemCount()), 3, '0'));
        workstationCount3.setText(StringUtils.leftPad(String.valueOf(workstation3.getWorkItemCount()), 3, '0'));
        workstationCount4.setText(StringUtils.leftPad(String.valueOf(workstation4.getWorkItemCount()), 3, '0'));

        switch (activeWorkstation) {
            case 0:
                backlogCount.setBackground(new Background(new BackgroundFill(javafx.scene.paint.Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
                break;
            case 1:
                workstationCount0.setBackground(new Background(new BackgroundFill(workstation0.getColor().lookupFXColor(), CornerRadii.EMPTY, Insets.EMPTY)));
                break;
            case 2:
                workstationCount1.setBackground(new Background(new BackgroundFill(workstation1.getColor().lookupFXColor(), CornerRadii.EMPTY, Insets.EMPTY)));
                break;
            case 3:
                workstationCount2.setBackground(new Background(new BackgroundFill(workstation2.getColor().lookupFXColor(), CornerRadii.EMPTY, Insets.EMPTY)));
                break;
            case 4:
                workstationCount3.setBackground(new Background(new BackgroundFill(workstation3.getColor().lookupFXColor(), CornerRadii.EMPTY, Insets.EMPTY)));
                break;
            case 5:
                workstationCount4.setBackground(new Background(new BackgroundFill(workstation4.getColor().lookupFXColor(), CornerRadii.EMPTY, Insets.EMPTY)));
                break;
            case 6:
                finishedGoodsCount.setBackground(new Background(new BackgroundFill(javafx.scene.paint.Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
                break;
            default:
        }

        backlogCount.setText(StringUtils.leftPad(String.valueOf(ScorecardService.getBacklog().getBacklogItemCount()), 3, '0'));
        finishedGoodsCount.setText(StringUtils.leftPad(String.valueOf(ScorecardService.getFinishedGoods().getFinishedGoodsTally()), 3, '0'));

        workstationLabel0.setText(workstation0.getColor().name() + ": " + workstation0.getCapacity());
        workstationLabel1.setText(workstation1.getColor().name() + ": " + workstation1.getCapacity());
        workstationLabel2.setText(workstation2.getColor().name() + ": " + workstation2.getCapacity());
        workstationLabel3.setText(workstation3.getColor().name() + ": " + workstation3.getCapacity());
        workstationLabel4.setText(workstation4.getColor().name() + ": " + workstation4.getCapacity());

        dayNumber.setText(String.valueOf(Board.getDayOfTheWeek() + 1));
        weekNumber.setText(String.valueOf(Board.getGameWeek() + 1));

        totalScore.setText(String.valueOf(ScorecardService.getTotalScore()));

        updateScorecardTable();
        updateHoldCardBox();
    }

    private void updateHoldCardBox() {
        Board.getWeekHoldCards().forEach(card -> buildHoldCards("2nd Chance"));
        Board.getGameHoldCards().forEach(card -> buildHoldCards(card.getInstructions()));

    }

    private void buildHoldCards(String text) {
        Rectangle rectangle = new Rectangle(110, 120, javafx.scene.paint.Color.WHITE);
        rectangle.setStroke(javafx.scene.paint.Color.YELLOW);
        rectangle.setStrokeWidth(2);
        Label label = new Label(text);
        label.setLayoutX(40);
        label.setLayoutY(60);
        AnchorPane anchorPane = new AnchorPane(rectangle, label);
        HBox.setMargin(anchorPane, new Insets(5));
        holdCardBox.getChildren().add(anchorPane);
    }

    private void buildServerCards(List<Server> serverSet, Pane serverHolder) throws IOException {
        serverHolder.getChildren().clear();
        for (Server server : serverSet) {
            String serverImageFile = getImageStringForServer(server);

            ImageView imageView = new ImageView(new Image(Objects.requireNonNull(ThroughputApplication.class.getResource(serverImageFile)).openStream()));
            imageView.setFitHeight(60);
            imageView.setFitWidth(53);

            //loop through skills
            VBox vBox = new VBox();
            if (server.getType().equals(Server.TYPE_HUMAN)) {
                int skillsCount = server.getSkills().size();
                server.getSkills().forEach(color -> {
                    Rectangle rectangle = new Rectangle(10, ((double) 60 / skillsCount), server.getColor().lookupFXColor());
                    vBox.getChildren().add(rectangle);
                });
                LOGGER.info("Human server of color {}", server.getColor());
            } else {
                LOGGER.info("Non Human server of color {}", server.getColor());
            }

            HBox hBox = new HBox(imageView, vBox);
            hBox.setPrefHeight(60);
            hBox.setPrefWidth(63);

            serverHolder.getChildren().add(hBox);
        }


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

    @FXML
    protected void runGame(ActionEvent actionEvent) {
        redrawBoard(0);
        buttonRunGame.setDisable(true);
        executeGame();
    }


    @SneakyThrows
    private void runDay() {
        LOGGER.debug("Run Day {}", Board.getDayOfTheWeek() + 1);

        Prompts.publishDayStart(Board.getDayOfTheWeek(), Board.getGameWeek());

        //Get Team mood and start moving work items
        int startValue = 0;
        int localBacklogCount = ScorecardService.getBacklog().getBacklogItemCount();
        if (localBacklogCount > 0) {
            startValue = Prompts.teamMood(SIX_SIDES);
        }
        Prompts.promptForWorkItemInitialMoves(gameDialogPane, startValue, localBacklogCount);

        for (int i = 0; i < Board.FIVE_STATIONS; i++) {
            runWorkstationDay(i);
            redrawBoard(i+1);
        }

        Board.returnServerToWorkstation();

        Board.augmentDayOfTheWeek();
        if (Board.getDayOfTheWeek() == (Board.RUN_DAYS - 1)) {
            ScoreCard scoreCard = ScorecardService.getScorecards()[Board.getGameWeek()];
            Board.getWeekHoldCards().clear();
            //Tally board
            scoreCard.setWorkInProcess(WorkstationService.tallyWorkInProcess());
            scoreCard.setFinishedGoods(ScorecardService.getFinishedGoods().getFinishedGoodsTally());
            scoreCard.setScore(ScorecardService.getFinishedGoods().calculateScore() - (ScorecardService.getBacklog().getBacklogItemCount() + scoreCard.getWorkInProcess()));
            //Remove finished Goods
            ScorecardService.getFinishedGoods().setFinishedGoodsTally(0);

            Prompts.publishEndWeek();
        }
        redrawBoard(6);
    }

    public void runDay(ActionEvent actionEvent) {
        //During run day activities week activities are hidden and run day and server moves disabled
        buttonRunDay.setDisable(true);
        buttonServerMoves.setDisable(true);

        runDay();
        //if it is not the last day show run day and enable server moves
        if (Board.getDayOfTheWeek() < (Board.RUN_DAYS - 1)) {
            buttonRunDay.setDisable(false);
            buttonServerMoves.setDisable(false);
        } else {
            //re-enable week buttons and disable day buttons
            dailyButtonBar.setVisible(false);
            buttonRunDay.setVisible(false);
            buttonServerMoves.setVisible(false);

            if (Board.getGameWeek() < Board.RUN_WEEKS) {
                Board.augmentGameWeek();
                weeklyButtonBar.setVisible(true);
                buttonRunWeek.setVisible(true);
                buttonAddSkills.setVisible(true);
                buttonAddSkills.setDisable(false);
            } else {
                gameButtonBar.setVisible(true);
                buttonEndGame.setVisible(true);
            }
        }
    }

    public void serverMoves(ActionEvent actionEvent) {

        try {
            Prompts.promptForServerMoves(gameDialogPane, Board.getInTrainingServer());
        } catch (IOException e) {
            throw new ThroughputRuntimeException(e);
        }

    }

    public void executeGame() {

        try {
            runWeek();

        } catch (IOException e) {
            throw new ThroughputRuntimeException(e);
        }

    }

    private void runWeek() throws IOException {
        //Start Week
        Prompts.publishStartWeek(Board.getGameWeek());
        //Hide Week Buttons
        weeklyButtonBar.setVisible(false);
        buttonRunWeek.setVisible(false);
        buttonAddSkills.setVisible(false);

        //Estimate Work Items
        Prompts.promptForWorkItemEstimates(gameDialogPane);
        redrawBoard(0);

        //Skills add is triggered by button push

        //Show buttons to run day
        dailyButtonBar.setVisible(true);
        buttonRunDay.setVisible(true);
        buttonRunWeek.setDisable(false);
        if (!isVanilla()) {
            buttonServerMoves.setVisible(true);
            buttonServerMoves.setDisable(false);
        }
    }

    public void runWeek(ActionEvent actionEvent) {
        try {
            runWeek();
        } catch (IOException e) {
            throw new ThroughputRuntimeException(e);
        }
    }

    private static boolean isVanilla() {
        return Board.getGameWeek() < 0;
    }

    public void addSkillsToServer(ActionEvent actionEvent) {

        try {
            boolean addSkills = false;
            if (Board.getGameWeek() != 0) {
                addSkills = Prompts.promptToDrawSkillsCard();
            }
            if (addSkills) {
                Prompts.promptToAddSkill(gameDialogPane);
            }

        } catch (IOException e) {
            throw new ThroughputRuntimeException(e);
        }
    }


    private void runWorkstationDay(int position) throws InterruptedException, IOException {
        //For each server
        for (Server server : Objects.requireNonNull(WorkstationService.getWorkstation(position)).getServers()) {
            LOGGER.info("Now serving server {}", server);
            ChanceResult result = Prompts.serverChanceCardPlay(server, position);
            switch (result) {
                case SUCCESS:
                    Prompts.promptForWorkItemWorkstationMoves(gameDialogPane,  position);
                    break;
                case FAILED:
                    if (Board.getWeekHoldCards().isEmpty()) {
                        TimeUnit.MILLISECONDS.sleep(500);
                    } else {
                        retry(position, server);
                    }
                    break;
                case EMPTY:
                    break;
            }
            bitActionsDetermined();
        }
    }

    private void retry(int position, Server server) throws IOException {

        //Prompt for do over
        boolean retry = Prompts.promptForServerRetry(server);
        if (retry && !Board.getWeekHoldCards().isEmpty()) {
            Board.getWeekHoldCards().remove(0);
            updateHoldCardBox();
            ChanceResult result = Prompts.serverChanceCardPlay(server, position);
            if (ChanceResult.SUCCESS.equals(result)) {
                Prompts.promptForWorkItemWorkstationMoves(gameDialogPane, position);
            }
        }
    }

    private void bitActionsDetermined() throws IOException {
        if (isVanilla()) {
            return;
        }
        BitCard bitCard = Prompts.drawBit(gameDialogPane, SIX_SIDES);
        //Discover bit actions handles a null bit card
        BoardAction boardAction = Board.discoverBitActions(bitCard, Board.getDayOfTheWeek(), Board.getGameWeek());
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
                    Prompts.promptToAutomateWorkstation(gameDialogPane);
                    return;
                case PAIR:
                    Prompts.implementPairedProgramming(gameDialogPane);
                    return;
                case AUGMENT:
                    Prompts.promptForFinishedGoodsAreNowFourPoints();
            }

        }

    }

    private static void activateTrap(Trap trap, BitCard bitCard) {

        boolean trapMitigated = Board.isTrapMitigated(bitCard);
        Prompts.promptForAppliedTrap(trap, trapMitigated);
        if (!trapMitigated) {
            if (trap.getEffected().equals(Board.TEAM) && trap.getDuration().equals(Board.WEEK)) {
                Board.augmentGameWeek();
            } else if (trap.getEffected().equals(Board.TEAM) && trap.getDuration().equals(Board.DAY)) {
                Board.augmentDayOfTheWeek();
            }
        } else {
            if (trap.getEffected().equals(Board.TEAM) && trap.getMitigatedDuration().equals(Board.DAY)) {
                Board.augmentDayOfTheWeek();
            }
        }
    }

    public void endGame(ActionEvent ignoredActionEvent) {
        Prompts.publishEndOfGame();
    }

}
