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
package com.michelin.throughputfxproject.entities.cards;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.File;

@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChanceCard implements Card {

    @CsvBindByName(column = "copies")
    private int copies;
    @CsvBindByName(column = "success")
    private boolean success;
    @CsvBindByName(column = "chance")
    private String chanceText;
    @CsvBindByName(column = "chance_style")
    private String chanceStyle;
    @CsvBindByName(column = "instructions")
    private String instructions;

    @Override
    public File getBackImage() {
        return new File("./cards/Chance.jpg");
    }

    @Override
    public String getType() {
        return Card.CHANCE;
    }

    @Override
    public String getTitle() {
        return Card.CHANCE;
    }

    @Override
    public ChanceCard typedClone() {
        return ChanceCard.builder().copies(this.copies).success(this.success).chanceText(this.chanceText).chanceStyle(this.chanceStyle).instructions(this.instructions).build();
    }

    @Override
    public String toString() {
        return "Result: " + getChanceText() + System.lineSeparator() +
                "Instructions: " + getInstructions();
    }
}
