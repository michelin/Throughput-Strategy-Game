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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
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


public class Prompts {
    public static final Logger LOGGER = LoggerFactory.getLogger(Prompts.class.getName());
    private static final String WORKSTATION_IS_EMPTY_NO_MOVES_ARE_POSSIBLE = "Workstation is empty, No moves are possible";


    private Prompts() {
        super();
    }


    public static BitCard drawBit(@NonNull Pane container, int dieSides, @NonNull TextArea gameBoardLog) throws IOException {
        //If not week 1 draw BIT card if they roll a 6
        int drawBitInt = DiceService.rollDie(DiceService.getDie(dieSides)).getValue();

        if (drawBitInt == Board.SIX_SIDES) {
            gameBoardLog.setText("Draw a " + Card.BOOSTER_INOCULATE_TRAP + " card!" + System.lineSeparator() + "Follow the instructions on the card.");
            gameBoardLog.getBackground().getImages().add(new BackgroundImage(new Image(Objects.requireNonNull(Prompts.class.getResource("icons/die_6.png")).openStream()), BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT));


            BitCard bitCard = (BitCard) CardService.pickACardDestructively(Card.BOOSTER_INOCULATE_TRAP);
            Objects.requireNonNull(bitCard);

            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(Prompts.class.getResource("bit-card.fxml"));
            Parent root = loader.load();

            stage.setScene(new Scene(root));

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
            stage.setTitle("Booster, Inoculation, Trap");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(container.getScene().getWindow());
            stage.showAndWait();

            gameBoardLog.getBackground().getImages().clear();
            //Follow the instructions on BIT card. If we get a hold card put in weekly hold or game hold.
            //Execute work item traps immediately, otherwise return traps for future execution.
            return bitCard;
            //Execute do-over hold cards -- Look for mitigating hold cards
        }
        return null;
    }


    public static void implementPairedProgramming(@NonNull Pane container, @NonNull TextArea gameBoardLog) throws IOException {

        if (WorkstationService.findIfPairPartnerIsAlreadyAssigned()) {
            gameBoardLog.setText(
                    "Partner assigned already" + System.lineSeparator() +
                            "You can move the pair partner at the start of day");

            return;
        }

        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("implement-pairs.fxml"));
        Parent root = loader.load();

        String implementPairsText = "Choose which color workstation to pair. Requires a HUMAN server on the same workstation to work" +
                System.lineSeparator() + "Pair Partner gives an additional chance for a successful day";

        stage.setScene(new Scene(root));
        TextArea node = (TextArea) root.getScene().lookup("#implementPairsText");
        node.setText(implementPairsText);

        ComboBox<Color> serverColorPicker = ((PairingController) loader.getController()).getWorkstationToPairWith();
        buildColorCombobox(serverColorPicker, Arrays.stream(WorkstationService.getWorkstations()).filter(Workstation::hasHumanServers).map(Workstation::getColor).toArray(Color[]::new));

