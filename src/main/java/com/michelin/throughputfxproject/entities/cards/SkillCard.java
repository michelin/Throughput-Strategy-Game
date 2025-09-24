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
package com.michelin.throughputfxproject.entities.cards;

import com.opencsv.bean.CsvBindByName;
import lombok.*;

import java.io.File;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SkillCard implements Card {

    @CsvBindByName(column = "copies")
    private int copies;
    @CsvBindByName(column = "skill")
    private String skill;
    @CsvBindByName(column = "skill_style")
    private String skillStyle;
    @CsvBindByName(column = "instructions")
    private String instructions;
    @CsvBindByName(column = "instructions_1")
    private String instructionsExtended;
    @CsvBindByName(column = "success")
    private boolean success;


    @Override
    public File getBackImage() {
        return new File("./cards/SkillTraining.jpg");
    }

    @Override
    public String getType() {
        return Card.SKILLS;
    }

    @Override
    public String getTitle() {
        return Card.SKILLS;
    }

    @Override
    public SkillCard typedClone() {
        return SkillCard.builder().copies(this.copies)
                .skill(this.skill)
                .skillStyle(this.skillStyle)
                .instructions(this.instructions)
                .instructionsExtended(this.instructionsExtended)
                .success(this.success)
                .build();
    }

    @Override
    public String toString() {
        return "Results: " + getSkill() + System.lineSeparator() +
                "Instructions: " + getInstructions() + System.lineSeparator() + getInstructionsExtended();
    }
}
