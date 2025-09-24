/*
 * Copyright 2025 Manufacture Française des Pneumatiques Michelin
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.michelin.throughputfxproject.services;


import com.michelin.throughputfxproject.ThroughputApplication;
import com.michelin.throughputfxproject.entities.cards.*;
import com.michelin.throughputfxproject.exceptions.ThroughputRuntimeException;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;

/**
 * The `CardService` class provides utility methods for managing and interacting with card decks.
 * It includes functionality for loading, shuffling, filtering, and converting card decks to JSON.
 * This class is designed to handle multiple types of card decks and supports operations
 * such as picking cards randomly or by ID, reloading decks, and parsing card data from CSV files.
 * <p>
 * This class is implemented as a utility class with a private constructor to prevent instantiation.
 * It uses a static initializer block to load card decks during class initialization.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
@Slf4j
public class CardService {

    private static final Map<String, List<Card>> decks = HashMap.newHashMap(5);
    private static final Random random = new Random();


    static {
        // Initialize the card decks by loading cards from CSV files for each deck type.
        // If an error occurs during the loading process, it is logged as an error.

        try {
            // Load the BOOSTER_INOCULATE_TRAP deck from the corresponding CSV file.
            decks.put(Card.BOOSTER_INOCULATE_TRAP, getCards(Card.BOOSTER_INOCULATE_TRAP));
        } catch (URISyntaxException | IOException e) {
            // Log an error if there is an issue building the BOOSTER_INOCULATE_TRAP deck.
            log.error("Trouble building BIT card decks ", e);
        }

        try {
            // Load the SKILLS deck from the corresponding CSV file.
            decks.put(Card.SKILLS, getCards(Card.SKILLS));
        } catch (URISyntaxException | IOException e) {
            // Log an error if there is an issue building the SKILLS deck.
            log.error("Trouble building SKILL card decks ", e);
        }

        try {
            // Load the CHANCE deck from the corresponding CSV file.
            decks.put(Card.CHANCE, getCards(Card.CHANCE));
        } catch (URISyntaxException | IOException e) {
            // Log an error if there is an issue building the CHANCE deck.
            log.error("Trouble building CHANCE card decks ", e);
        }

        try {
            // Load the AUTOMATED_CHANCE deck from the corresponding CSV file.
            decks.put(Card.AUTOMATED_CHANCE, getCards(Card.AUTOMATED_CHANCE));
        } catch (URISyntaxException | IOException e) {
            // Log an error if there is an issue building the AUTOMATED_CHANCE deck.
            log.error("Trouble building AUTO CHANCE card decks ", e);
        }
    }


    /**
     * Picks a random card from the specified deck.
     * The method retrieves the deck by its name, shuffles it, and selects a random card.
     *
     * @param deckName The name of the deck to pick a card from.
     * @return A randomly selected card from the specified deck.
     * @throws IllegalArgumentException If the deck name is not recognized.
     */
    public static Card pickACard(String deckName) {
        List<Card> deck = getCardDeck(deckName);
        return shuffleDeck(deck).get(random.nextInt(deck.size()));
    }

    /**
     * Retrieves the card deck by its name.
     * If the deck is not found or is empty, it attempts to reload the deck from the corresponding CSV file.
     *
     * @param deckName The name of the deck to retrieve.
     * @return The list of cards in the specified deck.
     * @throws IllegalArgumentException   If the deck name is not recognized.
     * @throws ThroughputRuntimeException If an error occurs while reloading the deck.
     */
    private static List<Card> getCardDeck(String deckName) {
        List<Card> deck = decks.get(deckName);
        if (deck == null) {
            throw new IllegalArgumentException(deckName + " is not a recognized card deck");
        }
        if (deck.isEmpty()) {
            try {
                deck = decks.put(deckName, getCards(deckName));
            } catch (URISyntaxException | IOException e) {
                throw new ThroughputRuntimeException(e);
            }
        }
        return deck;
    }

 /**
  * Shuffles the given deck of cards.
  * This method logs the state of the deck before and after shuffling for debugging purposes.
  * It uses the `Collections.shuffle` method to randomize the order of the cards in the deck.
  *
  * @param <T>  The type of cards in the deck, which must extend the `Card` class.
  * @param deck The list of cards to be shuffled.
  * @return The shuffled deck of cards.
  */
 private static <T extends Card> List<T> shuffleDeck(List<T> deck) {

     // Log the deck state before shuffling
     log.debug("before shuffle {}", Arrays.toString(deck.toArray()));

     // Shuffle the list
     Collections.shuffle(deck);

     // Log the deck state after shuffling
     log.debug("after shuffle {}", Arrays.toString(deck.toArray()));

     return deck;
 }

   /**
    * Retrieves a list of cards for the specified deck name.
    * This method uses a switch expression to determine the appropriate CSV file and card type
    * for the given deck name. It then loads the cards from the CSV file and returns them as a list.
    *
    * @param deckName The name of the deck to retrieve cards for.
    * @return A list of cards for the specified deck, or an empty list if the deck name is not recognized.
    * @throws URISyntaxException If the URI of the CSV file is invalid.
    * @throws IOException        If an I/O error occurs while reading the CSV file.
    */
   private static List<Card> getCards(String deckName) throws URISyntaxException, IOException {
       return switch (deckName) {
           case Card.BOOSTER_INOCULATE_TRAP ->
                   new ArrayList<>(geCardsFromCsv(Objects.requireNonNull(ThroughputApplication.class.getResource("cards/bit-" + System.getProperty("run.topic", "default") + ".csv")), BitCard.class));
           case Card.SKILLS ->
                   new ArrayList<>(geCardsFromCsv(Objects.requireNonNull(ThroughputApplication.class.getResource("cards/skills.csv")), SkillCard.class));
           case Card.CHANCE ->
                   new ArrayList<>(geCardsFromCsv(Objects.requireNonNull(ThroughputApplication.class.getResource("cards/chance.csv")), ChanceCard.class));
           case Card.AUTOMATED_CHANCE ->
                   new ArrayList<>(geCardsFromCsv(Objects.requireNonNull(ThroughputApplication.class.getResource("cards/robot_chance.csv")), ChanceRobotCard.class));
           default -> Collections.emptyList();
       };
   }

  /**
   * Reads cards from a CSV file and multiplies them based on the number of copies specified.
   * This method converts the given URL to a file path and delegates the processing to the `cardMultiplier` method.
   *
   * @param <T>    The type of cards to be read, which must extend the `Card` class.
   * @param pathUrl The URL of the CSV file containing the card data.
   * @param clazz   The class type of the cards to be read.
   * @return A list of cards read from the CSV file, including multiplied copies.
   * @throws URISyntaxException If the URI of the CSV file is invalid.
   * @throws IOException        If an I/O error occurs while reading the CSV file.
   */
  private static <T extends Card> List<T> geCardsFromCsv(URL pathUrl, Class<T> clazz) throws URISyntaxException, IOException {
      Path filePath = Paths.get(pathUrl.toURI());
      return cardMultiplier(filePath, clazz);
  }

  /**
   * Multiplies the cards read from a file based on the number of copies specified for each card.
   * This method clones each card the required number of times and adds the clones to the resulting list.
   *
   * @param <T>      The type of cards to be processed, which must extend the `Card` class.
   * @param filePath The path to the file containing the card data.
   * @param clazz    The class type of the cards to be processed.
   * @return A list of cards, including multiplied copies.
   * @throws IOException If an I/O error occurs while reading the file.
   */
  @SuppressWarnings("unchecked")
  private static <T extends Card> List<T> cardMultiplier(Path filePath, Class<T> clazz) throws IOException {
      List<T> cards = cardBeanBuilder(filePath, clazz);
      List<T> cloneCards = new ArrayList<>(cards);
      cards.forEach(card -> {
          for (int i = 0; i < card.getCopies() - 1; i++) {
              Card clone = card.typedClone();
              cloneCards.add((T) clone);
          }
      });
      return cloneCards;
  }

  /**
   * Reads and parses card data from a CSV file into a list of card objects.
   * This method uses the OpenCSV library to map the CSV data to objects of the specified class type.
   *
   * @param <T>      The type of cards to be parsed, which must extend the `Card` class.
   * @param filePath The path to the CSV file containing the card data.
   * @param clazz    The class type of the cards to be parsed.
   * @return A list of card objects parsed from the CSV file.
   * @throws IOException If an I/O error occurs while reading the file.
   */
  private static <T extends Card> List<T> cardBeanBuilder(Path filePath, Class<T> clazz) throws IOException {
      Reader reader = Files.newBufferedReader(filePath);
      CsvToBean<T> cb = new CsvToBeanBuilder<T>(reader)
              .withType(clazz).withSeparator('|')
              .build();
      return cb.parse();
  }

 /**
  * Picks a random card destructively from the BOOSTER_INOCULATE_TRAP deck.
  * This method removes the selected card from the deck to ensure it cannot be picked again.
  *
  * @return A randomly selected `BitCard` from the BOOSTER_INOCULATE_TRAP deck.
  * @throws IllegalArgumentException If the BOOSTER_INOCULATE_TRAP deck is not recognized or is empty.
  */
 public static BitCard pickACardDestructively() {
     List<Card> deck = getCardDeck(Card.BOOSTER_INOCULATE_TRAP);
     return (BitCard) deck.remove(random.nextInt(deck.size()));
 }

 /**
  * Reloads the BOOSTER_INOCULATE_TRAP deck with a filtered list of cards.
  * This method filters the deck based on the provided JSON list of card IDs, retaining only the cards
  * whose IDs match the ones in the list.
  *
  * @param bitDeckJson A list of card IDs (as `Object`) to filter the BOOSTER_INOCULATE_TRAP deck.
  * @throws IllegalArgumentException If the BOOSTER_INOCULATE_TRAP deck is not recognized.
  */
 public static void reloadCards(List<Object> bitDeckJson) {
     List<Card> bitDeck = decks.get(Card.BOOSTER_INOCULATE_TRAP);
     log.debug("Deck size {}", bitDeck.size());
     List<Integer> unmodifiableList = bitDeckJson.stream().map(Integer.class::cast).toList();
     List<Integer> ids = new ArrayList<>(unmodifiableList);
     List<Card> filteredDeck = new ArrayList<>();
     for (Card card : bitDeck) {
         Integer id = ((BitCard) card).getId();
         if (ids.remove(id)) {
             filteredDeck.add(card);
         }
     }

     decks.put(Card.BOOSTER_INOCULATE_TRAP, filteredDeck);
     log.debug("Deck size after reload {}", filteredDeck.size());
 }

 /**
  * Reloads a list of hold cards from the BOOSTER_INOCULATE_TRAP deck based on the provided JSON list of card IDs.
  * This method destructively picks cards from the deck by their IDs and returns them as a list.
  *
  * @param holdDeckJson A list of card IDs (as `Object`) to reload from the BOOSTER_INOCULATE_TRAP deck.
  * @return A list of `BitCard` objects corresponding to the provided card IDs.
  * @throws IllegalArgumentException If the BOOSTER_INOCULATE_TRAP deck is not recognized or a card ID is invalid.
  */
 public static List<BitCard> reloadHoldCards(List<Object> holdDeckJson) {
     List<Integer> unmodifiableList = holdDeckJson.stream().map(Integer.class::cast).toList();
     List<BitCard> filteredDeck = new ArrayList<>();
     unmodifiableList.forEach(id -> filteredDeck.add(pickACardDestructivelyById(id)));

     return filteredDeck;
 }

