package com.michelin.throughputfxproject.controllers;

import com.michelin.throughputfxproject.Board;
import com.michelin.throughputfxproject.Color;
import com.michelin.throughputfxproject.entities.ScoreCard;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;


public class BoardController {

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
    private  Label servers0;
    @FXML
    private  Label servers1;
    @FXML
    private  Label servers2;
    @FXML
    private  Label servers3;
    @FXML
    private  Label servers4;
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
    protected void initialize(){

        servers0.setText(Color.BLUE.name());
        servers1.setText(Color.GREEN.name());
        servers2.setText(Color.ROSE.name());
        servers3.setText(Color.YELLOW.name());
        servers4.setText(Color.VIOLET.name());

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

        dayNumber.setText(String.valueOf(Board.getInstance().getDayOfTheWeek()+1));
        weekNumber.setText(String.valueOf(Board.getInstance().getGameWeek()+1));

        totalScore.setText("0");

        ObservableList<ScoreCard> scoreCards = FXCollections. observableArrayList(Board.getInstance().getScoreCards());
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
    protected void updateWeekAndDay() {



    }
}
