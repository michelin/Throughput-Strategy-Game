package com.michelin.throughputfxproject.services;

import com.michelin.throughputfxproject.entities.state.Die;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.ThreadLocalRandom;


public class DiceService {
    public static final Logger LOGGER = LoggerFactory.getLogger(DiceService.class.getName());

    private DiceService() {
        super();
    }


    public static void rollDice(@NonNull Die[] dice) {
        for (Die currentDie : dice) {
            currentDie.setValue(rollDie(currentDie).getValue());
        }
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

    public static String getDieImage(int dieFace) {
        return switch (dieFace) {
            case 2 -> "icons/die_2.png";
            case 3 -> "icons/die_3.png";
            case 4 -> "icons/die_4.png";
            case 5 -> "icons/die_5.png";
            case 6 -> "icons/die_6.png";
            default -> "icons/die_1.png";
        };
    }
}
