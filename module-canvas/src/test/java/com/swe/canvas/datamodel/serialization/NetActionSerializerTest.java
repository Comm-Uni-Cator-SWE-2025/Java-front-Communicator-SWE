package com.swe.canvas.datamodel.serialization;

import java.awt.Color;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.swe.canvas.datamodel.action.Action;
import com.swe.canvas.datamodel.action.CreateShapeAction;
import com.swe.canvas.datamodel.action.ModifyShapeAction;
import com.swe.canvas.datamodel.canvas.ShapeState;
import com.swe.canvas.datamodel.shape.Point;
import com.swe.canvas.datamodel.shape.RectangleShape;
import com.swe.canvas.datamodel.shape.Shape;
import com.swe.canvas.datamodel.shape.ShapeId;

/**
 * JUnit 5 tests for the manual serialization and deserialization of Action objects
 * using the ShapeSerializer (and its helper ManualJsonParserUtils).
 */
public class NetActionSerializerTest {

    private ShapeId shapeId;
    private String userId;
    private long timestamp;
    private ShapeState testState1; // A simple red square
    private ShapeState testState2; // A modified blue square

    /**
     * Sets up common test data before each test.
     */
    @BeforeEach
    void setUp() {
        shapeId = new ShapeId("shape-123");
        userId = "test-user";
        timestamp = 1700000000000L; // Example timestamp

        // State 1: A red square
        Shape shape1 = new RectangleShape(
            shapeId,
            List.of(new Point(10, 10), new Point(20, 20)),
            2.0,
            Color.RED,
            userId,
            userId
        );
        // Note: The lastModified timestamp (123L) will be lost in serialization,
        // as it's not part of the C# JSON format. We will test for the deserialized default (0L).
        testState1 = new ShapeState(shape1, false, 123L);

        // State 2: A modified blue square, moved
        Shape shape2 = new RectangleShape(
            shapeId,
            List.of(new Point(50, 50), new Point(60, 60)),
            4.0,
            Color.BLUE,
            userId,
            userId
        );
        testState2 = new ShapeState(shape2, false, 456L);
    }

    /**
     * Tests the full round-trip (serialize -> deserialize) for a ModifyShapeAction,
     * which uses both prevState and newState.
     */
    @Test
    void testActionSerialization_ModifyAction_RoundTrip() {
        System.out.println("\n--- Running Test: ModifyAction Round-Trip ---");
        
        // 1. Arrange: Create a complex ModifyAction
        Action originalAction = new ModifyShapeAction(
            "action-abc",
            userId,
            timestamp,
            shapeId,
            testState1, // prevState
            testState2  // newState
        );

        // 2. Act: Serialize and then Deserialize
        String json = NetActionSerializer.serializeAction(originalAction);
        System.out.println("Serialized JSON:\n" + json);
        Action restoredAction = NetActionSerializer.deserializeAction(json);

        // 3. Assert: Check if the restored action is identical
        assertNotNull(json, "JSON string should not be null.");
        assertNotNull(restoredAction, "Restored action should not be null.");
        assertTrue(restoredAction instanceof ModifyShapeAction, "Restored action should be of type ModifyShapeAction.");

        // Check base Action fields
        assertEquals(originalAction.getActionId(), restoredAction.getActionId(), "ActionId mismatch.");
        assertEquals(originalAction.getActionType(), restoredAction.getActionType(), "ActionType mismatch.");
        assertEquals(originalAction.getUserId(), restoredAction.getUserId(), "UserId mismatch.");
        assertEquals(originalAction.getShapeId(), restoredAction.getShapeId(), "ShapeId mismatch.");

        // Check nested ShapeStates
        assertShapeStatesEqual(originalAction.getPrevState(), restoredAction.getPrevState(), "prevState mismatch.");
        assertShapeStatesEqual(originalAction.getNewState(), restoredAction.getNewState(), "newState mismatch.");
        
        System.out.println("TEST SUCCESSFUL: ModifyAction round-trip complete.");
    }

    /**
     * Tests the full round-trip for a CreateShapeAction, which has a null prevState.
     */
    @Test
    void testActionSerialization_CreateAction_RoundTrip() {
        System.out.println("\n--- Running Test: CreateAction Round-Trip ---");

        // 1. Arrange: Create a CreateAction (prevState is null)
        Action originalAction = new CreateShapeAction(
            "action-xyz",
            userId,
            timestamp,
            shapeId,
            testState1 // newState
        );

        // 2. Act: Serialize and then Deserialize
        String json = NetActionSerializer.serializeAction(originalAction);
        System.out.println("Serialized JSON:\n" + json);
        Action restoredAction = NetActionSerializer.deserializeAction(json);

        // 3. Assert: Check if the restored action is identical
        assertNotNull(json, "JSON string should not be null.");
        assertNotNull(restoredAction, "Restored action should not be null.");
        assertTrue(restoredAction instanceof CreateShapeAction, "Restored action should be of type CreateShapeAction.");

        // Check base Action fields
        assertEquals(originalAction.getActionId(), restoredAction.getActionId(), "ActionId mismatch.");
        
        // Key check: PrevState must be null
        assertNull(originalAction.getPrevState(), "Original prevState should be null.");
        assertNull(restoredAction.getPrevState(), "Restored prevState should be null.");

        // Check nested newState
        assertShapeStatesEqual(originalAction.getNewState(), restoredAction.getNewState(), "newState mismatch.");

        System.out.println("TEST SUCCESSFUL: CreateAction round-trip complete.");
    }

    // =========================================================================
    // Private Helper Assertions
    // =========================================================================

    /**
     * Helper method to assert equality between two ShapeState objects.
     * It specifically ignores the lastModified timestamp, as we know it's
     * lost during our custom serialization (to match the C# format).
     */
    private void assertShapeStatesEqual(ShapeState expected, ShapeState actual, String message) {
        if (expected == null) {
            assertNull(actual, "Expected null ShapeState, but was not null.");
            return;
        }
        assertNotNull(actual, "Expected a ShapeState, but was null.");

        // Check metadata
        assertEquals(expected.isDeleted(), actual.isDeleted(), "IsDeleted mismatch.");
        
        // We test that the deserialized timestamp is 0L, as it's omitted from the JSON
        assertEquals(0L, actual.getLastModified(), "Deserialized lastModified timestamp should be 0L.");

        // Check the nested Shape object
        assertShapesEqual(expected.getShape(), actual.getShape());
    }

    /**
     * Helper method to assert equality between two Shape objects.
     */
    private void assertShapesEqual(Shape expected, Shape actual) {
        if (expected == null) {
            assertNull(actual, "Expected null Shape, but was not null.");
            return;
        }
        assertNotNull(actual, "Expected a Shape, but was null.");

        assertEquals(expected.getShapeId(), actual.getShapeId(), "ShapeId mismatch.");
        assertEquals(expected.getShapeType(), actual.getShapeType(), "ShapeType mismatch.");
        assertEquals(expected.getCreatedBy(), actual.getCreatedBy(), "CreatedBy mismatch.");
        assertEquals(expected.getLastUpdatedBy(), actual.getLastUpdatedBy(), "LastModifiedBy mismatch.");
        assertEquals(expected.getColor(), actual.getColor(), "Color mismatch.");
        assertEquals(expected.getThickness(), actual.getThickness(), 0.001, "Thickness mismatch.");
        
        // List.equals() works for Point objects if Point.equals() is implemented (which it is).
        assertEquals(expected.getPoints(), actual.getPoints(), "Points list mismatch.");
    }
}
