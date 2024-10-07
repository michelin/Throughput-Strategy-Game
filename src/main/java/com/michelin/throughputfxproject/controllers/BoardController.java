package com.michelin.throughputfxproject.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.net.URL;

public class BoardController {



    @FXML
    private Label backlogCount;
    @FXML
    private Label finishedGoodsCount;
    @FXML
    private VBox backlogBox;
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
    private VBox finishedGoodsBox;
    @FXML
    private Label header;
    @FXML
    private Label header1;
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

        servers0.setText("Servers 0");
        servers1.setText("Servers 1");
        servers2.setText("Servers 2");
        servers3.setText("Servers 3");
        servers4.setText("Servers 4");

        workstationCount0.setText("000");
        workstationCount1.setText("000");
        workstationCount2.setText("000");
        workstationCount3.setText("000");
        workstationCount4.setText("000");

        backlogCount.setText("000");
        finishedGoodsCount.setText("000");

        workstationLabel0.setText("Workstation");
        workstationLabel1.setText("Workstation");
        workstationLabel2.setText("Workstation");
        workstationLabel3.setText("Workstation");
        workstationLabel4.setText("Workstation");
    }
}
