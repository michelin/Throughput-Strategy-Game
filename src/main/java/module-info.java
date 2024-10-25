module com.michelin.throughputfxproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.kordamp.bootstrapfx.core;
    requires org.apache.commons.lang3;
    requires static lombok;
    requires com.opencsv;
    requires java.sql;
    requires ch.qos.logback.core;
    requires org.slf4j;
    requires java.desktop;

    opens com.michelin.throughputfxproject to javafx.fxml;
    exports com.michelin.throughputfxproject;
    exports com.michelin.throughputfxproject.entities;
    exports com.michelin.throughputfxproject.entities.cards;
    exports com.michelin.throughputfxproject.entities.servers;
    exports com.michelin.throughputfxproject.exceptions;
    exports com.michelin.throughputfxproject.services;
    exports com.michelin.throughputfxproject.controllers;
    exports com.michelin.throughputfxproject.control;
    opens com.michelin.throughputfxproject.css to javafx.fxml;
    opens com.michelin.throughputfxproject.controllers to javafx.fxml;
    opens com.michelin.throughputfxproject.control to javafx.fxml;
}