package com.michelin.throughputfxproject.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Die {
    private final int sides;
    private int value;

    public Die(int sides) {
        this.sides = sides;
    }

    @Override
    public String toString() {
        return "Die{" + "sides=" + getSides() +
                ", value=" + getValue() +
                '}';
    }
}
