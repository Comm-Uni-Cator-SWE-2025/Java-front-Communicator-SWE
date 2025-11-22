package com.swe.ux.viewmodels;

import com.swe.ux.model.analytics.ShapeCount;
import com.swe.ux.service.ShapeDataService;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ShapeViewModel {
    private final ShapeDataService dataService;
    private final ObservableList<ShapeCount> allData;
    private final IntegerProperty currentStartIndex;
    private final BooleanProperty autoMode;
    private final IntegerProperty windowSize;

    public ShapeViewModel() {
        this.dataService = new ShapeDataService();
        this.allData = FXCollections.observableArrayList();
        this.currentStartIndex = new SimpleIntegerProperty(0);
        this.autoMode = new SimpleBooleanProperty(true);
        this.windowSize = new SimpleIntegerProperty(3); // Show 3 snapshots at a time
    }

    public void fetchAndUpdateData() {
        String json = dataService.fetchNextData();
        
        if (!json.isEmpty()) {
            ShapeCount newData = dataService.parseJson(json);
            allData.add(newData);
            autoMode.set(true);
        }
        
        calculateViewBounds();
    }

    private void calculateViewBounds() {
        int n = allData.size();
        
        if (autoMode.get()) {
            // Show last 3 snapshots
            currentStartIndex.set(Math.max(0, n - windowSize.get()));
        }
    }

    public void moveNext() {
        autoMode.set(false);
        if (currentStartIndex.get() + 1 <= allData.size() - windowSize.get()) {
            currentStartIndex.set(currentStartIndex.get() + 1);
        }
    }

    public void movePrevious() {
        autoMode.set(false);
        if (currentStartIndex.get() - 1 >= 0) {
            currentStartIndex.set(currentStartIndex.get() - 1);
        }
    }

    public ObservableList<ShapeCount> getWindowData() {
        int start = currentStartIndex.get();
        int end = Math.min(start + windowSize.get(), allData.size());
        
        if (start >= allData.size()) {
            return FXCollections.observableArrayList();
        }
        
        return FXCollections.observableArrayList(allData.subList(start, end));
    }

    public ObservableList<ShapeCount> getAllData() {
        return allData;
    }

    public int getCurrentStartIndex() {
        return currentStartIndex.get();
    }

    public IntegerProperty currentStartIndexProperty() {
        return currentStartIndex;
    }

    public BooleanProperty autoModeProperty() {
        return autoMode;
    }

    public IntegerProperty windowSizeProperty() {
        return windowSize;
    }
}
