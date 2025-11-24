package com.swe.ux.views;

import com.swe.ux.viewmodels.ScreenVideoTelemetryViewModel;
import javafx.collections.FXCollections;
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
    private final ScreenVideoTelemetryViewModel viewModel;

    private Label avgValue;
    private Label maxValue;
    private Label minValue;
    private Label p95Value;
    private Label sessionLabel;
    private Label cameraChip;
    private Label screenChip;
    private LineChart<Number, Number> fpsChart;
    private NumberAxis xAxis;
    private NumberAxis yAxis;

    public ScreenVideoTelemetryView(ScreenVideoTelemetryViewModel viewModel) {
        this.viewModel = viewModel;
        setSpacing(12);
        setPadding(new Insets(12));
        setStyle("-fx-background-color: rgba(240, 243, 249, 0.75); -fx-background-radius: 12;");
        setFillWidth(true);
        setMinSize(0, 0);
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        buildLayout();
        bindToViewModel();
        refresh();
    }

    private void buildLayout() {
        Label title = new Label("Screen & Video Telemetry");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        sessionLabel = new Label("Session --");
        sessionLabel.setStyle("-fx-text-fill: #4b5563; -fx-font-size: 12px;");

        HBox statusRow = new HBox(8);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        cameraChip = buildStatusChip("Camera: --");
        screenChip = buildStatusChip("Screen: --");
        statusRow.getChildren().addAll(cameraChip, screenChip);

        GridPane metricsGrid = new GridPane();
        metricsGrid.setHgap(8);
        metricsGrid.setVgap(8);

        avgValue = new Label("-- fps");
        maxValue = new Label("-- fps");
        minValue = new Label("-- fps");
        p95Value = new Label("-- fps");

        metricsGrid.add(buildMetricCard("Average FPS", avgValue), 0, 0);
        metricsGrid.add(buildMetricCard("Peak FPS", maxValue), 1, 0);
        metricsGrid.add(buildMetricCard("Min FPS", minValue), 0, 1);
        metricsGrid.add(buildMetricCard("P95 (worst 5%)", p95Value), 1, 1);

        Label chartTitle = new Label("Recent FPS (every 3s)");
        chartTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        createChart();

        getChildren().addAll(title, sessionLabel, statusRow, metricsGrid, chartTitle, fpsChart);
        VBox.setMargin(metricsGrid, new Insets(4, 0, 4, 0));
        VBox.setVgrow(fpsChart, Priority.ALWAYS);
    }

    private VBox buildMetricCard(String title, Label value) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        card.setBorder(new javafx.scene.layout.Border(new BorderStroke(
                Color.rgb(220, 225, 233),
                BorderStrokeStyle.SOLID,
                new CornerRadii(10),
                new BorderWidths(1)
        )));

        Label t = new Label(title);
        t.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");

        value.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        card.getChildren().addAll(t, value);
        return card;
    }

    private Label buildStatusChip(String text) {
        Label chip = new Label(text);
        chip.setPadding(new Insets(6, 10, 6, 10));
        chip.setStyle("-fx-background-color: #e5e7eb; -fx-background-radius: 999;");
        return chip;
    }

    private void bindToViewModel() {
        viewModel.avgFpsProperty().addListener((obs, oldVal, newVal) -> avgValue.setText(formatFps(newVal)));
        viewModel.maxFpsProperty().addListener((obs, oldVal, newVal) -> maxValue.setText(formatFps(newVal)));
        viewModel.minFpsProperty().addListener((obs, oldVal, newVal) -> minValue.setText(formatFps(newVal)));
        viewModel.p95FpsProperty().addListener((obs, oldVal, newVal) -> p95Value.setText(formatFps(newVal)));

        viewModel.sessionLabelProperty().addListener((obs, oldVal, newVal) -> sessionLabel.setText(newVal));

        viewModel.withCameraProperty().addListener((obs, oldVal, newVal) -> updateStatusChip(cameraChip, "Camera", newVal));
        viewModel.withScreenProperty().addListener((obs, oldVal, newVal) -> updateStatusChip(screenChip, "Screen", newVal));

        viewModel.getFpsHistory().addListener((javafx.collections.ListChangeListener<Double>) change -> refreshChart());
    }

    private String formatFps(Number value) {
        if (value == null) {
            return "-- fps";
        }
        return String.format("%.1f fps", value.doubleValue());
    }

    private void updateStatusChip(Label chip, String label, boolean active) {
        chip.setText(label + (active ? ": ON" : ": OFF"));
        chip.setStyle("-fx-background-radius: 999; -fx-padding: 6 10 6 10; "
                + (active
                ? "-fx-background-color: rgba(16, 185, 129, 0.2); -fx-text-fill: #065f46;"
                : "-fx-background-color: #e5e7eb; -fx-text-fill: #4b5563;"));
    }

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
        fpsChart.setMinHeight(160);
        fpsChart.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    }

    private void refreshChart() {
        ObservableList<Double> fpsHistory = viewModel.getFpsHistory();

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        int intervalSeconds = 3;

        for (int i = 0; i < fpsHistory.size(); i++) {
            Double fps = fpsHistory.get(i);
            if (fps != null) {
                series.getData().add(new XYChart.Data<>(i * intervalSeconds, fps));
            }
        }

        fpsChart.getData().setAll(series);

        if (!fpsHistory.isEmpty()) {
            double min = fpsHistory.stream().filter(v -> v != null).mapToDouble(Double::doubleValue).min().orElse(0);
            double max = fpsHistory.stream().filter(v -> v != null).mapToDouble(Double::doubleValue).max().orElse(0);
            double padding = Math.max(1, (max - min) * 0.2);
            yAxis.setAutoRanging(false);
            yAxis.setLowerBound(Math.max(0, min - padding));
            yAxis.setUpperBound(max + padding);
        } else {
            yAxis.setAutoRanging(true);
        }
    }
}
