package com.michelin.throughputfxproject;

public enum Color {

    BLUE, ROSE, GREEN, VIOLET, YELLOW, GRAY;


    public String initialWithColor() {
        return String.valueOf(this.name().charAt(0));

    }

    public String nameWithColor() {
        return this.name();
    }
}