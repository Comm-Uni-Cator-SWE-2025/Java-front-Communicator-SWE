/**
 *  Contributed by Jyoti & Kishore.
 */

package com.swe.ux.viewmodels;

import com.swe.controller.RPCinterface.AbstractRPC;
import com.swe.ux.model.analytics.SentimentPoint;
import com.swe.ux.service.SentimentDataService;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Comparator;
import java.util.List;

/**
 * ViewModel for sentiment analytics.
 */
public class SentimentViewModel {
    /** Window size constant. */
    private static final int WINDOW_SIZE = 10;
    /** Half window constant. */
    private static final int HALF_WINDOW = 2;
    /** Fixed lower bound for y-axis. */
    private static final double FIXED_MIN_Y = -10.0;
    /** Fixed upper bound for y-axis. */
    private static final double FIXED_MAX_Y = 10.0;
    
    /** Data service for sentiment analytics. */
    private final SentimentDataService dataService;
    
    /** All sentiment data. */
    private final ObservableList<SentimentPoint> allData;
    /** Current start index property. */
    private final IntegerProperty currentStartIndex;
    /** Auto mode property. */
    private final BooleanProperty autoMode;
    /** Window size property. */
    private final IntegerProperty windowSize;
    
    /** Minimum Y property for UI binding. */
    private final DoubleProperty minY;
    /** Maximum Y property for UI binding. */
    private final DoubleProperty maxY;
    /** Lower bound property for UI binding. */
    private final IntegerProperty lowerBound;
    /** Upper bound property for UI binding. */
    private final IntegerProperty upperBound;
    /** RPC instance. */
    private final AbstractRPC rpc;

    /**
     * Creates a new SentimentViewModel.
     * @param rpcParam The RPC instance
     */
    public SentimentViewModel(final AbstractRPC rpcParam) {
        this.rpc = rpcParam;
        this.dataService = new SentimentDataService();
        this.allData = FXCollections.observableArrayList();
        this.currentStartIndex = new SimpleIntegerProperty(0);
        this.autoMode = new SimpleBooleanProperty(true);
        this.windowSize = new SimpleIntegerProperty(WINDOW_SIZE);
        
        this.minY = new SimpleDoubleProperty(FIXED_MIN_Y);
        this.maxY = new SimpleDoubleProperty(FIXED_MAX_Y);
        this.lowerBound = new SimpleIntegerProperty(0);
        this.upperBound = new SimpleIntegerProperty(0);
    }

    /**
     * Fetches and updates sentiment data.
     */
    public void fetchAndUpdateData() {
        final String json = dataService.fetchNextData(rpc);
        
        if (!json.isEmpty()) {
            System.out.println("Updating Sentiment Data with: " + json);
            updateData(json);
        }
        
        calculateViewBounds();
    }

    private void updateData(final String json) {
        String lastTime = "";
        if (!allData.isEmpty()) {
            lastTime = allData.get(allData.size() - 1).getTime();
        }

        final List<SentimentPoint> newPoints = dataService.parseJson(json);
        
        for (final SentimentPoint point : newPoints) {
            if (lastTime.isEmpty() || point.getTime().compareTo(lastTime) > 0) {
                allData.add(point);
            }
        }
        
        FXCollections.sort(allData, Comparator.comparing(SentimentPoint::getTime));
    }

    private void calculateViewBounds() {
        final int n = allData.size();
        
        if (autoMode.get()) {
            currentStartIndex.set(Math.max(0, n - (windowSize.get() / HALF_WINDOW)));
        }
        
        final int end = Math.min(currentStartIndex.get() + windowSize.get(), allData.size());
        
        // Always keep the same y-axis range so the chart doesn't shrink/expand with data
        minY.set(FIXED_MIN_Y);
        maxY.set(FIXED_MAX_Y);
        lowerBound.set(currentStartIndex.get());
        upperBound.set(currentStartIndex.get() + windowSize.get() - 1);
    }

    /**
     * Moves to next data window.
     */
    public void moveNext() {
        autoMode.set(false);
        if (currentStartIndex.get() + (windowSize.get() / HALF_WINDOW) < allData.size()) {
            currentStartIndex.set(currentStartIndex.get() + (windowSize.get() / HALF_WINDOW));
            calculateViewBounds();
        }
    }

    /**
     * Moves to previous data window.
     */
    public void movePrevious() {
        autoMode.set(false);
        if (currentStartIndex.get() - (windowSize.get() / HALF_WINDOW) >= 0) {
            currentStartIndex.set(currentStartIndex.get() - (windowSize.get() / HALF_WINDOW));
            calculateViewBounds();
        }
    }

    /**
     * Moves the window to the given start index, typically driven by a slider.
     * Auto mode is disabled to allow manual control.
     *
     * @param startIndex the desired starting index for the window
     */
    public void moveWindowTo(final int startIndex) {
        autoMode.set(false);
        final int clamped = clampStartIndex(startIndex);
        currentStartIndex.set(clamped);
        calculateViewBounds();
    }

    /**
     * Maximum valid start index for the current dataset.
     *
     * @return the highest index the window can start at
     */
    public int getMaxStartIndex() {
        final int size = allData.size();
        final int window = windowSize.get();
        if (size <= window) {
            return 0;
        }
        return size - window;
    }

    private int clampStartIndex(final int startIndex) {
        return Math.max(0, Math.min(startIndex, getMaxStartIndex()));
    }

    /**
     * Gets the data in the current window.
     * @return Observable list of sentiment points
     */
    public ObservableList<SentimentPoint> getWindowData() {
        final int end = Math.min(currentStartIndex.get() + windowSize.get(), allData.size());
        return FXCollections.observableArrayList(
            allData.subList(currentStartIndex.get(), end)
        );
    }

    /**
     * Gets all data.
     * @return All sentiment data
     */
    public ObservableList<SentimentPoint> getAllData() {
        return allData;
    }

    /**
     * Returns the current start index property.
     * @return Current start index property
     */
    public IntegerProperty currentStartIndexProperty() {
        return currentStartIndex;
    }

    /**
     * Gets the current start index.
     * @return Current start index value
     */
    public int getCurrentStartIndex() {
        return currentStartIndex.get();
    }

    /**
     * Returns the auto mode property.
     * @return Auto mode property
     */
    public BooleanProperty autoModeProperty() {
        return autoMode;
    }

    /**
     * Returns the window size property.
     * @return Window size property
     */
    public IntegerProperty windowSizeProperty() {
        return windowSize;
    }

    /**
     * Returns the minimum Y property.
     * @return Minimum Y property
     */
    public DoubleProperty minYProperty() {
        return minY;
    }

    /**
     * Returns the maximum Y property.
     * @return Maximum Y property
     */
    public DoubleProperty maxYProperty() {
        return maxY;
    }

    /**
     * Returns the lower bound property.
     * @return Lower bound property
     */
    public IntegerProperty lowerBoundProperty() {
        return lowerBound;
    }

    /**
     * Returns the upper bound property.
     * @return Upper bound property
     */
    public IntegerProperty upperBoundProperty() {
        return upperBound;
    }
}
