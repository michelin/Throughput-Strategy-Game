package com.michelin.throughputfxproject;

import com.michelin.throughputfxproject.controllers.*;
import com.michelin.throughputfxproject.entities.Card;
import com.michelin.throughputfxproject.entities.Server;
import com.michelin.throughputfxproject.entities.Trap;
import com.michelin.throughputfxproject.entities.Workstation;
import com.michelin.throughputfxproject.entities.cards.BitCard;
import com.michelin.throughputfxproject.entities.cards.ChanceCard;
import com.michelin.throughputfxproject.entities.cards.ChanceRobotCard;
import com.michelin.throughputfxproject.entities.cards.SkillCard;
import com.michelin.throughputfxproject.entities.servers.AutomatedServer;
import com.michelin.throughputfxproject.entities.servers.HumanServer;
import com.michelin.throughputfxproject.exceptions.ThroughputRuntimeException;
import com.michelin.throughputfxproject.services.CardService;
import com.michelin.throughputfxproject.services.DiceService;
import com.michelin.throughputfxproject.services.ScorecardService;
import com.michelin.throughputfxproject.services.WorkstationService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.michelin.throughputfxproject.services.DiceService.getDieImage;


public class Prompts {
    public static final Logger LOGGER = LoggerFactory.getLogger(Prompts.class.getName());
    public static final String THROUGHPUT = "Throughput";
    public static final String START_THE_WEEK = "Click on Run Week to start the week";
    private static final String WORKSTATION_IS_EMPTY_NO_MOVES_ARE_POSSIBLE = "Workstation is empty, No moves are possible";


    private Prompts() {
        super();
    }

    public static BitCard drawBit(@NonNull Pane container, int dieSides, @NonNull TextArea gameBoardLog) throws IOException {
        //If not week 1 draw BIT card if they roll a 6
        int drawBitInt = DiceService.rollDie(DiceService.getDie(dieSides)).getValue();

        String gameBoardLogText = "Draw a " + Card.BOOSTER_INOCULATE_TRAP + " card!" + System.lineSeparator() + "Follow the instructions on the card.";

        if (drawBitInt == Board.SIX_SIDES) {


            alertWithGameBoardUpdate("BIT", gameBoardLog, gameBoardLogText, 2);

            BitCard bitCard = (BitCard) CardService.pickACardDestructively(Card.BOOSTER_INOCULATE_TRAP);
            Objects.requireNonNull(bitCard);

            FXMLLoader loader = new FXMLLoader(Prompts.class.getResource("bit-card.fxml"));
            Parent root = loader.load();

            Label labelTitle = ((BITController) loader.getController()).getCardTitle();
            labelTitle.setText(bitCard.getTitle());

            Label subtitleLabel = ((BITController) loader.getController()).getCardSubtitle();
            subtitleLabel.setText(bitCard.getSubtitle());

            Label reasonLabel = ((BITController) loader.getController()).getCardReason();
            reasonLabel.setText(bitCard.getReason());

            Label instructionLabel = ((BITController) loader.getController()).getCardInstructions();
            instructionLabel.setText(bitCard.getInstructions());

            Label descriptionLabel = ((BITController) loader.getController()).getCardDescription();
            descriptionLabel.setText(bitCard.getDescription());

            Label descriptionTitleLabel = ((BITController) loader.getController()).getCardDescriptionTitle();
            descriptionTitleLabel.setText(bitCard.getDescriptionTitle());

            String descritionImgString = bitCard.getDescriptionImg();
            if (descritionImgString != null) {
                ImageView descriptionImageView = ((BITController) loader.getController()).getDescriptionImage();
                Image descriptionImg = new Image(Objects.requireNonNull(Prompts.class.getResource(descritionImgString)).openStream());
                descriptionImageView.setImage(descriptionImg);
            }

            // Create a popup and add the stack pane to it
            createModalStage("Booster, Inoculation, Trap", container, root, 5);

            gameBoardLog.getBackground().getImages().clear();
            //Follow the instructions on BIT card. If we get a hold card put in weekly hold or game hold.
            //Execute work item traps immediately, otherwise return traps for future execution.
            return bitCard;
            //Execute do-over hold cards -- Look for mitigating hold cards
        }
        return null;
    }

