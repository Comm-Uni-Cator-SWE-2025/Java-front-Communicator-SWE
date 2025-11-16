package com.swe.canvas.datamodel.serialization;


import java.awt.Color;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.swe.canvas.datamodel.canvas.ShapeState;
import com.swe.canvas.datamodel.shape.Point;
import com.swe.canvas.datamodel.shape.Shape;
import com.swe.canvas.datamodel.shape.ShapeType;

/**
 * JUnit 5 tests for ManualJsonConverter deserialization logic.
 *
 * NOTE: Assumes the ManualJsonConverter class and its dependencies (ShapeState, Shape, Point, etc.)
 * are available in the main source path.
 */
public class ManualSerializerTest {

    // IMPORTANT: The Type must be uppercase to match Java's ShapeType enum constants.
    private static final String VALID_RECTANGLE_JSON =
        "{"
            + "\"ShapeId\":\"41dcea40-11bf-4d4e-a89f-4eda8508caaf\","
            + "\"Type\":\"RECTANGLE\","
            + "\"Points\":[{\"X\":256.0,\"Y\":179.0},{\"X\":498.0,\"Y\":354.0}],"
            + "\"Color\":\"#FF000000\","
            + "\"Thickness\":2.0,"
            + "\"CreatedBy\":\"user_default\","
            + "\"LastModifiedBy\":\"user_default\","
            + "\"IsDeleted\":false"
        + "}";

    private static final String INVALID_MISSING_TYPE_JSON =
        "{"
            + "\"ShapeId\":\"11111111-1111-1111-1111-111111111111\","
            + "\"Points\":[{\"X\":1.0,\"Y\":1.0}],"
            + "\"Color\":\"#FF000000\","
            + "\"Thickness\":1.0,"
            + "\"CreatedBy\":\"user_default\","
            + "\"LastModifiedBy\":\"user_default\","
            + "\"IsDeleted\":false"
        + "}";


    @Test
    void testDeserializeShape_ValidRectangle_Success() {
        System.out.println("\n--- Running Deserialization Test: Valid Rectangle ---");
        
        // Act
        final ShapeState state = ManualSerializer.deserializeShape(VALID_RECTANGLE_JSON);

        // Assert
        assertNotNull(state, "ShapeState should not be null after deserialization.");
        assertNotNull(state.getShape(), "Shape should not be null.");

        final Shape shape = state.getShape();
        
        // Check Type and Metadata
        assertEquals(ShapeType.RECTANGLE, shape.getShapeType(), "Shape type must be RECTANGLE.");
        assertEquals("41dcea40-11bf-4d4e-a89f-4eda8508caaf", shape.getShapeId().getValue(), "Shape ID mismatch.");
        assertEquals("user_default", shape.getCreatedBy(), "CreatedBy mismatch.");
        assertFalse(state.isDeleted(), "IsDeleted should be false.");

        // Check Properties
        assertEquals(2.0, shape.getThickness(), 0.001, "Thickness mismatch.");
        assertEquals(Color.BLACK, shape.getColor(), "Color mismatch.");
        
        // Check Points
        final List<Point> points = shape.getPoints();
        assertEquals(2, points.size(), "Should have exactly 2 points.");
        
        assertEquals(256.0, points.get(0).getX(), 0.001, "Point 1 X mismatch.");
        assertEquals(179.0, points.get(0).getY(), 0.001, "Point 1 Y mismatch.");
        
        assertEquals(498.0, points.get(1).getX(), 0.001, "Point 2 X mismatch.");
        assertEquals(354.0, points.get(1).getY(), 0.001, "Point 2 Y mismatch.");

        System.out.println("TEST SUCCESSFUL: Valid Rectangle Deserialized.");
    }

    @Test
    void testDeserializeShape_NullOrEmptyInput_ReturnsNull() {
        System.out.println("\n--- Running Deserialization Test: Null/Empty Input ---");
        
        // Act & Assert
        assertEquals(null, ManualSerializer.deserializeShape(null), "Null input should return null.");
        assertEquals(null, ManualSerializer.deserializeShape(""), "Empty string input should return null.");
        assertEquals(null, ManualSerializer.deserializeShape("null"), "'null' string input should return null.");

        System.out.println("TEST SUCCESSFUL: Null/Empty input handled.");
    }

    @Test
    void testDeserializeShape_MissingRequiredField_ThrowsException() {
        System.out.println("\n--- Running Deserialization Test: Missing Required Field ---");
        
        // Act & Assert
        // Attempting to deserialize JSON missing the 'Type' field should fail.
        assertThrows(SerializationException.class, 
                     () -> ManualSerializer.deserializeShape(INVALID_MISSING_TYPE_JSON), 
                     "Deserialization should throw SerializationException for missing 'Type' field.");
        
        System.out.println("TEST SUCCESSFUL: Missing 'Type' field correctly triggered an exception.");
    }
}
