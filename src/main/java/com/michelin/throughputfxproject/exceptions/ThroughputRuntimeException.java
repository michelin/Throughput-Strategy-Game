package com.michelin.throughputfxproject.exceptions;


import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class ThroughputRuntimeException extends RuntimeException {
    public static final Logger LOGGER = LoggerFactory.getLogger(ThroughputRuntimeException.class.getName());

    public ThroughputRuntimeException(Exception e) {
        LOGGER.error("Generic Error", e);
    }

    public ThroughputRuntimeException(IllegalArgumentException e) {
        Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.CLOSE);
        alert.setTitle("Retry");
        alert.setHeaderText(null);
        alert.show();
        LOGGER.error("IllegalArgumentException", e);
    }
}
