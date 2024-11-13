package com.michelin.throughputfxproject.controllers;

import com.michelin.throughputfxproject.ThroughputApplication;
import com.michelin.throughputfxproject.entities.Color;
import com.michelin.throughputfxproject.entities.actions.Trap;
import com.michelin.throughputfxproject.entities.cards.*;
import com.michelin.throughputfxproject.entities.servers.AutomatedServer;
import com.michelin.throughputfxproject.entities.servers.HumanServer;
import com.michelin.throughputfxproject.entities.servers.Server;
import com.michelin.throughputfxproject.entities.state.Workstation;
import com.michelin.throughputfxproject.exceptions.ThroughputRuntimeException;
import com.michelin.throughputfxproject.services.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.*;
import javafx.util.Duration;
import javafx.util.StringConverter;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static com.michelin.throughputfxproject.services.DiceService.getDieImage;


public class Prompts {
    private static final Logger LOGGER = LoggerFactory.getLogger(Prompts.class.getName());
    private static final String THROUGHPUT = "Throughput";
    private static final String START_THE_WEEK = "Click on Run Week to start the week";
    private static final String WORKSTATION_IS_EMPTY_NO_MOVES_ARE_POSSIBLE = "Workstation is empty, No moves are possible";


    private Prompts() {
        super();
    }

