package com.michelin.throughputfxproject.entities.state;

import com.michelin.throughputfxproject.entities.Color;
import com.michelin.throughputfxproject.entities.servers.HumanServer;
import com.michelin.throughputfxproject.entities.servers.Server;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;


@Getter
@Setter
public class Workstation {

    @lombok.NonNull
    private Color color;
    private Set<Server> servers = HashSet.newHashSet(5);
    private int capacity;
    private int workItemCount;


    public Workstation(@NonNull HumanServer server, int capacity, Color color) {
        servers.add(server);
        this.capacity = capacity;
        this.color = color;
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
}
