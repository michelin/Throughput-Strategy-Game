package com.michelin.throughputfxproject.entities.state;



/**
 * Represents an entity that can be saved to a JSON format.
 * Classes implementing this interface must provide an implementation
 * for converting their state to a JSON string.
 */
public interface Savable {
    /**
     * Converts the implementing class's state to its JSON representation.
     *
     * @return A `String` containing the JSON representation of the object.
     */
    String toJSON();
}
