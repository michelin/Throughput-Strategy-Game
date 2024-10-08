package com.michelin.throughputfxproject.entities;

import javafx.beans.property.SimpleIntegerProperty;


public class ScoreCard {
    private final SimpleIntegerProperty week = new SimpleIntegerProperty();
    private final SimpleIntegerProperty score= new SimpleIntegerProperty();
    private final SimpleIntegerProperty estimate= new SimpleIntegerProperty();
    private final SimpleIntegerProperty workInProcess= new SimpleIntegerProperty();
    private final SimpleIntegerProperty finishedGoods= new SimpleIntegerProperty();

    public ScoreCard(int week, int score, int finishedGoods, int estimate, int wip) {
        setScore(score);
        setFinishedGoods(finishedGoods);
        setEstimate(estimate);
        setWeek(week);
        setWorkInProcess(wip);

    }

    public int getWorkInProcess() {
        return workInProcess.get();
    }

    public SimpleIntegerProperty workInProcessProperty() {
        return workInProcess;
    }

    public int getWeek() {
        return week.get();
    }

    public SimpleIntegerProperty weekProperty() {
        return week;
    }

    public int getScore() {
        return score.get();
    }

    public SimpleIntegerProperty scoreProperty() {
        return score;
    }

    public int getFinishedGoods() {
        return finishedGoods.get();
    }

    public SimpleIntegerProperty finishedGoodsProperty() {
        return finishedGoods;
    }

    public int getEstimate() {
        return estimate.get();
    }

    public SimpleIntegerProperty estimateProperty() {
        return estimate;
    }


    public void setWorkInProcess(int workInProcess) {
        this.workInProcess.set(workInProcess);
    }

    public void setWeek(int week) {
        this.week.set(week);
    }

    public void setScore(int score) {
        this.score.set(score);
    }

    public void setFinishedGoods(int finishedGoods) {
        this.finishedGoods.set(finishedGoods);
    }

    public void setEstimate(int estimate) {
        this.estimate.set(estimate);
    }


}
