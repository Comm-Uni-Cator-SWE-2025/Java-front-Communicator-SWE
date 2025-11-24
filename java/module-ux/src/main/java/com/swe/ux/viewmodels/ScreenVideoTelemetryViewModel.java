package com.swe.ux.viewmodels;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.swe.ux.model.analytics.ScreenVideoTelemetryModel;
import com.swe.ux.service.TelemetryDataService;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * View-model for surfacing screen/video telemetry metrics to the UI.
 */
public class ScreenVideoTelemetryViewModel {
    /** Maximum snapshots constant. */
    private static final int MAX_SNAPSHOTS = 6;
    /** Seconds per sample constant. */
    private static final int SECONDS_PER_SAMPLE = 1000;
    /** Minutes per hour constant. */
    private static final int MINUTES_PER_HOUR = 60;
    /** Rounding factor constant. */
    private static final double ROUNDING_FACTOR = 10.0;
    
    /** Data service for telemetry. */
    private final TelemetryDataService dataService;
    /** Observable list of snapshots. */
    private final ObservableList<ScreenVideoTelemetryModel> snapshots;
    /** Max snapshots property. */
    private final IntegerProperty maxSnapshots;
    /** Latest snapshot property. */
    private final ObjectProperty<ScreenVideoTelemetryModel> latestSnapshot;
    /** FPS history list. */
    private final ObservableList<Double> fpsHistory;

    /** Average FPS property. */
    private final DoubleProperty avgFps;
    /** Maximum FPS property. */
    private final DoubleProperty maxFps;
    /** Minimum FPS property. */
    private final DoubleProperty minFps;
    /** P95 FPS property. */
    private final DoubleProperty p95Fps;
    /** Camera status property. */
    private final BooleanProperty withCamera;
    /** Screen status property. */
    private final BooleanProperty withScreen;
    /** Session label property. */
    private final StringProperty sessionLabel;

    /**
     * Creates a new ScreenVideoTelemetryViewModel.
     */
    public ScreenVideoTelemetryViewModel() {
        this.dataService = new TelemetryDataService();
        this.snapshots = FXCollections.observableArrayList();
        this.latestSnapshot = new SimpleObjectProperty<>(null);
        this.fpsHistory = FXCollections.observableArrayList();
        this.maxSnapshots = new SimpleIntegerProperty(MAX_SNAPSHOTS);

        this.avgFps = new SimpleDoubleProperty(0);
        this.maxFps = new SimpleDoubleProperty(0);
        this.minFps = new SimpleDoubleProperty(0);
        this.p95Fps = new SimpleDoubleProperty(0);
        this.withCamera = new SimpleBooleanProperty(false);
        this.withScreen = new SimpleBooleanProperty(false);
        this.sessionLabel = new SimpleStringProperty("No session yet");
    }

    /**
     * Fetches and updates telemetry data.
     */
    public void fetchAndUpdateData() {
        final String json = dataService.fetchNextData();
        if (json == null || json.isEmpty()) {
            return;
        }

        final ScreenVideoTelemetryModel model = dataService.parseJson(json);
        snapshots.add(model);
        trimToMaxSnapshots();
        latestSnapshot.set(model);
        refreshDerived(model);
    }

    private void trimToMaxSnapshots() {
        while (snapshots.size() > maxSnapshots.get()) {
            snapshots.remove(0);
        }
    }

    private void refreshDerived(final ScreenVideoTelemetryModel model) {
        if (model == null) {
            return;
        }

        withCamera.set(model.isWithCamera());
        withScreen.set(model.isWithScreen());

        avgFps.set(safeRound(model.getAvgFps()));
        maxFps.set(safeRound(model.getMaxFps()));
        minFps.set(safeRound(model.getMinFps()));
        p95Fps.set(safeRound(model.getP95Fps()));

        if (model.getFpsEvery3Seconds() != null) {
            fpsHistory.setAll(new ArrayList<>(model.getFpsEvery3Seconds()));
        } else {
            fpsHistory.setAll(new ArrayList<>());
        }

        sessionLabel.set(buildSessionLabel(model));
    }

    private double safeRound(final Double value) {
        if (value == null) {
            return 0.0;
        }
        return Math.round(value * ROUNDING_FACTOR) / ROUNDING_FACTOR;
    }

    private String buildSessionLabel(final ScreenVideoTelemetryModel model) {
        final long start;
        if (model.getStartTime() != null) {
            start = model.getStartTime();
        } else {
            start = 0L;
        }
        
        final long end;
        if (model.getEndTime() != null) {
            end = model.getEndTime();
        } else {
            end = start;
        }
        
        final long durationMs = Math.max(0L, end - start);
        final long seconds = durationMs / SECONDS_PER_SAMPLE;
        final long minutes = seconds / MINUTES_PER_HOUR;
        final long leftoverSeconds = seconds % MINUTES_PER_HOUR;

        final SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
        final String startStr = fmt.format(new Date(start));
        final String endStr = fmt.format(new Date(end));

        return "Session " + startStr + " - " + endStr + "  (" + minutes + "m " + leftoverSeconds + "s)";
    }

    /**
     * Gets the snapshots list.
     * @return Observable list of snapshots
     */
    public ObservableList<ScreenVideoTelemetryModel> getSnapshots() {
        return snapshots;
    }

    /**
     * Gets the FPS history list.
     * @return Observable list of FPS values
     */
    public ObservableList<Double> getFpsHistory() {
        return fpsHistory;
    }

    /**
     * Returns the average FPS property.
     * @return Average FPS property
     */
    public DoubleProperty avgFpsProperty() {
        return avgFps;
    }

    /**
     * Returns the maximum FPS property.
     * @return Maximum FPS property
     */
    public DoubleProperty maxFpsProperty() {
        return maxFps;
    }

    /**
     * Returns the minimum FPS property.
     * @return Minimum FPS property
     */
    public DoubleProperty minFpsProperty() {
        return minFps;
    }

    /**
     * Returns the P95 FPS property.
     * @return P95 FPS property
     */
    public DoubleProperty p95FpsProperty() {
        return p95Fps;
    }

    /**
     * Returns the camera status property.
     * @return Camera status property
     */
    public BooleanProperty withCameraProperty() {
        return withCamera;
    }

    /**
     * Returns the screen status property.
     * @return Screen status property
     */
    public BooleanProperty withScreenProperty() {
        return withScreen;
    }

    /**
     * Returns the session label property.
     * @return Session label property
     */
    public StringProperty sessionLabelProperty() {
        return sessionLabel;
    }

    /**
     * Returns the latest snapshot property.
     * @return Latest snapshot property
     */
    public ObjectProperty<ScreenVideoTelemetryModel> latestSnapshotProperty() {
        return latestSnapshot;
    }

    /**
     * Returns the max snapshots property.
     * @return Max snapshots property
     */
    public IntegerProperty maxSnapshotsProperty() {
        return maxSnapshots;
    }
}