    protected static BitCard drawBit(@NonNull Pane container, int dieSides, @NonNull TextArea gameBoardLog, int numberRequiredForSuccess) throws IOException {

        LOGGER.debug("drawBIT");

        //If not week 1 draw BIT card if they roll a 6
        int drawBitInt = DiceService.rollDie(DiceService.getDie(dieSides)).getValue();

        String gameBoardLogText = "Draw a " + Card.BOOSTER_INOCULATE_TRAP + " card!" + System.lineSeparator() + "Follow the instructions on the card.";

        if (drawBitInt >= numberRequiredForSuccess) {


            alertWithGameBoardUpdate("BIT", gameBoardLog, gameBoardLogText, 3000);

            BitCard bitCard = CardService.pickACardDestructively();
            Objects.requireNonNull(bitCard);

            FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("bit-card.fxml"));
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
                Image descriptionImg = new Image(Objects.requireNonNull(ThroughputApplication.class.getResource(descritionImgString)).openStream());
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

    @SuppressWarnings({"java:S1190", "java:S117"})
    private static void alertWithGameBoardUpdate(String title, @NonNull TextArea gameBoardLog, @NonNull String gameBoardLogText, int timeoutDurationMillis) {

        Alert alert = makeAlert(title, gameBoardLogText);

        Timeline idleStage = new Timeline(new KeyFrame(Duration.millis(timeoutDurationMillis), _ -> {
            gameBoardLog.setText(gameBoardLogText);
            alert.setResult(ButtonType.OK);
            alert.hide();
        }));
        idleStage.setCycleCount(1);
        idleStage.playFromStart();

        alert.showAndWait();

    }

    public static void alertWithoutBoardUpdate(@NonNull String title, @NonNull String text, int timeoutDurationMillis){

        Alert alert = makeAlert(title, text);
        Timeline idleStage = new Timeline(new KeyFrame(Duration.millis(timeoutDurationMillis), _ -> {
            alert.hide();
        }));
        idleStage.setCycleCount(1);
        idleStage.playFromStart();

        alert.showAndWait();
    }

    private static Alert makeAlert(@NonNull String title, @NonNull String text) {
        Text alertText = new Text(text);
        alertText.setWrappingWidth(200);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.getDialogPane().setContent(alertText);
        alert.getButtonTypes().set(0, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        return alert;
    }


    private static void setModalPosition(Window modalWindow) {
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        double screenWidth = bounds.getWidth();
        double screenHeight = bounds.getHeight();

        LOGGER.debug("Screen height {}  Screen Width {}", screenHeight, screenWidth);

        Platform.runLater(() -> {
            double windowWidth = modalWindow.getWidth();
            double windowHeight = modalWindow.getHeight();
            LOGGER.debug("Window height {}  Window Width {}", windowHeight, windowWidth);

            double x = (screenWidth / 2) - (windowWidth / 2);
            double y = ((screenHeight - windowHeight) / 2);
            LOGGER.debug("y {}  x {}", y, x);

            modalWindow.setX(x);
            modalWindow.setY(y);
        });

    }

    protected static void implementPairedProgramming(@NonNull Pane container, @NonNull TextArea gameBoardLog) throws IOException {
        LOGGER.debug("implementPairedProgramming");

        if (WorkstationService.findIfPairPartnerIsAlreadyAssigned()) {
            gameBoardLog.setText(
                    "Partner assigned already" + System.lineSeparator() + System.lineSeparator() +
                            "You can move the pair partner at the start of day");
            return;
        }

        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("implement-pairs.fxml"));
        Parent root = loader.load();

        String implementPairsText = "Choose which color workstation to pair." + System.lineSeparator() + "Requires a HUMAN server on the same workstation to work" +
                System.lineSeparator() + System.lineSeparator() + "Pair Partner gives an additional chance for a successful day";

        TextArea node = ((PairingController) loader.getController()).getImplementPairsText();
        node.setText(implementPairsText);

        ComboBox<Color> serverColorPicker = ((PairingController) loader.getController()).getWorkstationToPairWith();
        buildColorComboBox(serverColorPicker, Arrays.stream(WorkstationService.getWorkstations()).filter(Workstation::hasHumanServers).map(Workstation::getColor).toArray(Color[]::new));

        createModalStage("Paired Work", container, root, 20);
    }

    @SuppressWarnings({"java:S1190", "java:S117"})
    private static void buildColorComboBox(ComboBox<Color> colorPicker, Color[] es) {
        colorPicker.getItems().addAll(es);
        colorPicker.setCellFactory(_ -> new ListCell<>() {
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

    protected static void promptForAppliedTrap(Trap trap, boolean isMitigated, @NonNull TextArea gameBoardLog) {
        LOGGER.debug("promptForAppliedTrap");

        String builder;
        if (isMitigated) {
            builder = "Applying Mitigation for trap" +
                    "  " + trap.effected() + " Loses " + trap.mitigatedDuration();
        } else {
            builder = "No Mitigation available for trap" +
                    "  " + trap.effected() + " Loses " + trap.duration();
        }
        gameBoardLog.setText(builder);
    }

    protected static void promptForFinishedGoodsAreNowFourPoints(@NonNull TextArea gameBoardLog) {
        LOGGER.debug("promptForFinishedGoodsAreNowFourPoints");

        String gameBoardLogText = "Augmenting finished goods." + System.lineSeparator() + "Their value is 4 pts for the remainder of the game";
        alertWithGameBoardUpdate(gameBoardLog, gameBoardLogText, 10000);
        ScorecardService.getFinishedGoods().setValue(4);
    }

    private static void alertWithGameBoardUpdate(@NonNull TextArea gameBoardLog, @NonNull String gameBoardLogText, int timeoutDuration) {
        alertWithGameBoardUpdate(THROUGHPUT, gameBoardLog, gameBoardLogText, timeoutDuration);
    }

    protected static void promptForPairRetry(@NonNull Server server, @NonNull TextArea gameBoardLog) {
        LOGGER.debug("promptForPairRetry");

        String gameBoardLogText = "Your first try failed for Server " + server.getColor().name() + System.lineSeparator() + "Partner steps in to help with a retry";
        alertWithGameBoardUpdate("Partner", gameBoardLog, gameBoardLogText, 10000);
    }

    protected static void promptForServerMoves(@NonNull Pane container, HumanServer inTraining, BoardController boardController) throws IOException {
        LOGGER.debug("promptForServerMoves");

        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("server-moves.fxml"));
        Parent root = loader.load();

        StringBuilder builder = new StringBuilder("At prompting please enter: Server Color > Workstation Color."
                + System.lineSeparator() + System.lineSeparator() + "Workstation is the receiving workstation."
                + System.lineSeparator() + System.lineSeparator() + "Worker must possess the skill (color) of the receiving workstation."
                + System.lineSeparator() + System.lineSeparator() + "You can only move Human Servers!");

        if (inTraining != null) {
            builder.append(System.lineSeparator()).append("You cannot move: ").append(inTraining.getColor().name()).append(" They are inTraining");
        }

        builder.append(System.lineSeparator()).append("Example BLUE>YELLOW  will move the Blue HUMAN SERVER to the Yellow Workstation");

        TextArea node = ((ServerMovesController) loader.getController()).getServerMovesText();
        node.setText(builder.toString());

        ComboBox<Color> serverColorPicker = ((ServerMovesController) loader.getController()).getServerToMove();
        buildColorComboBox(serverColorPicker, Color.humanColorValues());

        ComboBox<Color> workstationColorPicker = ((ServerMovesController) loader.getController()).getWorkstationToMoveTo();
        buildColorComboBox(workstationColorPicker, Color.humanColorValues());

        ((ServerMovesController) loader.getController()).setBoardController(boardController);

        createModalStage("Server Moves", container, root, 15);

    }

    @SuppressWarnings({"java:S1190", "java:S117"})
    protected static boolean promptForServerRetry(@NonNull Server server) {
        LOGGER.debug("promptForServerRetry");

        Text text = new Text("Your first try failed for Server " + server.getColor().name() + System.lineSeparator() + "You have a Retry card, would you like to use it? 'Y/N'");
        text.setWrappingWidth(105);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.getDialogPane().setContent(text);
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        alert.setTitle("Retry");
        alert.setHeaderText(null);

        Timeline idleStage = new Timeline(new KeyFrame(Duration.seconds(15), _ -> {
            alert.setResult(ButtonType.YES);
            alert.hide();
        }));
        idleStage.setCycleCount(1);
        idleStage.play();

        ButtonType button = alert.showAndWait().orElse(null);

        return button == null || button == ButtonType.YES;

    }

    protected static void promptForWorkItemEstimates(@NonNull Pane container) throws IOException {
        LOGGER.debug("promptForWorkItemEstimates");
        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("submit-estimate.fxml"));
        Parent root = loader.load();

        Button submitButton = ((EstimateController) loader.getController()).getEstimateButton();

        createModalStageWithButton("Estimate your Week", container, root, 15, submitButton);
    }

    @SuppressWarnings({"java:S1190", "java:S117"})
    private static void createModalStageWithButton(@NonNull String title, @NonNull Pane container, @NonNull Parent root, int timeoutDuration, Button button) {

        Stage stage = createModalStageWithoutAction(title, container, root);

        Timeline idleStage = new Timeline(new KeyFrame(Duration.seconds(timeoutDuration), _ -> button.fire()));
        idleStage.setCycleCount(1);
        idleStage.playFromStart();

        stage.showAndWait();

    }

    private static Stage createModalStageWithoutAction(@NonNull String title, @NonNull Pane container, @NonNull Parent root) {
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(container.getScene().getWindow());
        stage.setResizable(false);
        setModalPosition(stage);
        return stage;
    }

    @SuppressWarnings({"java:S1190", "java:S117"})
    private static void createModalStage(@NonNull String title, @NonNull Pane container, @NonNull Parent root, int timeoutDuration) {

        Stage stage = createModalStageWithoutAction(title, container, root);

        Timeline idleStage = new Timeline(new KeyFrame(Duration.seconds(timeoutDuration), _ -> stage.hide()));
        idleStage.setCycleCount(1);
        idleStage.playFromStart();

        stage.showAndWait();

    }

    protected static void promptForWorkItemInitialMoves(@NonNull Pane container, int startValue, int backlogCount, @NonNull TextArea gameBoardLog) throws IOException {
        LOGGER.debug("promptForWorkItemInitialMoves");

        if (backlogCount <= 0) {
            String gameBoardLogText = "Backlog is Empty" + System.lineSeparator() + System.lineSeparator() + "No moves possible";
            alertWithGameBoardUpdate(gameBoardLog, gameBoardLogText, 10000);
            return;
        }

        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("move-initial-work-item.fxml"));
        Parent root = loader.load();

        final int maxIntToMove = Math.min(startValue, backlogCount);
        String builder = "Choose how many items to move to your 1st workstation from the backlog" +
                System.lineSeparator() + System.lineSeparator() +
                "At prompting please enter any number <= " + startValue + "." +
                System.lineSeparator() + System.lineSeparator() +
                "Example:   '" + maxIntToMove + "' will move " + maxIntToMove + " work items to the 1st workstation";

        TextArea node = ((InitialWorkItemsController) loader.getController()).getWorkItemMoveText();
        node.setText(builder);

        TextField workItemResponseText = ((InitialWorkItemsController) loader.getController()).getWorkItemMoveResponseText();
        workItemResponseText.setText(String.valueOf(maxIntToMove));

        Text workstationMaxText = ((InitialWorkItemsController) loader.getController()).getTxtWorkstationMax();
        workstationMaxText.setText(String.valueOf(maxIntToMove));

        Button submitButton = ((InitialWorkItemsController) loader.getController()).getWorkItemMoveButton();
        createModalStageWithButton("Move Items", container, root, 10, submitButton);
    }

    protected static void promptForWorkItemWorkstationMoves(@NonNull Pane container, Workstation workstation, int workstationPosition) throws IOException {
        LOGGER.debug("promptForWorkItemWorkstationMoves");

        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("move-work-item.fxml"));
        Parent root = loader.load();

        StringBuilder builder = new StringBuilder();
        final int maxIntToMove = Math.min(workstation.getCapacity(), workstation.getWorkItemCount());
        builder.append("Choose how many items to move from the ")
                .append(workstation.getColor().name())
                .append(" workstation to the next.")
                .append(System.lineSeparator()).append(System.lineSeparator())
                .append("At prompting please enter any number <= ")
                .append(maxIntToMove)
                .append(System.lineSeparator()).append(System.lineSeparator()).append("Example: '")
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

        Text workstationMaxText = ((WorkItemsController) loader.getController()).getTxtWorkstationMax();
        workstationMaxText.setText(String.valueOf(maxIntToMove));

        TextField workItemResponseText = ((WorkItemsController) loader.getController()).getWorkItemMoveResponseText();
        workItemResponseText.setText(String.valueOf(maxIntToMove));

        Button submitButton = ((WorkItemsController) loader.getController()).getWorkItemMoveButton();
        createModalStageWithButton("Move Items", container, root, 10, submitButton);
    }

