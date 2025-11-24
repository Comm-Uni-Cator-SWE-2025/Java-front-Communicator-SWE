package com.swe.ux.binding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.swe.controller.Meeting.ParticipantRole;
import com.swe.controller.Meeting.UserProfile;

/**
 * Unit tests for PropertyListeners utility class.
 */
class PropertyListenersTest {

    @Test
    void testOf_BiConsumer() {
        // Arrange
        String[] oldValue = {null};
        String[] newValue = {null};
        PropertyChangeListener listener = PropertyListeners.of((String old, String newVal) -> {
            oldValue[0] = old;
            newValue[0] = newVal;
        });

        PropertyChangeEvent event = new PropertyChangeEvent(
            this, "test", "old", "new"
        );

        // Act
        listener.propertyChange(event);

        // Assert
        assertEquals("old", oldValue[0]);
        assertEquals("new", newValue[0]);
    }

    @Test
    void testOnChanged_Consumer() {
        // Arrange
        String[] capturedValue = {null};
        PropertyChangeListener listener = PropertyListeners.onChanged((String value) -> {
            capturedValue[0] = value;
        });

        PropertyChangeEvent event = new PropertyChangeEvent(
            this, "test", "old", "new"
        );

        // Act
        listener.propertyChange(event);

        // Assert
        assertEquals("new", capturedValue[0]);
    }

    @Test
    void testOnBooleanChanged() {
        // Arrange
        Boolean[] capturedValue = {null};
        PropertyChangeListener listener = PropertyListeners.onBooleanChanged(value -> {
            capturedValue[0] = value;
        });

        PropertyChangeEvent event = new PropertyChangeEvent(
            this, "test", false, true
        );

        // Act
        listener.propertyChange(event);

        // Assert
        assertTrue(capturedValue[0]);
    }

    @Test
    void testOnStringChanged() {
        // Arrange
        String[] capturedValue = {null};
        PropertyChangeListener listener = PropertyListeners.onStringChanged(value -> {
            capturedValue[0] = value;
        });

        PropertyChangeEvent event = new PropertyChangeEvent(
            this, "test", "old", "new"
        );

        // Act
        listener.propertyChange(event);

        // Assert
        assertEquals("new", capturedValue[0]);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void testOnListChanged() {
        // Arrange
        List<String>[] capturedValue = (List<String>[]) new List[1];
        PropertyChangeListener listener = PropertyListeners.onListChanged((List<String> value) -> {
            capturedValue[0] = value;
        });

        List<String> oldList = new ArrayList<>();
        List<String> newList = new ArrayList<>();
        newList.add("item1");
        newList.add("item2");

        PropertyChangeEvent event = new PropertyChangeEvent(
            this, "test", oldList, newList
        );

        // Act
        listener.propertyChange(event);

        // Assert
        assertNotNull(capturedValue[0]);
        assertEquals(2, capturedValue[0].size());
        assertEquals("item1", capturedValue[0].get(0));
    }

    @Test
    void testOnUserProfileChanged() {
        // Arrange
        UserProfile[] capturedValue = {null};
        PropertyChangeListener listener = PropertyListeners.onUserProfileChanged(value -> {
            capturedValue[0] = value;
        });

        UserProfile newUser = new UserProfile(
            "test@example.com",
            "Test User",
            ParticipantRole.STUDENT
        );

        PropertyChangeEvent event = new PropertyChangeEvent(
            this, "test", null, newUser
        );

        // Act
        listener.propertyChange(event);

        // Assert
        assertNotNull(capturedValue[0]);
        assertEquals("test@example.com", capturedValue[0].getEmail());
    }

    @Test
    void testOnChanged_Runnable() {
        // Arrange
        boolean[] runnableCalled = {false};
        PropertyChangeListener listener = PropertyListeners.onChanged(() -> {
            runnableCalled[0] = true;
        });

        PropertyChangeEvent event = new PropertyChangeEvent(
            this, "test", "old", "new"
        );

        // Act
        listener.propertyChange(event);

        // Assert
        assertTrue(runnableCalled[0]);
    }
}

