package com.michelin.throughputfxproject.services;

import com.michelin.throughputfxproject.entities.state.Backlog;
import com.michelin.throughputfxproject.entities.state.Board;
import com.michelin.throughputfxproject.entities.state.FinishedGoods;
import com.michelin.throughputfxproject.entities.state.ScoreCard;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class ScorecardService {

    public static final Logger LOGGER = LoggerFactory.getLogger(ScorecardService.class.getName());
    
    @Getter
    private static final Backlog backlog = new Backlog();
    @Getter
    private static final FinishedGoods finishedGoods = new FinishedGoods();
    @Getter
    private static final ScoreCard[] scorecards = new ScoreCard[Board.getInstance().getRunPeriods()];

    static {
        for (int scorecardIndex = 0; scorecardIndex < Board.getInstance().getRunPeriods(); scorecardIndex++) {
            scorecards[scorecardIndex] = new ScoreCard(scorecardIndex + 1, 0, 0, 0, 0);
        }
    }

    private ScorecardService() {
    //empty constructor
    }

    public static ScoreCard getScorecardForCurrentWeek() {
        return scorecards[(Board.getInstance().getCurrentPeriod() - 1)];
    }

    public static int getTotalScore() {
        int totalScores = Arrays.stream(scorecards).filter(scoreCard -> scoreCard.getPeriod() < (Board.getInstance().getCurrentPeriod() + 1)).mapToInt(ScoreCard::getScore).sum();
        if (Board.getInstance().gameIsOver()) return totalScores;
        return totalScores + currentWeekRunningScore();
    }

    public static int currentWeekRunningScore() {
        return (Math.round(WorkstationService.tallyWorkInProcessScore() * -1)) + Math.round((backlog.backlogScore(Board.getInstance().getStationCount()) * -1)) + finishedGoods.calculateScore();
    }

    @SuppressWarnings("unchecked")
    public static void reloadScorecards(Map<String, Object> scorecardServiceJson) {
       List<Object> scorecardsJson =  (List<Object>) scorecardServiceJson.get("scorecards");
        scorecardsJson.forEach(scorecard-> {
            Integer estimate = (Integer) ((Map<String, Object>)scorecard).get("estimate");
            Integer period = (Integer)((Map<String, Object>)scorecard).get("period");
            Integer score = (Integer)((Map<String, Object>)scorecard).get("score");
            Integer finishedGoods = (Integer)((Map<String, Object>)scorecard).get("finishedGoods");
            Integer workInProcess = (Integer)((Map<String, Object>)scorecard).get("workInProcess");
            scorecards[period-1] = new ScoreCard( period,  score,  finishedGoods,  estimate,  workInProcess);
        });

        backlog.setBacklogItemCount((Integer) ((Map<String, Object>)scorecardServiceJson.get("backlog")).get("backlogItemCount"));
        var finishedGoodsMap = (Map<String, Object>) scorecardServiceJson.get("finishedGoods");
        finishedGoods.setFinishedGoodsTally((Integer)finishedGoodsMap.get("finishedGoodsTally"));
        finishedGoods.setValue((Integer)finishedGoodsMap.get("currentValue"));
    }

    public static String toJSON() {

        List<String> stringList = Arrays.stream(getScorecards()).map(ScoreCard::toJSON).toList();
        return "\"scorecardService\": {" + backlog.toJSON() +
                "," + finishedGoods.toJSON() + ", \"scorecards\": [" + String.join(",", stringList) + "]}";

    }
}
