package com.michelin.throughputfxproject.exceptions;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;


/**
 * The `ThroughputRuntimeException` class extends `RuntimeException` to provide
 * custom exception handling for throughput-related errors.
 * It includes constructors for handling generic exceptions and illegal argument exceptions,
 * with logging and optional user alerts.
 */
@Slf4j
@ToString(callSuper = true)
public class ThroughputRuntimeException extends RuntimeException {


    /**
     * Constructs a new `ThroughputRuntimeException` with the specified exception.
     * Logs the exception as a generic error.
     *
     * @param e The exception that caused this runtime exception.
     */
    public ThroughputRuntimeException(Exception e) {
        super(e);
        log.error("Generic Error", e);
    }

    /**
     * Constructs a new `ThroughputRuntimeException` with the specified `IllegalArgumentException`.
     * Logs the exception as an illegal argument error and displays an alert dialog with the error message.
     *
     * @param e The `IllegalArgumentException` that caused this runtime exception.
     */
    public ThroughputRuntimeException(IllegalArgumentException e) {
        super(e);
        log.error("IllegalArgumentException", e);

        Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.CLOSE);
        alert.setTitle("Retry");
        alert.setHeaderText(null);
        alert.show();
    }


}
