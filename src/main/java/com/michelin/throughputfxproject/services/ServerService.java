package com.michelin.throughputfxproject.services;

import com.michelin.throughputfxproject.entities.Color;
import com.michelin.throughputfxproject.entities.servers.AutomatedServer;
import com.michelin.throughputfxproject.entities.servers.HumanServer;
import com.michelin.throughputfxproject.entities.servers.PairPartner;
import com.michelin.throughputfxproject.entities.servers.Server;
import com.michelin.throughputfxproject.entities.state.Board;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * A service class that provides utility methods for managing and recreating server instances.
 * This class includes methods for retrieving or creating `HumanServer` and `AutomatedServer` instances,
 * recreating servers from attributes, and managing server skills.
 * <p>
 * The class is marked as `@NoArgsConstructor` with `PRIVATE` access to prevent instantiation.
 * It also uses Lombok annotations for `@EqualsAndHashCode`, `@ToString`, and logging with `@Slf4j`.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
@Slf4j
public class ServerService {

    private static final Set<HumanServer> humanServers = HashSet.newHashSet(5);
    private static final Set<AutomatedServer> automatedServers = HashSet.newHashSet(3);
    private static final PairPartner pairPartner = new PairPartner();


/**
 * Retrieves a `HumanServer` instance with the specified color.
 * If a `HumanServer` with the given color already exists, it is returned.
 * Otherwise, a new `HumanServer` is created, added to the set of human servers, and returned.
 *
 * @param color The color of the `HumanServer` to retrieve or create.
 * @return The `HumanServer` instance with the specified color.
 */
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

/**
 * Retrieves the singleton instance of the `PairPartner` class.
 *
 * @return The singleton `PairPartner` instance.
 */
public static PairPartner getPairPartnerInstance() {
    return pairPartner;
}

/**
 * Retrieves an `AutomatedServer` instance with the specified color.
 * If an `AutomatedServer` with the given color already exists, it is returned.
 * Otherwise, a new `AutomatedServer` is created, added to the set of automated servers, and returned.
 *
 * @param color The color of the `AutomatedServer` to retrieve or create.
 * @return The `AutomatedServer` instance with the specified color.
 */
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

   /**
    * Recreates a `Server` instance from a map of attributes.
    * The map should contain the server's color, type, and skills.
    * If the map is null, the method returns null.
    *
    * @param serverMap A map containing the server's attributes:
    *                  - "color": The color of the server (String).
    *                  - "type": The type of the server (String).
    *                  - "skills": A list of skill colors (List<String>).
    * @return A `Server` instance created from the provided attributes, or null if the map is null.
    */
   @SuppressWarnings("unchecked")
   public static Server recreateServerFromMap(Map<String, Object> serverMap) {

       if (serverMap == null)    return null;

       Color serverColor = Color.valueOf((String) serverMap.get("color"));
       String serverType = (String) serverMap.get("type");
       List<String> skillsJson = (List<String>)serverMap.get("skills");
       List<Color> skillsColors = skillsJson.stream().map(Color::valueOf).toList();
       return ServerService.recreateServer(serverColor, serverType, skillsColors);
   }

   /**
    * Recreates a `Server` instance based on the provided attributes.
    * Depending on the server type, it creates a `HumanServer`, `AutomatedServer`, or uses the singleton `PairPartner`.
    * Newly created servers are added to their respective sets.
    *
    * @param color The color of the server.
    * @param type The type of the server (e.g., human, automated, or partner).
    * @param skills A list of skill colors for the server (only applicable for `HumanServer`).
    * @return The recreated `Server` instance, or null if the type is invalid.
    */
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

   /**
    * Removes all skills from the most skilled `HumanServer` in the set.
    * If the most skilled server has more than one skill, its skills are removed,
    * and the server is returned to its original workstation on the board.
    * If no `HumanServer` exists or none have more than one skill, the method does nothing.
    */
   public static void removeSkillsFromMostSkilledServer() {
       HumanServer mostSkilled = humanServers.stream().max(Comparator.comparingInt(HumanServer::skillsCount)).orElse(null);
       if (mostSkilled != null && mostSkilled.skillsCount() > 1) {
           mostSkilled.removeSkills();
           Board.getInstance().returnServerToOriginalWorkstation(mostSkilled);
       }
   }

}
