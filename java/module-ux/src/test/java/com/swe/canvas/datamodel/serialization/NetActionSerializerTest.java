/*
 * -----------------------------------------------------------------------------
 * File: NetActionSerializerTest.java
 * Owner: Gajjala Bhavani Shankar
 * Roll Number : 112201026
 * Module : Canvas
 *
 * -----------------------------------------------------------------------------
 */

package com.swe.canvas.datamodel.serialization;

import com.swe.canvas.datamodel.action.*;
import com.swe.canvas.datamodel.canvas.ShapeState;
import com.swe.canvas.datamodel.shape.Point;
import com.swe.canvas.datamodel.shape.RectangleShape;
import com.swe.canvas.datamodel.shape.Shape;
import com.swe.canvas.datamodel.shape.ShapeId;
import com.swe.canvas.datamodel.shape.ShapeType;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NetActionSerializerTest {

    @Test
    void testSerializeAction_Null() {
        assertEquals("null", NetActionSerializer.serializeAction(null));
    }

    @Test
    void testDeserializeAction_NullOrEmpty() {
        assertNull(NetActionSerializer.deserializeAction(null));
        assertNull(NetActionSerializer.deserializeAction(""));
        assertNull(NetActionSerializer.deserializeAction("null"));
    }

    @Test
    void testRoundTrip_CreateAction() {
        Shape shape = new RectangleShape(new ShapeId("s1"),
                List.of(new Point(0, 0), new Point(10, 10)), 1.0, Color.RED, "u1", "u1");
        ShapeState newState = new ShapeState(shape, false, 100L);

        Action action = new CreateShapeAction("act-1", "u1", 100L, new ShapeId("s1"), newState);

        String json = NetActionSerializer.serializeAction(action);
        Action deserialized = NetActionSerializer.deserializeAction(json);

        assertNotNull(deserialized);
        assertTrue(deserialized instanceof CreateShapeAction);
        assertEquals("act-1", deserialized.getActionId());
    }

    @Test
    void testRoundTrip_ModifyAction() {
        Shape shape = new RectangleShape(new ShapeId("s1"),
                List.of(new Point(0, 0), new Point(10, 10)), 1.0, Color.RED, "u1", "u1");
        ShapeState prev = new ShapeState(shape, false, 100L);
        ShapeState next = new ShapeState(shape, false, 200L);

        Action action = new ModifyShapeAction("act-2", "u1", 200L, new ShapeId("s1"), prev, next);

        String json = NetActionSerializer.serializeAction(action);
        Action deserialized = NetActionSerializer.deserializeAction(json);

        assertNotNull(deserialized);
        assertTrue(deserialized instanceof ModifyShapeAction);
    }

    @Test
    void testRoundTrip_DeleteAction() {
        Shape shape = new RectangleShape(new ShapeId("s1"),
                List.of(new Point(0, 0), new Point(10, 10)), 1.0, Color.RED, "u1", "u1");
        ShapeState prev = new ShapeState(shape, false, 100L);
        ShapeState next = new ShapeState(shape, true, 200L);

        Action action = new DeleteShapeAction("act-3", "u1", 200L, new ShapeId("s1"), prev, next);

        String json = NetActionSerializer.serializeAction(action);
        Action deserialized = NetActionSerializer.deserializeAction(json);

        assertNotNull(deserialized);
        assertTrue(deserialized instanceof DeleteShapeAction);
    }

    @Test
    void testRoundTrip_ResurrectAction() {
        Shape shape = new RectangleShape(new ShapeId("s1"),
                List.of(new Point(0, 0), new Point(10, 10)), 1.0, Color.RED, "u1", "u1");
        ShapeState prev = new ShapeState(shape, true, 100L);
        ShapeState next = new ShapeState(shape, false, 200L);

        Action action = new ResurrectShapeAction("act-4", "u1", 200L, new ShapeId("s1"), prev, next);

        String json = NetActionSerializer.serializeAction(action);
        Action deserialized = NetActionSerializer.deserializeAction(json);

        assertNotNull(deserialized);
        assertTrue(deserialized instanceof ResurrectShapeAction);
    }

    @Test
    void testDeserialize_MissingFields() {
        String json = "{}"; // Missing ActionId, Type, etc.
        assertThrows(SerializationException.class, () -> NetActionSerializer.deserializeAction(json));
    }

    @Test
    void testDeserialize_UnknownType() {
        String json = "{" +
                "\"ActionId\":\"1\"," +
                "\"ActionType\":\"UNKNOWN\"," + // Unknown type
                "\"Next\": {}" +
                "}";

        // Will fail either at Type.valueOf or switch case depending on implementation
        // details
        // NetActionSerializer uses ActionType.valueOf which throws
        // IllegalArgumentException,
        // caught and wrapped in SerializationException
        assertThrows(SerializationException.class, () -> NetActionSerializer.deserializeAction(json));
    }

    @Test
    void testSerialize_NullStates() {
        // Force null states (normally protected by Action constructor, but testing
        // serializer robustness)
        // We can't easily create an Action with null states due to constructor checks.
        // But we can manually craft JSON with null states.
        String json = "{" +
                "\"ActionId\":\"1\"," +
                "\"ActionType\":\"CREATE\"," +
                "\"Prev\": null," +
                "\"Next\": null" +
                "}";

        assertThrows(SerializationException.class, () -> NetActionSerializer.deserializeAction(json));
    }
}