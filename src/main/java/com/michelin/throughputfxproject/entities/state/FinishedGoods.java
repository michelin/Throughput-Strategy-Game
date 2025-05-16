package com.michelin.throughputfxproject.entities.state;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@EqualsAndHashCode
public class FinishedGoods implements Savable {

    public static final int STARTING_FINISHED_GOODS_VALUE = 3;
    /**
     * The tally of finished goods. This value represents the total count
     * of finished goods currently tracked.
     */
    private int finishedGoodsTally;

    /**
     * The multiplier value used in score calculations.
     * Defaults to 3 if not explicitly set.
     */
    private int value;


    /**
     * Calculates the score based on the tally of finished goods and the multiplier value.
     * The formula used is: `finishedGoodsTally * value`.
     *
     * @return The calculated score as an integer.
     */
    public int calculateScore() {
        return finishedGoodsTally * value;
    }

  /**
   * Converts the current `FinishedGoods` state to its JSON representation.
   * The JSON format includes the tally of finished goods and the current multiplier value.
   *
   * @return A `String` representing the JSON format of the `FinishedGoods` object.
   */
  public String toJSON(){
      return "\"finishedGoods\":{" +
              "\"finishedGoodsTally\":" + finishedGoodsTally +
              ",\"currentValue\":" + value +
              "}";
  }
}
