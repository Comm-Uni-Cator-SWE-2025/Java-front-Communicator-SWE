package com.swe.ux.viewmodel;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Base class for all ViewModels in the MVVM architecture.
 * Provides property change support for data binding.
 */
public abstract class BaseViewModel {
    protected final PropertyChangeSupport propertyChangeSupport;

    public BaseViewModel() {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * Helper method to add a listener for a specific property
     * @param propertyName The name of the property to listen to
     * @param listener The listener to be called when the property changes
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Called when the view is being destroyed
     */
    public void onCleared() {
        // Can be overridden by subclasses to clean up resources
    }
}
