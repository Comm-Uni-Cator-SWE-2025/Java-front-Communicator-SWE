package com.swe.canvas.datamodel.serialization;

import java.awt.Color;
import java.util.List;

import com.swe.canvas.datamodel.action.Action;
import com.swe.canvas.datamodel.action.ActionType;
import com.swe.canvas.datamodel.action.CreateShapeAction;
import com.swe.canvas.datamodel.action.DeleteShapeAction;
import com.swe.canvas.datamodel.action.ModifyShapeAction;
import com.swe.canvas.datamodel.action.ResurrectShapeAction;
import com.swe.canvas.datamodel.canvas.ShapeState;
import com.swe.canvas.datamodel.shape.EllipseShape;
import com.swe.canvas.datamodel.shape.FreehandShape;
import com.swe.canvas.datamodel.shape.LineShape;
import com.swe.canvas.datamodel.shape.Point;
import com.swe.canvas.datamodel.shape.RectangleShape;
import com.swe.canvas.datamodel.shape.Shape;
import com.swe.canvas.datamodel.shape.ShapeId;
import com.swe.canvas.datamodel.shape.ShapeType;
import com.swe.canvas.datamodel.shape.TriangleShape;

/**
 * Provides manual JSON serialization and deserialization for the Canvas data model
 * (Shapes and Actions) using only core Java classes.
 *
 * This class handles the high-level structure and relies on
 * {@link JsonUtils} for low-level parsing and helper functions.
 */
public final class ShapeSerializer {

    private ShapeSerializer() {
        // Utility class
    }

    // =========================================================================
    // Shape Serialization/Deserialization
    // =========================================================================

