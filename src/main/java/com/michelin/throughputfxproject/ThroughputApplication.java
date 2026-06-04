/*
 * Copyright 2025 Manufacture Française des Pneumatiques Michelin
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.michelin.throughputfxproject;

import com.michelin.throughputfxproject.entities.state.Board;
import javafx.application.Platform;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;

import java.io.*;
import java.util.Objects;
import java.util.Properties;

/**
 * The main application class for the ThroughputFX project.
 * This class extends the JavaFX `Application` class and serves as the entry point
 * for the JavaFX application. It includes the `main` method to initialize the application
 * and the `start` method to set up the primary stage.
 */
@NoArgsConstructor()
@EqualsAndHashCode(callSuper = true)
@ToString
@Slf4j
public class ThroughputApplication extends Application {
    private static final String PRISM_ORDER = "prism.order";
    public static final int DEFAULT_DIE_SIDES = 6;
    public static final int DEFAULT_RUN_STATIONS = 5;
    public static final int DEFAULT_RUN_PERIODS = 5;
    public static final int DEFAULT_RUN_TURNS = 5;
    private static final double RESIZE_BORDER = 14.0;

    private enum ResizeDirection {
        NONE,
        N,
        S,
        E,
        W,
        NE,
        NW,
        SE,
        SW
    }

    private static boolean isWslEnvironment() {
        String wslDistro = System.getenv("WSL_DISTRO_NAME");
        String wslInterop = System.getenv("WSL_INTEROP");
        return (wslDistro != null && !wslDistro.isBlank()) || (wslInterop != null && !wslInterop.isBlank());
    }

    /**
     * The main entry point for the ThroughputFX application.
     * This method initializes the application by parsing command-line arguments,
     * loading configuration properties, and setting up the application state.
     * Finally, it launches the JavaFX application.
     *
     * @param args Command-line arguments passed to the application.
     */
    public static void main(String[] args) {

      Options options = new Options();
      options.addOption(Option.builder().longOpt("sides").hasArg().desc("Die Sides").build());
      options.addOption(Option.builder().longOpt("stations").hasArg().desc("Number Workstations").build());
      options.addOption(Option.builder().longOpt("periods").hasArg().desc("Number Periods").build());
      options.addOption(Option.builder().longOpt("turns").hasArg().desc("Number Turns").build());
      options.addOption(Option.builder().longOpt("configType").hasArg().desc("Configuration Type (e.g., 'restaurant', 'management')").build());

      CommandLine cmd;
      try {
          cmd = new DefaultParser().parse(options, args);
      } catch (ParseException e) {
          log.info(e.getMessage());
          System.exit(1);
          return;
      }

      int sides = getAnInt(cmd.getOptionValue("sides"), DEFAULT_DIE_SIDES);
      int stations = getAnInt(cmd.getOptionValue("stations"), DEFAULT_RUN_STATIONS);
      int periods = getAnInt(cmd.getOptionValue("periods"), DEFAULT_RUN_PERIODS);
      int turns = getAnInt(cmd.getOptionValue("turns"), DEFAULT_RUN_TURNS);

      log.debug("The arguments are sides: {} stations: {} periods: {} turns: {}", sides, stations, periods, turns);

      // JavaFX on Linux/Wayland can occasionally show a window frame without painting scene content.
      // Prefer software rendering in that environment unless the user explicitly overrides prism.order.
      String osName = System.getProperty("os.name", "").toLowerCase();
      String waylandDisplay = System.getenv("WAYLAND_DISPLAY");
            boolean isWsl = isWslEnvironment();

      if (isWsl) {
          // WSLg can report a valid window while failing to paint JavaFX scene graph with GPU pipelines.
          // These defaults keep rendering in software and stabilize frame presentation.
          if (System.getProperty(PRISM_ORDER) == null) {
              System.setProperty(PRISM_ORDER, "sw");
          }
          if (System.getProperty("prism.lcdtext") == null) {
              System.setProperty("prism.lcdtext", "false");
          }
          if (System.getProperty("sun.java2d.xrender") == null) {
              System.setProperty("sun.java2d.xrender", "false");
          }
          log.info("Detected WSL environment; applying JavaFX software rendering defaults");
      }

      if (osName.contains("linux")
              && waylandDisplay != null
              && !waylandDisplay.isBlank()
              && System.getProperty(PRISM_ORDER) == null) {
          System.setProperty(PRISM_ORDER, "sw");
          log.info("Detected Wayland; forcing JavaFX software rendering via prism.order=sw");
      }

        String configType = cmd.getOptionValue("configType");
        String configFileName = (configType != null && !configType.isEmpty())
                ? String.format("config-%s.properties", configType)
                : "config.properties";

      try (InputStream input = ThroughputApplication.class.getResourceAsStream(configFileName)) {
          if (input != null) {
              Properties propertiesFile = new Properties();
              propertiesFile.load(input);

              if (log.isDebugEnabled()) {
                  log.debug(propertiesFile.getProperty("run.topic"));
                  log.debug(propertiesFile.getProperty("period.label"));
                  log.debug(propertiesFile.getProperty("run.label"));
              }

              System.getProperties().putAll(propertiesFile);
          } else {
              log.error("config.properties not found");
          }
      } catch (IOException ex) {
          log.error("Problem loading properties", ex);
      }

      Board.initializeInstance(sides, stations, periods, turns);
      launch();
    }

