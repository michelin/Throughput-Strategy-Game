package com.michelin.throughputfxproject.services;

import com.michelin.throughputfxproject.Color;
import com.michelin.throughputfxproject.entities.Server;
import com.michelin.throughputfxproject.entities.servers.AutomatedServer;
import com.michelin.throughputfxproject.entities.servers.HumanServer;
import com.michelin.throughputfxproject.entities.servers.PairPartner;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class ServerService {
    public static final Logger LOGGER = LoggerFactory.getLogger(ServerService.class.getName());

    private static final Set<HumanServer> humanServers = new HashSet<>(5);
    private static final Set<AutomatedServer> automatedServers = new HashSet<>(3);
    private static final PairPartner pairPartner = new PairPartner();

    private ServerService() {
        super();
    }

    public static Server addSkill(@NonNull HumanServer server, @NonNull Color skill) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Original array: {}", Arrays.toString(server.getSkills().toArray()));
        }
        // add skill
        server.getSkills().add(skill); // Add element to the end

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Resized array: {}", Arrays.toString(server.getSkills().toArray()));
        }
        return server;
    }

    public static HumanServer getHumanServer(Color color) {
        for (HumanServer humanServer : humanServers) {
            if (humanServer.getColor().equals(color)) {
                return humanServer;
            }
        }
        HumanServer humanServer = new HumanServer(color);
        humanServers.add(new HumanServer(color));
        return humanServer;
    }

    public static AutomatedServer getRobotServer(Color color) {
        for (AutomatedServer automatedServer : automatedServers) {
            if (automatedServer.getColor().equals(color)) {
                return automatedServer;
            }
        }
        AutomatedServer automatedServer = new AutomatedServer(color);
        automatedServers.add(new AutomatedServer(color));
        return automatedServer;
    }

    public static PairPartner getPairPartnerInstance() {
        return pairPartner;
    }

}
