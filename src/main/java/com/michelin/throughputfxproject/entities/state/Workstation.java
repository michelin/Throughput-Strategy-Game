package com.michelin.throughputfxproject.entities.state;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.michelin.throughputfxproject.entities.Color;
import com.michelin.throughputfxproject.entities.servers.HumanServer;
import com.michelin.throughputfxproject.entities.servers.Server;
import com.michelin.throughputfxproject.exceptions.ThroughputRuntimeException;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;



@Getter
@Setter
public class Workstation implements Savable {

    public static final Logger LOGGER = LoggerFactory.getLogger(Workstation.class.getName());
    @lombok.NonNull
    private Color color;
    private Set<Server> servers = HashSet.newHashSet(5);
    private int capacity;
    private int workItemCount;
    private boolean active = true;


    public Workstation(@NonNull HumanServer server, int capacity, Color color) {
        this.servers.add(server);
        this.capacity = capacity;
        this.color = color;
    }

    public Workstation(Collection<Server>  serverList, int capacity, Color color, int workItemCount, boolean active) {
        this.servers.clear();
        this.servers.addAll(serverList);
        this.capacity = capacity;
        this.color = color;
        this.active = active;
        this.workItemCount = workItemCount;
    }

    public void addToWorkItemCount(int amount) {
        if (amount < 0) throw new AssertionError();
        workItemCount += amount;
    }

    public boolean hasHumanServers() {
        return servers.stream().anyMatch(server -> server.getType().equals(Server.TYPE_HUMAN));
    }

    public void subtractFromWorkItemCount(int amount) {
        if (amount < 0) throw new AssertionError();
        if (amount > workItemCount) throw new AssertionError();
        workItemCount -= amount;
    }

    @Override
    public String toString() {
        return  "Workstation=" + getColor().name() + System.lineSeparator() +
                ", Capacity=" + getCapacity() + System.lineSeparator() +
                ", Wrk Items=" + getWorkItemCount() + System.lineSeparator() +
                ", Servers=" + getServers();
    }

    public String toJSON(){
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json;
        try {
             json = ow.writeValueAsString(this);
            LOGGER.info("Workstation {}", json);
        } catch (JsonProcessingException e) {
            throw new ThroughputRuntimeException(e);
        }
        return json;
    }

    public @NonNull Color getColor() {
        if(!active) return Color.GRAY;
        return color;
    }
}
