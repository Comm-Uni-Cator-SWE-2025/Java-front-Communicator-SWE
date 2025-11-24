package com.swe.canvas.datamodel.serialization;

import com.swe.canvas.datamodel.canvas.ShapeState;
import com.swe.canvas.datamodel.shape.*;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ShapeSerializerTest {

    @Test
    void testSerializeShape_Null() {
        assertNull(ShapeSerializer.serializeShape(null));
        assertNull(ShapeSerializer.serializeShape(new ShapeState(null, false, 0)));
    }

    @Test
    void testRoundTrip_Freehand() {
        List<Point> points = List.of(new Point(0, 0), new Point(10, 10), new Point(20, 20));
        Shape shape = new FreehandShape(new ShapeId("s1"), points, 2.0, Color.BLUE, "u1", "u1");
        ShapeState state = new ShapeState(shape, false, 100L);

        String json = ShapeSerializer.serializeShape(state);
        ShapeState restored = ShapeSerializer.deserializeShape(json);

        assertNotNull(restored);
        assertEquals(ShapeType.FREEHAND, restored.getShape().getShapeType());
        assertEquals(3, restored.getShape().getPoints().size());
    }

    @Test
    void testRoundTrip_AllShapes() {
        List<Point> box = List.of(new Point(0, 0), new Point(10, 10));

        // Rectangle
        Shape rect = new RectangleShape(new ShapeId("r1"), box, 1, Color.BLACK, "u", "u");
        assertNotNull(ShapeSerializer.deserializeShape(ShapeSerializer.serializeShape(new ShapeState(rect, false, 1))));

        // Ellipse
        Shape ell = new EllipseShape(new ShapeId("e1"), box, 1, Color.BLACK, "u", "u");
        assertNotNull(ShapeSerializer.deserializeShape(ShapeSerializer.serializeShape(new ShapeState(ell, false, 1))));

        // Triangle
        Shape tri = new TriangleShape(new ShapeId("t1"), box, 1, Color.BLACK, "u", "u");
        assertNotNull(ShapeSerializer.deserializeShape(ShapeSerializer.serializeShape(new ShapeState(tri, false, 1))));

        // Line
        Shape line = new LineShape(new ShapeId("l1"), box, 1, Color.BLACK, "u", "u");
        assertNotNull(ShapeSerializer.deserializeShape(ShapeSerializer.serializeShape(new ShapeState(line, false, 1))));
    }

    @Test
    void testMapSerialization() {
        Map<ShapeId, ShapeState> map = new HashMap<>();
        ShapeId id1 = new ShapeId("s1");
        Shape shape1 = new RectangleShape(id1, List.of(new Point(0, 0), new Point(10, 10)), 1, Color.RED, "u", "u");
        map.put(id1, new ShapeState(shape1, false, 100L));

        String json = ShapeSerializer.serializeShapesMap(map);
        Map<ShapeId, ShapeState> restored = ShapeSerializer.deserializeShapesMap(json);

        assertEquals(1, restored.size());
        assertTrue(restored.containsKey(id1));
        assertEquals(Color.RED, restored.get(id1).getShape().getColor());
    }

    @Test
    void testMapSerialization_EmptyAndNull() {
        assertEquals("{}", ShapeSerializer.serializeShapesMap(null));
        assertEquals("{}", ShapeSerializer.serializeShapesMap(new HashMap<>()));

        assertTrue(ShapeSerializer.deserializeShapesMap(null).isEmpty());
        assertTrue(ShapeSerializer.deserializeShapesMap("").isEmpty());
        assertTrue(ShapeSerializer.deserializeShapesMap("{}").isEmpty());
    }

    @Test
    void testDeserialize_UnknownType() {
        String json = "{" +
                "\"ShapeId\":\"s1\"," +
                "\"Type\":\"UNKNOWN_TYPE\"," +
                "\"Points\":[]," +
                "\"Color\":\"#00000000\"," +
                "\"Thickness\":1," +
                "\"CreatedBy\":\"u\"," +
                "\"LastModifiedBy\":\"u\"," +
                "\"IsDeleted\":false" +
                "}";
        assertThrows(SerializationException.class, () -> ShapeSerializer.deserializeShape(json));
    }

    @Test
    void testDeserialize_MissingField() {
        String json = "{\"ShapeId\":\"s1\"}"; // Missing Type, etc.
        assertThrows(SerializationException.class, () -> ShapeSerializer.deserializeShape(json));
    }
}