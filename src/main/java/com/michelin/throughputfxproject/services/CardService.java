package com.michelin.throughputfxproject.services;


import com.michelin.throughputfxproject.ThroughputApplication;
import com.michelin.throughputfxproject.entities.Card;
import com.michelin.throughputfxproject.entities.cards.BitCard;
import com.michelin.throughputfxproject.entities.cards.ChanceCard;
import com.michelin.throughputfxproject.entities.cards.ChanceRobotCard;
import com.michelin.throughputfxproject.entities.cards.SkillCard;
import com.michelin.throughputfxproject.exceptions.ThroughputRuntimeException;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class CardService {
    public static final Logger LOGGER = LoggerFactory.getLogger(CardService.class.getName());
    private static final Map<String, List<Card>> decks = HashMap.newHashMap(5);
    private static final Random random = new Random();


    static{
        try {
            decks.put(Card.BOOSTER_INOCULATE_TRAP, getCards(Card.BOOSTER_INOCULATE_TRAP));
        } catch (URISyntaxException | IOException e) {
            LOGGER.error("Trouble building BIT card decks ", e);
        }

        try {
            decks.put(Card.SKILLS, getCards(Card.SKILLS));
        } catch (URISyntaxException | IOException e) {
            LOGGER.error("Trouble building SKILL card decks ", e);
        }

        try {
            decks.put(Card.CHANCE, getCards(Card.CHANCE));
        } catch (URISyntaxException | IOException e) {
            LOGGER.error("Trouble building CHANCE card decks ", e);
        }

        try {
            decks.put(Card.AUTOMATED_CHANCE, getCards(Card.AUTOMATED_CHANCE));
        } catch (URISyntaxException | IOException e) {
            LOGGER.error("Trouble building AUTO CHANCE card decks ", e);
        }
    }

    private CardService() {


    }


    private static List<Card> getCards(String deckName) throws URISyntaxException, IOException {
        switch (deckName) {
            case Card.BOOSTER_INOCULATE_TRAP:
                return new ArrayList<>(geCardsFromCsv(Objects.requireNonNull(ThroughputApplication.class.getResource("cards/bit.csv")), BitCard.class));
            case Card.SKILLS:
                return new ArrayList<>(geCardsFromCsv(Objects.requireNonNull(ThroughputApplication.class.getResource("cards/skills.csv")), SkillCard.class));
            case Card.CHANCE:
                return new ArrayList<>(geCardsFromCsv(Objects.requireNonNull(ThroughputApplication.class.getResource("cards/chance.csv")), ChanceCard.class));
            case Card.AUTOMATED_CHANCE:
                return new ArrayList<>(geCardsFromCsv(Objects.requireNonNull(ThroughputApplication.class.getResource("cards/robot_chance.csv")), ChanceRobotCard.class));
            default:
                return Collections.emptyList();
        }
    }

    private static <T extends Card> List<T> geCardsFromCsv(URL pathUrl, Class<T> clazz) throws URISyntaxException, IOException {
        Path filePath = Paths.get(pathUrl.toURI());
        return cardMultiplier(filePath, clazz);
    }

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

    private static <T extends Card> List<T> cardBeanBuilder(Path filePath, Class<T> clazz) throws IOException {
        Reader reader = Files.newBufferedReader(filePath);
        CsvToBean<T> cb = new CsvToBeanBuilder<T>(reader)
                .withType(clazz).withSeparator('|')
                .build();
        return cb.parse();
    }

    public static Card pickACard(String deckName) {
        List<Card> deck = getCardDeck(deckName);
        return shuffleDeck(deck).get(random.nextInt(deck.size()));
    }

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

    private static <T extends Card> List<T> shuffleDeck(List<T> deck) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("before shuffle {}", Arrays.toString(deck.toArray()));
        }
        // Convert array to list for shuffling

        // Shuffle the list
        Collections.shuffle(deck);

        // Print the shuffled array
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("after shuffle {}", Arrays.toString(deck.toArray()));
        }

        return deck;
    }

    public static Card pickACardDestructively(String deckName) {
        List<Card> deck = getCardDeck(deckName);
        return deck.remove(random.nextInt(deck.size()));
    }


}
