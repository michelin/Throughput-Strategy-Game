package com.michelin.throughputfxproject.entities;

import lombok.Getter;

@Getter
public class HelpAction implements BoardAction {

    private final HelpActionType type;

    public HelpAction(HelpActionType type) {
        this.type = type;
    }

    public enum HelpActionType {
        ADD_ONE, DOUBLE, AUTOMATE, PAIR,AUGMENT
    }
}
