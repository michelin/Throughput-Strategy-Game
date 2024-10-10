package com.michelin.throughputfxproject.services;

import com.michelin.throughputfxproject.Board;
import com.michelin.throughputfxproject.entities.Backlog;
import com.michelin.throughputfxproject.entities.FinishedGoods;
import com.michelin.throughputfxproject.entities.ScoreCard;
import lombok.Getter;

import java.util.Arrays;


public class ScorecardService {

    @Getter
    private  final Backlog backlog = new Backlog();
    @Getter
    private  final FinishedGoods finishedGoods = new FinishedGoods();
    @Getter
    private  final ScoreCard[] scorecards = new ScoreCard[Board.RUN_WEEKS];
    private static ScorecardService instance = null;


    private ScorecardService() {
        for (int i = 0; i < Board.RUN_WEEKS; i++) {
            scorecards[i] = new ScoreCard(i + 1, 0, 0, 0, 0);
        }
    }

    public static ScorecardService getInstance() {
        if(instance == null){
            instance = new ScorecardService();
        }
        return instance;
    }


    public int getTotalScore() {
        return Arrays.stream(scorecards).mapToInt(ScoreCard::getScore).sum();
    }
}
