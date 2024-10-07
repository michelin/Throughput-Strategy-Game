package com.michelin.throughputfxproject.exceptions;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThroughputRuntimeException extends RuntimeException {
    public static final Logger LOGGER = LoggerFactory.getLogger(ThroughputRuntimeException.class.getName());

    public ThroughputRuntimeException(Exception e) {
        LOGGER.error("Generic Error", e);
    }

    public ThroughputRuntimeException(IllegalArgumentException e) {
        LOGGER.error("IllegalArgumentException", e);
    }
}
