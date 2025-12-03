package com.swe.ux.views;

import com.swe.controller.Meeting.UserProfile;
import com.swe.ux.model.analytics.ShapeCount;
import com.swe.controller.RPCinterface.AbstractRPC;
import com.swe.ux.model.analytics.SentimentPoint;
import com.swe.ux.viewmodels.MeetingViewModel;
import com.swe.ux.viewmodels.ScreenVideoTelemetryViewModel;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

import com.swe.ux.viewmodels.SentimentViewModel;
import com.swe.ux.viewmodels.ShapeViewModel;

/**
 * JavaFX pane that renders the Sentiment + Shape analytics dashboard.
 * Extracted from the standalone SentimentView application so it can be embedded
 * in Swing.
 * Contributed by Kishore & Jyoti.
 */
public class SentimentViewPane extends StackPane {
    /** Default padding value. */
    private static final int DEFAULT_PADDING = 10;
    /** Default spacing value. */
    private static final int DEFAULT_SPACING = 10;
    /** Default gap value. */
    private static final int DEFAULT_GAP = 5;
    /** Default preferred width. */
    private static final int PREFERRED_WIDTH = 1200;
    /** Default preferred height. */
    private static final int PREFERRED_HEIGHT = 800;
    /** Animation duration in milliseconds. */
    private static final int ANIMATION_DURATION_MS = 500;
    /** Timer interval in milliseconds. */
    private static final int TIMER_INTERVAL_MS = 1000;
    /** Bar chart category gap. */
    private static final int BAR_CHART_CATEGORY_GAP = 30;
    /** Number of color options. */
    private static final int COLOR_COUNT = 4;
    /** Message box padding. */
    private static final int MESSAGE_BOX_PADDING = 10;
    /** Message box spacing. */
    private static final int MESSAGE_BOX_SPACING = 5;
    /** Font size for title label. */
    private static final int TITLE_FONT_SIZE = 14;
    /** Font size for message label. */
    private static final int MESSAGE_FONT_SIZE = 12;
    /** Maximum samples to keep for user count chart. */
    private static final int MAX_USER_COUNT_POINTS = 30;

    /** Sentiment view model. */
    private final SentimentViewModel viewModel;
    /** Shape view model. */
    private final ShapeViewModel shapeViewModel;
    /** Telemetry view model. */
    private final ScreenVideoTelemetryViewModel telemetryViewModel;
    /** All messages list. */
    private final List<String> allMessages;

    /** Root grid pane. */
    private GridPane root;
    /** Top left pane. */
    private VBox topLeftPane;
    /** Top right pane. */
    private VBox topRightPane;
    /** Bottom left pane. */
    private VBox bottomLeftPane;
    /** Bottom right pane. */
    private VBox bottomRightPane;
    /** Telemetry view. */
    private ScreenVideoTelemetryView telemetryView;
    /** Message container. */
    private VBox messageContainer;
    /** Message scroll pane. */
    private ScrollPane messageScrollPane;
    /** Line chart. */
    private LineChart<Number, Number> chart;
    /** X axis. */
    private NumberAxis xAxis;
    /** Y axis. */
    private NumberAxis yAxis;
    /** Slider to drag the sentiment window. */
    private Slider sentimentWindowSlider;
    /** User count chart. */
    private LineChart<Number, Number> userCountChart;
    /** User count X axis. */
    private NumberAxis userCountXAxis;
    /** User count Y axis. */
    private NumberAxis userCountYAxis;
    /** Bar chart. */
    private BarChart<String, Number> barChart;
    /** Category axis. */
    private CategoryAxis categoryAxis;
    /** Bar Y axis. */
    private NumberAxis barYAxis;
    /** RPC instance. */
    private AbstractRPC rpc;
    /** Meeting view model. */
    private MeetingViewModel meetingViewModel;
    /** Rolling user count history. */
    private final Deque<UserCountSample> userCountHistory;
    /** Sample index for user counts. */
    private int userCountSampleIndex;
    /** Flag to avoid slider feedback loops. */
    private boolean programmaticSliderUpdate;

