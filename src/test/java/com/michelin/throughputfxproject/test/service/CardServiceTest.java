package com.michelin.throughputfxproject.test.service;

import com.michelin.throughputfxproject.entities.cards.*;
import org.junit.jupiter.api.*;


import java.util.*;

import static com.michelin.throughputfxproject.services.CardService.*;
import static org.junit.jupiter.api.Assertions.*;

class CardServiceTest {

    @BeforeEach
    void setUp() {
        // Optionally reset static state if needed
    }

    @Test
    void pickACard_returnsCardFromDeck() {
        Card card = pickACard(Card.BOOSTER_INOCULATE_TRAP);
        assertNotNull(card);
    }

    @Test
    void pickACard_invalidDeck_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> pickACard("INVALID_DECK"));
    }

    @Test
    void pickACardDestructively_removesCard() {
        BitCard card = pickACardDestructively();
        assertNotNull(card);
    }

    @Test
    void reloadCards_filtersDeck() {
        List<Object> ids = List.of(1, 2, 3);
        assertDoesNotThrow(() -> reloadCards(ids));
    }

    @Test
    void reloadHoldCards_returnsCardsById() {
        List<Object> ids = List.of(1, 2);
        List<BitCard> cards = reloadHoldCards(ids);
        assertEquals(ids.size(), cards.size());
    }

    @Test
    void pickACardDestructivelyById_returnsCorrectCard() {
        BitCard card = pickACardDestructivelyById(1);
        assertNotNull(card);
        assertEquals(1, card.getId());
    }

    @Test
    void pickACardDestructivelyById_invalidId_throwsException() {
        assertThrows(NoSuchElementException.class, () -> pickACardDestructivelyById(-1));
    }

    @Test
    void toJSON_returnsJsonString() {
        String json = toJSON();
        assertTrue(json.startsWith("\"bitDeck\":"));
    }
}

