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
