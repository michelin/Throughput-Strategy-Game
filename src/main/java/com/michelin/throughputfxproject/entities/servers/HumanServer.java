/*
 * Copyright 2025 Manufacture Française des Pneumatiques Michelin
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.michelin.throughputfxproject.entities.servers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.michelin.throughputfxproject.entities.Color;
import com.michelin.throughputfxproject.exceptions.ThroughputRuntimeException;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Getter
@Setter
public class HumanServer implements Server {

    static final Logger LOGGER = LoggerFactory.getLogger(HumanServer.class);
    private static final String DEFAULT_HUMAN_IMAGE = "servers/server_black.jpg";
    private final Color color;
    private final Set<Color> skills = HashSet.newHashSet(5);
    private String type = TYPE_HUMAN;


    public HumanServer(@NonNull Color color) {
        this.color = color;
        skills.add(color);
    }

    public HumanServer(@NonNull Color color, Collection<Color> skills) {
        this.color = color;
        this.skills.clear();
        this.skills.addAll(skills);
    }


    @Override
    public String getImage() {
        return DEFAULT_HUMAN_IMAGE;
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

    @Override
    public String toJSON() {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json;
        try {
            json = ow.writeValueAsString(this);
            LOGGER.info("Human Server {}", json);
        } catch (JsonProcessingException e) {
            throw new ThroughputRuntimeException(e);
        }
        return json;
    }

}
