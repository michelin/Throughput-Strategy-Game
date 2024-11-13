module com.michelin.throughputfxproject.tests {
    requires com.michelin.throughputfxproject;
    requires org.mockito.junit.jupiter;
    requires org.mockito;
    requires org.junit.jupiter.api;
    requires javafx.controls;
    requires javafx.fxml;

    opens com.michelin.throughputfxproject.tests to org.junit.platform.commons;
}