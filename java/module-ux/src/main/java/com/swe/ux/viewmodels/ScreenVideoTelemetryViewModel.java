package com.swe.ux.viewmodels;

import com.swe.ux.model.analytics.ScreenVideoTelemetryModel;
import com.swe.ux.service.TelemetryDataService;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * View-model for surfacing screen/video telemetry metrics to the UI.
 */
public class ScreenVideoTelemetryViewModel {
    private final TelemetryDataService dataService;
    private final ObservableList<ScreenVideoTelemetryModel> snapshots;
    private final IntegerProperty maxSnapshots;
    private final ObjectProperty<ScreenVideoTelemetryModel> latestSnapshot;
    private final ObservableList<Double> fpsHistory;

    private final DoubleProperty avgFps;
    private final DoubleProperty maxFps;
    private final DoubleProperty minFps;
    private final DoubleProperty p95Fps;
    private final BooleanProperty withCamera;
    private final BooleanProperty withScreen;
    private final StringProperty sessionLabel;

    public ScreenVideoTelemetryViewModel() {
        this.dataService = new TelemetryDataService();
        this.snapshots = FXCollections.observableArrayList();
        this.latestSnapshot = new SimpleObjectProperty<>(null);
        this.fpsHistory = FXCollections.observableArrayList();
        this.maxSnapshots = new SimpleIntegerProperty(6);

        this.avgFps = new SimpleDoubleProperty(0);
        this.maxFps = new SimpleDoubleProperty(0);
        this.minFps = new SimpleDoubleProperty(0);
        this.p95Fps = new SimpleDoubleProperty(0);
        this.withCamera = new SimpleBooleanProperty(false);
        this.withScreen = new SimpleBooleanProperty(false);
        this.sessionLabel = new SimpleStringProperty("No session yet");
    }

    public void fetchAndUpdateData() {
        String json = dataService.fetchNextData();
        if (json == null || json.isEmpty()) {
            return;
        }

        ScreenVideoTelemetryModel model = dataService.parseJson(json);
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

    private void refreshDerived(ScreenVideoTelemetryModel model) {
        if (model == null) {
            return;
        }

        withCamera.set(model.isWithCamera());
        withScreen.set(model.isWithScreen());

        avgFps.set(safeRound(model.getAvgFps()));
        maxFps.set(safeRound(model.getMaxFps()));
        minFps.set(safeRound(model.getMinFps()));
        p95Fps.set(safeRound(model.getP95Fps()));

        fpsHistory.setAll(model.getFpsEvery3Seconds() != null
                ? new ArrayList<>(model.getFpsEvery3Seconds())
                : new ArrayList<>());

        sessionLabel.set(buildSessionLabel(model));
    }

    private double safeRound(Double value) {
        if (value == null) {
            return 0.0;
        }
        return Math.round(value * 10.0) / 10.0;
    }

    private String buildSessionLabel(ScreenVideoTelemetryModel model) {
        long start = model.getStartTime() != null ? model.getStartTime() : 0L;
        long end = model.getEndTime() != null ? model.getEndTime() : start;
        long durationMs = Math.max(0L, end - start);
        long seconds = durationMs / 1000;
        long minutes = seconds / 60;
        long leftoverSeconds = seconds % 60;

        SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
        String startStr = fmt.format(new Date(start));
        String endStr = fmt.format(new Date(end));

        return "Session " + startStr + " - " + endStr + "  (" + minutes + "m " + leftoverSeconds + "s)";
    }

    // Getters / properties
    public ObservableList<ScreenVideoTelemetryModel> getSnapshots() {
        return snapshots;
    }

    public ObservableList<Double> getFpsHistory() {
        return fpsHistory;
    }

    public DoubleProperty avgFpsProperty() {
        return avgFps;
    }

    public DoubleProperty maxFpsProperty() {
        return maxFps;
    }

    public DoubleProperty minFpsProperty() {
        return minFps;
    }

    public DoubleProperty p95FpsProperty() {
        return p95Fps;
    }

    public BooleanProperty withCameraProperty() {
        return withCamera;
    }

    public BooleanProperty withScreenProperty() {
        return withScreen;
    }

    public StringProperty sessionLabelProperty() {
        return sessionLabel;
    }

    public ObjectProperty<ScreenVideoTelemetryModel> latestSnapshotProperty() {
        return latestSnapshot;
    }

    public IntegerProperty maxSnapshotsProperty() {
        return maxSnapshots;
    }
}
