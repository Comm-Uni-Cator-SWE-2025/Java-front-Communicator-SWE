package com.swe.ux.model.analytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Model for screen/video telemetry data including FPS metrics.
 */
public class ScreenVideoTelemetryModel {
    /**
     * Percentile value for 95th percentile calculation.
     */
    private static final double PERCENTILE_95 = 0.05;

    /**
     * Start time of the telemetry period.
     */
    private Long startTime;
    /**
     * End time of the telemetry period.
     */
    private Long endTime;
    /**
     * FPS values recorded every 3 seconds.
     */
    private ArrayList<Double> fpsEvery3Seconds;

    /**
     * Whether camera was active.
     */
    private boolean withCamera;
    /**
     * Whether screen sharing was active.
     */
    private boolean withScreen;

    /**
     * Average FPS.
     */
    private Double avgFps;
    /**
     * Maximum FPS.
     */
    private Double maxFps;
    /**
     * Minimum FPS.
     */
    private Double minFps;
    /**
     * 95th percentile FPS (worst 5%).
     */
    private Double p95Fps;

    /**
     * Creates a new telemetry model.
     *
     * @param start the start time
     * @param end the end time
     * @param fpsList the FPS values list
     * @param camera whether camera was active
     * @param screen whether screen sharing was active
     */
    public ScreenVideoTelemetryModel(final Long start, final Long end,
                                     final ArrayList<Double> fpsList,
                                     final boolean camera, final boolean screen) {
        this.startTime = start;
        this.endTime = end;
        if (fpsList != null) {
            this.fpsEvery3Seconds = new ArrayList<>(fpsList);
        } else {
            this.fpsEvery3Seconds = new ArrayList<>();
        }
        this.withCamera = camera;
        this.withScreen = screen;
        calculateMetrics(); // Calculate metrics from fpsEvery3Seconds
    }

    /**
     * Checks if camera was active.
     *
     * @return true if camera was active
     */
    public boolean isWithCamera() {
        return withCamera;
    }

    /**
     * Checks if screen sharing was active.
     *
     * @return true if screen sharing was active
     */
    public boolean isWithScreen() {
        return withScreen;
    }

    /**
     * Gets the start time.
     *
     * @return the start time
     */
    public Long getStartTime() {
        return startTime;
    }

    /**
     * Gets the end time.
     *
     * @return the end time
     */
    public Long getEndTime() {
        return endTime;
    }

    /**
     * Gets the FPS values list.
     *
     * @return the FPS values
     */
    public ArrayList<Double> getFpsEvery3Seconds() {
        return fpsEvery3Seconds;
    }

    /**
     * Gets the average FPS.
     *
     * @return the average FPS
     */
    public Double getAvgFps() {
        return avgFps;
    }

    /**
     * Gets the maximum FPS.
     *
     * @return the maximum FPS
     */
    public Double getMaxFps() {
        return maxFps;
    }

    /**
     * Gets the minimum FPS.
     *
     * @return the minimum FPS
     */
    public Double getMinFps() {
        return minFps;
    }

    /**
     * Gets the 95th percentile FPS.
     *
     * @return the 95th percentile FPS
     */
    public Double getP95Fps() {
        return p95Fps;
    }


    /**
     * Calculates FPS metrics from the data.
     */
    private void calculateMetrics() {
        if (fpsEvery3Seconds == null || fpsEvery3Seconds.isEmpty()) {
            avgFps = 0.0;
            maxFps = 0.0;
            minFps = 0.0;
            p95Fps = 0.0;
            return;
        }

        // Compute average
        double sum = 0.0;
        double max = Double.NEGATIVE_INFINITY;
        double min = Double.POSITIVE_INFINITY;

        for (final Double fps : fpsEvery3Seconds) {
            if (fps == null) {
                continue;
            }
            sum += fps;
            if (fps > max) {
                max = fps;
            }
            if (fps < min) {
                min = fps;
            }
        }

        avgFps = sum / fpsEvery3Seconds.size();
        maxFps = max;
        minFps = min;

        // Compute 95th percentile (worst 5%)
        final List<Double> sorted = new ArrayList<>(fpsEvery3Seconds);
        Collections.sort(sorted);

        // 95th percentile index → floor(0.05 * n)
        int index = (int) Math.floor(PERCENTILE_95 * sorted.size());
        if (index < 0) {
            index = 0;
        }
        if (index >= sorted.size()) {
            index = sorted.size() - 1;
        }

        p95Fps = sorted.get(index);
    }

}
