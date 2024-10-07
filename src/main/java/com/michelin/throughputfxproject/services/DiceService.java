package com.michelin.throughputfxproject.services;

import com.michelin.throughputfxproject.entities.Die;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;


public class DiceService {
    public static final Logger LOGGER = LoggerFactory.getLogger(DiceService.class.getName());

    private DiceService() {
        super();
    }

    public static int getTotalDiceValue(@NonNull Die[] dice) {
        return Arrays.stream(dice).mapToInt(Die::getValue).sum();
    }

    public static int[] getEachDiceValue(@NonNull Die[] dice) {
        int[] result = new int[dice.length];
        for (int i = 0; i < dice.length; i++) {
            result[i] = dice[i].getValue();
        }
        return result;
    }

    public static Die[] rollDice(@NonNull Die[] dice) {
        for (Die currentDie : dice) {
            currentDie.setValue(rollDie(currentDie).getValue());
        }
        return dice;
    }

    public static Die rollDie(@NonNull Die die) {
        die.setValue(ThreadLocalRandom.current().nextInt(1, die.getSides() + 1));
        return die;
    }


    public static Die getDie(int sides) {
        if (sides == 0) throw new IllegalArgumentException("sides cannot be zero");
        return new Die(sides);
    }

    public static Die[] getDice(int sides, int diceCount) {
        if (sides == 0) throw new IllegalArgumentException("sides cannot be zero");
        Die[] result = new Die[diceCount];
        for (int i = 0; i < diceCount; i++) {
            result[i] = getDie(sides);
        }
        return result;
    }
}
