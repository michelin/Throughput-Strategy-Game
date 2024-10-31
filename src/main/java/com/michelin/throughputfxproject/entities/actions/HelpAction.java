package com.michelin.throughputfxproject.entities.actions;


public record HelpAction(
        HelpAction.HelpActionType type) implements BoardAction {

    public enum HelpActionType {
        ADD_ONE, DOUBLE, AUTOMATE, PAIR, AUGMENT
    }
}
