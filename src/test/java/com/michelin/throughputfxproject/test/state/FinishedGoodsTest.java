/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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

