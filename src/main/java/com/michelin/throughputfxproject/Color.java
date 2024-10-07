package com.michelin.throughputfxproject;

public enum Color {

    BLUE, ROSE, GREEN, VIOLET, YELLOW, GRAY;

    private static final String ANSI_RED_BACK = "\u001B[41m";
    private static final String ANSI_GREEN_BACK = "\u001B[42m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_RESET = "\u001B[0m";

    public String initialWithColor() {
        switch (this) {
            case ROSE:
                return ANSI_RED_BACK + this.name().charAt(0) + ANSI_RESET;
            case GREEN:
                return ANSI_GREEN_BACK + this.name().charAt(0) + ANSI_RESET;
            case YELLOW:
                return ANSI_YELLOW + this.name().charAt(0) + ANSI_RESET;
            case BLUE:
                return ANSI_BLUE + this.name().charAt(0) + ANSI_RESET;
            case VIOLET:
                return ANSI_PURPLE + this.name().charAt(0) + ANSI_RESET;
            case GRAY:
                break;
        }
        return this.name();
    }

    public String nameWithColor() {
        switch (this) {
            case ROSE:
                return ANSI_RED_BACK + this.name() + ANSI_RESET;
            case GREEN:
                return ANSI_GREEN_BACK + this.name() + ANSI_RESET;
            case YELLOW:
                return ANSI_YELLOW + this.name() + ANSI_RESET;
            case BLUE:
                return ANSI_BLUE + this.name() + ANSI_RESET;
            case VIOLET:
                return ANSI_PURPLE + this.name() + ANSI_RESET;
            case GRAY:
                break;
        }
        return this.name();
    }
}