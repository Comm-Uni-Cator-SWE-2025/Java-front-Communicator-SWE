/**
 *  Contributed by Kishore & Jyoti.
 */
package com.swe.ux.views;

import com.swe.ux.model.analytics.ShapeCount;
import com.swe.controller.RPCinterface.AbstractRPC;
import com.swe.ux.model.analytics.SentimentPoint;
import com.swe.ux.service.MessageDataService;
import com.swe.ux.viewmodels.ShapeViewModel;
import com.swe.ux.viewmodels.MeetingViewModel;
import com.swe.ux.viewmodels.SentimentViewModel;
import com.swe.ux.views.ScreenVideoTelemetryView;
import com.swe.ux.viewmodels.ShapeViewModel;
import com.swe.ux.viewmodels.SentimentViewModel;
import com.swe.ux.viewmodels.ScreenVideoTelemetryViewModel;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

import javax.swing.SwingUtilities;

/**
 * JavaFX pane that renders the Sentiment + Shape analytics dashboard.
 * Extracted from the standalone SentimentView application so it can be embedded
 * in Swing.
 */
public class SentimentViewPane extends StackPane {
    private final SentimentViewModel viewModel;
    private final ShapeViewModel shapeViewModel;
    private final ScreenVideoTelemetryViewModel telemetryViewModel;
    private final MessageDataService messageDataService;
    private final List<String> allMessages;

    private GridPane root;
    private VBox topLeftPane;
    private VBox topRightPane;
    private VBox bottomLeftPane;
    private VBox bottomRightPane;
    private ScreenVideoTelemetryView telemetryView;
    private VBox messageContainer;
    private ScrollPane messageScrollPane;
    private LineChart<Number, Number> chart;
    private NumberAxis xAxis;
    private NumberAxis yAxis;
    private BarChart<String, Number> barChart;
    private CategoryAxis categoryAxis;
    private NumberAxis barYAxis;
    AbstractRPC rpc;
    MeetingViewModel meetingViewModel;

    public SentimentViewPane(MeetingViewModel meetingViewModel) {
        this.rpc = meetingViewModel.rpc;
        this.meetingViewModel = meetingViewModel;
        this.viewModel = new SentimentViewModel(rpc);
        this.shapeViewModel = new ShapeViewModel();
        this.telemetryViewModel = new ScreenVideoTelemetryViewModel();
        this.messageDataService = new MessageDataService();
        this.allMessages = new ArrayList<>();

        setPadding(new Insets(10));
        setPrefSize(1200, 800);

        buildLayout();
        bindPaneSizing();
        startPeriodicUpdates();
    }

