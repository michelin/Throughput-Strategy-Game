package com.michelin.throughputfxproject.entities;

public enum Color {

    BLUE, ROSE, GREEN, VIOLET, YELLOW, GRAY;


    public String initialWithColor() {
        return String.valueOf(this.name().charAt(0));

    }

    public static Color[] automatedColorValues() {
        return new Color[]{ROSE, GREEN,  YELLOW};
    }

    public static Color[] humanColorValues() {
        return new Color[]{BLUE, ROSE, GREEN, VIOLET, YELLOW};
    }

    public javafx.scene.paint.Color lookupFXColor() {

        return switch (this) {
            case BLUE -> javafx.scene.paint.Color.BLUE;
            case VIOLET -> javafx.scene.paint.Color.PURPLE;
            case YELLOW -> javafx.scene.paint.Color.YELLOW;
            case GREEN -> javafx.scene.paint.Color.GREEN;
            case ROSE -> javafx.scene.paint.Color.PINK;
            default -> javafx.scene.paint.Color.WHITE;
        };
    }

        public javafx.scene.paint.Color lookupFontColor() {

            return switch (this) {
                case BLUE -> javafx.scene.paint.Color.YELLOW;
                case VIOLET, GREEN -> javafx.scene.paint.Color.WHITE;
                case YELLOW -> javafx.scene.paint.Color.BLUE;
                default -> javafx.scene.paint.Color.BLACK;
            };

    }

}