    private static void alertWithGameBoardUpdate(String title, @NonNull TextArea gameBoardLog, @NonNull String gameBoardLogText, int timeoutDuration) {
        Text alertText = new Text(gameBoardLogText);
        alertText.setWrappingWidth(100);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.getDialogPane().setContent(alertText);
        alert.getButtonTypes().set(0,ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);

        Timeline idleStage = new Timeline(new KeyFrame(Duration.seconds(timeoutDuration), event -> {
            gameBoardLog.setText(gameBoardLogText);
            alert.setResult(ButtonType.OK);
            alert.hide();
        }));
        idleStage.setCycleCount(1);
        idleStage.play();

        alert.showAndWait();
    }

    private static void createModalStage(@NonNull String title, @NonNull Pane container, @NonNull Parent root, int timeoutDuration) {
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(container.getScene().getWindow());

        Timeline idleStage = new Timeline(new KeyFrame(Duration.seconds(timeoutDuration), event -> stage.hide()));
        idleStage.setCycleCount(1);
        idleStage.play();

        stage.showAndWait();
    }

    public static void implementPairedProgramming(@NonNull Pane container, @NonNull TextArea gameBoardLog) throws IOException {

        if (WorkstationService.findIfPairPartnerIsAlreadyAssigned()) {
            gameBoardLog.setText(
                    "Partner assigned already" + System.lineSeparator() +
                            "You can move the pair partner at the start of day");
            return;
        }

        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("implement-pairs.fxml"));
        Parent root = loader.load();

        String implementPairsText = "Choose which color workstation to pair. Requires a HUMAN server on the same workstation to work" +
                System.lineSeparator() + "Pair Partner gives an additional chance for a successful day";

        TextArea node = (TextArea) root.getScene().lookup("#implementPairsText");
        node.setText(implementPairsText);

        ComboBox<Color> serverColorPicker = ((PairingController) loader.getController()).getWorkstationToPairWith();
        buildColorComboBox(serverColorPicker, Arrays.stream(WorkstationService.getWorkstations()).filter(Workstation::hasHumanServers).map(Workstation::getColor).toArray(Color[]::new));

