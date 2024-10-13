package com.michelin.throughputfxproject;

public enum Color {

    BLUE, ROSE, GREEN, VIOLET, YELLOW, GRAY;


    public String initialWithColor() {
        return String.valueOf(this.name().charAt(0));

    }

    public String nameWithColor() {
        return this.name();
    }

    public static Color lookupByFXColor(javafx.scene.paint.Color fxColor) {

        if (fxColor.equals(javafx.scene.paint.Color.BLUE)) {
            return Color.BLUE;
        } else if (fxColor.equals(javafx.scene.paint.Color.PURPLE)) {
            return Color.VIOLET;
        } else if (fxColor.equals(javafx.scene.paint.Color.YELLOW)) {
            return Color.YELLOW;
        } else if (fxColor.equals(javafx.scene.paint.Color.GREEN)) {
            return Color.GREEN;
        } else if (fxColor.equals(javafx.scene.paint.Color.PINK)) {
            return Color.ROSE;
        }
        return Color.GRAY;
    }
}