package com.michelin.throughputfxproject.control;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

public class IntegerTextField extends TextField {
    public IntegerTextField() {
        TextFormatter<Integer> textFormatter = new TextFormatter<>(change -> {
            if (change.getText().matches("\\d*")) {
                try {
                    int value = Integer.parseInt(change.getControlNewText());
                    if (value >= 0 && value < 100) {
                        return change;
                    }
                } catch (NumberFormatException _) {
                    // Ignore
                }
            }
            return null;
        });
        setTextFormatter(textFormatter);
    }
}