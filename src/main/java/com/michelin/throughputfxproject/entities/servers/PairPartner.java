package com.michelin.throughputfxproject.entities.servers;


import com.michelin.throughputfxproject.entities.Color;
import lombok.Setter;
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
    public Set<Color> getSkills() {
        return Set.of();
    }

    @Override
    public String getImage() {
        return "servers/server_pair.jpg";
    }

    @Override
    public String getBackImage() {
        return "cards/WomanJugglingTires.jpg";
    }

    @Override
    public String toJSON() {
        return "\"server\":{\"type\": \"" + getType()  + "\"}";
    }
}
