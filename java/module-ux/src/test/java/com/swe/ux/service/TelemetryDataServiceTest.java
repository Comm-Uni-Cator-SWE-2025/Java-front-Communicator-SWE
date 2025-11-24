package com.swe.ux.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.swe.ux.model.analytics.ScreenVideoTelemetryModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TelemetryDataService}.
 */
class TelemetryDataServiceTest {

    private TelemetryDataService service;

    @BeforeEach
    void setUp() {
        service = new TelemetryDataService();
    }

    @Test
    void fetchNextData_cyclesThroughFourSamples() {
        String first = service.fetchNextData();

        // Consume the remaining 3 samples
        for (int i = 0; i < 3; i++) {
            service.fetchNextData();
        }

        String wrapped = service.fetchNextData();
        assertEquals(first, wrapped, "After four fetches the data should wrap to the first sample");
    }

    @Test
    void parseJson_buildsTelemetryModelWithMetrics() {
        String json = """
                {
                  "startTime": 1000,
                  "endTime": 1600,
                  "withCamera": true,
                  "withScreen": false,
                  "fpsEvery3Seconds": [30.0, 29.5, 28.0]
                }
                """;

        ScreenVideoTelemetryModel model = service.parseJson(json);

        assertEquals(1000L, model.getStartTime());
        assertEquals(1600L, model.getEndTime());
        assertTrue(model.isWithCamera());
        assertFalse(model.isWithScreen());
        assertEquals(3, model.getFpsEvery3Seconds().size());
        assertEquals((30.0 + 29.5 + 28.0) / 3.0, model.getAvgFps());
        assertEquals(30.0, model.getMaxFps());
        assertEquals(28.0, model.getMinFps());
        assertEquals(28.0, model.getP95Fps(), "With 3 samples the 95th percentile picks the smallest value");
    }
}

