package com.swe.ux.binding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.swe.controller.Meeting.UserProfile;

/**
 * Utility class for creating property change listeners with lambda expressions.
 */
public final class PropertyListeners {
    
    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private PropertyListeners() {
    }
    
    /**
     * Creates a property change listener that calls the given consumer with old and new values.
     * @param consumer The consumer to call when the property changes
     * @param <T> The type of the property value
     * @return A PropertyChangeListener
     */
    @SuppressWarnings("unchecked")
    public static <T> PropertyChangeListener of(final BiConsumer<T, T> consumer) {
        return (final PropertyChangeEvent evt) -> {
            final T oldValue = (T) evt.getOldValue();
            final T newValue = (T) evt.getNewValue();
            consumer.accept(oldValue, newValue);
        };
    }
    
    /**
     * Creates a property change listener that only cares about the new value.
     * @param consumer The consumer to call with the new value when the property changes
     * @param <T> The type of the property value
     * @return A PropertyChangeListener
     */
    @SuppressWarnings("unchecked")
    public static <T> PropertyChangeListener onChanged(final Consumer<T> consumer) {
        return (final PropertyChangeEvent evt) -> {
            final T newValue = (T) evt.getNewValue();
            consumer.accept(newValue);
        };
    }
    
    /**
     * Creates a property change listener that calls the given runnable when the property changes.
     * @param runnable The runnable to call when the property changes
     * @return A PropertyChangeListener
     */
    public static PropertyChangeListener onChanged(final Runnable runnable) {
        return (final PropertyChangeEvent evt) -> runnable.run();
    }
    
    /**
     * Creates a property change listener for boolean properties.
     * @param consumer The consumer to call with the new boolean value when the property changes
     * @return A PropertyChangeListener
     */
    public static PropertyChangeListener onBooleanChanged(final Consumer<Boolean> consumer) {
        return onChanged(consumer);
    }
    
    /**
     * Creates a property change listener for string properties.
     * @param consumer The consumer to call with the new string value when the property changes
     * @return A PropertyChangeListener
     */
    public static PropertyChangeListener onStringChanged(final Consumer<String> consumer) {
        return onChanged(consumer);
    }
    
    /**
     * Creates a property change listener for list properties.
     * @param consumer The consumer to call with the new list value when the property changes
     * @param <T> The type of elements in the list
     * @return A PropertyChangeListener
     */
    @SuppressWarnings("unchecked")
    public static <T> PropertyChangeListener onListChanged(final Consumer<List<T>> consumer) {
        return onChanged(consumer);
    }
    
    /**
     * Creates a property change listener for UserProfile properties.
     * @param consumer The consumer to call with the new UserProfile value when the property changes
     * @return A PropertyChangeListener
     */
    public static PropertyChangeListener onUserProfileChanged(final Consumer<UserProfile> consumer) {
        return onChanged(consumer);
    }
}
