package com.swe.ux.views;

import com.swe.ux.viewmodels.ScreenVideoTelemetryViewModel;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

/**
 * JavaFX view that surfaces screen/video telemetry (FPS, camera/screen status).
 */
public class ScreenVideoTelemetryView extends VBox {
    /** View model for telemetry data. */
    private final ScreenVideoTelemetryViewModel viewModel;

    /** Average FPS label. */
    private Label avgValue;
    /** Maximum FPS label. */
    private Label maxValue;
    /** Minimum FPS label. */
    private Label minValue;
    /** P95 FPS label. */
    private Label p95Value;
    /** Session label. */
    private Label sessionLabel;
    /** Camera status chip. */
    private Label cameraChip;
    /** Screen status chip. */
    private Label screenChip;
    /** FPS line chart. */
    private LineChart<Number, Number> fpsChart;
    /** X-axis for chart. */
    private NumberAxis xAxis;
    /** Y-axis for chart. */
    private NumberAxis yAxis;
    /**
     * Card border radius.
     */
    private static final int CARD_BORDER_RADIUS = 10;

    /** Spacing constant. */
    private static final int SPACING = 12;
    /** Padding constant. */
    private static final int PADDING = 12;
    /** Status row spacing. */
    private static final int STATUS_ROW_SPACING = 8;
    /** Metrics grid horizontal gap. */
    private static final int METRICS_GRID_HGAP = 8;
    /** Metrics grid vertical gap. */
    private static final int METRICS_GRID_VGAP = 8;
    /** Metric card spacing. */
    private static final int METRIC_CARD_SPACING = 4;
    /** Metric card padding. */
    private static final int METRIC_CARD_PADDING = 10;
    /** Border color red component. */
    private static final int BORDER_RED = 220;
    /** Border color green component. */
    private static final int BORDER_GREEN = 225;
    /** Border color blue component. */
    private static final int BORDER_BLUE = 233;
    /** Chip padding top/bottom. */
    private static final int CHIP_PADDING_TB = 6;
    /** Chip padding left/right. */
    private static final int CHIP_PADDING_LR = 10;
    /** Chart minimum height. */
    private static final int CHART_MIN_HEIGHT = 160;
    /** Chart interval in seconds. */
    private static final int CHART_INTERVAL_SECONDS = 3;
    /** Chart padding factor. */
    private static final double CHART_PADDING_FACTOR = 0.2;

    /**
     * Constructs a ScreenVideoTelemetryView with the given view model.
     *
     * @param telemetryViewModel the view model for telemetry data
     */
    public ScreenVideoTelemetryView(final ScreenVideoTelemetryViewModel telemetryViewModel) {
        this.viewModel = telemetryViewModel;
        setSpacing(SPACING);
        setPadding(new Insets(PADDING));
        setStyle("-fx-background-color: rgba(240, 243, 249, 0.75); -fx-background-radius: 12;");
        setFillWidth(true);
        setMinSize(0, 0);
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        buildLayout();
        bindToViewModel();
        refresh();
    }

    private void buildLayout() {
        final Label title = new Label("Screen & Video Telemetry");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        sessionLabel = new Label("Session --");
        sessionLabel.setStyle("-fx-text-fill: #4b5563; -fx-font-size: 12px;");

        final HBox statusRow = new HBox(STATUS_ROW_SPACING);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        cameraChip = buildStatusChip("Camera: --");
        screenChip = buildStatusChip("Screen: --");
        statusRow.getChildren().addAll(cameraChip, screenChip);

        final GridPane metricsGrid = new GridPane();
        metricsGrid.setHgap(METRICS_GRID_HGAP);
        metricsGrid.setVgap(METRICS_GRID_VGAP);

        avgValue = new Label("-- fps");
        maxValue = new Label("-- fps");
        minValue = new Label("-- fps");
        p95Value = new Label("-- fps");

        metricsGrid.add(buildMetricCard("Average FPS", avgValue), 0, 0);
        metricsGrid.add(buildMetricCard("Peak FPS", maxValue), 1, 0);
        metricsGrid.add(buildMetricCard("Min FPS", minValue), 0, 1);
        metricsGrid.add(buildMetricCard("P95 (worst 5%)", p95Value), 1, 1);

        final Label chartTitle = new Label("Recent FPS (every 3s)");
        chartTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        createChart();

        getChildren().addAll(title, sessionLabel, statusRow, metricsGrid, chartTitle, fpsChart);
        VBox.setMargin(metricsGrid, new Insets(METRIC_CARD_SPACING, 0, METRIC_CARD_SPACING, 0));
        VBox.setVgrow(fpsChart, Priority.ALWAYS);
    }

    private VBox buildMetricCard(final String title, final Label value) {
        final VBox card = new VBox(METRIC_CARD_SPACING);
        card.setPadding(new Insets(METRIC_CARD_PADDING));
        card.setStyle("-fx-background-color: white; -fx-background-radius: " + CARD_BORDER_RADIUS + ";");
        card.setBorder(new javafx.scene.layout.Border(new BorderStroke(
                Color.rgb(BORDER_RED, BORDER_GREEN, BORDER_BLUE),
                BorderStrokeStyle.SOLID,
                new CornerRadii(CARD_BORDER_RADIUS),
                new BorderWidths(1)
        )));

        final Label t = new Label(title);
        t.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");

        value.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        card.getChildren().addAll(t, value);
        return card;
    }

