module com.michelin.throughputfxproject.test {
    requires com.michelin.throughputfxproject;
    requires org.mockito.junit.jupiter;
    requires org.mockito;
    requires org.junit.jupiter.api;
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;


    opens com.michelin.throughputfxproject.test.controller to org.junit.platform.commons;
    opens com.michelin.throughputfxproject.test.service to org.junit.platform.commons;
    opens com.michelin.throughputfxproject.test.state to org.junit.platform.commons;
}