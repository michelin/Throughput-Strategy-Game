package com.michelin.throughputfxproject.entities.servers;

import com.michelin.throughputfxproject.Color;
import com.michelin.throughputfxproject.entities.Server;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class AutomatedServer implements Server {
    private final Color color;
    private final Set<Color> skills = new HashSet<>(1);
    private String type = TYPE_AUTOMATED;

    public AutomatedServer(Color color) {
        this.color = color;
        skills.add(color);
    }


    @Override
    public String getBehavior() {
        return BEHAVIOR_SERVE;
    }

    @Override
    public String getSkillsString() {
               return color.name();
    }

    @Override
    public File geImage() {
        return new File("./cards/IndustrialRobot.jpg");
    }

    @Override
    public File geBackImage() {
        return new File("./cards/IndustrialRobot.jpg");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AutomatedServer that = (AutomatedServer) o;
        return color == that.color;
    }

    @Override
    public int hashCode() {
        int result = color.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return  getType() + '\'' +
                ", name: " + color + '\'' +
                ", skills=" + getSkills();

    }

}
