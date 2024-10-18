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
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;


public class Prompts {
    public static final Logger LOGGER = LoggerFactory.getLogger(Prompts.class.getName());
    private static final String WORKSTATION_IS_EMPTY_NO_MOVES_ARE_POSSIBLE = "Workstation is empty, No moves are possible";


    private Prompts() {
        super();
    }


    public static BitCard drawBit(@NonNull Pane container, int dieSides) throws IOException {
        //If not week 1 draw BIT card if they roll a 6
        int drawBitInt = DiceService.rollDie(DiceService.getDie(dieSides)).getValue();
        //todo set this back to max die sides
        if (drawBitInt > 2) {

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Follow the instructions on the card.", ButtonType.OK);
            alert.setHeaderText("Draw a " + Card.BOOSTER_INOCULATE_TRAP + " card!");
            alert.setTitle(Card.BOOSTER_INOCULATE_TRAP);
            alert.showAndWait();

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

            ImageView backImageView = ((BITController) loader.getController()).getCardBackImage();
            Image backImage = new Image(Objects.requireNonNull(Prompts.class.getResource("cards/BIT.jpg")).openStream());
            backImageView.setImage(backImage);
            // Create an image view with the desired image

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


            //Follow the instructions on BIT card. If we get a hold card put in weekly hold or game hold.
            //Execute work item traps immediately, otherwise return traps for future execution.
            return bitCard;
            //Execute do-over hold cards -- Look for mitigating hold cards
        }
        return null;
    }


    public static void implementPairedProgramming(@NonNull Pane container) throws IOException {

        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("implement-pairs.fxml"));
        Parent root = loader.load();

        String implementPairsText = "Choose which color workstation to pair. Requires a HUMAN server on the same workstation to work" +
                "  Pair Partner gives an additional chance for a successful day";

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


    public static void promptForAppliedTrap(Trap trap, boolean isMitigated) {

        String builder;
        if (isMitigated) {
            builder = "Applying Mitigation for trap" +
                    "  " + trap.getEffected() + " Loses " + trap.getMitigatedDuration();
        } else {
            builder = "No Mitigation available for trap" +
                    "  " + trap.getEffected() + " Loses " + trap.getDuration();
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION, builder, ButtonType.OK);
        alert.setTitle("TRAP!");
        alert.showAndWait();

    }