    /**
     * Manually serializes a ShapeState object into a JSON string, matching the C# IShape format.
     * @param shapeState The shape state to serialize.
     * @return A JSON string.
     */
    public static String serializeShape(final ShapeState shapeState) {
        if (shapeState == null || shapeState.getShape() == null) {
            return null;
        }

        final Shape shape = shapeState.getShape();
        final StringBuilder sb = new StringBuilder();

        sb.append("{");
        
        // 1. ShapeId
        sb.append(JsonUtils.jsonEscape("ShapeId")).append(":").append(JsonUtils.jsonEscape(shape.getShapeId().getValue())).append(",");
        
        // 2. Type
        sb.append(JsonUtils.jsonEscape("Type")).append(":").append(JsonUtils.jsonEscape(shape.getShapeType().toString())).append(",");

        // 3. Points Array
        sb.append(JsonUtils.jsonEscape("Points")).append(":[");
        final List<Point> points = shape.getPoints();
        for (int i = 0; i < points.size(); i++) {
            final Point p = points.get(i);
            sb.append("{");
            sb.append(JsonUtils.jsonEscape("X")).append(":").append(p.getX()).append(",");
            sb.append(JsonUtils.jsonEscape("Y")).append(":").append(p.getY());
            sb.append("}");
            if (i < points.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("],");

        // 4. Color
        sb.append(JsonUtils.jsonEscape("Color")).append(":").append(JsonUtils.jsonEscape(JsonUtils.colorToHex(shape.getColor()))).append(",");
        
        // 5. Thickness
        sb.append(JsonUtils.jsonEscape("Thickness")).append(":").append(shape.getThickness()).append(",");

        // 6. CreatedBy
        sb.append(JsonUtils.jsonEscape("CreatedBy")).append(":").append(JsonUtils.jsonEscape(shape.getCreatedBy())).append(",");
        
        // 7. LastModifiedBy
        sb.append(JsonUtils.jsonEscape("LastModifiedBy")).append(":").append(JsonUtils.jsonEscape(shape.getLastUpdatedBy())).append(",");
        
        // 8. IsDeleted
        sb.append(JsonUtils.jsonEscape("IsDeleted")).append(":").append(shapeState.isDeleted());
        
        sb.append("}");
        return sb.toString();
    }

    /**
     * Manually deserializes a JSON string back into a ShapeState object.
     * @param json The JSON string.
     * @return The ShapeState object.
     */
    public static ShapeState deserializeShape(final String json) {
        if (json == null || json.isEmpty() || "null".equals(json)) {
            return null;
        }

        try {
            final String content = json.substring(1, json.length() - 1); // Remove outer { }

            final String shapeId = JsonUtils.extractString(content, "ShapeId");
            final String typeName = JsonUtils.extractString(content, "Type");
            final String colorHex = JsonUtils.extractString(content, "Color");
            final double thickness = JsonUtils.extractDouble(content, "Thickness");
            final String createdBy = JsonUtils.extractString(content, "CreatedBy");
            final String lastModifiedBy = JsonUtils.extractString(content, "LastModifiedBy");
            final boolean isDeleted = JsonUtils.extractBoolean(content, "IsDeleted");

            final List<Point> points = JsonUtils.extractPoints(content);

            if (shapeId == null || typeName == null || createdBy == null || lastModifiedBy == null || points == null) {
                throw new SerializationException("Missing crucial shape field during deserialization.");
            }

            final ShapeType shapeType = ShapeType.valueOf(typeName);
            final Color color = JsonUtils.hexToColor(colorHex);
            final ShapeId id = new ShapeId(shapeId);

            final Shape newShape;
            final long zeroL = 0L;
            switch (shapeType) {
                case FREEHAND:
                    newShape = new FreehandShape(id, points, thickness, color, createdBy, lastModifiedBy);
                    break;
                case RECTANGLE:
                    newShape = new RectangleShape(id, points, thickness, color, createdBy, lastModifiedBy);
                    break;
                case TRIANGLE:
                    newShape = new TriangleShape(id, points, thickness, color, createdBy, lastModifiedBy);
                    break;
                case LINE:
                    newShape = new LineShape(id, points, thickness, color, createdBy, lastModifiedBy);
                    break;
                case ELLIPSE:
                    newShape = new EllipseShape(id, points, thickness, color, createdBy, lastModifiedBy);
                    break;
                default:
                    throw new SerializationException("Unknown ShapeType during manual parsing: " + typeName);
            }

            // Use 0L for lastModified since the value is not included in the payload.
            return new ShapeState(newShape, isDeleted, zeroL);

        } catch (final Exception e) {
            throw new SerializationException("Failed to manually deserialize ShapeState: " + e.getMessage(), e);
        }
    }

    // =========================================================================
    // Action Serialization/Deserialization
    // =========================================================================

    /**
     * Manually serializes an Action object, including nested ShapeState objects.
     * @param action The action to serialize.
     * @return A JSON string.
     */
    public static String serializeAction(final Action action) {
        if (action == null) {
            return "null";
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("{");

        // Simple metadata fields
        sb.append(JsonUtils.jsonEscape("ActionType")).append(":").append(JsonUtils.jsonEscape(action.getActionType().toString())).append(",");
        sb.append(JsonUtils.jsonEscape("ActionId")).append(":").append(JsonUtils.jsonEscape(action.getActionId())).append(",");
        sb.append(JsonUtils.jsonEscape("ShapeId")).append(":").append(JsonUtils.jsonEscape(action.getShapeId().getValue())).append(",");
        sb.append(JsonUtils.jsonEscape("UserId")).append(":").append(JsonUtils.jsonEscape(action.getUserId())).append(",");
        sb.append(JsonUtils.jsonEscape("Timestamp")).append(":").append(action.getTimestamp()).append(",");

        // PrevState (nested ShapeState JSON)
        final String prevStateJson = serializeShape(action.getPrevState());
        sb.append(JsonUtils.jsonEscape("PrevState")).append(":");
        if (prevStateJson != null) {
            sb.append(prevStateJson); // Append raw JSON object
        } else {
            sb.append("null");
        }
        sb.append(",");

        // NewState (nested ShapeState JSON)
        final String newStateJson = serializeShape(action.getNewState());
        sb.append(JsonUtils.jsonEscape("NewState")).append(":");
        if (newStateJson != null) {
            sb.append(newStateJson); // Append raw JSON object
        } else {
            sb.append("null");
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * Manually deserializes a JSON string back into a concrete Action object.
     * @param json The JSON string.
     * @return The Action object.
     */
    public static Action deserializeAction(final String json) {
        if (json == null || json.isEmpty() || "null".equals(json)) {
            return null;
        }

        // Same highly fragile parsing approach based on the exact structure output by serializeAction.
        try {
            final String content = json.substring(1, json.length() - 1); // Remove outer { }

            final String actionId = JsonUtils.extractString(content, "ActionId");
            final String shapeId = JsonUtils.extractString(content, "ShapeId");
            final String userId = JsonUtils.extractString(content, "UserId");
            final ActionType actionType = ActionType.valueOf(JsonUtils.extractString(content, "ActionType"));
            final long timestamp = JsonUtils.extractLong(content, "Timestamp");

            // Extract nested JSON strings for PrevState and NewState
            final String prevStateJson = JsonUtils.extractNestedJson(content, "PrevState");
            final String newStateJson = JsonUtils.extractNestedJson(content, "NewState");

            // Deserialize nested ShapeState objects
            final ShapeState prevState = deserializeShape(prevStateJson);
            final ShapeState newState = deserializeShape(newStateJson);

            if (actionId == null || shapeId == null || userId == null || actionType == null || newState == null) {
                throw new SerializationException("Missing crucial action field during deserialization.");
            }

            final ShapeId targetId = new ShapeId(shapeId);

            // Factory logic to create the correct concrete Action subclass
            switch (actionType) {
                case CREATE:
                    return new CreateShapeAction(actionId, userId, timestamp, targetId, newState);
                case MODIFY:
                    return new ModifyShapeAction(actionId, userId, timestamp, targetId, prevState, newState);
                case DELETE:
                    return new DeleteShapeAction(actionId, userId, timestamp, targetId, prevState, newState);
                case RESURRECT:
                    return new ResurrectShapeAction(actionId, userId, timestamp, targetId, prevState, newState);
                default:
                    throw new SerializationException("Unknown action type: " + actionType);
            }

        } catch (final Exception e) {
            throw new SerializationException("Failed to manually deserialize Action: " + e.getMessage(), e);
        }
    }

    /**
     * Test helper to serialize a shape without a ShapeState.
     * @param shape The shape to serialize.
     * @return The serialized JSON string.
     */
    public static String testSerializeShapeOnly(final Shape shape) {
        if (shape == null) {
            return "{}";
        }
        // Since we need ShapeState for the isDeleted flag, we mock a ShapeState for serialization.
        // We know that for a newly drawn shape, isDeleted is false and timestamp is irrelevant for the payload structure.
        final long zeroL = 0L;
        final ShapeState tempState = new ShapeState(shape, false, zeroL);
        return serializeShape(tempState);
    }
}