    private void buildLayout() {
        System.out.println("Building Sentiment View Pane Layout...");
        root = new GridPane();
        root.setHgap(5);
        root.setVgap(5);
        root.setPadding(new Insets(0));

        topLeftPane = new VBox(10);
        topLeftPane.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-padding: 10;");
        topLeftPane.setMinHeight(0);
        topLeftPane.setMinWidth(0);

        topRightPane = new VBox(10);
        topRightPane.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-padding: 10;");
        topRightPane.setMinHeight(0);
        topRightPane.setMinWidth(0);

        bottomLeftPane = new VBox(10);
        bottomLeftPane.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-padding: 10;");
        bottomLeftPane.setMinHeight(0);
        bottomLeftPane.setMinWidth(0);

        setupMessagePane();

        bottomRightPane = new VBox(10);
        bottomRightPane.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-padding: 10;");
        bottomRightPane.setMinHeight(0);
        bottomRightPane.setMinWidth(0);
        telemetryView = new ScreenVideoTelemetryView(telemetryViewModel);
        telemetryView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        bottomRightPane.getChildren().add(telemetryView);
        VBox.setVgrow(telemetryView, Priority.ALWAYS);

        GridPane.setHgrow(topLeftPane, Priority.ALWAYS);
        GridPane.setVgrow(topLeftPane, Priority.ALWAYS);
        GridPane.setHgrow(topRightPane, Priority.ALWAYS);
        GridPane.setVgrow(topRightPane, Priority.ALWAYS);
        GridPane.setHgrow(bottomLeftPane, Priority.ALWAYS);
        GridPane.setVgrow(bottomLeftPane, Priority.ALWAYS);
        GridPane.setHgrow(bottomRightPane, Priority.ALWAYS);
        GridPane.setVgrow(bottomRightPane, Priority.ALWAYS);

        root.add(topLeftPane, 0, 0);
        root.add(topRightPane, 1, 0);
        root.add(bottomLeftPane, 0, 1);
        root.add(bottomRightPane, 1, 1);

        Button prevBtn = new Button("Previous");
        Button nextBtn = new Button("Next");

        prevBtn.setOnAction(e -> {
            viewModel.movePrevious();
            updateChartWithAnimation(true);
        });

        nextBtn.setOnAction(e -> {
            viewModel.moveNext();
            updateChartWithAnimation(true);
        });

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(prevBtn, nextBtn);

        Button shapePrevBtn = new Button("Previous");
        Button shapeNextBtn = new Button("Next");

        shapePrevBtn.setOnAction(e -> {
            shapeViewModel.movePrevious();
            updateBarChartWithAnimation(true);
        });

        shapeNextBtn.setOnAction(e -> {
            shapeViewModel.moveNext();
            updateBarChartWithAnimation(true);
        });

        HBox shapeButtonBox = new HBox(10);
        shapeButtonBox.getChildren().addAll(shapePrevBtn, shapeNextBtn);

        createChart();
        topLeftPane.getChildren().addAll(buttonBox, chart);
        VBox.setVgrow(chart, Priority.ALWAYS);

        createBarChart();
        topRightPane.getChildren().addAll(shapeButtonBox, barChart);
        VBox.setVgrow(barChart, Priority.ALWAYS);

        getChildren().add(root);

        telemetryViewModel.fetchAndUpdateData();
        telemetryView.refresh();
    }

    private void bindPaneSizing() {
        widthProperty().addListener((obs, oldVal, newVal) -> updatePaneWidths(newVal.doubleValue()));
        heightProperty().addListener((obs, oldVal, newVal) -> updatePaneHeights(newVal.doubleValue()));
    }

    private void updatePaneWidths(double width) {
        if (width <= 0) {
            return;
        }
        double halfWidth = width / 2;
        setPaneWidth(topLeftPane, halfWidth);
        setPaneWidth(topRightPane, halfWidth);
        setPaneWidth(bottomLeftPane, halfWidth);
        setPaneWidth(bottomRightPane, halfWidth);
    }

    private void updatePaneHeights(double height) {
        if (height <= 0) {
            return;
        }
        double halfHeight = height / 2;
        setPaneHeight(topLeftPane, halfHeight);
        setPaneHeight(topRightPane, halfHeight);
        setPaneHeight(bottomLeftPane, halfHeight);
        setPaneHeight(bottomRightPane, halfHeight);
    }

    private void setPaneWidth(Region pane, double width) {
        pane.setMaxWidth(width);
    }

    private void setPaneHeight(Region pane, double height) {
        pane.setMaxHeight(height);
    }

    private void setupMessagePane() {
        Label titleLabel = new Label("Messages");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        messageContainer = new VBox(10);
        messageContainer.setPadding(new Insets(5));

        messageScrollPane = new ScrollPane(messageContainer);
        messageScrollPane.setFitToWidth(true);
        messageScrollPane.setStyle("-fx-background-color: transparent;");
        messageScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        messageScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        VBox.setVgrow(messageScrollPane, Priority.ALWAYS);

        bottomLeftPane.getChildren().addAll(titleLabel, messageScrollPane);
    }

