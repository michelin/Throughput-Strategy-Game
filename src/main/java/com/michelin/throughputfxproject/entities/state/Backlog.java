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
 *
 * The formula used is: backlogItemCount * (1 / (stationCount + 1)).
 *
 * @param stationCount The number of stations. Must be non-negative.
 * @return The calculated backlog score as a float.
 */
public float backlogScore(int stationCount) {
    return (backlogItemCount * ((float) 1 / (stationCount + 1)));
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
