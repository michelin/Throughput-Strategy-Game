package com.michelin.throughputfxproject.entities.cards;


import java.io.File;

public interface Card {
    String BOOSTER_INOCULATE_TRAP = "BoosterInoculationTrap";
    String SKILLS = "SKILLS";
    String CHANCE = "CHANCE";
    String AUTOMATED_CHANCE = "ROBOT_AUTOMATED_CHANCE";

    /**
     * Retrieves the back image of the card.
     *
     * @return A `File` object representing the back image of the card.
     */
    File getBackImage();

    /**
     * Retrieves the number of copies of the card.
     *
     * @return An `int` representing the number of copies.
     */
    int getCopies();

    /**
     * Retrieves the instructions associated with the card.
     *
     * @return A `String` containing the card's instructions.
     */
    String getInstructions();

    /**
     * Retrieves the title of the card.
     *
     * @return A `String` representing the card's title.
     */
    String getTitle();

    /**
     * Retrieves the type of the card.
     *
     * @return A `String` representing the type of the card.
     */
    String getType();

    /**
     * Creates a clone of the card with the same type.
     *
     * @return A new `Card` object that is a typed clone of the current card.
     */
    Card typedClone();

}
