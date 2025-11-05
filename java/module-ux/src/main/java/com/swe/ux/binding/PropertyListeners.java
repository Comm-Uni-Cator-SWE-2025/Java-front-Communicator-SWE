package com.swe.ux.binding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Utility class for creating property change listeners with lambda expressions.
 */
public class PropertyListeners {
    
    /**
     * Creates a property change listener that calls the given consumer with old and new values.
     * @param consumer The consumer to call when the property changes
     * @param <T> The type of the property value
     * @return A PropertyChangeListener
     */
    @SuppressWarnings("unchecked")
    public static <T> PropertyChangeListener of(BiConsumer<T, T> consumer) {
        return (PropertyChangeEvent evt) -> {
            T oldValue = (T) evt.getOldValue();
            T newValue = (T) evt.getNewValue();
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
    public static <T> PropertyChangeListener onChanged(Consumer<T> consumer) {
        return (PropertyChangeEvent evt) -> {
            T newValue = (T) evt.getNewValue();
            consumer.accept(newValue);
        };
    }
    
    /**
     * Creates a property change listener for boolean properties.
     * @param consumer The consumer to call with the new boolean value when the property changes
     * @return A PropertyChangeListener
     */
    public static PropertyChangeListener onBooleanChanged(Consumer<Boolean> consumer) {
        return onChanged(consumer);
    }
    
    /**
     * Creates a property change listener for string properties.
     * @param consumer The consumer to call with the new string value when the property changes
     * @return A PropertyChangeListener
     */
    public static PropertyChangeListener onStringChanged(Consumer<String> consumer) {
        return onChanged(consumer);
    }
    
    /**
     * Creates a property change listener for list properties.
     * @param consumer The consumer to call with the new list value when the property changes
     * @param <T> The type of elements in the list
     * @return A PropertyChangeListener
     */
    @SuppressWarnings("unchecked")
    public static <T> PropertyChangeListener onListChanged(Consumer<List<T>> consumer) {
        return onChanged(consumer);
    }
    
    /**
     * Creates a property change listener that calls the given runnable when the property changes.
     * @param runnable The runnable to call when the property changes
     * @return A PropertyChangeListener
     */
    public static PropertyChangeListener onChanged(Runnable runnable) {
        return (PropertyChangeEvent evt) -> runnable.run();
    }
}
