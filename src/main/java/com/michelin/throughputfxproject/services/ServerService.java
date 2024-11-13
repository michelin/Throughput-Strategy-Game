package com.michelin.throughputfxproject.services;

import com.michelin.throughputfxproject.entities.Color;
import com.michelin.throughputfxproject.entities.servers.AutomatedServer;
import com.michelin.throughputfxproject.entities.servers.HumanServer;
import com.michelin.throughputfxproject.entities.servers.PairPartner;
import com.michelin.throughputfxproject.entities.servers.Server;
import com.michelin.throughputfxproject.entities.state.Board;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class ServerService {
    public static final Logger LOGGER = LoggerFactory.getLogger(ServerService.class.getName());

    private static final Set<HumanServer> humanServers = HashSet.newHashSet(5);
    private static final Set<AutomatedServer> automatedServers = HashSet.newHashSet(3);
    private static final PairPartner pairPartner = new PairPartner();

    private ServerService() {
        super();
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

    public static PairPartner getPairPartnerInstance() {
        return pairPartner;
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

    @SuppressWarnings("unchecked")
    public static Server recreateServerFromMap(Map<String, Object> serverMap) {

        if (serverMap == null)    return null;

        Color serverColor = Color.valueOf((String) serverMap.get("color"));
        String serverType = (String) serverMap.get("type");
        List<String> skillsJson = (List<String>)serverMap.get("skills");
        List<Color> skillsColors = skillsJson.stream().map(Color::valueOf).toList();
        return ServerService.recreateServer(serverColor, serverType, skillsColors);
    }

    public static Server recreateServer(Color color, String type, List<Color> skills) {
        Server server = null;
        switch (type) {
            case Server.TYPE_HUMAN -> {
                server = new HumanServer(color, skills);
                humanServers.add((HumanServer) server);
            }
            case Server.TYPE_AUTOMATED -> {
                server = new AutomatedServer(color);
                automatedServers.add((AutomatedServer) server);
            }
            case Server.TYPE_PARTNER -> server = pairPartner;
            default -> {
                //do nothing
            }
        }
        return server;
    }

    public static void removeSkillsFromMostSkilledServer() {
        HumanServer mostSkilled = humanServers.stream().max(Comparator.comparingInt(HumanServer::skillsCount)).orElse(null);
        if (mostSkilled != null && mostSkilled.skillsCount() > 1) {
            mostSkilled.removeSkills();
            Board.getInstance().returnServerToOriginalWorkstation(mostSkilled);
        }
    }

}
