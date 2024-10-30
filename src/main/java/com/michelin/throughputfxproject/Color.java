package com.michelin.throughputfxproject;

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

        switch (this) {
            case BLUE:
                return javafx.scene.paint.Color.BLUE;
            case VIOLET:
                return javafx.scene.paint.Color.PURPLE;
            case YELLOW:
                return javafx.scene.paint.Color.YELLOW;
            case GREEN:
                return javafx.scene.paint.Color.GREEN;
            case ROSE:
                return javafx.scene.paint.Color.PINK;
            default:
                return javafx.scene.paint.Color.WHITE;
        }
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