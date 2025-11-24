package com.swe.ux.model.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SentimentPoint}.
 */
class SentimentPointTest {

    @Test
    void gettersReturnValues() {
        SentimentPoint point = new SentimentPoint("2023-11-20T10:00", 0.75);

        assertEquals("2023-11-20T10:00", point.getTime());
        assertEquals(0.75, point.getSentiment());
    }
}

