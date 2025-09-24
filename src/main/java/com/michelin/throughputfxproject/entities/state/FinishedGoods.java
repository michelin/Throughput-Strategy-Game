/*
 * Copyright 2025 Manufacture Française des Pneumatiques Michelin
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
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
     * Adds a specified amount to the tally of finished goods.
     * If the provided amount is negative, it is treated as zero.
     *
     * @param amount The amount to add to the finished goods tally.
     *               Negative values are treated as zero.
     */
    public void addToFinishedGoods(int amount) {
        if (amount < 0) {
            amount = 0;
        }
        finishedGoodsTally += amount;
    }


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