    private void updateMessages() {
        String json = messageDataService.fetchNextData(rpc);
        List<String> messages = messageDataService.parseJson(json);

        for (int i = messages.size() - 1; i >= 0; i--) {
            allMessages.add(0, messages.get(i));
        }

        messageContainer.getChildren().clear();

        for (int i = 0; i < allMessages.size(); i += 2) {
            HBox row = new HBox(10);
            row.setPrefHeight(Region.USE_COMPUTED_SIZE);

            VBox box1 = createMessageBox(allMessages.get(i), i);
            HBox.setHgrow(box1, Priority.ALWAYS);
            row.getChildren().add(box1);

            if (i + 1 < allMessages.size()) {
                VBox box2 = createMessageBox(allMessages.get(i + 1), i + 1);
                HBox.setHgrow(box2, Priority.ALWAYS);
                row.getChildren().add(box2);
            }

            messageContainer.getChildren().add(row);
        }
    }

    private VBox createMessageBox(String message, int index) {
        VBox box = new VBox(5);
        box.setPadding(new Insets(10));
        box.setStyle(
                "-fx-background-color: " + getColorByIndex(index) + ";" +
                        "-fx-background-radius: 5;" +
                        "-fx-border-color: #cccccc;" +
                        "-fx-border-radius: 5;" +
                        "-fx-border-width: 1;");
        box.setMaxWidth(Double.MAX_VALUE);

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 12px;");
        messageLabel.setMaxWidth(Double.MAX_VALUE);

        box.getChildren().add(messageLabel);

        return box;
    }

    private String getColorByIndex(int index) {
        String[] colors = {
                "#E3F2FD",
                "#F3E5F5",
                "#E8F5E9",
                "#FFF3E0"
        };
        return colors[index % 4];
    }

    private void createChart() {
        xAxis = new NumberAxis();
        yAxis = new NumberAxis();
        xAxis.setLabel("Index (Time Order)");
        yAxis.setLabel("Sentiment");

        chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Sentiment Trend");
        chart.setAnimated(false);
    }

    private void createBarChart() {
        categoryAxis = new CategoryAxis();
        barYAxis = new NumberAxis();
        categoryAxis.setLabel("Data Snapshot");
        barYAxis.setLabel("Count");

        barChart = new BarChart<>(categoryAxis, barYAxis);
        barChart.setTitle("Shape Count Distribution (Last 3 Snapshots)");
        barChart.setAnimated(false);
        barChart.setCategoryGap(30);
        barChart.setBarGap(0);
        barChart.setLegendVisible(true);
    }

    private void updateBarChart() {
        updateBarChartWithAnimation(false);
    }

    private void updateBarChartWithAnimation(boolean animate) {
        ObservableList<ShapeCount> windowData = shapeViewModel.getWindowData();

        barChart.getData().clear();

        if (windowData.isEmpty()) {
            return;
        }

        XYChart.Series<String, Number> freeHandSeries = new XYChart.Series<>();
        freeHandSeries.setName("Free Hand");

        XYChart.Series<String, Number> straightLineSeries = new XYChart.Series<>();
        straightLineSeries.setName("Straight Line");

        XYChart.Series<String, Number> rectangleSeries = new XYChart.Series<>();
        rectangleSeries.setName("Rectangle");

        XYChart.Series<String, Number> ellipseSeries = new XYChart.Series<>();
        ellipseSeries.setName("Ellipse");

        XYChart.Series<String, Number> triangleSeries = new XYChart.Series<>();
        triangleSeries.setName("Triangle");

        int startIndex = shapeViewModel.getCurrentStartIndex();
        for (int i = 0; i < windowData.size(); i++) {
            ShapeCount data = windowData.get(i);
            String snapshotLabel = "T" + (startIndex + i + 1);

            freeHandSeries.getData().add(new XYChart.Data<>(snapshotLabel, data.getFreeHand()));
            straightLineSeries.getData().add(new XYChart.Data<>(snapshotLabel, data.getStraightLine()));
            rectangleSeries.getData().add(new XYChart.Data<>(snapshotLabel, data.getRectangle()));
            ellipseSeries.getData().add(new XYChart.Data<>(snapshotLabel, data.getEllipse()));
            triangleSeries.getData().add(new XYChart.Data<>(snapshotLabel, data.getTriangle()));
        }

        barChart.getData().addAll(
                freeHandSeries,
                straightLineSeries,
                rectangleSeries,
                ellipseSeries,
                triangleSeries);

        applyBarColors();

        if (animate) {
            // Placeholder for future animated transitions if needed
        }
    }