        createModalStage("Paired Work", container, root, 10);
    }

    private static void buildColorComboBox(ComboBox<Color> colorPicker, Color[] es) {
        colorPicker.getItems().addAll(es);
        colorPicker.setCellFactory(listView -> new ListCell<>() {
            @Override
            public void updateItem(Color color, boolean empty) {
                super.updateItem(color, empty);
                setText(color == null ? "" : color.name());
                if (color != null) {
                    setBackground(new Background(new BackgroundFill(color.lookupFXColor(), null, null)));
                    setTextFill(color.lookupFontColor());
                }
            }
        });
        colorPicker.setConverter(new StringConverter<>() {
            @Override
            public String toString(Color color) {
                if (color == null) {
                    return "Choose Color";
                }
                return color.name();
            }

            @Override
            public Color fromString(String s) {
                try {
                    return Color.valueOf(s);
                } catch (IllegalArgumentException e) {
                    throw new ThroughputRuntimeException(e);
                }
            }
        });
    }

    public static void promptForAppliedTrap(Trap trap, boolean isMitigated, @NonNull TextArea gameBoardLog) {

        String builder;
        if (isMitigated) {
            builder = "Applying Mitigation for trap" +
                    "  " + trap.getEffected() + " Loses " + trap.getMitigatedDuration();
        } else {
            builder = "No Mitigation available for trap" +
                    "  " + trap.getEffected() + " Loses " + trap.getDuration();
        }
        gameBoardLog.setText(builder);
    }

    public static void promptForFinishedGoodsAreNowFourPoints(@NonNull TextArea gameBoardLog) {
        String gameBoardLogText = "Augmenting finished goods. Their value is 4 pts for the remainder of the game";
        alertWithGameBoardUpdate( gameBoardLog, gameBoardLogText, 2);
        ScorecardService.getFinishedGoods().setValue(4);
    }

    public static void promptForPairRetry(@NonNull Server server, @NonNull TextArea gameBoardLog) {
        String gameBoardLogText = "Your first try failed for Server " + server.getColor().name() + "Partner steps in to help with a retry";
        alertWithGameBoardUpdate("Partner", gameBoardLog, gameBoardLogText, 2);
    }

    public static void promptForServerMoves(@NonNull Pane container, HumanServer inTraining) throws IOException {

        FXMLLoader loader = new FXMLLoader(Prompts.class.getResource("server-moves.fxml"));
        Parent root = loader.load();

        StringBuilder builder = new StringBuilder("At prompting please enter: Server Color > Workstation Color."
                + System.lineSeparator() + "Workstation is the receiving workstation."
                + System.lineSeparator() + "Worker must possess the skill (color) of the receiving workstation."
                + System.lineSeparator() + "You can only move Human Servers!");
        if (inTraining != null && LOGGER.isInfoEnabled()) {
            builder.append(System.lineSeparator()).append("You cannot move: ").append(inTraining.getColor().name()).append(" They are inTraining");
        }
        builder.append(System.lineSeparator()).append("Example BLUE>YELLOW  will move the Blue HUMAN SERVER to the Yellow Workstation");

        TextArea node = (TextArea) root.getScene().lookup("#serverMovesText");
        node.setText(builder.toString());

        ComboBox<Color> serverColorPicker = ((ServerMovesController) loader.getController()).getServerToMove();
        buildColorComboBox(serverColorPicker, Color.humanColorValues());

        ComboBox<Color> workstationColorPicker = ((ServerMovesController) loader.getController()).getWorkstationToMoveTo();
        buildColorComboBox(workstationColorPicker, Color.humanColorValues());

        createModalStage("Server Moves", container, root, 15);

    }

    public static boolean promptForServerRetry(@NonNull Server server) {

        Text text = new Text("Your first try failed for Server " + server.getColor().name() + " You have a Retry card, would you like to use it? 'Y/N'");
        text.setWrappingWidth(100);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.getDialogPane().setContent(text);
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        alert.setTitle("Retry");
        alert.setHeaderText(null);

        Timeline idleStage = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            alert.setResult(ButtonType.YES);
            alert.hide();
        }));
        idleStage.setCycleCount(1);
        idleStage.play();

        ButtonType button = alert.showAndWait().orElse(null);

        return button == null || button == ButtonType.YES;

    }

    public static void promptForWorkItemEstimates(@NonNull Pane container) throws IOException {

        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("submit-estimate.fxml"));
        Parent root = loader.load();

        Button submitButton = ((EstimateController) loader.getController()).getEstimateButton();

        createModalStageWithButton("Estimate your Week", container, root, 15, submitButton);
    }

    private static void createModalStageWithButton(@NonNull String title, @NonNull Pane container, @NonNull Parent root, int timeoutDuration, Button button) {
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(container.getScene().getWindow());

        Timeline idleStage = new Timeline(new KeyFrame(Duration.seconds(timeoutDuration), event -> button.fire()));
        idleStage.setCycleCount(1);
        idleStage.play();

        stage.showAndWait();
    }

    public static void promptForWorkItemInitialMoves(@NonNull Pane container, int startValue, int backlogCount, @NonNull TextArea gameBoardLog) throws IOException {

        if (backlogCount <= 0) {
            String gameBoardLogText = "Backlog is Empty" + System.lineSeparator() + "Team Mood is ignored";
            alertWithGameBoardUpdate(gameBoardLog, gameBoardLogText, 3);
            return;
        }

        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("move-initial-work-item.fxml"));
        Parent root = loader.load();

        String builder = "Choose how many items to move to your 1st workstation from the backlog" + System.lineSeparator() + "At prompting please enter any number <= " + startValue + "." +
                System.lineSeparator() + "Example:   '" + startValue + "' will move " + startValue + " work items to the 1st workstation";

        TextArea node = ((InitialWorkItemsController) loader.getController()).getWorkItemMoveText();
        node.setText(builder);

        TextField workItemResponseText = ((InitialWorkItemsController) loader.getController()).getWorkItemMoveResponseText();
        workItemResponseText.setText(String.valueOf(startValue));

        Button submitButton = ((InitialWorkItemsController) loader.getController()).getWorkItemMoveButton();
        createModalStageWithButton("Move Items", container, root, 10, submitButton);
    }

    private static void alertWithGameBoardUpdate(@NonNull TextArea gameBoardLog, @NonNull String gameBoardLogText, int timeoutDuration) {
        alertWithGameBoardUpdate(THROUGHPUT, gameBoardLog, gameBoardLogText, timeoutDuration);
    }

    public static void promptForWorkItemWorkstationMoves(@NonNull Pane container, int workstationPosition) throws IOException {

        Workstation workstation = WorkstationService.getWorkstation(workstationPosition);

        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("move-work-item.fxml"));
        Parent root = loader.load();

        StringBuilder builder = new StringBuilder();
        final int maxIntToMove = Math.min(workstation.getCapacity(), workstation.getWorkItemCount());
        builder.append("Choose how many items to move from the ")
                .append(workstation.getColor().name())
                .append(" workstation to the next ->  At prompting please enter any number <= ")
                .append(maxIntToMove)
                .append(System.lineSeparator()).append("Example: '")
                .append(maxIntToMove)
                .append("' will move '")
                .append(maxIntToMove);

        if (workstationPosition == 4) {
            builder.append("' work items to finished goods");
        } else {
            builder.append("' work items to the next workstation");
        }

        TextArea node = ((WorkItemsController) loader.getController()).getWorkItemMoveText();
        node.setText(builder.toString());

        Text workstationPositionText = ((WorkItemsController) loader.getController()).getTxtWorkstationPosition();
        workstationPositionText.setText(String.valueOf(workstationPosition));

        TextField workItemResponseText = ((WorkItemsController) loader.getController()).getWorkItemMoveResponseText();
        workItemResponseText.setText(String.valueOf(maxIntToMove));

        Button submitButton = ((WorkItemsController) loader.getController()).getWorkItemMoveButton();
        createModalStageWithButton("Move Items", container, root, 10, submitButton);
    }

    public static void promptToAddSkill(@NonNull Pane container) throws IOException {

        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("add-skills.fxml"));
        Parent root = loader.load();

        String builder = "Choose which server to add a skill and which skill to add. " + System.lineSeparator() +
                " At prompting please enter: Server Color>Skill Color." +
                " You can only assign to Human Servers!" + System.lineSeparator() +
                " Example BLUE>YELLOW  will assign the BLUE HUMAN SERVER a YELLOW skill";

        TextArea node = ((SkillsController) loader.getController()).getSkillAddText();
        node.setText(builder);

        ComboBox<Color> serverColorPicker = ((SkillsController) loader.getController()).getServerToAddSkills();
        buildColorComboBox(serverColorPicker, Color.humanColorValues());

        ComboBox<Color> skillColorPicker = ((SkillsController) loader.getController()).getSkillsToAddToServer();
        buildColorComboBox(skillColorPicker, Color.humanColorValues());

        createModalStage("Add Skills to Server", container, root, 15);

    }

    public static void promptToAugmentWorkstationCapacity(@NonNull Pane container, boolean timesTwo) throws IOException {

        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("add-capacity.fxml"));
        Parent root = loader.load();

        Workstation workstationBlue = Objects.requireNonNull(WorkstationService.getWorkstation(Color.BLUE));

        TextArea node = ((AddedCapacityController) loader.getController()).getAddCapacityText();
        node.setText(getWorkstationCapacityText(timesTwo, workstationBlue));

        ComboBox<Color> workstationToAddCapacity = ((AddedCapacityController) loader.getController()).getWorkstationToAddCapacity();
        buildColorComboBox(workstationToAddCapacity, Color.humanColorValues());

        createModalStage("Add Capacity", container, root, 10);

    }

    private static String getWorkstationCapacityText(boolean timesTwo, Workstation workstationBlue) {
        StringBuilder builder = new StringBuilder();
        if (!timesTwo) {
            builder.append("Choose which workstation to add one to its capacity");
            builder.append("  Example: BLUE will augment the BLUE workstation from ");
            builder.append(workstationBlue.getCapacity());
            builder.append(" to ");
            builder.append(Math.min(workstationBlue.getCapacity() + 1, Board.SIX_SIDES));
        } else {
            builder.append("Choose which workstation to double its capacity");
            builder.append("  Example: BLUE will augment the BLUE workstation from ");
            builder.append(workstationBlue.getCapacity());
            builder.append(" to ");
            builder.append(Math.min(workstationBlue.getCapacity() + 1, Board.SIX_SIDES));
        }
        return builder.toString();
    }

    public static void promptToAutomateWorkstation(@NonNull Pane container, @NonNull TextArea gameBoardLog) throws IOException {

        if (WorkstationService.findDeployedAutomatedServers().size() == 3) {
            gameBoardLog.setText("No Robots Left!" + System.lineSeparator() + "There are no workstations available to automate");
            return;
        }

        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("add-automation.fxml"));
        Parent root = loader.load();

        String builder = "Choose which color workstation to add automation " + Color.GREEN.name() + "|" + Color.ROSE.name() + "|" + Color.YELLOW.name() +
                "/n Example: GREEN will will automate the GREEN workstation. The human server will remain until moved";

        TextArea node = ((AddAutomationController) loader.getController()).getAddAutomationText();
        node.setText(builder);

        ComboBox<Color> serverWorkstationColorPicker = ((AddAutomationController) loader.getController()).getWorkstationToAddAutomation();
        List<Color> serverColors = WorkstationService.findDeployedAutomatedServers().stream().map(Server::getColor).collect(Collectors.toList());
        List<Color> leftoverColors = Arrays.stream(Color.automatedColorValues()).filter(color -> !serverColors.contains(color)).collect(Collectors.toList());
        buildColorComboBox(serverWorkstationColorPicker, leftoverColors.toArray(Color[]::new));

        createModalStage("Automate Workstation", container, root, 10);
    }

    public static boolean promptToDrawSkillsCard(@NonNull Pane container) throws IOException {


        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("skill-card.fxml"));
        Parent root = loader.load();

        SkillCard skillCard = (SkillCard) CardService.pickACard(Card.SKILLS);
        ((SkillController) loader.getController()).getCardSkill().setText(skillCard.getSkill());
        ((SkillController) loader.getController()).getCardInstructions().setText(skillCard.getInstructions());
        ((SkillController) loader.getController()).getCardInstructionsExtended().setText(skillCard.getInstructionsExtended());

        AtomicBoolean addSkill = new AtomicBoolean(false);
        Button addButton = ((SkillController) loader.getController()).getSkillAddButton();
        addButton.setOnAction(event -> {
            addSkill.set(true);
            addButton.getParent().getScene().getWindow().hide();
        });

        createModalStageWithButton("Click to Add Skill", container, root, 5, addButton);

        return addSkill.get();
    }

    public static void promptToStartGame(@NonNull TextArea gameBoardLog) {
        String gameBoardLogText = "Week: " + Board.getGameWeek() + System.lineSeparator() +  START_THE_WEEK;
        alertWithGameBoardUpdate(gameBoardLog, gameBoardLogText, 5);
    }

    public static void publishDayStart(TextArea gameBoardLog) {
        String gameBoardLogText = "Day: " + Board.getDayOfTheWeek() + "  Week:  " + Board.getGameWeek() + System.lineSeparator() + "Click on Run Day to start the day";
        alertWithGameBoardUpdate(gameBoardLog, gameBoardLogText, 2);
    }

    public static void publishEndOfGame(TextArea gameBoardLog) {
        String gameBoardLogText = "End of Game." + System.lineSeparator() + "Assess and discuss how you did";
        alertWithGameBoardUpdate(gameBoardLog, gameBoardLogText, 5);
    }

    public static void publishEndWeek(TextArea gameBoardLog) {
        String gameBoardLogText = "End of Week." + System.lineSeparator() + "Prepare for the start of the next week";
        alertWithGameBoardUpdate(gameBoardLog, gameBoardLogText, 2);
    }

    public static void publishStartWeek(TextArea gameBoardLog) {
        String gameBoardLogText = "Week: " + Board.getGameWeek()  + START_THE_WEEK;
        alertWithGameBoardUpdate(gameBoardLog, gameBoardLogText, 2);

    }

    public static ChanceResult serverChanceCardPlay(@NonNull Pane container, @NonNull Server server, int position, @NonNull TextArea gameBoardLog) throws IOException {

        Workstation workstation = WorkstationService.getWorkstation(position);
        if (workstation.getWorkItemCount() == 0) {
            String gameBoardLogText = "No Moves." + System.lineSeparator() + workstation.getColor() + " " + WORKSTATION_IS_EMPTY_NO_MOVES_ARE_POSSIBLE;
            alertWithGameBoardUpdate(gameBoardLog, gameBoardLogText, 2);
            return ChanceResult.EMPTY;
        }

        ChanceCard chanceCard;
        FXMLLoader loader;
        if (server instanceof AutomatedServer) {
            chanceCard = (ChanceRobotCard) CardService.pickACard(Card.AUTOMATED_CHANCE);
            loader = new FXMLLoader(ThroughputApplication.class.getResource("chance-robot-card.fxml"));
        } else {
            chanceCard = (ChanceCard) CardService.pickACard(Card.CHANCE);
            loader = new FXMLLoader(ThroughputApplication.class.getResource("chance-card.fxml"));
        }

        Parent root = loader.load();

        ((ChanceController) loader.getController()).getCardInstructions().setText(chanceCard.getInstructions());
        ((ChanceController) loader.getController()).getCardChance().setText(workstation.getColor().name() + " - " + chanceCard.getChanceText());

        createModalStage("Chance", container, root, 3);

        return chanceCard.isSuccess() ? ChanceResult.SUCCESS : ChanceResult.FAILED;

    }

    public static void showInfoCard(Pane container) throws IOException {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("info.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.setTitle("Game Information");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(container.getScene().getWindow());
        stage.show();
    }

    public static void showRulesCard(Pane container) throws IOException {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("rules.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.setTitle("Game Rules");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(container.getScene().getWindow());
        stage.show();
    }

    public static int teamMood(@NonNull Pane container, int dieSides) throws IOException {

        int teamMood = DiceService.rollDie(DiceService.getDie(dieSides)).getValue();

        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("die-roll.fxml"));
        Parent root = loader.load();

        String builder = "Team Mood is " + teamMood + " and backlog has " + ScorecardService.getBacklog().getBacklogItemCount() + " work items" + " At the prompt you can move up to " + Math.min(teamMood, ScorecardService.getBacklog().getBacklogItemCount()) + " items into your 1st workstation";

        Label dieText = ((DieController) loader.getController()).getDieText();
        dieText.setText(builder);

        Label dieHeaderText = ((DieController) loader.getController()).getDieHeaderText();
        dieHeaderText.setText("Rolled for Team Mood");

        ImageView backImageView = ((DieController) loader.getController()).getDieImage();
        Image backImage = new Image(Objects.requireNonNull(Prompts.class.getResource(getDieImage(teamMood))).openStream());
        backImageView.setImage(backImage);

        createModalStage("Team Mood", container, root, 5);

        return Math.min(teamMood, ScorecardService.getBacklog().getBacklogItemCount());
    }

}
