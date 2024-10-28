package com.michelin.throughputfxproject.entities.servers;

import com.michelin.throughputfxproject.Color;
import com.michelin.throughputfxproject.entities.Server;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class HumanServer implements Server {

    private final Color color;
    private final Set<Color> skills = new HashSet<>(5);
    private String type = TYPE_HUMAN;


    public HumanServer(@NonNull Color color) {
        this.color = color;
        skills.add(color);
    }


    @Override
    public String getImage() {
        String serverImageFile;
        switch (color) {
            case BLUE:
                serverImageFile = "servers/server_blue.jpg";
                break;
            case GREEN:
                serverImageFile = "servers/server_green.jpg";
                break;
            case YELLOW:
                serverImageFile = "servers/server_yellow.jpg";
                break;
            case VIOLET:
                serverImageFile = "servers/server_violet.jpg";
                break;
            case ROSE:
            default:
                serverImageFile = "servers/server_rose.jpg";
        }
        return serverImageFile;
    }


    @Override
    public String getBackImage() {
        return "cards/WomanJugglingTires.jpg";
    }

    @Override
    public int hashCode() {
        int result = color.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HumanServer that = (HumanServer) o;
        return color == that.color;
    }

    @Override
    public String toString() {
        return getType() +
                ", name: " + color +
                ", skills=" + getSkills();
    }
}
