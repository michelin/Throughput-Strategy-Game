package com.michelin.throughputfxproject.entities;


import javafx.beans.property.SimpleIntegerProperty;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ScoreCard {

    private final int week;
    private  int score;
    private  int estimate;
    private  int workInProcess;
    private  int finishedGoods;

    public ScoreCard(int week, int score, int finishedGoods, int estimate, int wip) {
        this.score = score;
        this.finishedGoods = finishedGoods;
        this.estimate = estimate;
        this.week = week;
        this.workInProcess = wip;

    }


    public SimpleIntegerProperty workInProcessProperty() {
        return new SimpleIntegerProperty(workInProcess);
    }

    public SimpleIntegerProperty weekProperty() {
        return new SimpleIntegerProperty(week);
    }

    public SimpleIntegerProperty scoreProperty() {
        return new SimpleIntegerProperty(score);
    }

    public SimpleIntegerProperty finishedGoodsProperty() {
        return new SimpleIntegerProperty(finishedGoods);
    }

    public SimpleIntegerProperty estimateProperty() {
        return new SimpleIntegerProperty(estimate);
    }




}
