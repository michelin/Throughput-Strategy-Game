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

module com.michelin.throughputfxproject {
    requires javafx.controls;
    requires javafx.base;
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
    opens com.michelin.throughputfxproject.controllers to javafx.fxml;
    opens com.michelin.throughputfxproject.control to javafx.fxml;
    exports com.michelin.throughputfxproject.entities;
    opens com.michelin.throughputfxproject.entities to javafx.fxml;
    opens com.michelin.throughputfxproject.entities.state to javafx.fxml;

}
