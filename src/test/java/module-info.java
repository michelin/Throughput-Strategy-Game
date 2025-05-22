module com.michelin.throughputfxproject.controllers {
    requires com.michelin.throughputfxproject;
    requires org.mockito.junit.jupiter;
    requires org.mockito;
    requires org.junit.jupiter.api;
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;

    opens com.michelin.throughputfxproject.controllerTests to org.junit.platform.commons;
}