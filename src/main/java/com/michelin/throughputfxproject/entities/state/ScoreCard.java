package com.michelin.throughputfxproject.entities.state;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.michelin.throughputfxproject.exceptions.ThroughputRuntimeException;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Getter
@Setter
public class ScoreCard implements Savable {

    public static final Logger LOGGER = LoggerFactory.getLogger(ScoreCard.class.getName());
    private final int period;
    private  int score;
    private  int estimate;
    private  int workInProcess;
    private  int finishedGoods;

    public ScoreCard(int period, int score, int finishedGoods, int estimate, int wip) {
        this.score = score;
        this.finishedGoods = finishedGoods;
        this.estimate = estimate;
        this.period = period;
        this.workInProcess = wip;

    }

    public String toJSON(){
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json;
        try {
            json = ow.writeValueAsString(this);
            LOGGER.info("Scorecard {}", json);
        } catch (JsonProcessingException e) {
            throw new ThroughputRuntimeException(e);
        }
        return json;

    }


}
