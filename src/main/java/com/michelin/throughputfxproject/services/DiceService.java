package com.michelin.throughputfxproject.services;

import com.michelin.throughputfxproject.entities.state.Die;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A utility class that provides services for creating, rolling, and managing dice.
 * This class includes methods for rolling single or multiple dice, creating dice with a specified number of sides,
 * and retrieving image file paths for dice faces.
 *
 * The class is marked as `@NoArgsConstructor` with `PRIVATE` access to prevent instantiation.
 * It also uses Lombok annotations for `@EqualsAndHashCode`, `@ToString`, and logging with `@Slf4j`.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
@Slf4j
public class DiceService {


/**
 * Rolls all the dice in the provided array.
 * Each die's value is updated by rolling it individually.
 *
 * @param dice An array of `Die` objects to be rolled. Must not be null.
 */
public static void rollDice(@NonNull Die[] dice) {
    for (Die currentDie : dice) {
        currentDie.setValue(rollDie(currentDie).getValue());
    }
}

/**
 * Rolls a single die and updates its value.
 * The value is set to a random number between 1 and the number of sides on the die.
 *
 * @param die The `Die` object to be rolled. Must not be null.
 * @return The updated `Die` object with the new value.
 */
public static Die rollDie(@NonNull Die die) {
    die.setValue(ThreadLocalRandom.current().nextInt(1, die.getSides() + 1));
    return die;
}

/**
 * Creates a new `Die` object with the specified number of sides.
 *
 * @param sides The number of sides for the die. Must be greater than zero.
 * @return A new `Die` object with the specified number of sides.
 * @throws IllegalArgumentException If the number of sides is zero.
 */
public static Die getDie(int sides) {
    if (sides == 0) throw new IllegalArgumentException("sides cannot be zero");
    return new Die(sides);
}

/**
 * Creates an array of `Die` objects, each with the specified number of sides.
 *
 * @param sides The number of sides for each die. Must be greater than zero.
 * @param diceCount The number of dice to create.
 * @return An array of `Die` objects with the specified number of sides.
 * @throws IllegalArgumentException If the number of sides is zero.
 */
public static Die[] getDice(int sides, int diceCount) {
    if (sides == 0) throw new IllegalArgumentException("sides cannot be zero");
    Die[] result = new Die[diceCount];
    for (int i = 0; i < diceCount; i++) {
        result[i] = getDie(sides);
    }
    return result;
}

  /**
   * Retrieves the file path of the image corresponding to the given die face.
   * The method maps die face values (1-6) to specific image file paths.
   * If the die face value is not between 2 and 6, the default image for die face 1 is returned.
   *
   * @param dieFace The face value of the die (integer).
   * @return The file path of the image corresponding to the die face.
   */
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
