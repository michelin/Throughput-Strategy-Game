package com.michelin.throughputfxproject.services;

import com.michelin.throughputfxproject.Board;
import com.michelin.throughputfxproject.entities.Backlog;
import com.michelin.throughputfxproject.entities.FinishedGoods;
import com.michelin.throughputfxproject.entities.ScoreCard;
import lombok.Getter;
import java.util.Arrays;
import static com.michelin.throughputfxproject.Board.*;


public class ScorecardService {

    @Getter
    private static final Backlog backlog = new Backlog();
    @Getter
    private static final FinishedGoods finishedGoods = new FinishedGoods();
    @Getter
    private static final ScoreCard[] scorecards = new ScoreCard[Board.RUN_WEEKS];

    static {
        for (int scorecardIndex = 0; scorecardIndex < Board.RUN_WEEKS; scorecardIndex++) {
            scorecards[scorecardIndex] = new ScoreCard(scorecardIndex + 1, 0, 0, 0, 0);
        }
    }

    private ScorecardService() {

    }

    public static int getTotalScore() {
        return Arrays.stream(scorecards).filter(scoreCard -> scoreCard.getWeek() < (Board.getGameWeek() + 1)).mapToInt(ScoreCard::getScore).sum() + currentWeekRunningScore();
    }

    private static int currentWeekRunningScore() {
        return (Math.round(WorkstationService.tallyWorkInProcessScore() * -1)) + Math.round((backlog.backlogScore() * -1)) + finishedGoods.calculateScore();
    }

    public static ScoreCard getScorecardForCurrentWeek(){
        return scorecards[(getGameWeek()-1)];
    }
}