/**
 * Picks a card destructively from the BOOSTER_INOCULATE_TRAP deck by its ID.
 * This method searches for a card with the specified ID in the deck, removes it, and returns it.
 * If no card with the given ID is found, an exception is thrown.
 *
 * @param id The ID of the card to pick from the BOOSTER_INOCULATE_TRAP deck.
 * @return The `BitCard` object with the specified ID.
 * @throws NoSuchElementException If no card with the given ID is found in the deck.
 */
public static BitCard pickACardDestructivelyById(int id) {
    List<Card> deck = getCardDeck(Card.BOOSTER_INOCULATE_TRAP);
    BitCard returnCard = (BitCard) deck.stream().filter(card -> ((BitCard) card).getId() == id).findFirst().orElseThrow();
    deck.remove(returnCard);
    return returnCard;
}

/**
 * Converts the BOOSTER_INOCULATE_TRAP deck to a JSON string representation.
 * This method maps each card in the deck to its JSON representation and combines them into a JSON array.
 *
 * @return A JSON string representing the BOOSTER_INOCULATE_TRAP deck.
 */
public static String toJSON() {
    List<String> stringList = getCardDeck(Card.BOOSTER_INOCULATE_TRAP).stream().map(BitCard.class::cast).map(BitCard::toJSON).toList();
    return "\"bitDeck\":[" + String.join(",", stringList) + "]";
}


}
