package com.swe.ux.analytics;

import com.swe.ux.model.analytics.ShapeCount;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Singleton collector that mirrors the live canvas state into
 * lightweight shape-count snapshots for analytics.
 */
public final class CanvasShapeMetricsCollector {
    /** Maximum number of snapshots to retain. */
    private static final int MAX_HISTORY = 30;
    /** Singleton instance. */
    private static final CanvasShapeMetricsCollector INSTANCE = new CanvasShapeMetricsCollector();
    /** Lock guarding history deque. */
    private final Object lock = new Object();
    /** Rolling history of shape counts. */
    private final Deque<ShapeCount> history = new ArrayDeque<>();

    private CanvasShapeMetricsCollector() {
    }

    /**
     * Returns the singleton collector.
     * @return collector instance
     */
    public static CanvasShapeMetricsCollector getInstance() {
        return INSTANCE;
    }

    /**
     * Records a new snapshot of the canvas state.
     * @param snapshot up-to-date shape count
     */
    public void recordSnapshot(final ShapeCount snapshot) {
        if (snapshot == null) {
            return;
        }
        synchronized (lock) {
            history.addLast(snapshot);
            while (history.size() > MAX_HISTORY) {
                history.removeFirst();
            }
        }
    }

    /**
     * Returns a copy of the current history so analytics panes can catch up.
     * @return list of snapshots ordered oldest to newest
     */
    public List<ShapeCount> getHistory() {
        synchronized (lock) {
            return new ArrayList<>(history);
        }
    }

    /**
     * Clears stored snapshots, useful when switching meetings.
     */
    public void reset() {
        synchronized (lock) {
            history.clear();
        }
    }
}
