package com.michelin.throughputfxproject.entities;

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

}
