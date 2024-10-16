package com.michelin.throughputfxproject.entities.servers;

import com.michelin.throughputfxproject.Color;
import com.michelin.throughputfxproject.entities.Server;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
    public String getBehavior() {
        return BEHAVIOR_SERVE;
    }

    @Override
    public String getSkillsString() {
        List<String> builder = new ArrayList<>();
        skills.forEach(skill -> {
            if (color.equals(skill)) {
                builder.add(skill.name());
            } else {
                builder.add(skill.initialWithColor());
            }
        });
        return String.join(",", builder);
    }

    @Override
    public File geImage() {
        return new File("./cards/WomanJugglingTires.jpg");
    }

    @Override
    public File geBackImage() {
        return new File("./cards/WomanJugglingTires.jpg");
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