    private void applyBarColors() {
        Platform.runLater(() -> {
            String[] colors = {
                    "#3498db",
                    "#e74c3c",
                    "#2ecc71",
                    "#f39c12",
                    "#9b59b6"
            };

            for (int i = 0; i < barChart.getData().size() && i < colors.length; i++) {
                for (XYChart.Data<String, Number> data : barChart.getData().get(i).getData()) {
                    if (data.getNode() != null) {
                        data.getNode().setStyle("-fx-bar-fill: " + colors[i] + ";");
                    }
                }
            }
        });
    }

    private void updateChart() {
        updateChartWithAnimation(false);
    }

    private void updateChartWithAnimation(boolean animate) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Sentiment Trend");

        int startIndex = viewModel.getCurrentStartIndex();

        for (int i = 0; i < viewModel.getWindowData().size(); i++) {
            SentimentPoint point = viewModel.getWindowData().get(i);
            series.getData().add(new XYChart.Data<>(startIndex + i, point.getSentiment()));
        }

        if (!viewModel.getAllData().isEmpty()) {
            double targetLowerBound = viewModel.lowerBoundProperty().get();
            double targetUpperBound = viewModel.upperBoundProperty().get();
            double targetMinY = viewModel.minYProperty().get();
            double targetMaxY = viewModel.maxYProperty().get();

            if (animate) {
                Timeline timeline = new Timeline();

                xAxis.setAutoRanging(false);
                yAxis.setAutoRanging(false);

                KeyValue kvXLower = new KeyValue(xAxis.lowerBoundProperty(), targetLowerBound);
                KeyValue kvXUpper = new KeyValue(xAxis.upperBoundProperty(), targetUpperBound);
                KeyValue kvYLower = new KeyValue(yAxis.lowerBoundProperty(), targetMinY);
                KeyValue kvYUpper = new KeyValue(yAxis.upperBoundProperty(), targetMaxY);

                KeyFrame kf = new KeyFrame(Duration.millis(500), kvXLower, kvXUpper, kvYLower, kvYUpper);
                timeline.getKeyFrames().add(kf);
                timeline.play();
            } else {
                xAxis.setAutoRanging(false);
                xAxis.setLowerBound(targetLowerBound);
                xAxis.setUpperBound(targetUpperBound);
                xAxis.setTickUnit(1);

                yAxis.setAutoRanging(false);
                yAxis.setLowerBound(targetMinY);
                yAxis.setUpperBound(targetMaxY);
            }
        }

        chart.getData().clear();
        chart.getData().add(series);
    }

    private void startPeriodicUpdates() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                if (!"You".equals(meetingViewModel.currentUser.getDisplayName())) {

                    // 1️⃣ Run all heavy work OFF the FX thread
                    CompletableFuture
                            .supplyAsync(() -> {

                                System.out.println("Fetching full data update...");

                                viewModel.fetchAndUpdateData();
                                shapeViewModel.fetchAndUpdateData();
                                telemetryViewModel.fetchAndUpdateData();

                                System.out.println("Background work complete.");

                                return true;
                            })

                            // 2️⃣ Once done, switch to JavaFX UI thread
                            .thenAccept(result -> {
                                Platform.runLater(() -> {
                                    telemetryView.refresh();
                                    updateChartWithAnimation(false);
                                    updateMessages();
                                    updateBarChartWithAnimation(false);
                                });
                            });
                }
            }
        }, 0, 1000);
    }

}
