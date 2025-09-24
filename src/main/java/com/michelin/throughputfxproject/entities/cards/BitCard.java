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

import com.michelin.throughputfxproject.entities.state.Savable;
import com.opencsv.bean.CsvBindByName;
import lombok.*;

import java.io.File;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BitCard implements Card, Savable {

    @CsvBindByName(column = "id")
    private int id;
    @CsvBindByName(column = "copies")
    private int copies;
    @CsvBindByName(column = "type")
    private String bitType;
    @CsvBindByName(column = "title")
    private String title;
    @CsvBindByName(column = "title_style")
    private String titleStyle;
    @CsvBindByName(column = "subtitle")
    private String subtitle;
    @CsvBindByName(column = "subtitle_style")
    private String subtitleStyle;
    @CsvBindByName(column = "reason")
    private String reason;
    @CsvBindByName(column = "instructions")
    private String instructions;
    @CsvBindByName(column = "description_title")
    private String descriptionTitle;
    @CsvBindByName(column = "description_img")
    private String descriptionImg;
    @CsvBindByName(column = "description")
    private String description;
    @CsvBindByName(column = "action")
    private int action;
    @CsvBindByName(column = "counterCard")
    private int counterCard;


    @Override
    public File getBackImage() {
        return new File("cards/BIT.jpg");
    }

    @Override
    public String getType() {
        return Card.BOOSTER_INOCULATE_TRAP;
    }

    public String toJSON() {
        return String.valueOf(getId());
    }


    @Override
    public BitCard typedClone() {
        return BitCard.builder().id(this.id)
                .copies(this.copies)
                .bitType(this.bitType)
                .title(this.title)
                .titleStyle(this.titleStyle)
                .subtitle(this.subtitle)
                .subtitleStyle(this.subtitleStyle)
                .reason(this.reason)
                .instructions(this.instructions)
                .descriptionTitle(this.descriptionTitle)
                .descriptionImg(this.descriptionImg)
                .description(this.description)
                .action(this.action)
                .counterCard(this.counterCard)
                .build();
    }

    @Override
    public String toString() {
        return "{" +
                "type='" + getType() + '\'' +
                ", title='" + getTitle() + '\'' +
                ", subtitle='" + getSubtitle() + '\'' +
                ", reason='" + getReason() + '\'' +
                ", instructions='" + getInstructions() + '\'' +
                ", descriptionTitle='" + getDescriptionTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                '}';
    }
}
