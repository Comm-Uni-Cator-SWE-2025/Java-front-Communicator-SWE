package com.swe.ux.analytics;

import com.swe.ux.model.analytics.ScreenVideoTelemetryModel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

/**
 * Central collector that aggregates live screen/video telemetry so both the
 * Swing surface (screen sharing) and the JavaFX sentiment view can share state.
 */
public final class ScreenShareTelemetryCollector {
    /** Maximum number of FPS samples to retain. */
    private static final int MAX_SAMPLES = 60;
    /** Minimum FPS value we consider valid to avoid divide-by-zero artefacts. */
    private static final double MIN_VALID_FPS = 0.1;
    /** Maximum FPS we consider realistic to avoid rendering spikes. */
    private static final double MAX_VALID_FPS = 120.0;
    /** Singleton instance. */
    private static final ScreenShareTelemetryCollector INSTANCE = new ScreenShareTelemetryCollector();
    /** Lock for guarding sample state. */
    private final Object sampleLock = new Object();
    /** Rolling FPS samples. */
    private final Deque<Double> fpsSamples = new ArrayDeque<>();
    /** Whether camera is currently active. */
    private volatile boolean cameraActive;
    /** Whether screen sharing is currently active. */
    private volatile boolean screenActive;
    /** Timestamp of when the current session window started. */
    private volatile long sessionStartMillis;
    /** Timestamp of last telemetry update. */
    private volatile long lastUpdateMillis;
    /** Last frame timestamp captured (nanoseconds). */
    private volatile long lastFrameNanos;

    private ScreenShareTelemetryCollector() {
        final long now = System.currentTimeMillis();
        sessionStartMillis = now;
        lastUpdateMillis = now;
    }

    /**
     * Returns the global singleton collector.
     * @return collector instance
     */
    public static ScreenShareTelemetryCollector getInstance() {
        return INSTANCE;
    }

    /**
     * Resets the rolling samples and restarts the logical session window.
     */
    public void resetSession() {
        synchronized (sampleLock) {
            fpsSamples.clear();
            sessionStartMillis = System.currentTimeMillis();
            lastUpdateMillis = sessionStartMillis;
            lastFrameNanos = 0L;
        }
    }

    /**
     * Registers that a frame was rendered. FPS is calculated based on the delta
     * with the previous frame.
     */
    public void recordFrameRendered() {
        final long nowNanos = System.nanoTime();
        double fpsSample = 0.0;
        synchronized (sampleLock) {
            if (lastFrameNanos > 0L) {
                final long delta = nowNanos - lastFrameNanos;
                if (delta > 0L) {
                    fpsSample = 1_000_000_000.0 / delta;
                }
            }
            lastFrameNanos = nowNanos;
            lastUpdateMillis = System.currentTimeMillis();
            if (fpsSample > 0.0) {
                addSample(fpsSample);
            }
        }
    }

    /**
     * Adds an explicit FPS sample (useful for tests or other modules that
     * already computed averages).
     * @param fpsSample the fps sample to add
     */
    public void recordFpsSample(final double fpsSample) {
        if (Double.isNaN(fpsSample) || Double.isInfinite(fpsSample)) {
            return;
        }
        synchronized (sampleLock) {
            lastUpdateMillis = System.currentTimeMillis();
            addSample(fpsSample);
        }
    }

    private void addSample(final double sample) {
        final double clamped = Math.max(MIN_VALID_FPS, Math.min(MAX_VALID_FPS, sample));
        fpsSamples.addLast(clamped);
        while (fpsSamples.size() > MAX_SAMPLES) {
            fpsSamples.removeFirst();
        }
    }

    /**
     * Updates the known camera active state.
     * @param active true if camera is active
     */
    public void setCameraActive(final boolean active) {
        cameraActive = active;
        lastUpdateMillis = System.currentTimeMillis();
    }

    /**
     * Updates the known screen sharing active state.
     * @param active true if screen sharing is active
     */
    public void setScreenActive(final boolean active) {
        if (screenActive && !active) {
            // a stop event closes the current session window
            lastFrameNanos = 0L;
        }
        if (!screenActive && active) {
            sessionStartMillis = System.currentTimeMillis();
            fpsSamples.clear();
        }
        screenActive = active;
        lastUpdateMillis = System.currentTimeMillis();
    }

    /**
     * Builds a snapshot model that the JavaFX dashboard can consume.
     * @return telemetry model populated with live data
     */
    public ScreenVideoTelemetryModel buildSnapshotModel() {
        synchronized (sampleLock) {
            final long end = Math.max(lastUpdateMillis, sessionStartMillis);
            return new ScreenVideoTelemetryModel(
                    sessionStartMillis,
                    end,
                    new ArrayList<>(fpsSamples),
                    cameraActive,
                    screenActive);
        }
    }
}
