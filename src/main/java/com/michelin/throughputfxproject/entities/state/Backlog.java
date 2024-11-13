package com.michelin.throughputfxproject.entities.state;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Backlog implements Savable {
    private int backlogItemCount;

    public void addToBacklog(int amount) {
        if (amount < 0) throw new AssertionError();
        backlogItemCount += amount;
    }

    public void subtractFromBacklog(int amount) {
        if (amount < 0) throw new AssertionError();
        if (amount > backlogItemCount) throw new AssertionError();
        backlogItemCount -= amount;
    }

    public float backlogScore(int stationCount){
        return (backlogItemCount * ((float)1/(stationCount+1)));
    }

    @Override
    public String toString() {
        return "Backlog: " + backlogItemCount;
    }

    public String toJSON(){
        return "\"backlog\":{" +
                "\"backlogItemCount\":" + backlogItemCount +
                "}";
    }
}
