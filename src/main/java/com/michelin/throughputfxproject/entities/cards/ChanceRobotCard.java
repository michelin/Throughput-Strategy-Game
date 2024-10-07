package com.michelin.throughputfxproject.entities.cards;

import com.michelin.throughputfxproject.entities.Card;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.File;

@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class ChanceRobotCard extends ChanceCard {


    @Override
    public File getBackImage() {
        return new File("./cards/ChanceTheRobot.jpg");
    }

    @Override
    public String getType() {
        return Card.AUTOMATED_CHANCE;
    }

    @Override
    public String getTitle() {
        return Card.AUTOMATED_CHANCE;
    }


    @Override
    public ChanceRobotCard typedClone() {
        return this.toBuilder().build();
    }

    @Override
    public String toString() {
        return "{" + "type='" + getType() + '\'' +
                ", chance='" + getChanceText() + '\'' +
                ", instructions='" + getInstructions() + '\'' +
                '}';
    }
}
