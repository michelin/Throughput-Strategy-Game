package com.michelin.throughputfxproject.entities.servers;

import com.michelin.throughputfxproject.entities.Color;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class HumanServer implements Server {

    private final Color color;
    private final Set<Color> skills = HashSet.newHashSet(5);
    private String type = TYPE_HUMAN;


    public HumanServer(@NonNull Color color) {
        this.color = color;
        skills.add(color);
    }


    @Override
    public String getImage() {
        return switch (color) {
            case BLUE -> "servers/server_blue.jpg";
            case GREEN -> "servers/server_green.jpg";
            case YELLOW -> "servers/server_yellow.jpg";
            case VIOLET -> "servers/server_violet.jpg";
            default -> "servers/server_rose.jpg";
        };
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

    public int skillsCount(){
        return skills.size();
    }

    public void removeSkills(){
        skills.clear();
        skills.add(color);
    }
}
