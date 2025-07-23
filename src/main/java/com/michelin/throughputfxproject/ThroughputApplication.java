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

package com.michelin.throughputfxproject;

import com.michelin.throughputfxproject.entities.state.Board;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
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
    public static final int DEFAULT_DIE_SIDES = 6;
    public static final int DEFAULT_RUN_STATIONS = 5;
    public static final int DEFAULT_RUN_PERIODS = 5;
    public static final int DEFAULT_RUN_TURNS = 5;

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
      options.addOption("sides", false, "Die Sides");
      options.addOption("stations", false, "Number Workstations");
      options.addOption("periods", false, "Number Periods");
      options.addOption("turns", false, "Number Turns");
      options.addOption("configType", false, "Configuration Type (e.g., 'restaurant', 'management')");

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

        Scene scene = new Scene(fxmlLoader.load(), 1000, 1000);
        scene.getStylesheets().add(Objects.requireNonNull(ThroughputApplication.class.getResource("css/throughput.css")).toExternalForm());
        stage.setTitle("Throughput");
        stage.setScene(scene);
        stage.show();
    }


}