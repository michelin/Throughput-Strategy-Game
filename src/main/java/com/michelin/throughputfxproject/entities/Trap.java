package com.michelin.throughputfxproject.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Trap implements BoardAction {
    private final String effected;
    private final String duration;
    private final String mitigatedDuration;
    private final int startWeek;
    private final int startDay;


    public Trap(String effected, String duration, int startWeek, int startDay, String mitigatedDuration) {
        this.effected = effected;
        this.duration = duration;
        this.startWeek = startWeek;
        this.startDay = startDay;
        this.mitigatedDuration = mitigatedDuration;
    }
}
