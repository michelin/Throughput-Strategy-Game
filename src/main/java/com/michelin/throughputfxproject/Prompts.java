package com.michelin.throughputfxproject;

import com.michelin.throughputfxproject.entities.*;
import com.michelin.throughputfxproject.entities.cards.BitCard;
import com.michelin.throughputfxproject.entities.cards.ChanceCard;
import com.michelin.throughputfxproject.entities.cards.ChanceRobotCard;
import com.michelin.throughputfxproject.entities.cards.SkillCard;
import com.michelin.throughputfxproject.entities.servers.AutomatedServer;
import com.michelin.throughputfxproject.entities.servers.HumanServer;
import com.michelin.throughputfxproject.entities.servers.ServerMove;
import com.michelin.throughputfxproject.exceptions.ThroughputRuntimeException;
import com.michelin.throughputfxproject.services.CardService;
import com.michelin.throughputfxproject.services.DiceService;
import com.michelin.throughputfxproject.services.ServerService;
import com.michelin.throughputfxproject.services.WorkstationService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Prompts {
    public static final Logger LOGGER = LoggerFactory.getLogger(Prompts.class.getName());
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String WORKSTATION_IS_EMPTY_NO_MOVES_ARE_POSSIBLE = "{}  {}workstation is empty, No moves are possible{}";
    private static final String GREEN_BLUE_ROSE_VIOLET_YELLOW = "\\b(GREEN|BLUE|ROSE|VIOLET|YELLOW)\\b";
    private static final String GREEN_ROSE_YELLOW = "\\b(GREEN|ROSE|YELLOW)\\b";

    private static final Pattern colorPattern = Pattern.compile(GREEN_BLUE_ROSE_VIOLET_YELLOW);
    private static final Pattern robotColorPattern = Pattern.compile(GREEN_ROSE_YELLOW);


    private Prompts() {
        super();
    }





    public static void asciiArt3(@NonNull Backlog backlog,@NonNull FinishedGoods finishedGoods) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ThroughputApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);

        String space = "    ";
        String dashes = "________________";
        String sides = "|              |";

        int[] scoreArray = new int[7];
        scoreArray[0] = backlog.getBacklogItemCount();
        scoreArray[6] = finishedGoods.getFinishedGoods();
        String[] colorArray = new String[5];

        String[][] serverBigArray = new String[3][5];
        Arrays.fill(serverBigArray[0], "In Training");
        for (int x = 0; x < WorkstationService.getWorkstations().length; x++) {
            Workstation workstation = WorkstationService.getWorkstations()[x];

            scoreArray[x + 1] = workstation.getWorkItemCount();

            String colorName = workstation.getColor().nameWithColor() + ": " + workstation.getCapacity();
            colorArray[x] = StringUtils.center(colorName, 25);

            List<Server> servers = workstation.getServers();
            for (int y = 0; y < servers.size(); y++) {
                Server server = servers.get(y);
                serverBigArray[y][x] = StringUtils.center(server.getSkillsString(), 25);
            }
        }

        String serverString0 = String.join(space, serverBigArray[0]);
        String serverString1 = Stream.of(serverBigArray[1]).filter(Objects::nonNull) // Filter out null values
                .collect(Collectors.joining(" "));
        String serverString2 = Stream.of(serverBigArray[2]).filter(Objects::nonNull) // Filter out null values
                .collect(Collectors.joining(" "));
        String sevenBoxes = "{}{}{}{}{}{}{}{}{}{}{}{}{}{}";
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(serverString2);
            LOGGER.info(serverString1);

            LOGGER.info("{}    {}    {}", StringUtils.center("", 16), serverString0, StringUtils.center("", 16));
            LOGGER.info(sevenBoxes, dashes, space, dashes, space, dashes, space, dashes, space, dashes, space, dashes, space, dashes, space);
            LOGGER.info(sevenBoxes, sides, space, sides, space, sides, space, sides, space, sides, space, sides, space, sides, space);

            StringBuilder builder = new StringBuilder();
            for (int x = 0; x < 7; x++) {
                builder.append(String.format("|%s|%s", StringUtils.center(String.format("%03d", scoreArray[x]), 14), space));
            }
            LOGGER.info(builder.toString());
            LOGGER.info(sevenBoxes, dashes, space, dashes, space, dashes, space, dashes, space, dashes, space, dashes, space, dashes, space);

            String colors = String.join(StringUtils.center(" ", 4), colorArray);

            String logStatement = StringUtils.center("BackLog", 16) + space + colors + space + StringUtils.center("FinGoods", 16);
            LOGGER.info(logStatement);
            LOGGER.info("");
            LOGGER.info("");
        }

    }

    public static BitCard drawBit(int dieSides) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ThroughputApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        //If not week 1 draw BIT card if they roll a 6
        int drawBitInt = DiceService.rollDie(DiceService.getDie(dieSides)).getValue();
        if (drawBitInt == 6) {
            LOGGER.info("Draw a {} card!", Card.BOOSTER_INOCULATE_TRAP);
            BitCard bitCard = (BitCard) CardService.getInstance().pickACardDestructively(Card.BOOSTER_INOCULATE_TRAP);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(bitCard.toString());
            }
            //Follow the instructions on BIT card. If we get a hold card put in weekly hold or game hold.
            //Execute work item traps immediately, otherwise return traps for future execution.
            return bitCard;
            //Execute do-over hold cards -- Look for mitigating hold cards

        }
        return null;
    }




    public static void implementPairedProgramming() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ThroughputApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);

        Scanner scanner = new Scanner(System.in);
        LOGGER.info("Choose which color workstation to pair. Requires a HUMAN server on the same workstation to work");
        LOGGER.info("Pair Partner gives an additional chance for a successful day");
        String workstationColorToAddPair = scanner.nextLine();
        try {
            WorkstationService.pairWorkstation(Color.valueOf(workstationColorToAddPair.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ThroughputRuntimeException(e);
        }

        scanner.close();
    }


    public static void promptForAppliedTrap(Trap trap) throws IOException{
        FXMLLoader fxmlLoader = new FXMLLoader(ThroughputApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);

        LOGGER.info("No Mitigation available for trap");
        LOGGER.info("{} Loses {}", trap.getEffected(), trap.getDuration());

    }

    public static void promptForMitigatedTrap(Trap trap) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ThroughputApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);

        LOGGER.info("Applying Mitigation for trap");
        LOGGER.info("{} Loses {}", trap.getEffected(), trap.getMitigatedDuration());
    }

    public static List<ServerMove> promptForServerMoves( HumanServer inTraining) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ThroughputApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);

        List<ServerMove> moves = new ArrayList<>();

        LOGGER.info("SERVER MOVES!! ->  At prompting please enter: Server Color > Workstation Color");
        LOGGER.info("Workstation is the receiving workstation - Worker must possess the skill (color) of the receiving workstation");
        LOGGER.info("You can only move Human Servers!");
        if (inTraining != null && LOGGER.isInfoEnabled()) {
            LOGGER.info("You cannot move: {}. They are inTraining", inTraining.getColor().nameWithColor());
        }
        LOGGER.info("Example BLUE>YELLOW  will move the Blue HUMAN SERVER to the Yellow Workstation");
        LOGGER.info("Type 'exit' when you are finished with your moves");

        while (true) {
            LOGGER.info("Next move: ");
            String wholeLine = scanner.nextLine();
            if (wholeLine.equalsIgnoreCase("exit")) {
                break;
            }
            if (testServerMoveRegex(wholeLine)) {
                String[] arguments = wholeLine.split(">");
                String serverToMove = arguments[0].toUpperCase(Locale.ROOT);
                if (inTraining == null) {
                    moves.add(new ServerMove(serverToMove, arguments[1].toUpperCase(Locale.ROOT)));
                } else {
                    if (Color.valueOf(serverToMove) != inTraining.getColor()) {
                        moves.add(new ServerMove(serverToMove, arguments[1].toUpperCase(Locale.ROOT)));
                    }
                }
                LOGGER.info("Registered move: {}", wholeLine);
            } else {
                LOGGER.info("Please use the COLOR>COLOR format");
                LOGGER.info("For example BLUE>YELLOW. Valid colors are GREEN|BLUE|ROSE|VIOLET|YELLOW");
            }
        }
        return moves;
    }

    public static boolean promptForServerRetry( @NonNull Server server) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ThroughputApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Your first try failed for Server {}", server.getColor().nameWithColor());
            LOGGER.info("You have a Retry card, would you like to use it? 'Y/N'");
        }

        String retry = scanner.nextLine();
        return retry.equalsIgnoreCase("Y");
    }

    public static int promptForWorkItemEstimates(int week, @NonNull ScoreCard scoreCard, @NonNull Backlog backlog) throws IOException{


        FXMLLoader fxmlLoader = new FXMLLoader(ThroughputApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);

        LOGGER.info("Choose how many items you want to start with (or add to) in your backlog ");
        LOGGER.info("'10' will add 10 work items to the backlog");

        int start = scanner.nextInt();


        backlog.addToBacklog(start);
        scanner.nextLine();
        return start;

    }

    public static int promptForWorkItemInitialMoves(int startValue, @NonNull Backlog backlog) throws IOException{
        FXMLLoader fxmlLoader = new FXMLLoader(ThroughputApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);

        if (backlog.getBacklogItemCount() == 0) {
            LOGGER.info("Backlog is empty, Team Mood is ignored");
            return 0;
        }
        LOGGER.info("Choose how many items to move to your 1st workstation from the backlog ->  At prompting please enter any number <= {}", startValue);
        LOGGER.info("'{}' will move {} work items to the 1st workstation", startValue, startValue);
        int moves = scanner.nextInt();
        scanner.nextLine();
        return moves;
    }

    public static int promptForWorkItemWorkstationMoves( @NonNull Workstation workstation, int workstationPosition) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ThroughputApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);

        if (LOGGER.isInfoEnabled()) {
            if (workstation.getWorkItemCount() == 0) {
                LOGGER.info(WORKSTATION_IS_EMPTY_NO_MOVES_ARE_POSSIBLE, workstation.getColor().nameWithColor(), ANSI_RED, ANSI_RESET);
                return 0;
            }
            final int maxIntToMove = Math.min(workstation.getCapacity(), workstation.getWorkItemCount());

            LOGGER.info("Choose how many items to move from the {} workstation to the next ->  At prompting please enter any number <= {}", workstation.getColor().nameWithColor(), maxIntToMove);

            if (workstationPosition == 4) {
                LOGGER.info("{} will move {} work items to finished goods", maxIntToMove, maxIntToMove);
            } else {
                LOGGER.info("{} will move {} work items to the next workstation", maxIntToMove, maxIntToMove);
            }

            int moves = scanner.nextInt();
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }
            return moves;
        }
        return -1;
    }

    public static void promptToAddOneToWorkstationCapacity( int dieSides) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(ThroughputApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);

        LOGGER.info("Choose which workstation to add one to its capacity");
        LOGGER.info("Example: BLUE will augment the BLUE workstation from {} to {}", Objects.requireNonNull(WorkstationService.getWorkstation(Color.BLUE)).getCapacity(), Math.min(Objects.requireNonNull(WorkstationService.getWorkstation(Color.BLUE)).getCapacity() + 1, dieSides));

        String workstationColorToAddCapacity = scanner.next(colorPattern);

        Color color = Color.valueOf(workstationColorToAddCapacity.toUpperCase());
        Objects.requireNonNull(WorkstationService.getWorkstation(color)).setCapacity(Math.min(Objects.requireNonNull(WorkstationService.getWorkstation(color)).getCapacity() + 1, dieSides));

        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }


    }

    public static HumanServer promptToAddSkill() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ThroughputApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);

        SkillCard skillCard = (SkillCard) CardService.getInstance().pickACard(Card.SKILLS);
        assert skillCard != null;
        LOGGER.info("Skill Card {}", skillCard);
        if (!skillCard.isSuccess()) {
            return null;
        }

        LOGGER.info(ANSI_CYAN + "Choose which server to add a skill and which skill to add" + ANSI_RESET);
        LOGGER.info("At prompting please enter: Server Color>Skill Color");
        LOGGER.info(ANSI_CYAN + "You can only assign to Human Servers!" + ANSI_RESET);
        LOGGER.info("Example BLUE>YELLOW  will assign the BLUE HUMAN SERVER a YELLOW skill");


        String wholeLine = scanner.nextLine();
        LOGGER.info("WHOLE LINE: {}", wholeLine);
        if (testServerMoveRegex(wholeLine)) {
            String[] arguments = wholeLine.split(">");
            HumanServer skillReceiver = ServerService.getHumanServer(Color.valueOf(arguments[0].toUpperCase()));
            skillReceiver.getSkills().add(Color.valueOf(arguments[1].toUpperCase()));
            return skillReceiver;
        } else {
            if (wholeLine.equalsIgnoreCase("none")) {
                return null;
            }
            LOGGER.info("Please use the COLOR>COLOR format");
            LOGGER.info("For example BLUE>YELLOW. Valid colors are GREEN|BLUE|ROSE|VIOLET|YELLOW");
            return promptToAddSkill(scanner);
        }


    }

    public static void promptToAutomateWorkstation() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ThroughputApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Choose which color workstation to add automation {}|{}|{}", Color.GREEN.nameWithColor(), Color.ROSE.nameWithColor(), Color.YELLOW.nameWithColor());
            LOGGER.info("Example: GREEN will will automate the GREEN workstation. The human server will remain until moved");
        }

        String workstationColorToAddAutomation = scanner.next(robotColorPattern);
        WorkstationService.automateWorkstation(Color.valueOf(workstationColorToAddAutomation.toUpperCase()));

        if (scanner.hasNext()) {
            scanner.nextLine();
        }

    }

    public static void promptToDoubleWorkstationCapacity( int dieSides) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ThroughputApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);

        LOGGER.info("Choose which workstation to double its capacity");
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Example: BLUE will augment the BLUE workstation from {} to {}", Objects.requireNonNull(WorkstationService.getWorkstation(Color.BLUE)).getCapacity(), Math.min(Objects.requireNonNull(WorkstationService.getWorkstation(Color.BLUE)).getCapacity() * 2, dieSides));
        }

        String workstationColorToAddCapacity = scanner.next();

        Color color = Color.valueOf(workstationColorToAddCapacity.toUpperCase());
        Objects.requireNonNull(WorkstationService.getWorkstation(color)).setCapacity(Math.min(Objects.requireNonNull(WorkstationService.getWorkstation(color)).getCapacity() * 2, dieSides));

        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }

    }

    public static void publishDayStart(int runDay, int runWeek) throws IOException{
        FXMLLoader fxmlLoader = new FXMLLoader(ThroughputApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        LOGGER.info("");
        LOGGER.info("");
        LOGGER.info("");
        LOGGER.info("Current Board -> Day: {}  Week:  {}", (runDay + 1), (runWeek + 1));
    }


    public static void publishStartWeek(int week) throws IOException{
        FXMLLoader fxmlLoader = new FXMLLoader(ThroughputApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        LOGGER.info("Week: {}", (week + 1));
    }

    public static void publishEndWeek(int week,@NonNull ScoreCard scoreCard) throws IOException{
        FXMLLoader fxmlLoader = new FXMLLoader(ThroughputApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);

        LOGGER.info("Finished week: {}", (week + 1));
        LOGGER.info("Score: {}", scoreCard.getScore());
        LOGGER.info("");
        LOGGER.info("");
    }


    public static boolean serverChanceCardPlay(@NonNull Server server, @NonNull Workstation workstation) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ThroughputApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);

        if (workstation.getWorkItemCount() == 0) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(WORKSTATION_IS_EMPTY_NO_MOVES_ARE_POSSIBLE, workstation.getColor().nameWithColor(), ANSI_RED, ANSI_RESET);
            }
            return false;
        }
        //Draw chance card
        ChanceCard chanceCard;
        if (server instanceof AutomatedServer) {
            chanceCard = (ChanceRobotCard) CardService.getInstance().pickACard(Card.AUTOMATED_CHANCE);
        } else {
            chanceCard = (ChanceCard) CardService.getInstance().pickACard(Card.CHANCE);
        }
        assert chanceCard != null;
        //If chance card successful prompt for move count up to capacity - depending on available work items

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Chance for: {}", workstation.getColor().nameWithColor());
            LOGGER.info(chanceCard.toString());
        }
        return chanceCard.isSuccess();
    }



    public static  int teamMood(int dieSides, @NonNull Backlog backlog) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ThroughputApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);

        LOGGER.info("Rolling for Team Mood");
        int teamMood = DiceService.rollDie(DiceService.getDie(dieSides)).getValue();
        LOGGER.info("Team Mood is {} and backlog has {} work items", teamMood, backlog.getBacklogItemCount());
        LOGGER.info("At the prompt you can move up to {} items into your 1st workstation", Math.min(teamMood, backlog.getBacklogItemCount()));
        return Math.min(teamMood, backlog.getBacklogItemCount());
    }

    private static boolean testServerMoveRegex(final String input) {
        // Compile regular expression
        final Pattern pattern = Pattern.compile("^\\b(GREEN|BLUE|ROSE|VIOLET|YELLOW)\\b\\s*+>\\s*\\b(GREEN|BLUE|ROSE|VIOLET|YELLOW)\\b+$", Pattern.CASE_INSENSITIVE);
        // Match regex against input
        final Matcher matcher = pattern.matcher(input);
        // Use results...
        return matcher.matches();
    }



}
