package com.michelin.throughputfxproject.entities.state;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FinishedGoods {
    private int finishedGoodsTally;
    private int value = 3;

    public void addToFinishedGoods(int amount) {
        if (amount < 0) {
            amount = 0;
        }
        finishedGoodsTally += amount;
    }

    public int calculateScore() {
        return finishedGoodsTally * value;
    }


    @Override
    public String toString() {
        return "Finished Goods: " + finishedGoodsTally;
    }
}
