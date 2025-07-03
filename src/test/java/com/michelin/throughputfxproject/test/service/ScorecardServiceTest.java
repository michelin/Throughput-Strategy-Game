package com.michelin.throughputfxproject.test.service;

import com.michelin.throughputfxproject.services.ScorecardService;
import com.michelin.throughputfxproject.entities.state.*;

import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import com.michelin.throughputfxproject.entities.state.Board;

class ScorecardServiceTest {
    @BeforeEach
    void resetBoard() {
        // Reset Board singleton state if possible, or mock if needed
        Board.initializeInstance(8,5,6,5);

    }

    @Test
    void getScorecardForCurrentWeek_returnsCorrectScorecard() {
        Board.initializeInstance(8,5,6,5);
        ScoreCard card = ScorecardService.getScorecardForCurrentWeek();
        assertNotNull(card);
        assertEquals(1, card.getPeriod());
    }

    @Test
    void getTotalScore_returnsSumOfScores() {
        Board.initializeInstance(8,5,6,5);
        ScorecardService.getScorecardForCurrentWeek().setScore(10);
        int total = ScorecardService.getTotalScore();
        assertTrue(total >= 10); // Could be higher if currentWeekRunningScore() adds more
    }

    @Test
    void reloadScorecards_updatesScorecardsAndState() {
        Map<String, Object> json = new HashMap<>();
        List<Object> scorecards = new ArrayList<>();
        Map<String, Object> scorecard = new HashMap<>();
        scorecard.put("estimate", 5);
        scorecard.put("period", 1);
        scorecard.put("score", 7);
        scorecard.put("finishedGoods", 3);
        scorecard.put("workInProcess", 2);
        scorecards.add(scorecard);
        json.put("scorecards", scorecards);
        Map<String, Object> backlog = new HashMap<>();
        backlog.put("backlogItemCount", 4);
        json.put("backlog", backlog);
        Map<String, Object> finishedGoods = new HashMap<>();
        finishedGoods.put("finishedGoodsTally", 8);
        finishedGoods.put("currentValue", 9);
        json.put("finishedGoods", finishedGoods);
        ScorecardService.reloadScorecards(json);
        assertEquals(5, ScorecardService.getScorecardForCurrentWeek().getEstimate());
        assertEquals(4, ScorecardService.BACKLOG.getBacklogItemCount());
        assertEquals(8, ScorecardService.FINISHED_GOODS.getFinishedGoodsTally());
        assertEquals(9, ScorecardService.FINISHED_GOODS.getValue());
    }

    @Test
    void toJSON_containsScorecardServiceKey() {
        String json = ScorecardService.toJSON();
        assertTrue(json.contains("scorecardService"));
    }
}

