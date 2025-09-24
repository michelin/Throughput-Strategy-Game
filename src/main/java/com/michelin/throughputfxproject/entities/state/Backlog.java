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

import com.michelin.throughputfxproject.exceptions.ThroughputRuntimeException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Backlog implements Savable {
    private int backlogItemCount;

    /**
     * Adds the specified amount to the backlog item count.
     * Throws an `AssertionError` if the amount is negative.
     *
     * @param amount The number of items to add to the backlog. Must be non-negative.
     */
    public void addToBacklog(int amount) {
        if (amount < 0) throw new ThroughputRuntimeException(new AssertionError("Amount to add cannot be negative"));
        backlogItemCount += amount;
    }

    /**
     * Calculates the backlog score based on the current backlog item count
     * and the number of stations.
     * <p>
     * The formula used is: backlogItemCount * (1 / (stationCount + 1)).
     *
     * @param stationCount The number of stations. Must be non-negative.
     * @return The calculated backlog score as a float.
     */
    public float backlogScore(int stationCount) {
        return (backlogItemCount * ((float) 1 / (stationCount + 1)));
    }

    /**
     * Subtracts the specified amount from the backlog item count.
     * Throws an `AssertionError` if the amount is negative or greater than the current backlog item count.
     *
     * @param amount The number of items to subtract from the backlog.
     *               Must be non-negative and less than or equal to the current backlog item count.
     */
    public void subtractFromBacklog(int amount) {
        log.info("Subtracting {} from backlog item count {}", amount, backlogItemCount);
        if (amount < 0) throw new ThroughputRuntimeException(new AssertionError("Amount to subtract cannot be negative"));
        if (amount > backlogItemCount)throw new ThroughputRuntimeException(new AssertionError("Amount to subtract cannot be greater than backlog item count"));
        backlogItemCount -= amount;
    }

    /**
     * Converts the current backlog state to its JSON representation.
     * The JSON format includes the backlog item count.
     *
     * @return A `String` representing the JSON format of the backlog.
     */
    public String toJSON() {
        return "\"backlog\":{" +
                "\"backlogItemCount\":" + backlogItemCount +
                "}";
    }
}
