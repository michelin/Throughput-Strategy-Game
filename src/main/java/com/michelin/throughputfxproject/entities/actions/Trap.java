package com.michelin.throughputfxproject.entities.actions;



public record Trap(String effected, String duration, int startWeek, int startDay,
                   String mitigatedDuration) implements BoardAction {
}
