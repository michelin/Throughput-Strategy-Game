package com.michelin.throughputfxproject;

import com.michelin.throughputfxproject.entities.state.Board;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import java.util.Properties;

public class ThroughputApplication extends Application {
    public static final int DEFAULT_DIE_SIDES = 6;
    public static final int DEFAULT_RUN_STATIONS = 5;
    public static final int DEFAULT_RUN_PERIODS = 5;
    public static final int DEFAULT_RUN_TURNS = 5;

    public static final Logger LOGGER = LoggerFactory.getLogger(ThroughputApplication.class.getName());

    public ThroughputApplication() {
        super();
    }

    public static void main(String[] args) {

        Options options = new Options();
        options.addOption("sides", false, "Die Sides");
        options.addOption("stations", false, "Number Workstations");
        options.addOption("periods", false, "Number Periods");
        options.addOption("turns", false, "Number Turns");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            LOGGER.info(e.getMessage());
            System.exit(1);
        }

        var arg1 = cmd.getOptionValue("sides");
        var arg2 = cmd.getOptionValue("stations");
        var arg3 = cmd.getOptionValue("periods");
        var arg4 = cmd.getOptionValue("turns");

        var sides = getAnInt(arg1, DEFAULT_DIE_SIDES);
        var stations = getAnInt(arg2, DEFAULT_RUN_STATIONS);
        var periods = getAnInt(arg3, DEFAULT_RUN_PERIODS);
        var turns = getAnInt(arg4, DEFAULT_RUN_TURNS);

        // Now you can use arg1, arg2, arg3, and arg4 in your program
        LOGGER.info("The sum of the arguments is: {} {} {} {}" , arg1 ,arg2 , arg3 , arg4);

        URL url = ThroughputApplication.class.getResource("config.properties");
        Objects.requireNonNull(url);
        try (InputStream input = new FileInputStream(new File(url.toURI()))) {

            Properties propertiesFile = new Properties();

            // load a properties file
            propertiesFile.load(input);

            // get the property value and print it out
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug(propertiesFile.getProperty("run.topic"));
                LOGGER.debug(propertiesFile.getProperty("period.label"));
                LOGGER.debug(propertiesFile.getProperty("run.label"));
            }

            System.getProperties().putAll(propertiesFile);

        } catch (IOException | URISyntaxException ex) {
            LOGGER.error("Problem loading properties",ex);
        }

        //Initiate the board
        Board.initializeInstance(sides, stations, periods, turns);
        launch();
    }

    private static int getAnInt(String arg1, int defaultInteger) {
        if (arg1 != null) {
            try {
                return Integer.parseInt(arg1);
            } catch (NumberFormatException e) {
                LOGGER.info(e.getMessage());
            }
        }
        return defaultInteger;

    }

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