package com.michelin.throughputfxproject.entities;

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

    @Override
    public String toString() {
        return "Backlog: " + backlogItemCount;
    }
}