    /**
     * Creates a new SentimentViewPane.
     * @param meetingViewModelParam The meeting view model
     */
    public SentimentViewPane(final MeetingViewModel meetingViewModelParam) {
        this.rpc = meetingViewModelParam.getRpc();
        this.meetingViewModel = meetingViewModelParam;
        this.viewModel = new SentimentViewModel(rpc);
        this.shapeViewModel = new ShapeViewModel();
        this.telemetryViewModel = new ScreenVideoTelemetryViewModel();
        this.allMessages = new ArrayList<>();
        this.userCountHistory = new ArrayDeque<>();
        this.userCountSampleIndex = 0;
        this.meetingViewModel.messages.addListener(evt ->
                Platform.runLater(this::updateMessages));

        setPadding(new Insets(DEFAULT_PADDING));
        setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);

        buildLayout();
        bindPaneSizing();
        startPeriodicUpdates();
    }

    private void buildLayout() {
        root = new GridPane();
        root.setHgap(DEFAULT_GAP);
        root.setVgap(DEFAULT_GAP);
        root.setPadding(new Insets(0));

        topLeftPane = new VBox(DEFAULT_SPACING);
        topLeftPane.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-padding: " + DEFAULT_PADDING + ";");
        topLeftPane.setMinHeight(0);
        topLeftPane.setMinWidth(0);

        topRightPane = new VBox(DEFAULT_SPACING);
        topRightPane.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-padding: " + DEFAULT_PADDING + ";");
        topRightPane.setMinHeight(0);
        topRightPane.setMinWidth(0);

        bottomLeftPane = new VBox(DEFAULT_SPACING);
        bottomLeftPane.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-padding: " + DEFAULT_PADDING + ";");
        bottomLeftPane.setMinHeight(0);
        bottomLeftPane.setMinWidth(0);

        setupMessagePane();

        bottomRightPane = new VBox(DEFAULT_SPACING);
        bottomRightPane.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-padding: " + DEFAULT_PADDING + ";");
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

        final Button prevBtn = new Button("Previous");
        final Button nextBtn = new Button("Next");

        prevBtn.setOnAction(e -> {
            viewModel.movePrevious();
            updateChartWithAnimation(true);
        });

        nextBtn.setOnAction(e -> {
            viewModel.moveNext();
            updateChartWithAnimation(true);
        });

        final HBox buttonBox = new HBox(DEFAULT_SPACING);
        buttonBox.getChildren().addAll(prevBtn, nextBtn);

        final Button shapePrevBtn = new Button("Previous");
        final Button shapeNextBtn = new Button("Next");

        shapePrevBtn.setOnAction(e -> {
            shapeViewModel.movePrevious();
            updateBarChartWithAnimation(true);
        });

        shapeNextBtn.setOnAction(e -> {
            shapeViewModel.moveNext();
            updateBarChartWithAnimation(true);
        });

        final HBox shapeButtonBox = new HBox(DEFAULT_SPACING);
        shapeButtonBox.getChildren().addAll(shapePrevBtn, shapeNextBtn);

        createChart();
        createTimelineSlider();
        createUserCountChart();
        final VBox participantSection = new VBox(5);
        participantSection.setAlignment(Pos.TOP_LEFT);
        participantSection.setPrefWidth(320);
        participantSection.setMaxWidth(360);
        final Label participantsLabel = new Label("Participants Over Time");
        participantsLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        participantSection.getChildren().addAll(participantsLabel, userCountChart);
        VBox.setVgrow(userCountChart, Priority.ALWAYS);

        final HBox chartsRow = new HBox(DEFAULT_SPACING);
        chartsRow.setAlignment(Pos.TOP_LEFT);
        chartsRow.getChildren().addAll(chart, participantSection);
        HBox.setHgrow(chart, Priority.ALWAYS);

        final VBox timelineControls = new VBox(6);
        timelineControls.getChildren().addAll(buttonBox, sentimentWindowSlider);

        topLeftPane.getChildren().addAll(timelineControls, chartsRow);
        VBox.setVgrow(chartsRow, Priority.ALWAYS);

        createBarChart();
        topRightPane.getChildren().addAll(shapeButtonBox, barChart);
        VBox.setVgrow(barChart, Priority.ALWAYS);

        getChildren().add(root);

        telemetryViewModel.fetchAndUpdateData();
        telemetryView.refresh();
        updateUserCountChart();
        com.swe.launcher.FrontendStateChecker.markUIReady();
    }

    private void bindPaneSizing() {
        widthProperty().addListener((obs, oldVal, newVal) -> updatePaneWidths(newVal.doubleValue()));
        heightProperty().addListener((obs, oldVal, newVal) -> updatePaneHeights(newVal.doubleValue()));
    }

    private void updatePaneWidths(final double width) {
        if (width <= 0) {
            return;
        }
        final double halfWidth = width / 2;
        setPaneWidth(topLeftPane, halfWidth);
        setPaneWidth(topRightPane, halfWidth);
        setPaneWidth(bottomLeftPane, halfWidth);
        setPaneWidth(bottomRightPane, halfWidth);
    }

    private void updatePaneHeights(final double height) {
        if (height <= 0) {
            return;
        }
        final double halfHeight = height / 2;
        setPaneHeight(topLeftPane, halfHeight);
        setPaneHeight(topRightPane, halfHeight);
        setPaneHeight(bottomLeftPane, halfHeight);
        setPaneHeight(bottomRightPane, halfHeight);
    }

    private void setPaneWidth(final Region pane, final double width) {
        pane.setMaxWidth(width);
    }

    private void setPaneHeight(final Region pane, final double height) {
        pane.setMaxHeight(height);
    }

    private void setupMessagePane() {
        final Label titleLabel = new Label("Messages");
        titleLabel.setStyle("-fx-font-size: " + TITLE_FONT_SIZE + "px; -fx-font-weight: bold;");

        messageContainer = new VBox(DEFAULT_SPACING);
        messageContainer.setPadding(new Insets(MESSAGE_BOX_SPACING));

        messageScrollPane = new ScrollPane(messageContainer);
        messageScrollPane.setFitToWidth(true);
        messageScrollPane.setStyle("-fx-background-color: transparent;");
        messageScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        messageScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        VBox.setVgrow(messageScrollPane, Priority.ALWAYS);

        bottomLeftPane.getChildren().addAll(titleLabel, messageScrollPane);
        updateMessages();
    }

    private void updateMessages() {
        if (messageContainer == null || meetingViewModel == null) {
            return;
        }
        final List<String> meetingMessages = meetingViewModel.messages.get();
        allMessages.clear();
        if (meetingMessages != null) {
            for (int i = meetingMessages.size() - 1; i >= 0; i--) {
                final String message = meetingMessages.get(i);
                if (message != null && !message.isBlank()) {
                    allMessages.add(message);
                }
            }
        }

        messageContainer.getChildren().clear();

        final int stepSize = 2;
        for (int i = 0; i < allMessages.size(); i += stepSize) {
            final HBox row = new HBox(DEFAULT_SPACING);
            row.setPrefHeight(Region.USE_COMPUTED_SIZE);

            final VBox box1 = createMessageBox(allMessages.get(i), i);
            HBox.setHgrow(box1, Priority.ALWAYS);
            row.getChildren().add(box1);

            if (i + 1 < allMessages.size()) {
                final VBox box2 = createMessageBox(allMessages.get(i + 1), i + 1);
                HBox.setHgrow(box2, Priority.ALWAYS);
                row.getChildren().add(box2);
            }

            messageContainer.getChildren().add(row);
        }
    }

    private VBox createMessageBox(final String message, final int index) {
        final VBox box = new VBox(MESSAGE_BOX_SPACING);
        box.setPadding(new Insets(MESSAGE_BOX_PADDING));
        final int borderRadius = 5;
        final int borderWidth = 1;
        box.setStyle(
                "-fx-background-color: " + getColorByIndex(index) + ";"
                        + "-fx-background-radius: " + borderRadius + ";"
                        + "-fx-border-color: #cccccc;"
                        + "-fx-border-radius: " + borderRadius + ";"
                        + "-fx-border-width: " + borderWidth + ";");
        box.setMaxWidth(Double.MAX_VALUE);

        final Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: " + MESSAGE_FONT_SIZE + "px;");
        messageLabel.setMaxWidth(Double.MAX_VALUE);

        box.getChildren().add(messageLabel);

        return box;
    }

    private String getColorByIndex(final int index) {
        final String[] colors = {
            "#E3F2FD",
            "#F3E5F5",
            "#E8F5E9",
            "#FFF3E0",
        };
        return colors[index % COLOR_COUNT];
    }

    private void createChart() {
        xAxis = new NumberAxis();
        yAxis = new NumberAxis();
        xAxis.setLabel("Index (Time Order)");
        yAxis.setLabel("Sentiment");

        chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Sentiment Trend");
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setMinHeight(260);
    }

    private void createTimelineSlider() {
        sentimentWindowSlider = new Slider(0, 0, 0);
        sentimentWindowSlider.setBlockIncrement(1);
        sentimentWindowSlider.setMajorTickUnit(1);
        sentimentWindowSlider.setMinorTickCount(0);
        sentimentWindowSlider.setSnapToTicks(true);
        sentimentWindowSlider.setShowTickLabels(false);
        sentimentWindowSlider.setShowTickMarks(false);
        sentimentWindowSlider.setDisable(true);
        sentimentWindowSlider.setMaxWidth(Double.MAX_VALUE);

        sentimentWindowSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (programmaticSliderUpdate) {
                return;
            }
            final int targetIndex = (int) Math.round(newVal.doubleValue());
            viewModel.moveWindowTo(targetIndex);
            updateChartWithAnimation(false);
        });

        viewModel.currentStartIndexProperty().addListener((obs, oldVal, newVal) ->
                Platform.runLater(() -> updateSliderFromViewModel(newVal.intValue())));
    }

    private void createUserCountChart() {
        userCountXAxis = new NumberAxis();
        userCountYAxis = new NumberAxis();
        userCountXAxis.setLabel("Sample");
        userCountYAxis.setLabel("Users");

        userCountChart = new LineChart<>(userCountXAxis, userCountYAxis);
        userCountChart.setTitle(null);
        userCountChart.setLegendVisible(false);
        userCountChart.setAnimated(false);
        userCountChart.setCreateSymbols(true);
        userCountChart.setMinHeight(220);
    }

    private void createBarChart() {
        categoryAxis = new CategoryAxis();
        barYAxis = new NumberAxis();
        categoryAxis.setLabel("Data Snapshot");
        barYAxis.setLabel("Count");

        barChart = new BarChart<>(categoryAxis, barYAxis);
        barChart.setTitle("Shape Count Distribution (Last 3 Snapshots)");
        barChart.setAnimated(false);
        barChart.setCategoryGap(BAR_CHART_CATEGORY_GAP);
        barChart.setBarGap(0);
        barChart.setLegendVisible(true);
    }

    private void updateBarChart() {
        updateBarChartWithAnimation(false);
    }

    private void updateBarChartWithAnimation(final boolean animate) {
        final ObservableList<ShapeCount> windowData = shapeViewModel.getWindowData();

        barChart.getData().clear();

        if (windowData.isEmpty()) {
            return;
        }

        final XYChart.Series<String, Number> freeHandSeries = new XYChart.Series<>();
        freeHandSeries.setName("Free Hand");

        final XYChart.Series<String, Number> straightLineSeries = new XYChart.Series<>();
        straightLineSeries.setName("Straight Line");

        final XYChart.Series<String, Number> rectangleSeries = new XYChart.Series<>();
        rectangleSeries.setName("Rectangle");

        final XYChart.Series<String, Number> ellipseSeries = new XYChart.Series<>();
        ellipseSeries.setName("Ellipse");

        final XYChart.Series<String, Number> triangleSeries = new XYChart.Series<>();
        triangleSeries.setName("Triangle");

        final int startIndex = shapeViewModel.getCurrentStartIndex();
        for (int i = 0; i < windowData.size(); i++) {
            final ShapeCount data = windowData.get(i);
            final String snapshotLabel = "T" + (startIndex + i + 1);

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
            // No action needed at this time
            return;
        }
    }

    private void applyBarColors() {
        Platform.runLater(() -> {
            final String[] colors = {
                "#3498db",
                "#e74c3c",
                "#2ecc71",
                "#f39c12",
                "#9b59b6",
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

    private void updateChartWithAnimation(final boolean animate) {
        final XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Sentiment Trend");

        final int startIndex = viewModel.getCurrentStartIndex();

        for (int i = 0; i < viewModel.getWindowData().size(); i++) {
            final SentimentPoint point = viewModel.getWindowData().get(i);
            series.getData().add(new XYChart.Data<>(startIndex + i, point.getSentiment()));
        }

        if (!viewModel.getAllData().isEmpty()) {
            final double targetLowerBound = viewModel.lowerBoundProperty().get();
            final double targetUpperBound = viewModel.upperBoundProperty().get();
            final double targetMinY = viewModel.minYProperty().get();
            final double targetMaxY = viewModel.maxYProperty().get();

            if (animate) {
                final Timeline timeline = new Timeline();

                xAxis.setAutoRanging(false);
                yAxis.setAutoRanging(false);

                final KeyValue kvXLower = new KeyValue(xAxis.lowerBoundProperty(), targetLowerBound);
                final KeyValue kvXUpper = new KeyValue(xAxis.upperBoundProperty(), targetUpperBound);
                final KeyValue kvYLower = new KeyValue(yAxis.lowerBoundProperty(), targetMinY);
                final KeyValue kvYUpper = new KeyValue(yAxis.upperBoundProperty(), targetMaxY);

                final KeyFrame kf = new KeyFrame(Duration.millis(ANIMATION_DURATION_MS),
                        kvXLower, kvXUpper, kvYLower, kvYUpper);
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
        refreshTimelineSliderBounds();
        updateSliderFromViewModel(startIndex);
    }

    private void refreshTimelineSliderBounds() {
        if (sentimentWindowSlider == null) {
            return;
        }
        sentimentWindowSlider.setMin(0);
        final int maxStartIndex = viewModel.getMaxStartIndex();
        sentimentWindowSlider.setMax(Math.max(0, maxStartIndex));
        final boolean canSlide = viewModel.getAllData().size() > viewModel.windowSizeProperty().get();
        sentimentWindowSlider.setDisable(!canSlide);
        sentimentWindowSlider.setBlockIncrement(Math.max(1, viewModel.windowSizeProperty().get() / 2));
    }

    private void updateSliderFromViewModel(final int index) {
        if (sentimentWindowSlider == null) {
            return;
        }
        programmaticSliderUpdate = true;
        sentimentWindowSlider.setValue(index);
        programmaticSliderUpdate = false;
    }

    private void updateUserCountChart() {
        if (meetingViewModel == null || userCountChart == null) {
            return;
        }
        recordUserCountSample();

        final XYChart.Series<Number, Number> series = new XYChart.Series<>();
        int minCount = Integer.MAX_VALUE;
        int maxCount = Integer.MIN_VALUE;
        for (UserCountSample sample : userCountHistory) {
            series.getData().add(new XYChart.Data<>(sample.index(), sample.count()));
            minCount = Math.min(minCount, sample.count());
            maxCount = Math.max(maxCount, sample.count());
        }

        userCountChart.getData().setAll(series);

        if (!userCountHistory.isEmpty()) {
            final UserCountSample first = userCountHistory.peekFirst();
            final UserCountSample last = userCountHistory.peekLast();
            userCountXAxis.setAutoRanging(false);
            userCountXAxis.setLowerBound(first.index());
            userCountXAxis.setUpperBound(last.index());
            final double tickUnit = Math.max(1, (last.index() - first.index()) / 5.0);
            userCountXAxis.setTickUnit(tickUnit);

            if (minCount == Integer.MAX_VALUE) {
                minCount = 0;
            }
            if (maxCount == Integer.MIN_VALUE) {
                maxCount = 0;
            }
            final int padding = Math.max(1, (maxCount - minCount) / 4);
            userCountYAxis.setAutoRanging(false);
            userCountYAxis.setLowerBound(Math.max(0, minCount - padding));
            userCountYAxis.setUpperBound(maxCount + padding);
        } else {
            userCountXAxis.setAutoRanging(true);
            userCountYAxis.setAutoRanging(true);
        }
    }

    private void recordUserCountSample() {
        if (meetingViewModel == null || meetingViewModel.getParticipants() == null) {
            return;
        }
        final List<UserProfile> participantsList = meetingViewModel.getParticipants().get();
        final int count = participantsList == null ? 0 : participantsList.size();
        userCountSampleIndex++;
        userCountHistory.addLast(new UserCountSample(userCountSampleIndex, count));
        while (userCountHistory.size() > MAX_USER_COUNT_POINTS) {
            userCountHistory.removeFirst();
        }
    }

    private static final class UserCountSample {
        private final int index;
        private final int count;

        UserCountSample(final int idx, final int value) {
            this.index = idx;
            this.count = value;
        }

        int index() {
            return index;
        }

        int count() {
            return count;
        }
    }

    private void startPeriodicUpdates() {
        final Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                if (!"You".equals(meetingViewModel.getCurrentUser().getDisplayName())) {

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
                                    updateUserCountChart();
                                });
                            });
                }
            }
        }, 0, TIMER_INTERVAL_MS);
    }

}