    private Label buildStatusChip(final String text) {
        final Label chip = new Label(text);
        chip.setPadding(new Insets(CHIP_PADDING_TB, CHIP_PADDING_LR, CHIP_PADDING_TB, CHIP_PADDING_LR));
        chip.setStyle("-fx-background-color: #e5e7eb; -fx-background-radius: 999;");
        return chip;
    }

    private void bindToViewModel() {
        viewModel.avgFpsProperty().addListener((obs, oldVal, newVal) -> avgValue.setText(formatFps(newVal)));
        viewModel.maxFpsProperty().addListener((obs, oldVal, newVal) -> maxValue.setText(formatFps(newVal)));
        viewModel.minFpsProperty().addListener((obs, oldVal, newVal) -> minValue.setText(formatFps(newVal)));
        viewModel.p95FpsProperty().addListener((obs, oldVal, newVal) -> p95Value.setText(formatFps(newVal)));

        viewModel.sessionLabelProperty().addListener((obs, oldVal, newVal) -> sessionLabel.setText(newVal));

        viewModel.withCameraProperty().addListener((obs, oldVal, newVal) ->
                updateStatusChip(cameraChip, "Camera", newVal));
        viewModel.withScreenProperty().addListener((obs, oldVal, newVal) ->
                updateStatusChip(screenChip, "Screen", newVal));

        viewModel.getFpsHistory().addListener((javafx.collections.ListChangeListener<Double>) change -> refreshChart());
    }

    private String formatFps(final Number value) {
        if (value == null) {
            return "-- fps";
        }
        return String.format("%.1f fps", value.doubleValue());
    }

    private void updateStatusChip(final Label chip, final String label, final boolean active) {
        final String statusText;
        if (active) {
            statusText = ": ON";
        } else {
            statusText = ": OFF";
        }
        chip.setText(label + statusText);
        final String chipStyle;
        if (active) {
            chipStyle = "-fx-background-radius: 999; -fx-padding: 6 10 6 10; "
                    + "-fx-background-color: rgba(16, 185, 129, 0.2); -fx-text-fill: #065f46;";
        } else {
            chipStyle = "-fx-background-radius: 999; -fx-padding: 6 10 6 10; "
                    + "-fx-background-color: #e5e7eb; -fx-text-fill: #4b5563;";
        }
        chip.setStyle(chipStyle);
    }

    /**
     * Refreshes the view with current data from the view model.
     */
    public void refresh() {
        avgValue.setText(formatFps(viewModel.avgFpsProperty().get()));
        maxValue.setText(formatFps(viewModel.maxFpsProperty().get()));
        minValue.setText(formatFps(viewModel.minFpsProperty().get()));
        p95Value.setText(formatFps(viewModel.p95FpsProperty().get()));
        sessionLabel.setText(viewModel.sessionLabelProperty().get());
        updateStatusChip(cameraChip, "Camera", viewModel.withCameraProperty().get());
        updateStatusChip(screenChip, "Screen", viewModel.withScreenProperty().get());

        refreshChart();
    }

    private void createChart() {
        xAxis = new NumberAxis();
        xAxis.setLabel("Seconds");
        yAxis = new NumberAxis();
        yAxis.setLabel("FPS");

        fpsChart = new LineChart<>(xAxis, yAxis);
        fpsChart.setAnimated(false);
        fpsChart.setLegendVisible(false);
        fpsChart.setCreateSymbols(true);
        fpsChart.setMinHeight(CHART_MIN_HEIGHT);
        fpsChart.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    }

    private void refreshChart() {
        final ObservableList<Double> fpsHistory = viewModel.getFpsHistory();

        final XYChart.Series<Number, Number> series = new XYChart.Series<>();
        final int intervalSeconds = CHART_INTERVAL_SECONDS;

        for (int i = 0; i < fpsHistory.size(); i++) {
            final Double fps = fpsHistory.get(i);
            if (fps != null) {
                series.getData().add(new XYChart.Data<>(i * intervalSeconds, fps));
            }
        }

        fpsChart.getData().setAll(series);

        if (!fpsHistory.isEmpty()) {
            final double min = fpsHistory.stream().filter(v -> v != null)
                    .mapToDouble(Double::doubleValue).min().orElse(0);
            final double max = fpsHistory.stream().filter(v -> v != null)
                    .mapToDouble(Double::doubleValue).max().orElse(0);
            final double padding = Math.max(1, (max - min) * CHART_PADDING_FACTOR);
            yAxis.setAutoRanging(false);
            yAxis.setLowerBound(Math.max(0, min - padding));
            yAxis.setUpperBound(max + padding);
        } else {
            yAxis.setAutoRanging(true);
        }
    }
}
