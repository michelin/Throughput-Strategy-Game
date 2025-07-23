/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.michelin.throughputfxproject.test.service;

import com.michelin.throughputfxproject.entities.state.Die;
import com.michelin.throughputfxproject.services.DiceService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DiceServiceTest {
    @Test
    void getDie_createsDieWithCorrectSides() {
        Die die = DiceService.getDie(6);
        assertEquals(6, die.getSides());
    }

    @Test
    void getDie_zeroSides_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> DiceService.getDie(0));
    }

    @Test
    void getDice_createsCorrectNumberOfDice() {
        Die[] dice = DiceService.getDice(6, 3);
        assertEquals(3, dice.length);
        for (Die die : dice) {
            assertEquals(6, die.getSides());
        }
    }

    @Test
    void getDice_zeroSides_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> DiceService.getDice(0, 2));
    }

    @Test
    void rollDie_setsValueWithinRange() {
        Die die = DiceService.getDie(6);
        DiceService.rollDie(die);
        assertTrue(die.getValue() >= 1 && die.getValue() <= 6);
    }

    @Test
    void rollDice_setsAllValuesWithinRange() {
        Die[] dice = DiceService.getDice(6, 5);
        DiceService.rollDice(dice);
        for (Die die : dice) {
            assertTrue(die.getValue() >= 1 && die.getValue() <= 6);
        }
    }

    @Test
    void getDieImage_returnsCorrectImagePath() {
        assertEquals("icons/die_1.png", DiceService.getDieImage(1));
        assertEquals("icons/die_2.png", DiceService.getDieImage(2));
        assertEquals("icons/die_3.png", DiceService.getDieImage(3));
        assertEquals("icons/die_4.png", DiceService.getDieImage(4));
        assertEquals("icons/die_5.png", DiceService.getDieImage(5));
        assertEquals("icons/die_6.png", DiceService.getDieImage(6));
        assertEquals("icons/die_1.png", DiceService.getDieImage(0));
        assertEquals("icons/die_1.png", DiceService.getDieImage(99));
    }
}

