package com.michelin.throughputfxproject;

import com.michelin.throughputfxproject.entities.*;
import com.michelin.throughputfxproject.entities.cards.BitCard;
import com.michelin.throughputfxproject.entities.cards.ChanceCard;
import com.michelin.throughputfxproject.entities.cards.ChanceRobotCard;
import com.michelin.throughputfxproject.entities.cards.SkillCard;
import com.michelin.throughputfxproject.entities.servers.AutomatedServer;
import com.michelin.throughputfxproject.entities.servers.HumanServer;
import com.michelin.throughputfxproject.services.CardService;
import com.michelin.throughputfxproject.services.DiceService;
import com.michelin.throughputfxproject.services.ScorecardService;
import com.michelin.throughputfxproject.services.WorkstationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


public class Prompts {
    public static final Logger LOGGER = LoggerFactory.getLogger(Prompts.class.getName());
    private static final String WORKSTATION_IS_EMPTY_NO_MOVES_ARE_POSSIBLE = "Workstation is empty, No moves are possible";


    private Prompts() {
        super();
    }


    public static BitCard drawBit(@NonNull Pane container, int dieSides) throws IOException {
        //If not week 1 draw BIT card if they roll a 6
        int drawBitInt = DiceService.rollDie(DiceService.getDie(dieSides)).getValue();
        if (drawBitInt == 6) {

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Draw a " + Card.BOOSTER_INOCULATE_TRAP + " card!", ButtonType.OK);
            alert.setTitle(Card.BOOSTER_INOCULATE_TRAP);
            alert.showAndWait();

            BitCard bitCard = (BitCard) CardService.getInstance().pickACardDestructively(Card.BOOSTER_INOCULATE_TRAP);
            Objects.requireNonNull(bitCard);

            // Create an image view with the desired image
            Image image = new Image(Objects.requireNonNull(CardPopup.class.getResource("cards/BIT.jpg")).openStream());
            ImageView imageView = new ImageView(image);

            // Set the image view's dimensions to 3.5 inches x 2.5 inches
            double width = 2.5 * 72; // Convert inches to pixels (assuming 72 DPI)
            double height = 3.5 * 72;
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
            imageView.setPreserveRatio(false);

            //todo - build the structure of the card
            bitCard.getDescriptionImg();
            VBox cardText = new VBox();
            cardText.getChildren().addAll(
                    new Label(bitCard.getTitle()),
                    new Label(bitCard.getSubtitle()),
                    new Label(bitCard.getReason()),
                    new Label(bitCard.getInstructions()),
                    new Label(bitCard.getDescriptionTitle()),
                    new Label(bitCard.getDescription()));

            Pane pane = new Pane(cardText);
            pane.setPrefSize(width, height);
            // Create a stack pane to hold the image view
            HBox hBox = new HBox(pane, imageView);

            // Create a popup and add the stack pane to it
            Popup popup = new Popup();
            popup.getContent().add(hBox);

            // Set the popup's size to match the image view
            popup.setWidth(width);
            popup.setHeight(height);

            // Show the popup
            popup.show(container.getScene().getWindow());

            //Follow the instructions on BIT card. If we get a hold card put in weekly hold or game hold.
            //Execute work item traps immediately, otherwise return traps for future execution.
            return bitCard;
            //Execute do-over hold cards -- Look for mitigating hold cards

        }
        return null;
    }


