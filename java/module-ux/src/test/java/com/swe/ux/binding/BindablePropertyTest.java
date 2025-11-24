package com.swe.ux.binding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for BindableProperty.
 */
class BindablePropertyTest {

    private BindableProperty<String> property;
    private boolean listenerCalled;
    private PropertyChangeEvent capturedEvent;

    @BeforeEach
    void setUp() {
        property = new BindableProperty<>("initial", "testProperty");
        listenerCalled = false;
        capturedEvent = null;
    }

    @Test
    void testInitialValue() {
        assertEquals("initial", property.get());
    }

    @Test
    void testSetValue() {
        // Act
        property.set("new value");

        // Assert
        assertEquals("new value", property.get());
    }

    @Test
    void testSetValue_NotifiesListener() {
        // Arrange
        PropertyChangeListener listener = evt -> {
            listenerCalled = true;
            capturedEvent = evt;
        };
        property.addListener(listener);

        // Act
        property.set("new value");

        // Assert
        assertTrue(listenerCalled);
        assertNotNull(capturedEvent);
        assertEquals("testProperty", capturedEvent.getPropertyName());
        assertEquals("initial", capturedEvent.getOldValue());
        assertEquals("new value", capturedEvent.getNewValue());
    }

    @Test
    void testSetValue_SameValue_DoesNotNotify() {
        // Arrange
        PropertyChangeListener listener = evt -> listenerCalled = true;
        property.addListener(listener);

        // Act
        property.set("initial"); // Same value

        // Assert
        assertFalse(listenerCalled);
    }

    @Test
    void testSetValue_NullValue() {
        // Act
        property.set(null);

        // Assert
        assertNull(property.get());
    }

    @Test
    void testRemoveListener() {
        // Arrange
        PropertyChangeListener listener = evt -> listenerCalled = true;
        property.addListener(listener);
        property.removeListener(listener);

        // Act
        property.set("new value");

        // Assert
        assertFalse(listenerCalled);
    }

    @Test
    void testMultipleListeners() {
        // Arrange
        boolean[] listener1Called = {false};
        boolean[] listener2Called = {false};

        PropertyChangeListener listener1 = evt -> listener1Called[0] = true;
        PropertyChangeListener listener2 = evt -> listener2Called[0] = true;

        property.addListener(listener1);
        property.addListener(listener2);

        // Act
        property.set("new value");

        // Assert
        assertTrue(listener1Called[0]);
        assertTrue(listener2Called[0]);
    }

    @Test
    void testBind_Unidirectional() {
        // Arrange
        BindableProperty<String> source = new BindableProperty<>("source", "source");
        BindableProperty<String> target = new BindableProperty<>("target", "target");

        // Act
        target.bind(source);
        source.set("new source value");

        // Assert
        assertEquals("new source value", target.get());
    }

    @Test
    void testBindBidirectional() {
        // Arrange
        BindableProperty<String> prop1 = new BindableProperty<>("value1", "prop1");
        BindableProperty<String> prop2 = new BindableProperty<>("value2", "prop2");

        // Act
        prop1.bindBidirectional(prop2);

        // Test prop1 -> prop2
        prop1.set("updated1");
        assertEquals("updated1", prop2.get());

        // Test prop2 -> prop1
        prop2.set("updated2");
        assertEquals("updated2", prop1.get());
    }

    @Test
    void testBindBidirectional_CircularPrevention() {
        // Arrange
        BindableProperty<String> prop1 = new BindableProperty<>("value1", "prop1");
        BindableProperty<String> prop2 = new BindableProperty<>("value1", "prop2");

        // Act
        prop1.bindBidirectional(prop2);
        prop1.set("new value");

        // Assert - should not cause infinite loop
        assertEquals("new value", prop1.get());
        assertEquals("new value", prop2.get());
    }

    @Test
    void testPropertyName() {
        // Arrange
        BindableProperty<Integer> namedProperty = new BindableProperty<>(0, "myProperty");

        // Act
        PropertyChangeListener listener = evt -> {
            assertEquals("myProperty", evt.getPropertyName());
        };
        namedProperty.addListener(listener);
        namedProperty.set(42);
    }

    @Test
    void testNullPropertyName() {
        // Arrange
        BindableProperty<Integer> property = new BindableProperty<>(0, null);

        // Act & Assert - should not throw
        assertDoesNotThrow(() -> property.set(42));
    }
}


