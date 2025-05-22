module com.michelin.throughputfxproject {
    requires javafx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires org.apache.commons.lang3;
    requires static lombok;
    requires com.opencsv;
    requires org.slf4j;
    requires java.desktop;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires java.sql;
    // add icon pack modules
    requires org.kordamp.ikonli.fontawesome5;
    requires org.apache.commons.cli;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires javafx.fxml;


    opens com.michelin.throughputfxproject to javafx.fxml;
    exports com.michelin.throughputfxproject;
    exports com.michelin.throughputfxproject.entities.actions;
    exports com.michelin.throughputfxproject.entities.state;
    exports com.michelin.throughputfxproject.entities.cards;
    exports com.michelin.throughputfxproject.entities.servers;
    exports com.michelin.throughputfxproject.exceptions;
    exports com.michelin.throughputfxproject.services;
    exports com.michelin.throughputfxproject.controllers;
    exports com.michelin.throughputfxproject.control;
    opens com.michelin.throughputfxproject.css to javafx.fxml;
    opens com.michelin.throughputfxproject.controllers to javafx.fxml,com.michelin.throughputfxproject.controllerTests;
    opens com.michelin.throughputfxproject.control to javafx.fxml;
    exports com.michelin.throughputfxproject.entities;
    opens com.michelin.throughputfxproject.entities to javafx.fxml;
    opens com.michelin.throughputfxproject.entities.state to javafx.fxml;

}