    public static void implementPairedProgramming(@NonNull Pane container) throws IOException {

        Stage stage = new Stage();
        Parent root = new FXMLLoader(ThroughputApplication.class.getResource("implement-pairs.fxml")).load();

        String implementPairsText = "Choose which color workstation to pair. Requires a HUMAN server on the same workstation to work" +
                "  Pair Partner gives an additional chance for a successful day";

        stage.setScene(new Scene(root));
        TextArea node = (TextArea) root.getScene().lookup("#implementPairsText");
        node.setText(implementPairsText);

        List<Color> listOfWorkstationsWithHumanServers = Arrays.stream(WorkstationService.getWorkstations()).filter(Workstation::hasHumanServers).map(Workstation::getColor).collect(Collectors.toList());
        ComboBox<Color> serverColorPicker = (ComboBox) root.getScene().lookup("#workstationToPairWith");
        buildColorCombobox(serverColorPicker, listOfWorkstationsWithHumanServers.toArray(new Color[0]));

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
                    "  " + trap.getEffected() + "Loses " + trap.getDuration();
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION, builder, ButtonType.OK);
        alert.setTitle("TRAP!");
        alert.showAndWait();

    }


    public static void promptForServerMoves(@NonNull Pane container, HumanServer inTraining) throws IOException {

        Stage stage = new Stage();
        Parent root = new FXMLLoader(ThroughputApplication.class.getResource("server-moves.fxml")).load();

        StringBuilder builder = new StringBuilder("SERVER MOVES!! ->  At prompting please enter: Server Color > Workstation Color.  Workstation is the receiving workstation - Worker must possess the skill (color) of the receiving workstation. You can only move Human Servers!");
        if (inTraining != null && LOGGER.isInfoEnabled()) {
            builder.append("You cannot move: ").append(inTraining.getColor().name()).append(" They are inTraining");
        }
        builder.append("Example BLUE>YELLOW  will move the Blue HUMAN SERVER to the Yellow Workstation");
        builder.append("Type 'exit' when you are finished with your moves");

        stage.setScene(new Scene(root));
        TextArea node = (TextArea) root.getScene().lookup("#serverMovesText");
        node.setText(builder.toString());

        ObservableList<javafx.scene.paint.Color> colors = FXCollections.observableArrayList(javafx.scene.paint.Color.BLUE, javafx.scene.paint.Color.PURPLE, javafx.scene.paint.Color.YELLOW, javafx.scene.paint.Color.GREEN, javafx.scene.paint.Color.PINK);
        ComboBox<javafx.scene.paint.Color> serverColorPicker = (ComboBox<javafx.scene.paint.Color>) root.getScene().lookup("#serverToMove");
        serverColorPicker.setItems(colors);

        ComboBox<javafx.scene.paint.Color> workstationColorPicker = (ComboBox<javafx.scene.paint.Color>) root.getScene().lookup("#workstationToMoveTo");
        workstationColorPicker.setItems(colors);


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
            alert.show();
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

    public static void promptForWorkItemWorkstationMoves(@NonNull Pane container, @NonNull Workstation workstation, int workstationPosition) throws IOException {
        if (workstation.getWorkItemCount() == 0) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Moves");
            alert.setHeaderText("No Moves " + workstation.getColor());
            alert.setContentText(workstation.getColor() + WORKSTATION_IS_EMPTY_NO_MOVES_ARE_POSSIBLE);
            alert.show();
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
        Parent root = new FXMLLoader(ThroughputApplication.class.getResource("add-capacity.fxml")).load();

        Workstation workstationBlue = Objects.requireNonNull(WorkstationService.getWorkstation(Color.BLUE));
        StringBuilder builder = getStringBuilder(timesTwo, workstationBlue);

        stage.setScene(new Scene(root));

        TextArea node = (TextArea) root.getScene().lookup("#addCapacityText");
        node.setText(builder.toString());

        ComboBox<Color> workstationToAddCapacity = (ComboBox) root.getScene().lookup("#workstationToAddCapacity");
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

        SkillCard skillCard = (SkillCard) CardService.getInstance().pickACard(Card.SKILLS);
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
        Parent root = new FXMLLoader(ThroughputApplication.class.getResource("add-skills.fxml")).load();

        String builder = "Choose which server to add a skill and which skill to add. " + System.lineSeparator() +
                " At prompting please enter: Server Color>Skill Color." +
                " You can only assign to Human Servers!" + System.lineSeparator() +
                " Example BLUE>YELLOW  will assign the BLUE HUMAN SERVER a YELLOW skill";

        stage.setScene(new Scene(root));

        TextArea node = (TextArea) root.getScene().lookup("#skillAddText");
        node.setText(builder);


        ComboBox<Color> serverColorPicker = (ComboBox) root.getScene().lookup("#serverToAddSkills");
        buildColorCombobox(serverColorPicker, Color.humanColorValues());

        ComboBox<Color> workstationColorPicker = (ComboBox) root.getScene().lookup("#skillsToAddToServer");
        buildColorCombobox(workstationColorPicker, Color.humanColorValues());

        stage.setTitle("Skills Add Window");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(container.getScene().getWindow());
        stage.showAndWait();


    }

    public static void promptToAutomateWorkstation() throws IOException {

        Stage stage = new Stage();
        Parent root = new FXMLLoader(ThroughputApplication.class.getResource("add-automation.fxml")).load();

        String builder = "Choose which color workstation to add automation " + Color.GREEN.name() + "|" + Color.ROSE.name() + "|" + Color.YELLOW.name() +
                "Example: GREEN will will automate the GREEN workstation. The human server will remain until moved";

        stage.setScene(new Scene(root));

        TextArea node = (TextArea) root.getScene().lookup("#addAutomationText");
        node.setText(builder);

        ComboBox<Color> serverWorkstationColorPicker = (ComboBox) root.getScene().lookup("#workstationToAddAutomation");
        buildColorCombobox(serverWorkstationColorPicker, Color.automatedColorValues());


    }

    private static void buildColorCombobox(ComboBox<Color> serverWorkstationColorPicker, Color[] es) {
        serverWorkstationColorPicker.getItems().addAll(es);
        serverWorkstationColorPicker.setCellFactory(listView -> new ListCell<Color>() {
            @Override
            public void updateItem(Color item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.name());
                    setBackground(new Background(new BackgroundFill(item.lookupFXColor(), null, null)));
                }
                setDisable(!empty);
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

    public static void publishEndWeek(int week, @NonNull ScoreCard scoreCard) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("End of Week");
        alert.setContentText("Prepare for the start of the next week");
        alert.showAndWait();


    }

    public static void publishEndOfGame(int week, @NonNull ScoreCard scoreCard) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("End of Game");
        alert.setContentText("Assess and discuss how you did");
        alert.showAndWait();

    }


    public static boolean serverChanceCardPlay(@NonNull Server server, @NonNull Workstation workstation) throws IOException {

        if (workstation.getWorkItemCount() == 0) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, WORKSTATION_IS_EMPTY_NO_MOVES_ARE_POSSIBLE, ButtonType.OK);
            alert.setHeaderText(workstation.getColor().name());
            alert.setTitle("Chance Card");
            alert.showAndWait();


            return false;
        }

        ChanceCard chanceCard;
        ImageView imageView;
        if (server instanceof AutomatedServer) {
            chanceCard = (ChanceRobotCard) CardService.getInstance().pickACard(Card.AUTOMATED_CHANCE);
            imageView = new ImageView(new Image(Objects.requireNonNull(ThroughputApplication.class.getResource("cards/ChanceTheRobot.jpg")).openStream()));
        } else {
            chanceCard = (ChanceCard) CardService.getInstance().pickACard(Card.CHANCE);
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

        return chanceCard.isSuccess();

    }


    public static int teamMood(int dieSides) {

        int teamMood = DiceService.rollDie(DiceService.getDie(dieSides)).getValue();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Team Mood");
        alert.setContentText("Rolled for Team Mood." + "  Team Mood is " + teamMood + " and backlog has " + ScorecardService.getInstance().getBacklog().getBacklogItemCount() + " work items" + " At the prompt you can move up to " + Math.min(teamMood, ScorecardService.getInstance().getBacklog().getBacklogItemCount()) + " items into your 1st workstation");
        alert.showAndWait();

        return Math.min(teamMood, ScorecardService.getInstance().getBacklog().getBacklogItemCount());
    }

    public static void promptForFinishedGoodsAreNowFourPoints() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Team Mood");
        alert.setContentText("Augmenting finished goods value to 4 for the remainder of the game");
        alert.show();
        ScorecardService.getInstance().getFinishedGoods().setValue(4);
    }


}
