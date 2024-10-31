package com.michelin.throughputfxproject.entities.servers;

import com.michelin.throughputfxproject.entities.Color;

import java.util.Set;


@SuppressWarnings("SameReturnValue")
public interface Server {
    String BEHAVIOR_SERVE = "SERVE";
    String BEHAVIOR_PAIR = "HELP";
    String TYPE_AUTOMATED = "AUTOMATED";
    String TYPE_HUMAN = "HUMAN";
    String TYPE_PARTNER = "PAIR";

    Color getColor();
    String getType();
    Set<Color> getSkills();
    String getImage();
    String getBackImage();

}
