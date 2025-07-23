/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.michelin.throughputfxproject.entities.state;

import lombok.*;

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
        if (amount < 0) throw new AssertionError();
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
        if (amount < 0) throw new AssertionError();
        if (amount > backlogItemCount) throw new AssertionError();
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
