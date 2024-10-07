package com.michelin.throughputfxproject.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FinishedGoods {
    private int finishedGoods;
    private int value = 3;

    public void addToFinishedGoods(int amount) {
        assert amount >= 0;
        finishedGoods += amount;
    }

    public int calculateScore(){
        return finishedGoods * value;
    }



    @Override
    public String toString() {
        return "Finished Goods: " + getFinishedGoods();
    }
}
