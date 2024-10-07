package com.michelin.throughputfxproject.entities;


import java.io.File;

public interface Card {
    String BOOSTER_INOCULATE_TRAP = "BoosterInoculationTrap";
    String SKILLS = "SKILLS";
    String CHANCE = "CHANCE";
    String AUTOMATED_CHANCE = "ROBOT_AUTOMATED_CHANCE";

    File getBackImage();

    String getType();

    String getInstructions();

    String getTitle();

    int getCopies();

    Card typedClone();

}
