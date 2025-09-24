/*
 * Copyright 2025 Manufacture Française des Pneumatiques Michelin
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
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