        stage.setTitle("Paired Work");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(container.getScene().getWindow());
        stage.showAndWait();
    }

    private static void buildColorCombobox(ComboBox<Color> colorPicker, Color[] es) {
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
        gameBoardLog.setText("Augmenting finished goods" + System.lineSeparator() + "Their value is 4 pts for the remainder of the game");
        ScorecardService.getFinishedGoods().setValue(4);
    }

    public static void promptForPairRetry(@NonNull Server server, @NonNull TextArea gameBoardLog) {
        gameBoardLog.setText("Your first try failed for Server " + server.getColor().name() + System.lineSeparator() + "Partner steps in to help with a retry");
    }

    public static void promptForServerMoves(@NonNull Pane container, HumanServer inTraining) throws IOException {

        Stage stage = new Stage();
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

        stage.setScene(new Scene(root));
        TextArea node = (TextArea) root.getScene().lookup("#serverMovesText");
        node.setText(builder.toString());


        ComboBox<Color> serverColorPicker = ((ServerMovesController) loader.getController()).getServerToMove();
        buildColorCombobox(serverColorPicker, Color.humanColorValues());

        ComboBox<Color> workstationColorPicker = ((ServerMovesController) loader.getController()).getWorkstationToMoveTo();
        buildColorCombobox(workstationColorPicker, Color.humanColorValues());

        stage.setTitle("Server Moves");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(container.getScene().getWindow());
        stage.showAndWait();

    }

    public static boolean promptForServerRetry(@NonNull Server server) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Your first try failed for Server " + server.getColor().name() + " You have a Retry card, would you like to use it? 'Y/N'", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Retry");
        ButtonType button = alert.showAndWait().orElse(null);

        return button == null || button == ButtonType.YES;

    }

    public static void promptForWorkItemEstimates(@NonNull Pane container) throws IOException {
        Stage stage = new Stage();
        Parent root = new FXMLLoader(ThroughputApplication.class.getResource("submit-estimate.fxml")).load();
        stage.setScene(new Scene(root));
        stage.setTitle("Estimate your Week");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(container.getScene().getWindow());
        stage.showAndWait();

    }

    public static void promptForWorkItemInitialMoves(@NonNull Pane container, int startValue, int backlogCount, @NonNull TextArea gameBoardLog) throws IOException {

        if (backlogCount <= 0) {
            gameBoardLog.setText("Backlog is Empty" + System.lineSeparator() + "Team Mood is ignored");
            return;
        }

            Stage stage = new Stage();
            Parent root = new FXMLLoader(ThroughputApplication.class.getResource("move-initial-work-item.fxml")).load();

            String builder = "Choose how many items to move to your 1st workstation from the backlog" + System.lineSeparator() + "At prompting please enter any number <= " + startValue + "." +
                    System.lineSeparator() + "Example:   '" + startValue + "' will move " + startValue + " work items to the 1st workstation";
            stage.setScene(new Scene(root));
            TextArea node = (TextArea) root.getScene().lookup("#workItemMoveText");
            node.setText(builder);

            TextField workItemResponseText = (TextField) root.getScene().lookup("#workItemMoveResponseText");
            workItemResponseText.setText(String.valueOf(startValue));

            stage.setTitle("Move Items");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(container.getScene().getWindow());
            stage.showAndWait();

    }

    public static void promptForWorkItemWorkstationMoves(@NonNull Pane container, int workstationPosition, @NonNull TextArea gameBoardLog) throws IOException {

        Workstation workstation = WorkstationService.getWorkstation(workstationPosition);
        if (workstation.getWorkItemCount() == 0) {
            gameBoardLog.setText("No Moves" + System.lineSeparator() + workstation.getColor() + " " + WORKSTATION_IS_EMPTY_NO_MOVES_ARE_POSSIBLE);
            return;
        }

        Stage stage = new Stage();
        Parent root = new FXMLLoader(ThroughputApplication.class.getResource("move-work-item.fxml")).load();

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

        stage.setScene(new Scene(root));
        TextArea node = (TextArea) root.getScene().lookup("#workItemMoveText");
        node.setText(builder.toString());

        Text workstationPositionText = (Text) root.getScene().lookup("#txtWorkstationPosition");
        workstationPositionText.setText(String.valueOf(workstationPosition));

        TextField workItemResponseText = (TextField) root.getScene().lookup("#workItemMoveResponseText");
        workItemResponseText.setText(String.valueOf(maxIntToMove));


        stage.setTitle("Move Items");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(container.getScene().getWindow());
        stage.showAndWait();
    }

    public static void promptToAddSkill(@NonNull Pane container) throws IOException {

        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("add-skills.fxml"));
        Parent root = loader.load();

        String builder = "Choose which server to add a skill and which skill to add. " + System.lineSeparator() +
                " At prompting please enter: Server Color>Skill Color." +
                " You can only assign to Human Servers!" + System.lineSeparator() +
                " Example BLUE>YELLOW  will assign the BLUE HUMAN SERVER a YELLOW skill";

        stage.setScene(new Scene(root));

        TextArea node = (TextArea) root.getScene().lookup("#skillAddText");
        node.setText(builder);

        ComboBox<Color> serverColorPicker = ((SkillsController) loader.getController()).getServerToAddSkills();
        buildColorCombobox(serverColorPicker, Color.humanColorValues());


        ComboBox<Color> skillColorPicker = ((SkillsController) loader.getController()).getSkillsToAddToServer();
        buildColorCombobox(skillColorPicker, Color.humanColorValues());

        stage.setTitle("Add Skills to Server");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(container.getScene().getWindow());
        stage.showAndWait();


    }

    public static void promptToAugmentWorkstationCapacity(@NonNull Pane container, boolean timesTwo) throws IOException {

        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("add-capacity.fxml"));
        Parent root = loader.load();

        Workstation workstationBlue = Objects.requireNonNull(WorkstationService.getWorkstation(Color.BLUE));
        StringBuilder builder = getStringBuilder(timesTwo, workstationBlue);

        stage.setScene(new Scene(root));

        TextArea node = (TextArea) root.getScene().lookup("#addCapacityText");
        node.setText(builder.toString());

        ComboBox<Color> workstationToAddCapacity = ((AddedCapacityController) loader.getController()).getWorkstationToAddCapacity();
        buildColorCombobox(workstationToAddCapacity, Color.humanColorValues());

        stage.setTitle("Add Capacity");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(container.getScene().getWindow());
        stage.showAndWait();
    }

    private static StringBuilder getStringBuilder(boolean timesTwo, Workstation workstationBlue) {
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
        return builder;
    }

    public static void promptToAutomateWorkstation(@NonNull Pane container, @NonNull TextArea gameBoardLog) throws IOException {

        if (WorkstationService.findDeployedAutomatedServers().size() == 3) {
            gameBoardLog.setText("No Robots Left!" + System.lineSeparator() + "There are no workstations available to automate");
            return;
        }

        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("add-automation.fxml"));
        Parent root = loader.load();

        String builder = "Choose which color workstation to add automation " + Color.GREEN.name() + "|" + Color.ROSE.name() + "|" + Color.YELLOW.name() +
                "/n Example: GREEN will will automate the GREEN workstation. The human server will remain until moved";

        stage.setScene(new Scene(root));

        TextArea node = (TextArea) root.getScene().lookup("#addAutomationText");
        node.setText(builder);

        ComboBox<Color> serverWorkstationColorPicker = ((AddAutomationController) loader.getController()).getWorkstationToAddAutomation();
        List<Color> serverColors = WorkstationService.findDeployedAutomatedServers().stream().map(Server::getColor).collect(Collectors.toList());
        List<Color> leftoverColors = Arrays.stream(Color.automatedColorValues()).filter(color -> !serverColors.contains(color)).collect(Collectors.toList());
        buildColorCombobox(serverWorkstationColorPicker, leftoverColors.toArray(Color[]::new));

        stage.setTitle("Automate Workstation");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(container.getScene().getWindow());
        stage.showAndWait();


    }

    public static boolean promptToDrawSkillsCard(@NonNull Pane container) throws IOException {

        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("skill-card.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root));

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

        stage.setTitle("Click to Add Skill");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(container.getScene().getWindow());
        stage.showAndWait();

        return addSkill.get();
    }

    public static void publishDayStart(TextArea gameBoardLog) {
        String gameBoardLogText = "Day: " + Board.getDayOfTheWeek() + "  Week:  " + Board.getGameWeek() + System.lineSeparator() + "Click on Run Day to start the day";
        gameBoardLog.setText(gameBoardLogText);
    }

    public static void publishEndOfGame(TextArea gameBoardLog) {
        String gameBoardLogText = "End of Game" + System.lineSeparator() + "Assess and discuss how you did";
        gameBoardLog.setText(gameBoardLogText);
    }

    public static void publishEndWeek(TextArea gameBoardLog) {
        String gameBoardLogText = "End of Week" + System.lineSeparator() + "Prepare for the start of the next week";
        gameBoardLog.setText(gameBoardLogText);
    }

    public static void publishStartWeek(TextArea gameBoardLog) {
        String gameBoardLogText = "Week: " + Board.getGameWeek() + System.lineSeparator() + "Click on Run Week to start the week";
        gameBoardLog.setText(gameBoardLogText);
    }

    public static ChanceResult serverChanceCardPlay(@NonNull Pane container, @NonNull Server server, int position, @NonNull TextArea gameBoardLog) throws IOException {

        Workstation workstation = WorkstationService.getWorkstation(position);
        if (workstation.getWorkItemCount() == 0) {
            gameBoardLog.setText("No Moves" + System.lineSeparator() + workstation.getColor() + " " + WORKSTATION_IS_EMPTY_NO_MOVES_ARE_POSSIBLE);
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

        Stage stage = new Stage();
        Parent root = loader.load();
        stage.setScene(new Scene(root));

        ((ChanceController) loader.getController()).getCardInstructions().setText(chanceCard.getInstructions());
        ((ChanceController) loader.getController()).getCardChance().setText(workstation.getColor().name() + " - " + chanceCard.getChanceText());

        stage.setTitle("Chance");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(container.getScene().getWindow());

        stage.showAndWait();

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
        stage.showAndWait();
    }

    public static void showRulesCard(Pane container) throws IOException {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("rules.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.setTitle("Game Rules");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(container.getScene().getWindow());
        stage.showAndWait();
    }

    public static int teamMood(@NonNull Pane container, int dieSides) throws IOException {

        int teamMood = DiceService.rollDie(DiceService.getDie(dieSides)).getValue();


        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("die-roll.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root));

        String builder = "Team Mood is " + teamMood + " and backlog has " + ScorecardService.getBacklog().getBacklogItemCount() + " work items" + " At the prompt you can move up to " + Math.min(teamMood, ScorecardService.getBacklog().getBacklogItemCount()) + " items into your 1st workstation";

        Label dieText = (Label) root.getScene().lookup("#dieText");
        dieText.setText(builder);

        Label dieHeaderText = (Label) root.getScene().lookup("#dieHeaderText");
        dieHeaderText.setText("Rolled for Team Mood");

        ImageView backImageView = (ImageView) root.getScene().lookup("#dieImage");
        Image backImage = new Image(Objects.requireNonNull(Prompts.class.getResource(getDieImage(teamMood))).openStream());
        backImageView.setImage(backImage);

        stage.setTitle("Team Mood");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(container.getScene().getWindow());
        stage.showAndWait();

        return Math.min(teamMood, ScorecardService.getBacklog().getBacklogItemCount());
    }

    private static String getDieImage(int dieFace) {
        switch (dieFace) {
            case 2:
                return "icons/die_2.png";
            case 3:
                return "icons/die_3.png";
            case 4:
                return "icons/die_4.png";
            case 5:
                return "icons/die_5.png";
            case 6:
                return "icons/die_6.png";
            default:
                return "icons/die_1.png";

        }
    }


}
