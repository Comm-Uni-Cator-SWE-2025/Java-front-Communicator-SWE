package com.swe.ux.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.swe.ux.model.analytics.ShapeCount;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ShapeDataService}.
 */
class ShapeDataServiceTest {

    private ShapeDataService service;

    @BeforeEach
    void setUp() {
        service = new ShapeDataService();
    }

    @Test
    void fetchNextData_cyclesThroughPredefinedSnapshots() {
        String first = service.fetchNextData();
        String second = service.fetchNextData();

        assertTrue(first.contains("\"freeHand\""));
        assertNotEquals(first, second, "Subsequent fetches should rotate data samples");
    }

    @Test
    void parseJson_returnsShapeCountsFromPayload() {
        String json = """
                {
                  "freeHand": 11,
                  "straightLine": 3,
                  "rectangle": 4,
                  "ellipse": 2,
                  "triangle": 6
                }
                """;

        ShapeCount count = service.parseJson(json);

        assertEquals(11, count.getFreeHand());
        assertEquals(3, count.getStraightLine());
        assertEquals(4, count.getRectangle());
        assertEquals(2, count.getEllipse());
        assertEquals(6, count.getTriangle());
    }

    @Test
    void parseJson_returnsZeroCountsWhenPayloadMalformed() {
        ShapeCount count = service.parseJson("not-json");

        assertEquals(0, count.getFreeHand());
        assertEquals(0, count.getStraightLine());
        assertEquals(0, count.getRectangle());
        assertEquals(0, count.getEllipse());
        assertEquals(0, count.getTriangle());
    }
}

