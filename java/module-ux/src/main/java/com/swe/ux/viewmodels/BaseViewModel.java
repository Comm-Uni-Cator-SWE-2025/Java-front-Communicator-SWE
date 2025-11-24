package com.swe.ux.viewmodels;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Base class for all ViewModels in the MVVM architecture.
 * Provides property change support for data binding.
 */
public abstract class BaseViewModel {
    /** Property change support for data binding. */
    private final PropertyChangeSupport propertyChangeSupport;

    public BaseViewModel() {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }

    /**
     * Adds a property change listener.
     * @param listener The listener to add
     */
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Helper method to add a listener for a specific property.
     * @param propertyName The name of the property to listen to
     * @param listener The listener to be called when the property changes
     */
    public void addPropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Removes a property change listener.
     * @param listener The listener to remove
     */
    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Fires a property change event.
     * @param propertyName The name of the property that changed
     * @param oldValue The old value
     * @param newValue The new value
     */
    protected void firePropertyChange(final String propertyName, final Object oldValue, final Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * Called when the view is being destroyed.
     */
    public void onCleared() {
        // Can be overridden by subclasses to clean up resources
    }
}
