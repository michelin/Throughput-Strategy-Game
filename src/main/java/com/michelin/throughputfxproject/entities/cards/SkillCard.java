package com.michelin.throughputfxproject.entities.cards;

import com.michelin.throughputfxproject.entities.Card;
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
