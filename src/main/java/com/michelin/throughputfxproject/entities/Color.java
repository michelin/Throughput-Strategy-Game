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
package com.michelin.throughputfxproject.entities;

import com.michelin.throughputfxproject.entities.state.Savable;

/**
 * The `Color` enum represents a set of predefined colors.
 * It implements the `Savable` interface, allowing each color to be serialized to JSON.
 */
public enum Color implements Savable {

    BLUE, ROSE, GREEN, VIOLET, YELLOW, RED, CYAN, ORANGE, BROWN, BLACK, GRAY;

    /**
     * Returns an array of colors that are used for automated processes.
     * These colors are predefined and include ROSE, GREEN, and YELLOW.
     *
     * @return An array of `Color` objects representing automated colors.
     */
    public static Color[] automatedColorValues() {
        return new Color[]{ROSE, GREEN, YELLOW};
    }

    /**
     * Returns an array of colors that are used for human interaction.
     * These colors are predefined and include a broader range of colors
     * such as BLUE, ROSE, GREEN, VIOLET, YELLOW, RED, CYAN, ORANGE, BROWN, and BLACK.
     *
     * @return An array of `Color` objects representing human-interaction colors.
     */
    public static Color[] humanColorValues() {
        return new Color[]{BLUE, ROSE, GREEN, VIOLET, YELLOW, RED, CYAN, ORANGE, BROWN, BLACK};
    }

    /**
     * Maps the current `Color` enum value to its corresponding JavaFX color.
     * Each enum value is associated with a specific JavaFX color.
     *
     * @return A `javafx.scene.paint.Color` object corresponding to the current `Color` value.
     */
    public javafx.scene.paint.Color lookupFXColor() {
        return switch (this) {
            case BLUE -> javafx.scene.paint.Color.BLUE;
            case VIOLET -> javafx.scene.paint.Color.PURPLE;
            case YELLOW -> javafx.scene.paint.Color.YELLOW;
            case GREEN -> javafx.scene.paint.Color.GREEN;
            case ROSE -> javafx.scene.paint.Color.PINK;
            case RED -> javafx.scene.paint.Color.RED;
            case CYAN -> javafx.scene.paint.Color.CYAN;
            case ORANGE -> javafx.scene.paint.Color.ORANGE;
            case BROWN -> javafx.scene.paint.Color.BROWN;
            case BLACK -> javafx.scene.paint.Color.BLACK;
            case GRAY -> javafx.scene.paint.Color.GRAY;
        };
    }

    /**
     * Maps the current `Color` enum value to its corresponding font color.
     * Each enum value is associated with a specific JavaFX font color.
     * <p>
     * - BLUE and RED map to YELLOW.
     * - VIOLET, GREEN, BROWN, and BLACK map to WHITE.
     * - YELLOW and ORANGE map to BLUE.
     * - All other colors default to BLACK.
     *
     * @return A `javafx.scene.paint.Color` object representing the font color
     * associated with the current `Color` value.
     */
    public javafx.scene.paint.Color lookupFontColor() {
        return switch (this) {
            case BLUE, RED -> javafx.scene.paint.Color.YELLOW;
            case VIOLET, GREEN, BROWN, BLACK -> javafx.scene.paint.Color.WHITE;
            case YELLOW, ORANGE -> javafx.scene.paint.Color.BLUE;
            default -> javafx.scene.paint.Color.BLACK;
        };
    }

    /**
     * Converts the current `Color` enum value to its JSON representation.
     * The JSON representation is a string containing the name of the color.
     *
     * @return A `String` representing the JSON format of the current `Color` value.
     */
    @Override
    public String toJSON() {
        return "\"" + this.name() + "\"";
    }
}