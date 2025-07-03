package com.michelin.throughputfxproject.test.state;

import com.michelin.throughputfxproject.entities.state.FinishedGoods;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FinishedGoodsTest {
    @Test
    void addToFinishedGoods_increasesTally() {
        FinishedGoods fg = FinishedGoods.builder().finishedGoodsTally(0).value(3).build();
        fg.addToFinishedGoods(5);
        assertEquals(5, fg.getFinishedGoodsTally());
    }

    @Test
    void addToFinishedGoods_negativeAmount_doesNotChangeTally() {
        FinishedGoods fg = FinishedGoods.builder().finishedGoodsTally(2).value(3).build();
        fg.addToFinishedGoods(-4);
        assertEquals(2, fg.getFinishedGoodsTally());
    }

    @Test
    void calculateScore_returnsCorrectProduct() {
        FinishedGoods fg = FinishedGoods.builder().finishedGoodsTally(4).value(5).build();
        assertEquals(20, fg.calculateScore());
    }

    @Test
    void toJSON_returnsCorrectJson() {
        FinishedGoods fg = FinishedGoods.builder().finishedGoodsTally(7).value(9).build();
        String json = fg.toJSON();
        assertEquals("\"finishedGoods\":{\"finishedGoodsTally\":7,\"currentValue\":9}", json);
    }
}

