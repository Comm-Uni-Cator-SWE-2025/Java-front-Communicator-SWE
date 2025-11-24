package com.swe.ux.model.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ScreenVideoTelemetryModel}.
 */
class ScreenVideoTelemetryModelTest {

    @Test
    void calculateMetricsFromFpsSamples() {
        ArrayList<Double> fpsSamples = new ArrayList<>(Arrays.asList(30.0, 20.0, 25.0, 35.0));

        ScreenVideoTelemetryModel model = new ScreenVideoTelemetryModel(
                1000L, 1600L, fpsSamples, true, true);

        assertEquals(1000L, model.getStartTime());
        assertEquals(1600L, model.getEndTime());
        assertEquals(true, model.isWithCamera());
        assertEquals(true, model.isWithScreen());
        assertEquals(4, model.getFpsEvery3Seconds().size());
        assertEquals((30.0 + 20.0 + 25.0 + 35.0) / 4.0, model.getAvgFps());
        assertEquals(35.0, model.getMaxFps());
        assertEquals(20.0, model.getMinFps());
        assertEquals(20.0, model.getP95Fps(), "95th percentile uses the lowest value for 4 samples");

        fpsSamples.add(10.0);
        assertEquals(4, model.getFpsEvery3Seconds().size(), "Constructor should defensively copy the list");
        assertNotSame(fpsSamples, model.getFpsEvery3Seconds());
    }

    @Test
    void calculateMetricsHandlesEmptyInput() {
        ScreenVideoTelemetryModel model = new ScreenVideoTelemetryModel(
                0L, 0L, new ArrayList<>(), false, false);

        assertEquals(0.0, model.getAvgFps());
        assertEquals(0.0, model.getMaxFps());
        assertEquals(0.0, model.getMinFps());
        assertEquals(0.0, model.getP95Fps());
    }
}

