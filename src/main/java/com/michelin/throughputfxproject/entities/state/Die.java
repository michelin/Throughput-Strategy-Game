package com.michelin.throughputfxproject.entities.state;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@EqualsAndHashCode
public class Die {

    @EqualsAndHashCode.Exclude
    private final int sides;
    /**
     * The amount of the die face showing after a roll
     */
    private int value;


}
