package com.swe.ux.binding;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Objects;

/**
 * A property that can be observed for changes.
 * @param <T> The type of the property value
 */
public class BindableProperty<T> {
    private T value;
    private final PropertyChangeSupport propertyChangeSupport;
    private final String propertyName;

    /**
     * Creates a new BindableProperty with an initial value and property name.
     * @param initialValue The initial value of the property
     * @param propertyName The name of the property (used for change events)
     */
    public BindableProperty(T initialValue, String propertyName) {
        this.value = initialValue;
        this.propertyName = propertyName != null ? propertyName : "";
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }

    /**
     * Gets the current value of the property.
     * @return The current value
     */
    public T get() {
        return value;
    }

    /**
     * Sets a new value for the property and notifies listeners if the value has changed.
     * @param newValue The new value
     */
    public void set(T newValue) {
        if (!Objects.equals(this.value, newValue)) {
            T oldValue = this.value;
            this.value = newValue;
            propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    /**
     * Adds a property change listener.
     * @param listener The listener to add
     */
    public void addListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Removes a property change listener.
     * @param listener The listener to remove
     */
    public void removeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Binds this property to another bindable property bidirectionally.
     * @param other The other property to bind to
     */
    public void bindBidirectional(BindableProperty<T> other) {
        // Update other when this changes
        this.addListener(evt -> {
            if (!other.get().equals(this.get())) {
                other.set(this.get());
            }
        });

        // Update this when other changes
        other.addListener(evt -> {
            if (!this.get().equals(other.get())) {
                this.set(other.get());
            }
        });
    }

    /**
     * Binds this property to another bindable property unidirectionally.
     * @param other The other property to bind to
     */
    public void bind(BindableProperty<T> other) {
        // Update this when other changes
        other.addListener(evt -> {
            if (!this.get().equals(other.get())) {
                this.set(other.get());
            }
        });
    }
}
