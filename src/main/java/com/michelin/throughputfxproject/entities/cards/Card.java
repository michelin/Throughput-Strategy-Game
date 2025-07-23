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
