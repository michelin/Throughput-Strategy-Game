package com.michelin.throughputfxproject.entities;

import com.michelin.throughputfxproject.entities.state.Savable;

public enum Color implements Savable {

    BLUE, ROSE, GREEN, VIOLET, YELLOW, RED, CYAN, ORANGE, BROWN, BLACK, GRAY;

    public static Color[] automatedColorValues() {
        return new Color[]{ROSE, GREEN, YELLOW};
    }

    public static Color[] humanColorValues() {
        return new Color[]{BLUE, ROSE, GREEN, VIOLET, YELLOW,RED, CYAN, ORANGE, BROWN, BLACK};
    }

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

    public javafx.scene.paint.Color lookupFontColor() {
        return switch (this) {
            case BLUE,RED -> javafx.scene.paint.Color.YELLOW;
            case VIOLET,GREEN,BROWN,BLACK-> javafx.scene.paint.Color.WHITE;
            case YELLOW,ORANGE -> javafx.scene.paint.Color.BLUE;
            default -> javafx.scene.paint.Color.BLACK;
        };
    }

    @Override
    public String toJSON() {
        return "\"" + this.name() + "\"";
    }
}