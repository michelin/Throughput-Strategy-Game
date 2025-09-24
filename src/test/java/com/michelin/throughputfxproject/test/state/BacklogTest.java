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
package com.michelin.throughputfxproject.test.state;

import com.michelin.throughputfxproject.entities.state.Backlog;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BacklogTest {
    @Test
    void addToBacklog_increasesCount() {
        Backlog backlog = new Backlog();
        backlog.addToBacklog(3);
        assertEquals(3, backlog.getBacklogItemCount());
    }

    @Test
    void addToBacklog_negativeAmount_throwsAssertionError() {
        Backlog backlog = new Backlog();
        assertThrows(AssertionError.class, () -> backlog.addToBacklog(-1));
    }

    @Test
    void backlogScore_calculatesCorrectly() {
        Backlog backlog = new Backlog();
        backlog.setBacklogItemCount(10);
        float score = backlog.backlogScore(4); // 10 * (1/5) = 2.0
        assertEquals(2.0f, score);
    }

    @Test
    void subtractFromBacklog_decreasesCount() {
        Backlog backlog = new Backlog();
        backlog.setBacklogItemCount(5);
        backlog.subtractFromBacklog(2);
        assertEquals(3, backlog.getBacklogItemCount());
    }

    @Test
    void subtractFromBacklog_negativeAmount_throwsAssertionError() {
        Backlog backlog = new Backlog();
        backlog.setBacklogItemCount(5);
        assertThrows(AssertionError.class, () -> backlog.subtractFromBacklog(-1));
    }

    @Test
    void subtractFromBacklog_tooLargeAmount_throwsAssertionError() {
        Backlog backlog = new Backlog();
        backlog.setBacklogItemCount(2);
        assertThrows(AssertionError.class, () -> backlog.subtractFromBacklog(3));
    }

    @Test
    void toJSON_returnsCorrectJson() {
        Backlog backlog = new Backlog();
        backlog.setBacklogItemCount(7);
        String json = backlog.toJSON();
        assertEquals("\"backlog\":{\"backlogItemCount\":7}", json);
    }
}
