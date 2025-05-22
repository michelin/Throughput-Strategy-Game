package com.michelin.throughputfxproject.services;

import com.michelin.throughputfxproject.entities.state.Backlog;
import com.michelin.throughputfxproject.entities.state.Board;
import com.michelin.throughputfxproject.entities.state.FinishedGoods;
import com.michelin.throughputfxproject.entities.state.ScoreCard;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A service class that provides utility methods for managing scorecards, backlog, and finished goods.
 * This class includes methods for retrieving scorecards, calculating scores, reloading data from JSON,
 * and converting the scorecard service data to a JSON string representation.
 * <p>
 * The class is marked as `@NoArgsConstructor` with `PRIVATE` access to prevent instantiation.
 * It also uses Lombok annotations for `@Getter`, `@EqualsAndHashCode`, `@ToString`, and logging with `@Slf4j`.
 */
@Getter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class ScorecardService {

    public static final Backlog BACKLOG = new Backlog();
    public static final FinishedGoods FINISHED_GOODS = FinishedGoods.builder().value(FinishedGoods.STARTING_FINISHED_GOODS_VALUE).build();
    @Getter
    protected static final ScoreCard[] SCORECARDS = new ScoreCard[Board.getInstance().getRunPeriods()];


  static {
//       Initializes the scorecards array with a new ScoreCard for each run period.
//       Each ScoreCard is initialized with default values: period number, score, finished goods,
//       estimate, and work in process all set to 0.
      for (int scorecardIndex = 0; scorecardIndex < Board.getInstance().getRunPeriods(); scorecardIndex++) {
          SCORECARDS[scorecardIndex] =  ScoreCard.builder().period(scorecardIndex + 1).build();
      }
  }

  /**
   * Retrieves the ScoreCard for the current week based on the current period of the Board.
   *
   * @return The ScoreCard corresponding to the current period.
   */
  public static ScoreCard getScorecardForCurrentWeek() {
      return SCORECARDS[(Board.getInstance().getCurrentPeriod() - 1)];
  }

  /**
   * Calculates the total score up to the current period.
   * If the game is over, it returns the sum of all scores.
   * Otherwise, it includes the running score for the current week.
   *
   * @return The total score as an integer.
   */
  public static int getTotalScore() {
      int totalScores = Arrays.stream(SCORECARDS)
              .filter(scoreCard -> scoreCard.getPeriod() < (Board.getInstance().getCurrentPeriod() + 1))
              .mapToInt(ScoreCard::getScore)
              .sum();
      if (Board.getInstance().gameIsOver()) return totalScores;
      return totalScores + currentWeekRunningScore();
  }

  /**
   * Calculates the running score for the current week.
   * The score is derived from the negative tally of work in process,
   * the negative backlog score, and the finished goods score.
   *
   * @return The current week's running score as an integer.
   */
  public static int currentWeekRunningScore() {
      return (Math.round(WorkstationService.tallyWorkInProcessScore() * -1)) +
             Math.round((BACKLOG.backlogScore(Board.getInstance().getStationCount()) * -1)) +
             FINISHED_GOODS.calculateScore();
  }

/**
 * Reloads the scorecards and updates the backlog and finished goods based on the provided JSON data.
 * The method parses the JSON map to extract scorecard details, backlog item count, and finished goods data.
 * It updates the `scorecards` array, `backlog`, and `finishedGoods` with the parsed values.
 *
 * @param scorecardServiceJson A map containing the JSON representation of the scorecard service data.
 *                             Expected keys:
 *                             - "scorecards": A list of scorecard objects, each containing:
 *                               - "estimate": The estimate value for the scorecard.
 *                               - "period": The period number for the scorecard.
 *                               - "score": The score value for the scorecard.
 *                               - "finishedGoods": The finished goods count for the scorecard.
 *                               - "workInProcess": The work in process count for the scorecard.
 *                             - "backlog": An object containing:
 *                               - "backlogItemCount": The count of backlog items.
 *                             - "finishedGoods": An object containing:
 *                               - "finishedGoodsTally": The tally of finished goods.
 *                               - "currentValue": The current value of finished goods.
 */
@SuppressWarnings("unchecked")
public static void reloadScorecards(Map<String, Object> scorecardServiceJson) {
   List<Object> scorecardsJson =  (List<Object>) scorecardServiceJson.get("scorecards");
    scorecardsJson.forEach(scorecard-> {
        Integer estimate = (Integer) ((Map<String, Object>)scorecard).get("estimate");
        Integer period = (Integer)((Map<String, Object>)scorecard).get("period");
        Integer score = (Integer)((Map<String, Object>)scorecard).get("score");
        Integer finishedGoods = (Integer)((Map<String, Object>)scorecard).get("finishedGoods");
        Integer workInProcess = (Integer)((Map<String, Object>)scorecard).get("workInProcess");
        SCORECARDS[period-1] =  ScoreCard.builder().period(period).score(score).finishedGoods(finishedGoods).estimate(estimate).workInProcess(workInProcess).build();
    });

    BACKLOG.setBacklogItemCount((Integer) ((Map<String, Object>)scorecardServiceJson.get("backlog")).get("backlogItemCount"));
    var finishedGoodsMap = (Map<String, Object>) scorecardServiceJson.get("finishedGoods");
    FINISHED_GOODS.setFinishedGoodsTally((Integer)finishedGoodsMap.get("finishedGoodsTally"));
    FINISHED_GOODS.setValue((Integer)finishedGoodsMap.get("currentValue"));
}

/**
 * Converts the scorecard service data into a JSON string representation.
 * The JSON includes details about the backlog, finished goods, and all scorecards.
 *
 * @return A JSON string representing the scorecard service data.
 */
public static String toJSON() {
    List<String> stringList = Arrays.stream(SCORECARDS).map(ScoreCard::toJSON).toList();
    return "\"scorecardService\": {" + BACKLOG.toJSON() +
            "," + FINISHED_GOODS.toJSON() + ", \"scorecards\": [" + String.join(",", stringList) + "]}";
}
}
