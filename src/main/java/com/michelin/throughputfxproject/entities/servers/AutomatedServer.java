package com.michelin.throughputfxproject.entities.servers;

import com.michelin.throughputfxproject.entities.Color;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class AutomatedServer implements Server {
    private final Color color;
    private final Set<Color> skills = HashSet.newHashSet(1);
    private String type = TYPE_AUTOMATED;

    public AutomatedServer(Color color) {
        this.color = color;
        skills.add(color);
    }

    @Override
    public String getImage() {
        return switch (color) {
            case GREEN -> "servers/robot_green.jpg";
            case YELLOW -> "servers/robot_yellow.jpg";
            default -> "servers/robot_rose.jpg";
        };
    }

    @Override
    public String getBackImage() {
        return "cards/IndustrialRobot.jpg";
    }

    @Override
    public String toJSON() {
        return "\"server\":{\"color\":" + getColor().name() + ",\"type\":\"" + getType()  + "\"}";
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

        AutomatedServer that = (AutomatedServer) o;
        return color == that.color;
    }

    @Override
    public String toString() {
        return getType() + '\'' + ", name: " + color + '\'' + ", skills=" + getSkills();

    }

}
