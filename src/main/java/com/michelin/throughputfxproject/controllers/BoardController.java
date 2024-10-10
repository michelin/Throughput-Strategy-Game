package com.michelin.throughputfxproject.controllers;

import com.michelin.throughputfxproject.Board;
import com.michelin.throughputfxproject.Color;
import com.michelin.throughputfxproject.Prompts;
import com.michelin.throughputfxproject.entities.ScoreCard;
import com.michelin.throughputfxproject.entities.Server;
import com.michelin.throughputfxproject.entities.Trap;
import com.michelin.throughputfxproject.entities.Workstation;
import com.michelin.throughputfxproject.entities.cards.BitCard;
import com.michelin.throughputfxproject.entities.servers.HumanServer;
import com.michelin.throughputfxproject.entities.servers.ServerMove;
import com.michelin.throughputfxproject.exceptions.ThroughputRuntimeException;
import com.michelin.throughputfxproject.services.ScorecardService;
import com.michelin.throughputfxproject.services.WorkstationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;


public class BoardController {

    public static final Logger LOGGER = LoggerFactory.getLogger(BoardController.class.getName());


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

        totalScore.setText("0");

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
        finishedGoodsCount.setText(StringUtils.leftPad(String.valueOf(ScorecardService.getInstance().getFinishedGoods().getFinishedGoods()), 3, '0'));

        workstationLabel0.setText(workstation0.getColor().name());
        workstationLabel1.setText(workstation1.getColor().name());
        workstationLabel2.setText(workstation2.getColor().name());
        workstationLabel3.setText(workstation3.getColor().name());
        workstationLabel4.setText(workstation4.getColor().name());

        dayNumber.setText(String.valueOf(Board.getInstance().getDayOfTheWeek() + 1));
        weekNumber.setText(String.valueOf(Board.getInstance().getGameWeek() + 1));

        totalScore.setText(StringUtils.leftPad(String.valueOf(ScorecardService.getInstance().getTotalScore()), 3, '0'));

        updateScorecardTable();
    }

    private void updateScorecardTable() {
        ObservableList<ScoreCard> scoreCards = FXCollections.observableArrayList(ScorecardService.getInstance().getScorecards());
        scoreTableView.setItems(scoreCards);

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

        scoreTableView.refresh();
    }

    @FXML
    protected void runGame(ActionEvent actionEvent) {

        reinitialize();
        runGame(gameDialogPane);
    }


    @SneakyThrows
    private void runDay(Pane container, boolean vanilla, HumanServer inTraining) {
        LOGGER.debug("Run Day {}", Board.getInstance().getDayOfTheWeek() + 1);

        Prompts.publishDayStart(Board.getInstance().getDayOfTheWeek(), Board.getInstance().getGameWeek());

        //If not Week 1 ask about moving servers
        if (!vanilla) {
            //Server Moves
            List<ServerMove> moves = Prompts.promptForServerMoves(inTraining);
            Board.getInstance().startDay(moves);
        }

        //Get Team mood and start moving work items
        final int startValue = Prompts.teamMood(container, Board.SIX_SIDES);

        Prompts.promptForWorkItemInitialMoves(container, startValue, ScorecardService.getInstance().getBacklog().getBacklogItemCount());


        for (int i = 0; i < Board.FIVE_STATIONS; i++) {
            runWorkstationDay(vanilla, i);
        }
        if (inTraining != null) {
            Board.getInstance().returnServerToWorkstation(inTraining);
        }
    }

    public void runGame(@NonNull Pane container) {

        try {
            runWeek(container);
            Board.getInstance().augmentGameWeek();

        } catch (IOException e) {
            throw new ThroughputRuntimeException(e);
        }

        if (Board.getInstance().getGameWeek() == Board.RUN_WEEKS) {
            Prompts.publishEndWeek(Board.getInstance().getGameWeek(), ScorecardService.getInstance().getScorecards()[Board.getInstance().getGameWeek()]);
        }
    }

    private void runWeek(Pane container) throws IOException {

        ScoreCard scoreCard = ScorecardService.getInstance().getScorecards()[Board.getInstance().getGameWeek()];
        Prompts.publishStartWeek(Board.getInstance().getGameWeek());

        //Estimate Work Items
        Prompts.promptForWorkItemEstimates(container);

        HumanServer inTraining = null;
        if (Board.getInstance().getGameWeek() > 0) {
            //Get skills
            inTraining = Prompts.promptToAddSkill();
            if (inTraining != null) {
                Board.getInstance().findAndRemoveServer(inTraining.getColor());
            }
        }

  //      runDay(container, Board.getInstance().getGameWeek() == 0, inTraining);
        Board.getInstance().augmentDayOfTheWeek();
        if (Board.getInstance().getDayOfTheWeek() == 4) {
            Board.getInstance().getWeekHoldCards().clear();
            //Tally board
            scoreCard.setWorkInProcess(WorkstationService.tallyWorkInProcess());
            scoreCard.setFinishedGoods(ScorecardService.getInstance().getFinishedGoods().getFinishedGoods());
            scoreCard.setScore(ScorecardService.getInstance().getFinishedGoods().calculateScore() - (ScorecardService.getInstance().getBacklog().getBacklogItemCount() + scoreCard.getWorkInProcess()));
            //Remove finished Goods
            ScorecardService.getInstance().getFinishedGoods().setFinishedGoods(0);
            Prompts.publishEndWeek(Board.getInstance().getGameWeek(), scoreCard);
        }
        reinitialize();
    }



    private void runWorkstationDay(boolean vanilla, int i) throws InterruptedException, IOException {
        //For each server
        Workstation workstation = WorkstationService.getWorkstation(i);
        for (Server server : workstation.getServers()) {

            Board.getInstance().handleChanceCardAndServerMovements(server, workstation, i);

            if (!vanilla) {
                BitCard bitCard = Prompts.drawBit(Board.SIX_SIDES);
                //Discover bit actions handles a null bit card
                Trap trap = Board.getInstance().discoverBitActions(bitCard, Board.getInstance().getDayOfTheWeek(), Board.getInstance().getGameWeek());
                if (bitCard != null && trap != null) {
                    boolean trapMitigated = Board.getInstance().isTrapMitigated(bitCard);
                    if (!trapMitigated) {
                        Prompts.promptForAppliedTrap(trap);
                        if (trap.getEffected().equals(Board.TEAM) && trap.getDuration().equals(Board.WEEK)) {
                            Board.getInstance().augmentGameWeek();
                        } else if (trap.getEffected().equals(Board.TEAM) && trap.getDuration().equals(Board.DAY)) {
                            Board.getInstance().augmentDayOfTheWeek();
                        }
                    } else {
                        Prompts.promptForMitigatedTrap(trap);
                        if (trap.getEffected().equals(Board.TEAM) && trap.getMitigatedDuration().equals(Board.DAY)) {
                            Board.getInstance().augmentDayOfTheWeek();
                        }
                    }
                }
            }
        }
    }


}
