package com.michelin.throughputfxproject.entities;

import lombok.Getter;

import java.util.Arrays;

@Getter
public class ScoreCard {
    private final int[] scores;
    private final int[] estimates;
    private final int[] finishedGoods;
    private final int[] backlog;
    private final int[] workInProcess;


    public ScoreCard(int weeks) {
        scores = new int[weeks];
        estimates= new int[weeks];
        finishedGoods= new int[weeks];
        workInProcess= new int[weeks];
        backlog= new int[weeks];
    }

    @Override
    public String toString() {
        return  "BCG=" + Arrays.toString(getBacklog()) + System.lineSeparator() +
                "SCO=" + Arrays.toString(getScores()) + System.lineSeparator() +
                "EST=" + Arrays.toString(getEstimates()) + System.lineSeparator() +
                "FIN=" + Arrays.toString(getFinishedGoods()) + System.lineSeparator() +
                "WIP=" + Arrays.toString(getWorkInProcess());
    }
}