    /**
     * Parses a string argument into an integer. If the argument is null or cannot be parsed,
     * the method returns a default integer value.
     *
     * @param arg1           The string argument to parse.
     * @param defaultInteger The default integer value to return if parsing fails.
     * @return The parsed integer value or the default value if parsing fails.
     */
    private static int getAnInt(String arg1, int defaultInteger) {
        if (arg1 != null) {
            try {
                return Integer.parseInt(arg1);
            } catch (NumberFormatException e) {
                log.info(e.getMessage());
            }
        }
        return defaultInteger;
    }

    /**
     * Starts the JavaFX application by loading the main scene from an FXML file.
     * The method sets the scene's dimensions, applies a CSS stylesheet, and displays the stage.
     *
     * @param stage The primary stage for this application.
     * @throws IOException If the FXML file or CSS stylesheet cannot be loaded.
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ThroughputApplication.class.getResource("board.fxml"));
        Pane root = fxmlLoader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(ThroughputApplication.class.getResource("css/throughput.css")).toExternalForm());

        var boardController = fxmlLoader.<com.michelin.throughputfxproject.controllers.BoardController>getController();

        stage.setTitle("Throughput");
        stage.setResizable(true);
        stage.setScene(scene);
        installEdgeResizeFallback(stage, scene, RESIZE_BORDER);
        stage.setOnShown(_ -> {
            log.info("Primary stage shown at x={}, y={}, w={}, h={}", stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
            Platform.runLater(() -> {
                boardController.redrawBoard();
                boardController.resizeStageToFitOuterScoreBox();
            });
        });
        stage.show();
    }

    private void installEdgeResizeFallback(Stage stage, Scene scene, double borderSize) {
        final ResizeDirection[] activeDirection = {ResizeDirection.NONE};
        final double[] pressScreenX = {0};
        final double[] pressScreenY = {0};
        final double[] startX = {0};
        final double[] startY = {0};
        final double[] startWidth = {0};
        final double[] startHeight = {0};

        scene.setOnMouseMoved(event -> {
            ResizeDirection direction = getResizeDirection(event.getX(), event.getY(), scene.getWidth(), scene.getHeight(), borderSize);
            scene.setCursor(toCursor(direction));
        });

        scene.setOnMousePressed(event -> {
            activeDirection[0] = getResizeDirection(event.getX(), event.getY(), scene.getWidth(), scene.getHeight(), borderSize);
            pressScreenX[0] = event.getScreenX();
            pressScreenY[0] = event.getScreenY();
            startX[0] = stage.getX();
            startY[0] = stage.getY();
            startWidth[0] = stage.getWidth();
            startHeight[0] = stage.getHeight();
        });

        scene.setOnMouseDragged(event -> {
            ResizeDirection direction = activeDirection[0];
            if (direction == ResizeDirection.NONE) {
                return;
            }

            double dx = event.getScreenX() - pressScreenX[0];
            double dy = event.getScreenY() - pressScreenY[0];

            if (direction == ResizeDirection.E || direction == ResizeDirection.NE || direction == ResizeDirection.SE) {
                stage.setWidth(Math.max(stage.getMinWidth(), startWidth[0] + dx));
            }
            if (direction == ResizeDirection.S || direction == ResizeDirection.SE || direction == ResizeDirection.SW) {
                stage.setHeight(Math.max(stage.getMinHeight(), startHeight[0] + dy));
            }
            if (direction == ResizeDirection.W || direction == ResizeDirection.NW || direction == ResizeDirection.SW) {
                double newWidth = Math.max(stage.getMinWidth(), startWidth[0] - dx);
                double widthDelta = startWidth[0] - newWidth;
                stage.setX(startX[0] + widthDelta);
                stage.setWidth(newWidth);
            }
            if (direction == ResizeDirection.N || direction == ResizeDirection.NE || direction == ResizeDirection.NW) {
                double newHeight = Math.max(stage.getMinHeight(), startHeight[0] - dy);
                double heightDelta = startHeight[0] - newHeight;
                stage.setY(startY[0] + heightDelta);
                stage.setHeight(newHeight);
            }
        });
    }

    private static ResizeDirection getResizeDirection(double x, double y, double width, double height, double border) {
        boolean left = x <= border;
        boolean right = x >= width - border;
        boolean top = y <= border;
        boolean bottom = y >= height - border;

        if (left && top) return ResizeDirection.NW;
        if (right && top) return ResizeDirection.NE;
        if (left && bottom) return ResizeDirection.SW;
        if (right && bottom) return ResizeDirection.SE;
        if (left) return ResizeDirection.W;
        if (right) return ResizeDirection.E;
        if (top) return ResizeDirection.N;
        if (bottom) return ResizeDirection.S;
        return ResizeDirection.NONE;
    }

    private static Cursor toCursor(ResizeDirection direction) {
        return switch (direction) {
            case N -> Cursor.N_RESIZE;
            case S -> Cursor.S_RESIZE;
            case E -> Cursor.E_RESIZE;
            case W -> Cursor.W_RESIZE;
            case NE -> Cursor.NE_RESIZE;
            case NW -> Cursor.NW_RESIZE;
            case SE -> Cursor.SE_RESIZE;
            case SW -> Cursor.SW_RESIZE;
            case NONE -> Cursor.DEFAULT;
        };
    }


}