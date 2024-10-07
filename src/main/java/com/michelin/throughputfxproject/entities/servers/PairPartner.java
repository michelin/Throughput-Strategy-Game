package com.michelin.throughputfxproject.entities.servers;


import com.michelin.throughputfxproject.Color;
import com.michelin.throughputfxproject.entities.Server;
import lombok.Setter;

import java.io.File;
import java.util.Set;


@Setter
public class PairPartner implements Server {


    @Override
    public Color getColor() {
        return Color.GRAY;
    }

    @Override
    public String getType() {
        return TYPE_PARTNER;
    }

    @Override
    public String getBehavior() {
        return BEHAVIOR_PAIR;
    }

    @Override
    public Set<Color> getSkills() {
        return Set.of();
    }

    @Override
    public String getSkillsString() {
        return getBehavior();
    }

    @Override
    public File geImage() {
        return new File("./servers/server_pair.jpg");
    }

    @Override
    public File geBackImage() {
        return new File("./cards/WomanJugglingTires.jpg");
    }
}
