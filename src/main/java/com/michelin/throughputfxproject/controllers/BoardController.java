package com.michelin.throughputfxproject.controllers;

import com.michelin.throughputfxproject.Board;
import com.michelin.throughputfxproject.Color;
import com.michelin.throughputfxproject.Prompts;
import com.michelin.throughputfxproject.entities.*;
import com.michelin.throughputfxproject.entities.cards.BitCard;
import com.michelin.throughputfxproject.exceptions.ThroughputRuntimeException;
import com.michelin.throughputfxproject.services.ScorecardService;
import com.michelin.throughputfxproject.services.WorkstationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.michelin.throughputfxproject.Board.SIX_SIDES;


public class BoardController {

    public static final Logger LOGGER = LoggerFactory.getLogger(BoardController.class.getName());

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
    private Label servers00;
    @FXML
    private Label servers10;
    @FXML
    private Label servers20;
    @FXML
    private Label servers30;
    @FXML
    private Label servers40;
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

        servers00.setText(Color.BLUE.name());
        servers10.setText(Color.GREEN.name());
        servers20.setText(Color.ROSE.name());
        servers30.setText(Color.YELLOW.name());
        servers40.setText(Color.VIOLET.name());

        workstationCount0.setText("000");
        workstationCount1.setText("000");
        workstationCount2.setText("000");
        workstationCount3.setText("000");
        workstationCount4.setText("000");

        backlogCount.setText("000");
        finishedGoodsCount.setText("000");

        workstationLabel0.setText(Color.BLUE.name());
        workstationLabel1.setText(Color.GREEN.name());
        workstationLabel2.setText(Color.ROSE.name());
        workstationLabel3.setText(Color.YELLOW.name());
        workstationLabel4.setText(Color.VIOLET.name());

        dayNumber.setText(String.valueOf(Board.getInstance().getDayOfTheWeek() + 1));
        weekNumber.setText(String.valueOf(Board.getInstance().getGameWeek() + 1));

        totalScore.setText("000");

