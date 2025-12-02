/**
 *  Contributed by Ram Charan.
 */

// package com.swe.ux.viewmodel;

package com.swe.ux.viewmodels;

import com.swe.ux.analytics.CanvasShapeMetricsCollector;
import com.swe.ux.model.analytics.ShapeCount;
import com.swe.ux.service.ShapeDataService;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;

/**
 * ViewModel for shape analytics.
 */
public class ShapeViewModel {
    /** Window size constant. */
    private static final int WINDOW_SIZE = 3;
    
    /** Data service for shape analytics. */
    private final ShapeDataService dataService;
    /** Live metrics collector fed by the canvas module. */
    private final CanvasShapeMetricsCollector metricsCollector;
    /** All shape count data. */
    private final ObservableList<ShapeCount> allData;
    /** Current start index property. */
    private final IntegerProperty currentStartIndex;
    /** Auto mode property. */
    private final BooleanProperty autoMode;
    /** Window size property. */
    private final IntegerProperty windowSize;

    /**
     * Creates a new ShapeViewModel.
     */
    public ShapeViewModel() {
        this.dataService = new ShapeDataService();
        this.metricsCollector = CanvasShapeMetricsCollector.getInstance();
        this.allData = FXCollections.observableArrayList();
        this.currentStartIndex = new SimpleIntegerProperty(0);
        this.autoMode = new SimpleBooleanProperty(true);
        this.windowSize = new SimpleIntegerProperty(WINDOW_SIZE); // Show 3 snapshots at a time
    }

    /**
     * Fetches and updates data.
     */
    public void fetchAndUpdateData() {
        final List<ShapeCount> liveSnapshots = metricsCollector.getHistory();
        if (!liveSnapshots.isEmpty()) {
            allData.setAll(liveSnapshots);
            autoMode.set(true);
        } else {
            final String json = dataService.fetchNextData();
            if (!json.isEmpty()) {
                final ShapeCount newData = dataService.parseJson(json);
                allData.add(newData);
                autoMode.set(true);
            }
        }
        calculateViewBounds();
    }

    private void calculateViewBounds() {
        final int n = allData.size();

        if (autoMode.get()) {
            // Show last 3 snapshots
            currentStartIndex.set(Math.max(0, n - windowSize.get()));
        }
    }

    /**
     * Moves to next view window.
     */
    public void moveNext() {
        autoMode.set(false);
        if (currentStartIndex.get() + 1 <= allData.size() - windowSize.get()) {
            currentStartIndex.set(currentStartIndex.get() + 1);
        }
    }

    /**
     * Moves to previous view window.
     */
    public void movePrevious() {
        autoMode.set(false);
        if (currentStartIndex.get() - 1 >= 0) {
            currentStartIndex.set(currentStartIndex.get() - 1);
        }
    }

    /**
     * Gets the data in the current window.
     * @return Observable list of shape counts in the window
     */
    public ObservableList<ShapeCount> getWindowData() {
        final int start = currentStartIndex.get();
        final int end = Math.min(start + windowSize.get(), allData.size());

        if (start >= allData.size()) {
            return FXCollections.observableArrayList();
        }

        return FXCollections.observableArrayList(allData.subList(start, end));
    }

    /**
     * Gets all data.
     * @return All shape count data
     */
    public ObservableList<ShapeCount> getAllData() {
        return allData;
    }

    /**
     * Gets current start index.
     * @return Current start index
     */
    public int getCurrentStartIndex() {
        return currentStartIndex.get();
    }

    /**
     * Returns the current start index property.
     * @return Current start index property
     */
    public IntegerProperty currentStartIndexProperty() {
        return currentStartIndex;
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
}
