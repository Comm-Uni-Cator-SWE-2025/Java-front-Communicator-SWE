package com.swe.ux.model.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ShapeCount}.
 */
class ShapeCountTest {

    @Test
    void gettersReturnValues() {
        ShapeCount count = new ShapeCount(1, 2, 3, 4, 5);

        assertEquals(1, count.getFreeHand());
        assertEquals(2, count.getStraightLine());
        assertEquals(3, count.getRectangle());
        assertEquals(4, count.getEllipse());
        assertEquals(5, count.getTriangle());
    }
}

