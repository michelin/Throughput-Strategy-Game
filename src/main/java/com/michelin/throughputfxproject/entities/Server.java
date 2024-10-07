package com.michelin.throughputfxproject.entities;

import com.michelin.throughputfxproject.Color;

import java.io.File;
import java.util.Set;


public interface Server {
    String BEHAVIOR_SERVE = "SERVE";
    String BEHAVIOR_PAIR = "HELP";
    String TYPE_AUTOMATED = "AUTOMATED";
    String TYPE_HUMAN = "HUMAN";
    String TYPE_PARTNER = "PAIR";

    Color getColor();
    String getType();
    String getBehavior();
    Set<Color> getSkills();
    String getSkillsString();
    File geImage();
    File geBackImage();

}
