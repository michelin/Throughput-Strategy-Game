/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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