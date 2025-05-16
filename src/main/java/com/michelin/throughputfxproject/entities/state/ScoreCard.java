package com.michelin.throughputfxproject.entities.state;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.michelin.throughputfxproject.exceptions.ThroughputRuntimeException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents a `ScoreCard` entity that implements the `Savable` interface.
 * This class contains fields and methods to manage and serialize the state
 * of a scorecard, including period, score, estimate, work in process, and finished goods.
 */
@Getter
@Setter
@Slf4j
@EqualsAndHashCode
@Builder
public class ScoreCard implements Savable {

    private final int period;
    private int score;
    private int estimate;
    private int workInProcess;
    private int finishedGoods;


  /**
   * Converts the current `ScoreCard` object to its JSON representation.
   * Utilizes the Jackson library to serialize the object with pretty printing.
   * Logs the generated JSON string at the debug level.
   *
   * @return A `String` containing the JSON representation of the `ScoreCard` object.
   * @throws ThroughputRuntimeException if a `JsonProcessingException` occurs during serialization.
   */
  public String toJSON() {
      ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String json;
      try {
          json = ow.writeValueAsString(this);
          log.debug("Scorecard {}", json);
      } catch (JsonProcessingException e) {
          throw new ThroughputRuntimeException(e);
      }
      return json;
  }


}
