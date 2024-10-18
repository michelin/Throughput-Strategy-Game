package com.michelin.throughputfxproject.entities;

import com.michelin.throughputfxproject.Board;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Backlog {
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

    public float backlogScore(){
        return (backlogItemCount * ((float)1/(Board.FIVE_STATIONS+1)));
    }

    @Override
    public String toString() {
        return "Backlog: " + backlogItemCount;
    }
}