    protected static void promptToAddSkill(@NonNull Pane container) throws IOException {
        LOGGER.debug("promptToAddSkill");

        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("add-skills.fxml"));
        Parent root = loader.load();

        String builder = "Choose which server to add a skill and which skill to add. " + System.lineSeparator() + System.lineSeparator() +
                " At prompting please enter: Server Color>Skill Color." +
                " You can only assign to Human Servers!" + System.lineSeparator() + System.lineSeparator() +
                " Example BLUE>YELLOW  will assign the BLUE HUMAN SERVER a YELLOW skill";

        TextArea node = ((SkillsController) loader.getController()).getSkillAddText();
        node.setText(builder);

        ComboBox<Color> serverColorPicker = ((SkillsController) loader.getController()).getServerToAddSkills();
        buildColorComboBox(serverColorPicker, Color.humanColorValues());

        ComboBox<Color> skillColorPicker = ((SkillsController) loader.getController()).getSkillsToAddToServer();
        buildColorComboBox(skillColorPicker, Color.humanColorValues());

        createModalStage("Add Skills to Server", container, root, 25);

    }

    protected static void promptToAugmentWorkstationCapacity(@NonNull Pane container, boolean timesTwo, int maxCapacity) throws IOException {
        LOGGER.debug("promptToAugmentWorkstationCapacity");

        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("add-capacity.fxml"));
        Parent root = loader.load();

        Workstation workstationBlue = Objects.requireNonNull(WorkstationService.getWorkstation(Color.BLUE));

        TextArea node = ((AddedCapacityController) loader.getController()).getAddCapacityText();
        node.setText(getWorkstationCapacityText(timesTwo, workstationBlue, maxCapacity));

        ComboBox<Color> workstationToAddCapacity = ((AddedCapacityController) loader.getController()).getWorkstationToAddCapacity();
        buildColorComboBox(workstationToAddCapacity, Color.humanColorValues());

        createModalStage("Add Capacity", container, root, 15);

    }

    private static String getWorkstationCapacityText(boolean timesTwo, Workstation workstationBlue, int maxCapacity) {
        StringBuilder builder = new StringBuilder();
        if (!timesTwo) {
            builder.append("Choose which workstation to add one to its capacity");
            builder.append(System.lineSeparator()).append(System.lineSeparator());
            builder.append("Example: BLUE will augment the BLUE workstation from ");
            builder.append(workstationBlue.getCapacity());
            builder.append(" to ");
            builder.append(Math.min(workstationBlue.getCapacity() + 1, maxCapacity));
        } else {
            builder.append("Choose which workstation to double its capacity");
            builder.append(System.lineSeparator()).append(System.lineSeparator());
            builder.append("Example: BLUE will augment the BLUE workstation from ");
            builder.append(workstationBlue.getCapacity());
            builder.append(" to ");
            builder.append(Math.min(workstationBlue.getCapacity() * 2, maxCapacity));
        }
        return builder.toString();
    }

    protected static void promptToAutomateWorkstation(@NonNull Pane container, @NonNull TextArea gameBoardLog) throws IOException {
        LOGGER.debug("promptToAutomateWorkstation");
        if (WorkstationService.findDeployedAutomatedServers().size() == 3) {
            gameBoardLog.setText("No Robots Left!" + System.lineSeparator() + "There are no workstations available to automate");
            return;
        }

        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("add-automation.fxml"));
        Parent root = loader.load();

        String builder = "Choose which color workstation to add automation " + System.lineSeparator() + System.lineSeparator() + Color.GREEN.name() + "|" + Color.ROSE.name() + "|" + Color.YELLOW.name() +
                System.lineSeparator() + System.lineSeparator() + "Example: GREEN will will automate the GREEN workstation. The human server will remain until moved";

        TextArea node = ((AddAutomationController) loader.getController()).getAddAutomationText();
        node.setText(builder);

        ComboBox<Color> serverWorkstationColorPicker = ((AddAutomationController) loader.getController()).getWorkstationToAddAutomation();
        List<Color> serverColors = WorkstationService.findDeployedAutomatedServers().stream().map(Server::getColor).toList();
        List<Color> leftoverColors = Arrays.stream(Color.automatedColorValues()).filter(color -> !serverColors.contains(color)).toList();
        buildColorComboBox(serverWorkstationColorPicker, leftoverColors.toArray(Color[]::new));

        createModalStage("Automate Workstation", container, root, 20);
    }

    protected static boolean promptToDrawSkillsCard(@NonNull Pane container) throws IOException {
        LOGGER.debug("promptToDrawSkillsCard");
        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("skill-card.fxml"));
        Parent root = loader.load();

        SkillCard skillCard = (SkillCard) CardService.pickACard(Card.SKILLS);
        ((SkillCardController) loader.getController()).getCardSkill().setText(skillCard.getSkill());
        ((SkillCardController) loader.getController()).getCardInstructions().setText(skillCard.getInstructions());
        ((SkillCardController) loader.getController()).getCardInstructionsExtended().setText(skillCard.getInstructionsExtended());
        ((SkillCardController) loader.getController()).getIsSuccessful().setText(String.valueOf(skillCard.isSuccess()));

        createModalStage("Skills", container, root, 5);
        return skillCard.isSuccess();
    }

    protected static void promptToRemoveSkill() {
        LOGGER.debug("promptToRemoveSkill");
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Replace skilled server", ButtonType.FINISH);
        alert.setTitle("Skills");
        alert.setHeaderText(null);
        ServerService.removeSkillsFromMostSkilledServer();

    }

    public static File promptToUploadPreviousGame(@NonNull Pane container, @NonNull File initialDirectory) {
        // Create a new stage for the dialog
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.UTILITY);
        dialogStage.initOwner(container.getScene().getWindow());
        dialogStage.setHeight(300);
        dialogStage.setWidth(300);

        dialogStage.setTitle("Upload Previous Game");

        // Create a label to display the selected file path
        Label filePathLabel = new Label();
        filePathLabel.setWrapText(true);

        // Create a button to trigger the file chooser
        AtomicReference<File> selectedFile = new AtomicReference<>();
        Button chooseFileButton = new Button("Choose Game File");
        chooseFileButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(initialDirectory);
            selectedFile.set(fileChooser.showOpenDialog(dialogStage));
            if (selectedFile.get() != null) {
                filePathLabel.setText(selectedFile.get().getAbsolutePath());
            }
        });

        // Create a button to close the dialog
        Button closeButton = new Button("Load File");
        closeButton.setOnAction(event -> dialogStage.close());

        // Create a vertical layout to arrange the controls
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setPrefWidth(300);
        layout.getChildren().addAll(filePathLabel, chooseFileButton, closeButton);

        // Create a scene and set it to the dialog stage
        Scene scene = new Scene(layout);
        dialogStage.setScene(scene);
        dialogStage.showAndWait(); // Show the dialog and wait for it to close

        return selectedFile.get();
    }

    protected static void publishEndOfGame(TextArea gameBoardLog) {
        String gameBoardLogText = "End of Game." + System.lineSeparator() + "Assess and discuss how you did";
        alertWithGameBoardUpdate(gameBoardLog, gameBoardLogText, 15000);
    }

    protected static void publishEndPeriod(TextArea gameBoardLog) {
        String gameBoardLogText = "End of Week." + System.lineSeparator() + "Prepare for the start of the next week";
        alertWithGameBoardUpdate(gameBoardLog, gameBoardLogText, 10000);
    }

    protected static void publishStartPeriod(TextArea gameBoardLog, int period) {
        String gameBoardLogText = "Week: " + period + System.lineSeparator() + START_THE_WEEK;
        alertWithGameBoardUpdate(gameBoardLog, gameBoardLogText, 100);

    }

    protected static void publishTurnStart(TextArea gameBoardLog, int period, int turn) {
        String gameBoardLogText = "Day: " + turn + "  Week:  " + period + System.lineSeparator() + "Click on Run Day to start the day";
        alertWithGameBoardUpdate(gameBoardLog, gameBoardLogText, 100);
    }

    protected static ChanceResult serverChanceCardPlay(@NonNull Pane container, @NonNull Server server, Workstation workstation, @NonNull TextArea gameBoardLog) throws IOException {

        if (workstation.getWorkItemCount() == 0) {
            String gameBoardLogText = "No Moves." + System.lineSeparator() + workstation.getColor() + " " + WORKSTATION_IS_EMPTY_NO_MOVES_ARE_POSSIBLE;
            alertWithGameBoardUpdate(gameBoardLog, gameBoardLogText, 12000);
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

        createModalStage("Chance", container, root, 5);

        return chanceCard.isSuccess() ? ChanceResult.SUCCESS : ChanceResult.FAILED;

    }

    protected static void showInfoCard(Pane container) throws IOException {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("info.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.setTitle("Game Information");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(container.getScene().getWindow());
        stage.show();
    }

    protected static void showRulesCard(Pane container) throws IOException {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("rules.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.setTitle("Game Rules");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(container.getScene().getWindow());
        stage.show();
    }

    protected static int teamMood(@NonNull Pane container, int dieSides) throws IOException {

        int teamMood = DiceService.rollDie(DiceService.getDie(dieSides)).getValue();

        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("die-roll.fxml"));
        Parent root = loader.load();

        String builder = "Team Mood is " + teamMood + " and backlog has " + ScorecardService.getBacklog().getBacklogItemCount() + " work items" + " At the prompt you can move up to " + Math.min(teamMood, ScorecardService.getBacklog().getBacklogItemCount()) + " items into your 1st workstation";

        Label dieText = ((DieController) loader.getController()).getDieText();
        dieText.setText(builder);

        Label dieHeaderText = ((DieController) loader.getController()).getDieHeaderText();
        dieHeaderText.setText("Rolled for Team Mood");

        ImageView backImageView = ((DieController) loader.getController()).getDieImage();
        Image backImage = new Image(Objects.requireNonNull(ThroughputApplication.class.getResource(getDieImage(teamMood))).openStream());
        backImageView.setImage(backImage);

        createModalStage("Team Mood", container, root, 5);

        return Math.min(teamMood, ScorecardService.getBacklog().getBacklogItemCount());
    }

}
