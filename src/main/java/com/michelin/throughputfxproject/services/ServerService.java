package com.michelin.throughputfxproject.services;

import com.michelin.throughputfxproject.entities.Color;
import com.michelin.throughputfxproject.entities.servers.AutomatedServer;
import com.michelin.throughputfxproject.entities.servers.HumanServer;
import com.michelin.throughputfxproject.entities.servers.PairPartner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;


public class ServerService {
    public static final Logger LOGGER = LoggerFactory.getLogger(ServerService.class.getName());

    private static final Set<HumanServer> humanServers = HashSet.newHashSet(5);
    private static final Set<AutomatedServer> automatedServers = HashSet.newHashSet(3);
    private static final PairPartner pairPartner = new PairPartner();

    private ServerService() {
        super();
    }

    public static void removeSkillsFromMostSkilledServer(){
        HumanServer mostSkilled = humanServers.stream().max(Comparator.comparingInt(HumanServer::skillsCount)).orElse(null);
        if(mostSkilled != null && mostSkilled.skillsCount() > 1) {
            mostSkilled.removeSkills();
        }
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