        updateScorecardTable();
    }

    private void reinitialize() {

        Workstation workstation0 = WorkstationService.getWorkstations()[0];
        Workstation workstation1 = WorkstationService.getWorkstations()[1];
        Workstation workstation2 = WorkstationService.getWorkstations()[2];
        Workstation workstation3 = WorkstationService.getWorkstations()[3];
        Workstation workstation4 = WorkstationService.getWorkstations()[4];


        servers00.setText(workstation0.getServers().get(0).getSkillsString());
        servers10.setText(workstation1.getServers().get(0).getSkillsString());
        servers20.setText(workstation2.getServers().get(0).getSkillsString());
        servers30.setText(workstation3.getServers().get(0).getSkillsString());
        servers40.setText(workstation4.getServers().get(0).getSkillsString());

        workstationCount0.setText(StringUtils.leftPad(String.valueOf(workstation0.getWorkItemCount()), 3, '0'));
        workstationCount1.setText(StringUtils.leftPad(String.valueOf(workstation1.getWorkItemCount()), 3, '0'));
        workstationCount2.setText(StringUtils.leftPad(String.valueOf(workstation2.getWorkItemCount()), 3, '0'));
        workstationCount3.setText(StringUtils.leftPad(String.valueOf(workstation3.getWorkItemCount()), 3, '0'));
        workstationCount4.setText(StringUtils.leftPad(String.valueOf(workstation4.getWorkItemCount()), 3, '0'));

        backlogCount.setText(StringUtils.leftPad(String.valueOf(ScorecardService.getInstance().getBacklog().getBacklogItemCount()), 3, '0'));
        finishedGoodsCount.setText(StringUtils.leftPad(String.valueOf(ScorecardService.getInstance().getFinishedGoods().getFinishedGoodsTally()), 3, '0'));

        workstationLabel0.setText(workstation0.getColor().name() + ": " + workstation0.getCapacity());
        workstationLabel1.setText(workstation1.getColor().name() + ": " + workstation1.getCapacity());
        workstationLabel2.setText(workstation2.getColor().name() + ": " + workstation2.getCapacity());
        workstationLabel3.setText(workstation3.getColor().name() + ": " + workstation3.getCapacity());
        workstationLabel4.setText(workstation4.getColor().name() + ": " + workstation4.getCapacity());

        dayNumber.setText(String.valueOf(Board.getInstance().getDayOfTheWeek() + 1));
        weekNumber.setText(String.valueOf(Board.getInstance().getGameWeek() + 1));

        totalScore.setText(StringUtils.leftPad(String.valueOf(ScorecardService.getInstance().getTotalScore()), 3, '0'));

        updateScorecardTable();
    }


    private void updateScorecardTable() {
        ObservableList<ScoreCard> scoreCards = FXCollections.observableArrayList(ScorecardService.getInstance().getScorecards());

        TableColumn<ScoreCard, String> weekCol = new TableColumn<>("Wk");
        weekCol.setCellValueFactory(new PropertyValueFactory<>(scoreCards.get(0).weekProperty().getName()));

        TableColumn<ScoreCard, String> estimatedCol = new TableColumn<>("Est");
        estimatedCol.setCellValueFactory(new PropertyValueFactory<>(scoreCards.get(0).estimateProperty().getName()));

        TableColumn<ScoreCard, String> wipCol = new TableColumn<>("WIP");
        wipCol.setCellValueFactory(new PropertyValueFactory<>(scoreCards.get(0).workInProcessProperty().getName()));

        TableColumn<ScoreCard, String> finishedGoodsCol = new TableColumn<>("FIN");
        finishedGoodsCol.setCellValueFactory(new PropertyValueFactory<>(scoreCards.get(0).finishedGoodsProperty().getName()));

        TableColumn<ScoreCard, String> scoreCol = new TableColumn<>("Pts");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>(scoreCards.get(0).scoreProperty().getName()));

        scoreTableView.getColumns().setAll(weekCol, estimatedCol, wipCol, finishedGoodsCol, scoreCol);

        scoreTableView.setItems(scoreCards);


        scoreTableView.refresh();
    }

    @FXML
    protected void runGame(ActionEvent actionEvent) {

        reinitialize();
        buttonRunGame.setDisable(true);
        executeGame();

    }


    @SneakyThrows
    private void runDay() {
        LOGGER.debug("Run Day {}", Board.getInstance().getDayOfTheWeek() + 1);

        Prompts.publishDayStart(Board.getInstance().getDayOfTheWeek(), Board.getInstance().getGameWeek());

        //Get Team mood and start moving work items
        final int startValue = Prompts.teamMood(SIX_SIDES);
        Prompts.promptForWorkItemInitialMoves(gameDialogPane, startValue, ScorecardService.getInstance().getBacklog().getBacklogItemCount());

        for (int i = 0; i < Board.FIVE_STATIONS; i++) {
            runWorkstationDay(Board.getInstance().getGameWeek() == 0, i);
            reinitialize();
        }

        Board.getInstance().returnServerToWorkstation();

        Board.getInstance().augmentDayOfTheWeek();
        if (Board.getInstance().getDayOfTheWeek() == (Board.RUN_DAYS - 1)) {
            ScoreCard scoreCard = ScorecardService.getInstance().getScorecards()[Board.getInstance().getGameWeek()];
            Board.getInstance().getWeekHoldCards().clear();
            //Tally board
            scoreCard.setWorkInProcess(WorkstationService.tallyWorkInProcess());
            scoreCard.setFinishedGoods(ScorecardService.getInstance().getFinishedGoods().getFinishedGoodsTally());
            scoreCard.setScore(ScorecardService.getInstance().getFinishedGoods().calculateScore() - (ScorecardService.getInstance().getBacklog().getBacklogItemCount() + scoreCard.getWorkInProcess()));
            //Remove finished Goods
            ScorecardService.getInstance().getFinishedGoods().setFinishedGoodsTally(0);

            Prompts.publishEndWeek(Board.getInstance().getGameWeek(), scoreCard);
        }
        reinitialize();
    }

    public void runDay(ActionEvent actionEvent) {
        //During run day activities week activities are hidden and run day and server moves disabled
        buttonRunDay.setDisable(true);
        buttonServerMoves.setDisable(true);


        runDay();
        //if it is not the last day show run day and enable server moves
        if (Board.getInstance().getDayOfTheWeek() < (Board.RUN_DAYS - 1)) {
            buttonRunDay.setDisable(false);
            buttonServerMoves.setDisable(false);
        } else {
            //re-enable week buttons and disable day buttons
            buttonRunDay.setVisible(false);
            buttonServerMoves.setVisible(false);

            if (Board.getInstance().getGameWeek() < Board.RUN_WEEKS) {
                Board.getInstance().augmentGameWeek();
                buttonRunWeek.setVisible(true);
                buttonAddSkills.setVisible(true);
                buttonAddSkills.setDisable(false);
            } else {
                buttonEndGame.setVisible(true);
            }

        }
    }

    public void serverMoves(ActionEvent actionEvent) {

        try {
            Prompts.promptForServerMoves(gameDialogPane, Board.getInstance().getInTrainingServer());
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

        if (Board.getInstance().getGameWeek() == Board.RUN_WEEKS) {
            Prompts.publishEndWeek(Board.getInstance().getGameWeek(), ScorecardService.getInstance().getScorecards()[Board.getInstance().getGameWeek()]);
        }
    }

    private void runWeek() throws IOException {
        //Start Week
        Prompts.publishStartWeek(Board.getInstance().getGameWeek());
        //Hide Week Buttons
        buttonRunWeek.setVisible(false);
        buttonAddSkills.setVisible(false);

        //Estimate Work Items
        Prompts.promptForWorkItemEstimates(gameDialogPane);
        reinitialize();

        //Skills add is triggered by button push

        //Show buttons to run day
        buttonRunDay.setVisible(true);
        if (Board.getInstance().getGameWeek() > 0) {
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

    public void addSkillsToServer(ActionEvent actionEvent) {

        try {
            boolean addSkills = false;
            if (Board.getInstance().getGameWeek() != 0) {
                addSkills = Prompts.promptToDrawSkillsCard();
            }
            if (addSkills) {
                Prompts.promptToAddSkill(gameDialogPane);
            }

        } catch (IOException e) {
            throw new ThroughputRuntimeException(e);
        }
    }


    private void runWorkstationDay(boolean vanilla, int position) throws InterruptedException, IOException {
        //For each server
        Workstation workstation = WorkstationService.getWorkstation(position);
        for (Server server : workstation.getServers()) {
            boolean success = Prompts.serverChanceCardPlay(server, workstation);
            if (success) {
                Prompts.promptForWorkItemWorkstationMoves(gameDialogPane, workstation, position);
            } else {
                if (Board.getInstance().getWeekHoldCards().isEmpty()) {
                    TimeUnit.SECONDS.sleep(1);
                } else {
                    retry(position, server, workstation);
                }
            }
            bitActionsDetermined(vanilla);
        }
    }

    private void retry(int position, Server server, Workstation workstation) throws IOException {
        //Prompt for do over
        boolean retry = Prompts.promptForServerRetry(server);
        if (retry) {
            Board.getInstance().getWeekHoldCards().remove(0);
            boolean secondChanceSuccess = Prompts.serverChanceCardPlay(server, workstation);
            if (secondChanceSuccess) {
                Prompts.promptForWorkItemWorkstationMoves(gameDialogPane, workstation, position);
            }
        }
    }

    private void bitActionsDetermined(boolean vanilla) throws IOException {
        if (vanilla) {
            return;
        }
        BitCard bitCard = Prompts.drawBit(gameDialogPane, SIX_SIDES);
        //Discover bit actions handles a null bit card
        BoardAction boardAction = Board.getInstance().discoverBitActions(bitCard, Board.getInstance().getDayOfTheWeek(), Board.getInstance().getGameWeek());
        if (boardAction == null) {
            return;
        }

        if (boardAction instanceof Trap) {
            activateTrap((Trap) boardAction, bitCard);
        } else if (boardAction instanceof HelpAction) {

            switch (((HelpAction) boardAction).getType()) {
                case ADD_ONE:
                    Prompts.promptToAugmentWorkstationCapacity(gameDialogPane,false);
                    return;
                case DOUBLE:
                    Prompts.promptToAugmentWorkstationCapacity(gameDialogPane,true);
                    return;
                case AUTOMATE:
                    Prompts.promptToAutomateWorkstation();
                    return;
                case PAIR:
                    Prompts.implementPairedProgramming(gameDialogPane);
                    return;
                case AUGMENT:
                    Prompts.promptForFinishedGoodsAreNowFourPoints();
            }

        }

    }

    private static void activateTrap(Trap trap, BitCard bitCard) throws IOException {

        boolean trapMitigated = Board.getInstance().isTrapMitigated(bitCard);
        Prompts.promptForAppliedTrap(trap, trapMitigated);
        if (!trapMitigated) {
            if (trap.getEffected().equals(Board.TEAM) && trap.getDuration().equals(Board.WEEK)) {
                Board.getInstance().augmentGameWeek();
            } else if (trap.getEffected().equals(Board.TEAM) && trap.getDuration().equals(Board.DAY)) {
                Board.getInstance().augmentDayOfTheWeek();
            }
        } else {
            if (trap.getEffected().equals(Board.TEAM) && trap.getMitigatedDuration().equals(Board.DAY)) {
                Board.getInstance().augmentDayOfTheWeek();
            }
        }
    }

    public void endGame(ActionEvent ignoredActionEvent) {
        ScoreCard scoreCard = ScorecardService.getInstance().getScorecards()[Board.getInstance().getGameWeek()];
        Prompts.publishEndOfGame(Board.getInstance().getGameWeek(), scoreCard);
    }

}