    public static void promptForServerMoves(@NonNull Pane container, HumanServer inTraining) throws IOException {

        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(Prompts.class.getResource("server-moves.fxml"));
        Parent root = loader.load();

        StringBuilder builder = new StringBuilder("SERVER MOVES!! ->  At prompting please enter: Server Color > Workstation Color.  Workstation is the receiving workstation - Worker must possess the skill (color) of the receiving workstation. You can only move Human Servers!");
        if (inTraining != null && LOGGER.isInfoEnabled()) {
            builder.append("You cannot move: ").append(inTraining.getColor().name()).append(" They are inTraining");
        }
        builder.append("Example BLUE>YELLOW  will move the Blue HUMAN SERVER to the Yellow Workstation");
        builder.append("Type 'exit' when you are finished with your moves");

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
        Optional<ButtonType> result = alert.showAndWait();

        ButtonType button = result.orElse(null);
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

    public static void promptForWorkItemInitialMoves(@NonNull Pane container, int startValue, int backlogCount) throws IOException {

        if (backlogCount <= 0) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Backlog moves");
            alert.setHeaderText("Backlog is Empty");
            alert.setContentText("Team Mood is ignored");
            alert.showAndWait();
        } else {

            Stage stage = new Stage();
            Parent root = new FXMLLoader(ThroughputApplication.class.getResource("move-initial-work-item.fxml")).load();

            String builder = "Choose how many items to move to your 1st workstation from the backlog ->  At prompting please enter any number <= " + startValue +
                    "   '" + startValue + "' will move " + startValue + " work items to the 1st workstation";
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


    }

    public static void promptForWorkItemWorkstationMoves(@NonNull Pane container, int workstationPosition) throws IOException {

       Workstation workstation =  WorkstationService.getWorkstation(workstationPosition);
        if (workstation.getWorkItemCount() == 0) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Moves");
            alert.setHeaderText("No Moves " + workstation.getColor());
            alert.setContentText(workstation.getColor() + WORKSTATION_IS_EMPTY_NO_MOVES_ARE_POSSIBLE);
            alert.showAndWait();
            return;
        }

        Stage stage = new Stage();
        Parent root = new FXMLLoader(ThroughputApplication.class.getResource("move-work-item.fxml")).load();

        StringBuilder builder = new StringBuilder();
        final int maxIntToMove = Math.min(workstation.getCapacity(), workstation.getWorkItemCount());
        builder.append("Choose how many items to move from the ").append(workstation.getColor().name()).append(" workstation to the next ->  At prompting please enter any number <= '").append(maxIntToMove);

        if (workstationPosition == 4) {
            builder.append(" '").append(maxIntToMove).append("' will move '").append(maxIntToMove).append("' work items to finished goods");
        } else {
            builder.append(" '").append(maxIntToMove).append("' will move '").append(maxIntToMove).append("' work items to the next workstation");
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


    public static boolean promptToDrawSkillsCard() throws IOException {

        SkillCard skillCard = (SkillCard) CardService.pickACard(Card.SKILLS);
        Alert alert = new Alert(Alert.AlertType.INFORMATION, skillCard.getInstructions(), ButtonType.YES, ButtonType.NO);
        alert.setTitle("Skills Card");
        ImageView imageView = new ImageView(new Image(Objects.requireNonNull(ThroughputApplication.class.getResource("cards/SkillTraining.jpg")).openStream()));
        imageView.setFitHeight(50);
        imageView.setPreserveRatio(true);
        alert.setGraphic(imageView);
        Optional<ButtonType> result = alert.showAndWait();

        ButtonType button = result.orElse(null);
        return button == null || button == ButtonType.YES;
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

    public static void promptToAutomateWorkstation(@NonNull Pane container) throws IOException {

        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("add-automation.fxml"));
        Parent root = loader.load();

        String builder = "Choose which color workstation to add automation " + Color.GREEN.name() + "|" + Color.ROSE.name() + "|" + Color.YELLOW.name() +
                "/n Example: GREEN will will automate the GREEN workstation. The human server will remain until moved";

        stage.setScene(new Scene(root));

        TextArea node = (TextArea) root.getScene().lookup("#addAutomationText");
        node.setText(builder);

        ComboBox<Color> serverWorkstationColorPicker = ((AddAutomationController) loader.getController()).getWorkstationToAddAutomation();
        buildColorCombobox(serverWorkstationColorPicker, Color.automatedColorValues());

        stage.setTitle("Automate Workstation");
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


    public static void publishDayStart(int runDay, int runWeek) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Start of Day");
        alert.setHeaderText("Start of Day");
        alert.setContentText("Current Board -> Day: " + (runDay + 1) + "  Week:  " + (runWeek + 1));
        alert.showAndWait();

    }


    public static void publishStartWeek(int week) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Start of Week");
        alert.setHeaderText("Start of Week");
        alert.setContentText("Week: " + (week + 1));
        alert.showAndWait();

    }

    public static void publishEndWeek() {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("End of Week");
        alert.setContentText("Prepare for the start of the next week");
        alert.showAndWait();


    }

    public static void publishEndOfGame() {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("End of Game");
        alert.setContentText("Assess and discuss how you did");
        alert.showAndWait();

    }


    public static ChanceResult serverChanceCardPlay(@NonNull Server server, int position) throws IOException {

        Workstation workstation = WorkstationService.getWorkstation(position);
        if (workstation.getWorkItemCount() == 0) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, WORKSTATION_IS_EMPTY_NO_MOVES_ARE_POSSIBLE, ButtonType.OK);
            alert.setHeaderText(workstation.getColor().name());
            alert.setTitle("Chance Card");
            alert.showAndWait();

            return ChanceResult.EMPTY;
        }

        ChanceCard chanceCard;
        ImageView imageView;
        if (server instanceof AutomatedServer) {
            chanceCard = (ChanceRobotCard) CardService.pickACard(Card.AUTOMATED_CHANCE);
            imageView = new ImageView(new Image(Objects.requireNonNull(ThroughputApplication.class.getResource("cards/ChanceTheRobot.jpg")).openStream()));
        } else {
            chanceCard = (ChanceCard) CardService.pickACard(Card.CHANCE);
            imageView = new ImageView(new Image(Objects.requireNonNull(ThroughputApplication.class.getResource("cards/Chance.jpg")).openStream()));
        }
        imageView.setFitHeight(50);
        imageView.setPreserveRatio(true);
        Objects.requireNonNull(chanceCard);
        Alert alert = new Alert(Alert.AlertType.INFORMATION, chanceCard.getInstructions(), ButtonType.OK);
        alert.setTitle("Chance Card");
        alert.setHeaderText(workstation.getColor().name() + " - " + chanceCard.getChanceText());
        alert.setGraphic(imageView);
        alert.showAndWait();

        return chanceCard.isSuccess() ? ChanceResult.SUCCESS : ChanceResult.FAILED;

    }


    public static int teamMood(int dieSides) {

        int teamMood = DiceService.rollDie(DiceService.getDie(dieSides)).getValue();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Team Mood");
        alert.setHeaderText("Rolled for Team Mood");
        alert.setContentText("Team Mood is " + teamMood + " and backlog has " + ScorecardService.getBacklog().getBacklogItemCount() + " work items" + " At the prompt you can move up to " + Math.min(teamMood, ScorecardService.getBacklog().getBacklogItemCount()) + " items into your 1st workstation");
        alert.showAndWait();

        return Math.min(teamMood, ScorecardService.getBacklog().getBacklogItemCount());
    }

    public static void promptForFinishedGoodsAreNowFourPoints() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Boost");
        alert.setHeaderText("Augmenting finished goods");
        alert.setContentText("Their value is 4 pts for the remainder of the game");
        alert.showAndWait();
        ScorecardService.getFinishedGoods().setValue(4);
    }


}
