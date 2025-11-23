/**
 *  Contributed by Jyoti & Kishore.
 */

package com.swe.ux.viewmodel;

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

public class SentimentViewModel {
    private final SentimentDataService dataService;
    
    private final ObservableList<SentimentPoint> allData;
    private final IntegerProperty currentStartIndex;
    private final BooleanProperty autoMode;
    private final IntegerProperty windowSize;
    
    // Observable properties for UI binding
    private final DoubleProperty minY;
    private final DoubleProperty maxY;
    private final IntegerProperty lowerBound;
    private final IntegerProperty upperBound;
    private final AbstractRPC rpc;

    public SentimentViewModel(AbstractRPC rpc) {
        this.rpc = rpc;
        this.dataService = new SentimentDataService();
        this.allData = FXCollections.observableArrayList();
        this.currentStartIndex = new SimpleIntegerProperty(0);
        this.autoMode = new SimpleBooleanProperty(true);
        this.windowSize = new SimpleIntegerProperty(10);
        
        this.minY = new SimpleDoubleProperty(0);
        this.maxY = new SimpleDoubleProperty(0);
        this.lowerBound = new SimpleIntegerProperty(0);
        this.upperBound = new SimpleIntegerProperty(0);
    }

    public void fetchAndUpdateData() {
        String json = dataService.fetchNextData(rpc);
        
        if (!json.isEmpty()) {
            System.out.println("Updating Sentiment Data with: " + json);
            updateData(json);
            autoMode.set(true);
        }
        
        calculateViewBounds();
    }

    private void updateData(String json) {
        String lastTime = "";
        if (!allData.isEmpty()) {
            lastTime = allData.get(allData.size() - 1).getTime();
        }

        List<SentimentPoint> newPoints = dataService.parseJson(json);
        
        for (SentimentPoint point : newPoints) {
            if (lastTime.isEmpty() || point.getTime().compareTo(lastTime) > 0) {
                allData.add(point);
            }
        }
        
        FXCollections.sort(allData, Comparator.comparing(SentimentPoint::getTime));
    }

    private void calculateViewBounds() {
        int n = allData.size();
        
        if (autoMode.get()) {
            currentStartIndex.set(Math.max(0, n - (windowSize.get() / 2)));
        }
        
        int end = Math.min(currentStartIndex.get() + windowSize.get(), allData.size());
        
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        
        for (int i = currentStartIndex.get(); i < end; i++) {
            double sentiment = allData.get(i).getSentiment();
            if (sentiment < min) min = sentiment;
            if (sentiment > max) max = sentiment;
        }
        
        minY.set(min - 1);
        maxY.set(max + 1);
        lowerBound.set(currentStartIndex.get());
        upperBound.set(currentStartIndex.get() + windowSize.get() - 1);
    }

    public void moveNext() {
        autoMode.set(false);
        if (currentStartIndex.get() + (windowSize.get() / 2) < allData.size()) {
            currentStartIndex.set(currentStartIndex.get() + (windowSize.get() / 2));
            calculateViewBounds();
        }
    }

    public void movePrevious() {
        autoMode.set(false);
        if (currentStartIndex.get() - (windowSize.get() / 2) >= 0) {
            currentStartIndex.set(currentStartIndex.get() - (windowSize.get() / 2));
            calculateViewBounds();
        }
    }

    public ObservableList<SentimentPoint> getWindowData() {
        int end = Math.min(currentStartIndex.get() + windowSize.get(), allData.size());
        return FXCollections.observableArrayList(
            allData.subList(currentStartIndex.get(), end)
        );
    }

    // Getters for properties
    public ObservableList<SentimentPoint> getAllData() {
        return allData;
    }

    public IntegerProperty currentStartIndexProperty() {
        return currentStartIndex;
    }

    public int getCurrentStartIndex() {
        return currentStartIndex.get();
    }

    public BooleanProperty autoModeProperty() {
        return autoMode;
    }

    public IntegerProperty windowSizeProperty() {
        return windowSize;
    }

    public DoubleProperty minYProperty() {
        return minY;
    }

    public DoubleProperty maxYProperty() {
        return maxY;
    }

    public IntegerProperty lowerBoundProperty() {
        return lowerBound;
    }

    public IntegerProperty upperBoundProperty() {
        return upperBound;
    }
}
