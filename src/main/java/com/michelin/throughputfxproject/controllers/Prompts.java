/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.michelin.throughputfxproject.controllers;

import com.michelin.throughputfxproject.ThroughputApplication;
import com.michelin.throughputfxproject.entities.Color;
import com.michelin.throughputfxproject.entities.actions.Trap;
import com.michelin.throughputfxproject.entities.cards.*;
import com.michelin.throughputfxproject.entities.servers.*;
import com.michelin.throughputfxproject.entities.state.Workstation;
import com.michelin.throughputfxproject.exceptions.ThroughputRuntimeException;
import com.michelin.throughputfxproject.services.*;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.*;
import javafx.util.Duration;
import javafx.util.StringConverter;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import static com.michelin.throughputfxproject.services.DiceService.getDieImage;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Prompts {
    private static final String THROUGHPUT = "Throughput";
    private static final String START_THE_WEEK = "Click on Run Week to start the week";
    private static final String WORKSTATION_IS_EMPTY_NO_MOVES_ARE_POSSIBLE = "Workstation is empty, No moves are possible";
    private static final int TIMEOUT_CONSTANT = 30;
    private static final int TIMEOUT_DURATION = TIMEOUT_CONSTANT/2;
    public static final int MODAL_TIMEOUT_DURATION = TIMEOUT_CONSTANT;
    public static final int ALERT_TIMEOUT_DURATION = TIMEOUT_CONSTANT/2;
    public static final int END_PERIOD_TIMEOUT_DURATION = TIMEOUT_CONSTANT/3;
    public static final int END_OF_GAME_TIMEOUT_DURATION = TIMEOUT_CONSTANT/2;

    /**
     * When {@code true}, all dialog auto-close timers are active (timed-run mode).
     * When {@code false}, dialogs wait for user interaction (manual mode).
     * Written by the FX thread; read by background game-turn threads — must be volatile.
     */
    private static volatile boolean timedRun = false;

    /**
     * Sets whether dialogs should auto-close on their built-in timers.
     * Call this whenever the timedRun checkbox state changes.
     *
     * @param timed {@code true} to enable auto-close timers, {@code false} to wait for user
     */
    public static void setTimedRun(boolean timed) {
        timedRun = timed;
    }

    /**
     * Reference to the board's countdown timer label so dialogs can show their remaining time.
     * Set once on startup via {@link #setCountdownTimer}; read from any thread.
     */
    private static volatile Label countdownTimer = null;

    /**
     * Sets the board's countdown timer label so timed dialogs can display their remaining time.
     *
     * @param label the {@code countdownTimer} label from the board FXML
     */
    public static void setCountdownTimer(Label label) {
        countdownTimer = label;
    }

    /**
     * Starts a visual countdown on the board timer label for {@code durationSeconds} seconds.
     * Must be called on the FX application thread.
     *
     * @return the running {@link Timeline}, or {@code null} if no label is registered
     */
    private static Timeline startCountdownTimer(int durationSeconds) {
        if (countdownTimer == null) return null;
        IntegerProperty timeSeconds = new SimpleIntegerProperty(durationSeconds);
        countdownTimer.textProperty().bind(timeSeconds.asString());
        countdownTimer.setTextFill(javafx.scene.paint.Color.DARKBLUE);
        Timeline countdown = new Timeline();
        countdown.getKeyFrames().add(new KeyFrame(Duration.seconds(durationSeconds + 1.0), new KeyValue(timeSeconds, 0)));
        countdown.setCycleCount(1);
        countdown.playFromStart();
        return countdown;
    }

    /**
     * Stops a running countdown and resets the board timer label to "X" in red.
     * Must be called on the FX application thread.
     *
     * @param countdown the Timeline returned by {@link #startCountdownTimer}, may be {@code null}
     */
    private static void stopCountdownTimer(Timeline countdown) {
        if (countdownTimer == null) return;
        if (countdown != null) countdown.stop();
        countdownTimer.textProperty().unbind();
        countdownTimer.setText("X");
        countdownTimer.setTextFill(javafx.scene.paint.Color.RED);
    }


    /**
     * Displays an alert without updating the game board and automatically hides it after a timeout.
     *
     * @param title                 The title of the alert dialog.
     * @param text                  The message to display in the alert dialog.
     * @param timeoutDurationMillis The duration in milliseconds before the alert is automatically hidden.
     */
    public static void alertWithoutBoardUpdate(@NonNull String title, @NonNull String text, int timeoutDurationMillis) {
        if (Platform.isFxApplicationThread()) {
            Alert alert = makeAlert(title, text);
            Timeline idleStage = new Timeline(new KeyFrame(Duration.millis(timeoutDurationMillis), _ -> alert.hide()));
            idleStage.setCycleCount(1);
            idleStage.playFromStart();
            alert.showAndWait();
            idleStage.stop();
        } else {
            CompletableFuture<Void> closed = new CompletableFuture<>();
            Platform.runLater(() -> {
                Alert alert = makeAlert(title, text);
                Timeline idleStage = new Timeline(new KeyFrame(Duration.millis(timeoutDurationMillis), _ -> alert.hide()));
                idleStage.setCycleCount(1);
                idleStage.playFromStart();
                alert.setOnHidden(_ -> {
                    idleStage.stop();
                    Platform.runLater(() -> closed.complete(null));
                });
                alert.show();
            });
            awaitFuture(closed);
        }
    }

    /**
     * Creates an alert dialog with the specified title and message.
     *
     * @param title The title of the alert dialog.
     * @param text  The message to display in the alert dialog.
     * @return The created alert dialog.
     */
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

    /**
     * Blocks the current background thread until the given future completes.
     * Must NOT be called from the FX Application Thread.
     */
    private static <T> T awaitFuture(CompletableFuture<T> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ThroughputRuntimeException(e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException re) throw re;
            if (cause instanceof Exception ex) throw new ThroughputRuntimeException(ex);
            throw new ThroughputRuntimeException(new RuntimeException(cause));
        }
    }

    /**
     * Draws a BIT card based on a die roll and displays its details in a modal dialog.
     * If the die roll meets or exceeds the required number for success, a BIT card is drawn,
     * its details are displayed, and it is returned. Otherwise, null is returned.
     *
     * @param container                The parent container for the modal dialog.
     * @param dieSides                 The number of sides on the die to be rolled.
     * @param gameBoardLog             The log area to display game-related messages.
     * @param numberRequiredForSuccess The minimum die roll required to draw a BIT card.
     * @return The drawn BIT card if successful, or null if the die roll fails.
     * @throws IOException If an error occurs while loading the FXML file.
     */
    public static BitCard drawBit(@NonNull Pane container, int dieSides, @NonNull TextArea gameBoardLog, int numberRequiredForSuccess) throws IOException {

        log.debug("drawBIT");

        //If not week 1 draw BIT card if they roll a 6
        int drawBitInt = DiceService.rollDie(DiceService.getDie(dieSides)).getValue();

        String gameBoardLogText = "Draw a " + Card.BOOSTER_INOCULATE_TRAP + " card!" + System.lineSeparator() + "Follow the instructions on the card.";

        if (drawBitInt >= numberRequiredForSuccess) {


            alertWithGameBoardUpdate("BIT", gameBoardLog, gameBoardLogText, MODAL_TIMEOUT_DURATION);

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
            createModalStage("Booster, Inoculation, Trap", container, root, MODAL_TIMEOUT_DURATION);

            gameBoardLog.setBackground(Background.EMPTY);
            //Follow the instructions on BIT card. If we get a hold card put in weekly hold or game hold.
            //Execute work item traps immediately, otherwise return traps for future execution.
            return bitCard;
            //Execute do-over hold cards -- Look for mitigating hold cards
        }
        return null;
    }

    /**
     * Displays an alert with a game board update and automatically hides it after a timeout.
     *
     * @param title                 The title of the alert dialog.
     * @param gameBoardLog          The log area to display the game-related message.
     * @param gameBoardLogText      The message to display in the game board log.
     * @param timeoutDuration       The duration in seconds before the alert is automatically hidden.
     */
    @SuppressWarnings({"java:S1190", "java:S117"})
    private static void alertWithGameBoardUpdate(String title, @NonNull TextArea gameBoardLog, @NonNull String gameBoardLogText, int timeoutDuration) {
        if (Platform.isFxApplicationThread()) {
            gameBoardLog.setText(gameBoardLogText);
            Alert alert = makeAlert(title, gameBoardLogText);
            if (timedRun) {
                Timeline countdownAnim = startCountdownTimer(timeoutDuration);
                Timeline idleStage = new Timeline(new KeyFrame(Duration.seconds(timeoutDuration), _ -> {
                    alert.setResult(ButtonType.OK);
                    alert.hide();
                }));
                idleStage.setCycleCount(1);
                idleStage.playFromStart();
                alert.showAndWait();
                idleStage.stop();
                stopCountdownTimer(countdownAnim);
            } else {
                alert.showAndWait();
            }
        } else {
            CompletableFuture<Void> closed = new CompletableFuture<>();
            Platform.runLater(() -> {
                gameBoardLog.setText(gameBoardLogText);
                Alert alert = makeAlert(title, gameBoardLogText);
                if (timedRun) {
                    Timeline countdownAnim = startCountdownTimer(timeoutDuration);
                    Timeline idleStage = new Timeline(new KeyFrame(Duration.seconds(timeoutDuration), _ -> alert.hide()));
                    idleStage.setCycleCount(1);
                    idleStage.playFromStart();
                    alert.setOnHidden(_ -> {
                        idleStage.stop();
                        stopCountdownTimer(countdownAnim);
                        Platform.runLater(() -> closed.complete(null));
                    });
                } else {
                    alert.setOnHidden(_ -> Platform.runLater(() -> closed.complete(null)));
                }
                alert.show();
            });
            awaitFuture(closed);
        }
    }

    /**
     * Creates a modal stage with a timeout duration.
     * The stage is automatically hidden after the specified timeout duration.
     *
     * @param title           The title of the modal stage.
     * @param container       The parent container for the modal stage.
     * @param root            The root node of the modal stage's scene.
     * @param timeoutDuration The duration in seconds before the stage is automatically hidden.
     */
    @SuppressWarnings({"java:S1190", "java:S117"})
    private static void createModalStage(@NonNull String title, @NonNull Pane container, @NonNull Parent root, int timeoutDuration) {
        if (Platform.isFxApplicationThread()) {
            Stage stage = createModalStageWithoutAction(title, container, root);
            if (timedRun) {
                Timeline countdownAnim = startCountdownTimer(timeoutDuration);
                Timeline idleStage = new Timeline(new KeyFrame(Duration.seconds(timeoutDuration), _ -> stage.hide()));
                idleStage.setCycleCount(1);
                idleStage.playFromStart();
                stage.showAndWait();
                idleStage.stop();
                stopCountdownTimer(countdownAnim);
            } else {
                stage.showAndWait();
            }
        } else {
            CompletableFuture<Void> closed = new CompletableFuture<>();
            Platform.runLater(() -> {
                Stage stage = createModalStageWithoutAction(title, container, root);
                if (timedRun) {
                    Timeline countdownAnim = startCountdownTimer(timeoutDuration);
                    Timeline idleStage = new Timeline(new KeyFrame(Duration.seconds(timeoutDuration), _ -> stage.hide()));
                    idleStage.setCycleCount(1);
                    idleStage.playFromStart();
                    stage.setOnHidden(_ -> {
                        idleStage.stop();
                        stopCountdownTimer(countdownAnim);
                        Platform.runLater(() -> closed.complete(null));
                    });
                } else {
                    stage.setOnHidden(_ -> Platform.runLater(() -> closed.complete(null)));
                }
                stage.show();
            });
            awaitFuture(closed);
        }
    }

    /**
     * Creates a modal dialog with a button that is automatically triggered after a timeout.
     * The dialog is displayed with the specified title and content, and the button is fired
     * when the timeout duration elapses.
     *
     * @param title     The title of the modal dialog.
     * @param container The parent container for the modal dialog.
     * @param root      The root node of the modal dialog's scene.
     * @param button    The button to be triggered after the timeout.
     */
    @SuppressWarnings({"java:S1190", "java:S117"})
    private static void createModalStageWithButton(@NonNull String title, @NonNull Pane container, @NonNull Parent root, Button button) {
        if (Platform.isFxApplicationThread()) {
            Stage stage = createModalStageWithoutAction(title, container, root);
            if (timedRun) {
                Timeline countdownAnim = startCountdownTimer(TIMEOUT_DURATION);
                Timeline idleStage = new Timeline(new KeyFrame(Duration.seconds(TIMEOUT_DURATION), _ -> button.fire()));
                idleStage.setCycleCount(1);
                idleStage.playFromStart();
                stage.showAndWait();
                idleStage.stop();
                stopCountdownTimer(countdownAnim);
            } else {
                stage.showAndWait();
            }
        } else {
            CompletableFuture<Void> closed = new CompletableFuture<>();
            Platform.runLater(() -> {
                Stage stage = createModalStageWithoutAction(title, container, root);
                if (timedRun) {
                    Timeline countdownAnim = startCountdownTimer(TIMEOUT_DURATION);
                    Timeline idleStage = new Timeline(new KeyFrame(Duration.seconds(TIMEOUT_DURATION), _ -> button.fire()));
                    idleStage.setCycleCount(1);
                    idleStage.playFromStart();
                    stage.setOnHidden(_ -> {
                        idleStage.stop();
                        stopCountdownTimer(countdownAnim);
                        Platform.runLater(() -> closed.complete(null));
                    });
                } else {
                    stage.setOnHidden(_ -> Platform.runLater(() -> closed.complete(null)));
                }
                stage.show();
            });
            awaitFuture(closed);
        }
    }

    /**
     * Creates a modal stage without any additional actions.
     * Configures the stage with the specified title, parent container, and root node.
     * The stage is centered on the screen and set to be non-resizable.
     *
     * @param title     The title of the modal stage.
     * @param container The parent container for the modal stage.
     * @param root      The root node of the modal stage's scene.
     * @return The created modal stage.
     */
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

    /**
     * Centers a modal window on the screen.
     *
     * @param modalWindow The modal window to position.
     */
    private static void setModalPosition(Window modalWindow) {
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        double screenWidth = bounds.getWidth();
        double screenHeight = bounds.getHeight();

        log.debug("Screen height {}  Screen Width {}", screenHeight, screenWidth);

        Platform.runLater(() -> {
            double windowWidth = modalWindow.getWidth();
            double windowHeight = modalWindow.getHeight();
            log.debug("Window height {}  Window Width {}", windowHeight, windowWidth);

            double x = (screenWidth / 2) - (windowWidth / 2);
            double y = ((screenHeight - windowHeight) / 2);
            log.debug("y {}  x {}", y, x);

            modalWindow.setX(x);
            modalWindow.setY(y);
        });

    }

    /**
     * Prompts the user to implement paired programming for a workstation.
     * Displays a modal dialog where the user can select a workstation to pair.
     * If a pair partner is already assigned, it updates the game board log with a message and exits.
     *
     * @param container    The parent container for the modal dialog.
     * @param gameBoardLog The log area to display game-related messages.
     * @throws IOException If an error occurs while loading the FXML file.
     */
    public static void implementPairedProgramming(@NonNull Pane container, @NonNull TextArea gameBoardLog) throws IOException {
        log.debug("implementPairedProgramming");

        // Check if a pair partner is already assigned
        if (WorkstationService.findIfPairPartnerIsAlreadyAssigned()) {
            gameBoardLog.setText(
                    "Partner assigned already" + System.lineSeparator() + System.lineSeparator() +
                            "You can move the pair partner at the start of day");
            return;
        }

        // Load the FXML file for the pairing dialog
        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("implement-pairs.fxml"));
        Parent root = loader.load();

        // Set the instructions text for the pairing dialog
        String implementPairsText = "Choose which color workstation to pair." + System.lineSeparator() +
                "Requires a HUMAN server on the same workstation to work" +
                System.lineSeparator() + System.lineSeparator() +
                "Pair Partner gives an additional chance for a successful day";

        TextArea node = ((PairingController) loader.getController()).getImplementPairsText();
        node.setText(implementPairsText);

        // Populate the color picker with workstations that have human servers
        ComboBox<Color> serverColorPicker = ((PairingController) loader.getController()).getWorkstationToPairWith();
        buildColorComboBox(serverColorPicker, Arrays.stream(WorkstationService.getWorkstations())
                .filter(Workstation::hasHumanServers)
                .map(Workstation::getColor)
                .toArray(Color[]::new));

        // Display the modal dialog
        createModalStage("Paired Work", container, root, MODAL_TIMEOUT_DURATION);
    }

    /**
     * Populates a `ComboBox` with a list of colors and configures its display and behavior.
     * The method sets up the items, custom cell rendering, and a string converter for the `ComboBox`.
     *
     * @param colorPicker The `ComboBox` to populate with color options.
     * @param es          An array of `Color` objects to add to the `ComboBox`.
     */
    @SuppressWarnings({"java:S1190", "java:S117"})
    private static void buildColorComboBox(ComboBox<Color> colorPicker, Color[] es) {
        // Add all colors to the ComboBox
        colorPicker.getItems().addAll(es);

        // Set a custom cell factory to define how each color is displayed
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

        // Set a string converter to handle color-to-string and string-to-color conversions
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

    /**
     * Logs and updates the game board log with a message about the application of a trap.
     * If the trap is mitigated, it logs the mitigated duration; otherwise, it logs the full duration.
     *
     * @param trap         The trap being applied.
     * @param isMitigated  A flag indicating whether the trap is mitigated (true) or not (false).
     * @param gameBoardLog The log area to display the trap-related message.
     */
    public static void promptForAppliedTrap(Trap trap, boolean isMitigated, @NonNull TextArea gameBoardLog) {
        log.debug("promptForAppliedTrap");

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

    /**
     * Logs and updates the game board log with a message about augmenting finished goods.
     * Sets the value of finished goods to 4 points for the remainder of the game.
     *
     * @param gameBoardLog The log area to display the finished goods augmentation message.
     */
    public static void promptForFinishedGoodsAreNowFourPoints(@NonNull TextArea gameBoardLog) {
        log.debug("promptForFinishedGoodsAreNowFourPoints");

        String gameBoardLogText = "Augmenting finished goods." + System.lineSeparator() + "Their value is 4 pts for the remainder of the game";
        alertWithGameBoardUpdate(gameBoardLog, gameBoardLogText, MODAL_TIMEOUT_DURATION);
        ScorecardService.FINISHED_GOODS.setValue(4);
    }

    /**
     * Displays an alert with a game board update and automatically hides it after a timeout.
     * Delegates to the overloaded {@code alertWithGameBoardUpdate} method with a default title.
     *
     * @param gameBoardLog     The log area to display the game-related message.
     * @param gameBoardLogText The message to display in the game board log.
     * @param timeoutDuration  The duration in seconds before the alert is automatically hidden.
     */
    private static void alertWithGameBoardUpdate(@NonNull TextArea gameBoardLog, @NonNull String gameBoardLogText, int timeoutDuration) {
        alertWithGameBoardUpdate(THROUGHPUT, gameBoardLog, gameBoardLogText, timeoutDuration);
    }

    /**
     * Logs a message and updates the game board log with a retry prompt for a server.
     * Displays a message indicating that a partner is stepping in to help with a retry.
     *
     * @param server       The server for which the retry is being prompted.
     * @param gameBoardLog The log area to display the retry-related message.
     */
    public static void promptForPairRetry(@NonNull Server server, @NonNull TextArea gameBoardLog) {
        log.debug("promptForPairRetry");

        String gameBoardLogText = "Your first try failed for Server " + server.getColor().name() + System.lineSeparator() + "Partner steps in to help with a retry";
        alertWithGameBoardUpdate("Partner", gameBoardLog, gameBoardLogText, 30);
    }

    /**
     * Prompts the user to move a human server to a workstation.
     * Displays a modal dialog where the user can select a server and a workstation to move to.
     * If a server is in training, it is excluded from the available options.
     *
     * @param container       The parent container for the modal dialog.
     * @param inTraining      The human server currently in training, if any.
     * @param boardController The controller managing the game board state.
     * @throws IOException If an error occurs while loading the FXML file.
     */
    public static void promptForServerMoves(@NonNull Pane container, HumanServer inTraining, BoardController boardController) throws IOException {
        log.debug("promptForServerMoves");

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

        createModalStage("Server Moves", container, root, MODAL_TIMEOUT_DURATION);
    }

    /**
     * Prompts the user to retry a server action using a retry card.
     * Displays a modal dialog asking the user if they want to use a retry card.
     * If no response is given within 45 seconds, the default response is "Yes".
     *
     * @param server The server for which the retry is being prompted.
     * @return True if the user chooses to use a retry card, false otherwise.
     */
    @SuppressWarnings({"java:S1190", "java:S117"})
    public static boolean promptForServerRetry(@NonNull Server server) {
        log.debug("promptForServerRetry");

        String retryMessage = "Your first try failed for Server " + server.getColor().name()
                + System.lineSeparator() + "You have a Retry card, would you like to use it? 'Y/N'";

        if (Platform.isFxApplicationThread()) {
            Text text = new Text(retryMessage);
            text.setWrappingWidth(105);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.getDialogPane().setContent(text);
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            alert.setTitle("Retry");
            alert.setHeaderText(null);
            if (timedRun) {
                Timeline countdownAnim = startCountdownTimer(ALERT_TIMEOUT_DURATION);
                Timeline idleStage = new Timeline(new KeyFrame(Duration.seconds(ALERT_TIMEOUT_DURATION), _ -> {
                    alert.setResult(ButtonType.YES);
                    alert.hide();
                }));
                idleStage.setCycleCount(1);
                idleStage.play();
                ButtonType button = alert.showAndWait().orElse(null);
                idleStage.stop();
                stopCountdownTimer(countdownAnim);
                return button == null || button == ButtonType.YES;
            } else {
                ButtonType button = alert.showAndWait().orElse(null);
                return button == ButtonType.YES;
            }
        } else {
            CompletableFuture<Boolean> result = new CompletableFuture<>();
            Platform.runLater(() -> {
                Text text = new Text(retryMessage);
                text.setWrappingWidth(105);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.getDialogPane().setContent(text);
                alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
                alert.setTitle("Retry");
                alert.setHeaderText(null);
                if (timedRun) {
                    Timeline countdownAnim = startCountdownTimer(ALERT_TIMEOUT_DURATION);
                    Timeline idleStage = new Timeline(new KeyFrame(Duration.seconds(ALERT_TIMEOUT_DURATION), _ -> {
                        alert.setResult(ButtonType.YES);
                        alert.hide();
                    }));
                    idleStage.setCycleCount(1);
                    idleStage.play();
                    alert.setOnHidden(_ -> {
                        idleStage.stop();
                        stopCountdownTimer(countdownAnim);
                        ButtonType clicked = alert.getResult();
                        Platform.runLater(() -> result.complete(clicked == null || clicked == ButtonType.YES));
                    });
                } else {
                    alert.setOnHidden(_ -> {
                        ButtonType clicked = alert.getResult();
                        Platform.runLater(() -> result.complete(clicked == ButtonType.YES));
                    });
                }
                alert.show();
            });
            return awaitFuture(result);
        }
    }

    /**
     * Prompts the user to estimate their work for the week.
     * Loads the "submit-estimate.fxml" file, retrieves the submit button from the controller,
     * and displays a modal dialog with a timeout that automatically triggers the submit button.
     *
     * @param container The parent container for the modal dialog.
     * @throws IOException If an error occurs while loading the FXML file.
     */
    public static void promptForWorkItemEstimates(@NonNull Pane container) throws IOException {
        log.debug("promptForWorkItemEstimates");
        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("submit-estimate.fxml"));
        Parent root = loader.load();

        Button submitButton = ((EstimateController) loader.getController()).getEstimateButton();

        createModalStageWithButton("Estimate your Week", container, root, submitButton);
    }



    /**
     * Prompts the user to move initial work items from the backlog to the first workstation.
     * Displays a modal dialog where the user can specify the number of items to move.
     * If the backlog is empty, a message is displayed, and no action is taken.
     *
     * @param container    The parent container for the modal dialog.
     * @param startValue   The maximum number of items that can be moved.
     * @param backlogCount The current number of items in the backlog.
     * @param gameBoardLog The log area to display game-related messages.
     * @throws IOException If an error occurs while loading the FXML file.
     */
    protected static void promptForWorkItemInitialMoves(@NonNull Pane container, int startValue, int backlogCount, @NonNull TextArea gameBoardLog) throws IOException {
        log.debug("promptForWorkItemInitialMoves");

        if (backlogCount <= 0) {
            String gameBoardLogText = "Backlog is Empty" + System.lineSeparator() + System.lineSeparator() + "No moves possible";
            alertWithGameBoardUpdate(gameBoardLog, gameBoardLogText, ALERT_TIMEOUT_DURATION);
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
        log.debug("Work Item Initial Moves Modal launch with maxIntToMove: {}", maxIntToMove);
        createModalStageWithButton("Move Items", container, root, submitButton);
    }

    /**
     * Prompts the user to move work items from a specified workstation to the next.
     * Displays a modal dialog where the user can specify the number of items to move.
     * If the workstation is the last one (position 4), the items are moved to finished goods.
     *
     * @param container           The parent container for the modal dialog.
     * @param workstation         The workstation from which the items are being moved.
     * @param workstationPosition The position of the workstation in the workflow (1-4).
     * @throws IOException If an error occurs while loading the FXML file.
     */
    protected static void promptForWorkItemWorkstationMoves(@NonNull Pane container, Workstation workstation, int workstationPosition) throws IOException {
        log.debug("promptForWorkItemWorkstationMoves");

        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("move-work-item.fxml"));
        Parent root = loader.load();

        StringBuilder builder = new StringBuilder();
        final int maxIntToMove = Math.min(workstation.getCapacity(), workstation.getWorkItemCount());
        log.debug("maxIntToMove: {}  -- workstation capacity: {} -- work item count {}", maxIntToMove, workstation.getCapacity(), workstation.getWorkItemCount());

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
        createModalStageWithButton("Move Items", container, root, submitButton);
    }

    /**
     * Prompts the user to add a skill to a server.
     * Displays a modal dialog where the user can select a server and a skill to assign.
     * Only human servers are eligible for skill assignment.
     *
     * @param container The parent container for the modal dialog.
     * @throws IOException If an error occurs while loading the FXML file.
     */
    protected static void promptToAddSkill(@NonNull Pane container) throws IOException {
        log.debug("promptToAddSkill");

        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("add-skills.fxml"));
        Parent root = loader.load();

        // Instructions for the user on how to add a skill
        String builder = "Choose which server to add a skill and which skill to add. " + System.lineSeparator() + System.lineSeparator() +
                " At prompting please enter: Server Color>Skill Color." +
                " You can only assign to Human Servers!" + System.lineSeparator() + System.lineSeparator() +
                " Example BLUE>YELLOW  will assign the BLUE HUMAN SERVER a YELLOW skill";

        // Set the instructions text in the dialog
        TextArea node = ((SkillsController) loader.getController()).getSkillAddText();
        node.setText(builder);

        // Populate the server color picker with available human servers
        ComboBox<Color> serverColorPicker = ((SkillsController) loader.getController()).getServerToAddSkills();
        buildColorComboBox(serverColorPicker, Color.humanColorValues());

        // Populate the skill color picker with available skills
        ComboBox<Color> skillColorPicker = ((SkillsController) loader.getController()).getSkillsToAddToServer();
        buildColorComboBox(skillColorPicker, Color.humanColorValues());

        // Display the modal dialog
        createModalStage("Add Skills to Server", container, root, MODAL_TIMEOUT_DURATION);
    }

    /**
     * Prompts the user to augment the capacity of a workstation.
     * Displays a modal dialog where the user can select a workstation and specify
     * whether to double its capacity or add one to its capacity.
     *
     * @param container   The parent container for the modal dialog.
     * @param timesTwo    A flag indicating whether to double the workstation's capacity (true)
     *                    or add one to its capacity (false).
     * @param maxCapacity The maximum allowable capacity for the workstation.
     * @throws IOException If an error occurs while loading the FXML file.
     */
    protected static void promptToAugmentWorkstationCapacity(@NonNull Pane container, boolean timesTwo, int maxCapacity) throws IOException {
        log.debug("promptToAugmentWorkstationCapacity");

        // Load the FXML file for the "Add Capacity" dialog
        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("add-capacity.fxml"));
        Parent root = loader.load();

        // Retrieve the workstation to augment (specifically the BLUE workstation)
        Workstation workstationBlue = Objects.requireNonNull(WorkstationService.getWorkstation(Color.BLUE));

        // Set the descriptive text for the capacity augmentation in the dialog
        TextArea node = ((AddedCapacityController) loader.getController()).getAddCapacityText();
        node.setText(getWorkstationCapacityText(timesTwo, workstationBlue, maxCapacity));

        // Populate the color picker with available workstation colors
        ComboBox<Color> workstationToAddCapacity = ((AddedCapacityController) loader.getController()).getWorkstationToAddCapacity();
        buildColorComboBox(workstationToAddCapacity, Color.humanColorValues());

        // Display the modal dialog
        createModalStage("Add Capacity", container, root, MODAL_TIMEOUT_DURATION);
    }

    /**
     * Generates a descriptive text for augmenting the capacity of a workstation.
     * Depending on the `timesTwo` flag, the text either describes adding one to the capacity
     * or doubling the capacity of the specified workstation.
     *
     * @param timesTwo        A flag indicating whether to double the workstation's capacity (true)
     *                        or add one to its capacity (false).
     * @param workstationBlue The workstation whose capacity is being augmented.
     * @param maxCapacity     The maximum allowable capacity for the workstation.
     * @return A string describing the capacity augmentation action.
     */
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

    /**
     * Prompts the user to automate a workstation.
     * Displays a modal dialog where the user can select a workstation to automate.
     * If all automated servers are already deployed, it updates the game board log
     * with a message and exits.
     *
     * @param container    The parent container for the modal dialog.
     * @param gameBoardLog The log area to display game-related messages.
     * @throws IOException If an error occurs while loading the FXML file.
     */
    protected static void promptToAutomateWorkstation(@NonNull Pane container, @NonNull TextArea gameBoardLog) throws IOException {
        log.debug("promptToAutomateWorkstation");

        // Check if all automated servers are already deployed
        if (WorkstationService.findDeployedAutomatedServers().size() == 3) {
            gameBoardLog.setText("No Robots Left!" + System.lineSeparator() + "There are no workstations available to automate");
            return;
        }

        // Load the FXML file for the automation dialog
        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("add-automation.fxml"));
        Parent root = loader.load();

        // Build the instructions text for the dialog
        String builder = "Choose which color workstation to add automation " + System.lineSeparator() + System.lineSeparator()
                + Color.GREEN.name() + "|" + Color.ROSE.name() + "|" + Color.YELLOW.name() + System.lineSeparator() + System.lineSeparator()
                + "Example: GREEN will will automate the GREEN workstation. The human server will remain until moved";

        // Set the instructions text in the dialog
        TextArea node = ((AddAutomationController) loader.getController()).getAddAutomationText();
        node.setText(builder);

        // Populate the color picker with available workstation colors
        ComboBox<Color> serverWorkstationColorPicker = ((AddAutomationController) loader.getController()).getWorkstationToAddAutomation();
        List<Color> serverColors = WorkstationService.findDeployedAutomatedServers().stream().map(Server::getColor).toList();
        List<Color> leftoverColors = Arrays.stream(Color.automatedColorValues()).filter(color -> !serverColors.contains(color)).toList();
        buildColorComboBox(serverWorkstationColorPicker, leftoverColors.toArray(Color[]::new));

        // Display the modal dialog
        createModalStage("Automate Workstation", container, root, MODAL_TIMEOUT_DURATION + 5);
    }

    /**
     * Prompts the user to draw a skills card.
     * Loads the "skill-card.fxml" file, displays the card details in a modal window,
     * and returns whether the skill card action was successful.
     *
     * @param container The parent container for the modal dialog.
     * @return True if the skill card action was successful, false otherwise.
     * @throws IOException If an error occurs while loading the FXML file.
     */
    protected static boolean promptToDrawSkillsCard(@NonNull Pane container) throws IOException {
        log.debug("promptToDrawSkillsCard");
        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("skill-card.fxml"));
        Parent root = loader.load();

        // Pick a skill card and set its details in the controller
        SkillCard skillCard = (SkillCard) CardService.pickACard(Card.SKILLS);
        ((SkillCardController) loader.getController()).getCardSkill().setText(skillCard.getSkill());
        ((SkillCardController) loader.getController()).getCardInstructions().setText(skillCard.getInstructions());
        ((SkillCardController) loader.getController()).getCardInstructionsExtended().setText(skillCard.getInstructionsExtended());
        ((SkillCardController) loader.getController()).getIsSuccessful().setText(String.valueOf(skillCard.isSuccess()));

        // Display the modal dialog
        createModalStage("Skills", container, root, MODAL_TIMEOUT_DURATION);
        return skillCard.isSuccess();
    }

    /**
     * Prompts the user to remove a skill from the most skilled server.
     * Displays an informational alert and removes the skill from the server.
     */
    protected static void promptToRemoveSkill() {
        log.debug("promptToRemoveSkill");
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Replace skilled server", ButtonType.FINISH);
        alert.setTitle("Skills");
        alert.setHeaderText(null);

        // Remove skills from the most skilled server
        ServerService.removeSkillsFromMostSkilledServer();
    }

    /**
     * Prompts the user to upload a file for a previous game.
     * Displays a modal dialog where the user can select a file from their system.
     *
     * @param container        The parent container for the modal dialog.
     * @param initialDirectory The initial directory to open in the file chooser.
     * @return The selected file, or null if no file was selected.
     */
    public static File promptToUploadPreviousGame(@NonNull Pane container, @NonNull File initialDirectory) {
        // Create a new stage for the dialog
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.UTILITY);
        dialogStage.initOwner(container.getScene().getWindow());
        dialogStage.setTitle("Upload Previous Game");
        dialogStage.setHeight(300);
        dialogStage.setWidth(300);

        // Label to display the selected file path
        Label filePathLabel = new Label();
        filePathLabel.setWrapText(true);

        // Atomic reference to store the selected file
        AtomicReference<File> selectedFile = new AtomicReference<>();

        // Button to open the file chooser
        Button chooseFileButton = new Button("Choose Game File");
        chooseFileButton.setOnAction(_ -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(initialDirectory);
            File file = fileChooser.showOpenDialog(dialogStage);
            if (file != null) {
                selectedFile.set(file);
                filePathLabel.setText(file.getAbsolutePath());
            }
        });

        // Button to close the dialog
        Button closeButton = new Button("Load File");
        closeButton.setOnAction(_ -> dialogStage.close());

        // Layout for the dialog
        VBox layout = new VBox(MODAL_TIMEOUT_DURATION, filePathLabel, chooseFileButton, closeButton);
        layout.setPadding(new Insets(20));
        layout.setPrefWidth(300);

        // Set the scene and display the dialog
        dialogStage.setScene(new Scene(layout));
        dialogStage.showAndWait();

        // Return the selected file
        return selectedFile.get();
    }

    /**
     * Publishes the end-of-game message to the game board log.
     *
     * @param gameBoardLog The log area to display the end-of-game message.
     */
    protected static void publishEndOfGame(TextArea gameBoardLog) {
        String gameBoardLogText = "End of Game." + System.lineSeparator() + "Assess and discuss how you did";
        alertWithGameBoardUpdate(gameBoardLog, gameBoardLogText, END_OF_GAME_TIMEOUT_DURATION);
    }

    /**
     * Publishes the end-of-week message to the game board log.
     *
     * @param gameBoardLog The log area to display the end-of-week message.
     */
    protected static void publishEndPeriod(TextArea gameBoardLog) {
        String gameBoardLogText = "End of Week." + System.lineSeparator() + "Prepare for the start of the next week";
        alertWithGameBoardUpdate(gameBoardLog, gameBoardLogText, END_PERIOD_TIMEOUT_DURATION);
    }

    /**
     * Publishes the start-of-week message to the game board log.
     *
     * @param gameBoardLog The log area to display the start-of-week message.
     * @param period       The current week number.
     */
    protected static void publishStartPeriod(TextArea gameBoardLog, int period) {
        String gameBoardLogText = "Week: " + period + System.lineSeparator() + START_THE_WEEK;
        alertWithGameBoardUpdate(gameBoardLog, gameBoardLogText, ALERT_TIMEOUT_DURATION);
    }

    /**
     * Publishes the start-of-day message to the game board log.
     *
     * @param gameBoardLog The log area to display the start-of-day message.
     * @param period       The current week number.
     * @param turn         The current day number within the week.
     */
    protected static void publishTurnStart(TextArea gameBoardLog, int period, int turn) {
        String gameBoardLogText = "Day: " + turn + "  Week:  " + period + System.lineSeparator() + "Click on Run Day to start the day";
        alertWithGameBoardUpdate(gameBoardLog, gameBoardLogText, MODAL_TIMEOUT_DURATION);
    }

    /**
     * Plays a server chance card based on the type of server and the workstation's state.
     * If the workstation has no work items, it logs a message and returns an empty result.
     * Otherwise, it picks a chance card, displays it in a modal window, and returns the result
     * of the chance card play.
     *
     * @param container    The parent container for the modal window.
     * @param server       The server attempting the chance card play.
     * @param workstation  The workstation associated with the server.
     * @param gameBoardLog The log area to display game-related messages.
     * @return A `ChanceResult` indicating the outcome of the chance card play.
     * @throws IOException If an error occurs while loading the FXML file.
     */
    protected static ChanceResult serverChanceCardPlay(@NonNull Pane container, @NonNull Server server, Workstation workstation, @NonNull TextArea gameBoardLog) throws IOException {

        // Check if the workstation has no work items
        if (workstation.getWorkItemCount() == 0) {
            String gameBoardLogText = "No Moves." + System.lineSeparator() + workstation.getColor() + " " + WORKSTATION_IS_EMPTY_NO_MOVES_ARE_POSSIBLE;
            alertWithGameBoardUpdate(gameBoardLog, gameBoardLogText, ALERT_TIMEOUT_DURATION);
            return ChanceResult.EMPTY;
        }

        // Determine the type of chance card to pick based on the server type
        ChanceCard chanceCard;
        FXMLLoader loader;
        if (server instanceof AutomatedServer) {
            chanceCard = (ChanceRobotCard) CardService.pickACard(Card.AUTOMATED_CHANCE);
            loader = new FXMLLoader(ThroughputApplication.class.getResource("chance-robot-card.fxml"));
        } else {
            chanceCard = (ChanceCard) CardService.pickACard(Card.CHANCE);
            loader = new FXMLLoader(ThroughputApplication.class.getResource("chance-card.fxml"));
        }

        // Load the FXML file for the chance card modal
        Parent root = loader.load();

        // Set the chance card instructions and details in the modal
        ((ChanceController) loader.getController()).getCardInstructions().setText(chanceCard.getInstructions());
        ((ChanceController) loader.getController()).getCardChance().setText(workstation.getColor().name() + " - " + chanceCard.getChanceText());

        // Display the modal window
        createModalStage("Chance", container, root, MODAL_TIMEOUT_DURATION);

        // Return the result of the chance card play
        return chanceCard.isSuccess() ? ChanceResult.SUCCESS : ChanceResult.FAILED;
    }

    /**
     * Displays the "Game Information" card in a modal window.
     *
     * @param container The parent container for the modal window.
     * @throws IOException If an error occurs while loading the FXML file.
     */
    protected static void showInfoCard(Pane container) throws IOException {
        // Create a new stage for the modal window
        Stage stage = new Stage();

        // Load the FXML file for the "Game Information" card
        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("info.fxml"));
        Parent root = loader.load();

        // Set the scene for the stage with the loaded FXML content
        stage.setScene(new Scene(root));

        // Set the title of the modal window
        stage.setTitle("Game Information");

        // Configure the stage to be a modal window
        stage.initModality(Modality.WINDOW_MODAL);

        // Set the owner of the modal window to the parent container's window
        stage.initOwner(container.getScene().getWindow());

        // Display the modal window
        stage.show();
    }

    /**
     * Displays the "Game Rules" card in a modal window.
     *
     * @param container The parent container for the modal window.
     * @throws IOException If an error occurs while loading the FXML file.
     */
    protected static void showRulesCard(Pane container) throws IOException {
        // Create a new stage for the modal window
        Stage stage = new Stage();

        // Load the FXML file for the "Game Rules" card
        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("rules.fxml"));
        Parent root = loader.load();

        // Set the scene for the stage with the loaded FXML content
        stage.setScene(new Scene(root));

        // Set the title of the modal window
        stage.setTitle("Game Rules");

        // Configure the stage to be a modal window
        stage.initModality(Modality.WINDOW_MODAL);

        // Set the owner of the modal window to the parent container's window
        stage.initOwner(container.getScene().getWindow());

        // Display the modal window
        stage.show();
    }

    /**
     * Calculates the team mood by rolling a die, determines the maximum number of items
     * that can be moved from the backlog to the first workstation, and displays the result
     * in a modal dialog.
     *
     * @param container The parent container for the modal dialog.
     * @param dieSides  The number of sides on the die to be rolled.
     * @return The maximum number of items that can be moved to the first workstation.
     * @throws IOException If an error occurs while loading the FXML file or resources.
     */
    protected static int teamMood(@NonNull Pane container, int dieSides) throws IOException {
        // Roll a die to determine the team mood
        int teamMood = DiceService.rollDie(DiceService.getDie(dieSides)).getValue();

        // Get the current backlog count
        int backlogCount = ScorecardService.BACKLOG.getBacklogItemCount();

        // Calculate the maximum number of items that can be moved
        int maxItemsToMove = Math.min(teamMood, backlogCount);

        // Load the FXML file for the die-roll modal dialog
        FXMLLoader loader = new FXMLLoader(ThroughputApplication.class.getResource("die-roll.fxml"));
        Parent root = loader.load();

        // Configure the modal dialog with the team mood and backlog information
        DieController controller = loader.getController();
        controller.getDieText().setText("Team Mood is " + teamMood + " and backlog has " + backlogCount
                + " work items. At the prompt, you can move up to " + maxItemsToMove + " items into your 1st workstation.");
        controller.getDieHeaderText().setText("Rolled for Team Mood");
        controller.getDieImage().setImage(new Image(Objects.requireNonNull(ThroughputApplication.class.getResource(getDieImage(teamMood))).openStream()));

        // Display the modal dialog
        createModalStage("Team Mood", container, root, MODAL_TIMEOUT_DURATION);

        // Return the maximum number of items that can be moved
        return maxItemsToMove;
    }

}
