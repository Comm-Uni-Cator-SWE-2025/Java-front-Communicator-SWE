package com.swe.canvas.datamodel.serialization;


import com.swe.canvas.datamodel.action.Action;
import com.swe.canvas.datamodel.action.ActionType;
import com.swe.canvas.datamodel.action.CreateShapeAction;
import com.swe.canvas.datamodel.action.DeleteShapeAction;
import com.swe.canvas.datamodel.action.ModifyShapeAction;
import com.swe.canvas.datamodel.action.ResurrectShapeAction;
import com.swe.canvas.datamodel.canvas.ShapeState;
import com.swe.canvas.datamodel.shape.ShapeId;

/**
 * Serializer for Action objects in network messages.
 *
 * <p>Provides manual JSON serialization and deserialization for Action objects
 * to support network transmission.</p>
 *
 * @author Canvas Team
 */
public final class NetActionSerializer {

    /**
     * Private constructor to prevent instantiation.
     */
    private NetActionSerializer() {
    }
    // =========================================================================
    // Action Serialization/Deserialization
    // =========================================================================

    /**
     * Manually serializes an Action object, including nested ShapeState objects.
     * Fields included: actionId, actionType, prevState, newState (and others from the base class).
     * @param action The action to serialize.
     * @return A JSON string.
     */
    public static String serializeAction(final Action action) {
        if (action == null) {
            return "null";
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("{");

        // 1. Core Metadata Fields
        sb.append(JsonUtils.jsonEscape("ActionId")).append(":").append(JsonUtils.jsonEscape(action.getActionId())).append(",");
        sb.append(JsonUtils.jsonEscape("ActionType")).append(":").append(JsonUtils.jsonEscape(action.getActionType().toString())).append(",");
        
        

        // 2. PrevState (nested ShapeState JSON)
        final String prevStateJson = ShapeSerializer.serializeShape(action.getPrevState());
        sb.append(JsonUtils.jsonEscape("Prev")).append(":");
        if (prevStateJson != null) {
            sb.append(prevStateJson); // Append raw JSON object
        } else {
            sb.append("null");
        }
        sb.append(",");

        // 3. NewState (nested ShapeState JSON)
        final String newStateJson = ShapeSerializer.serializeShape(action.getNewState());
        sb.append(JsonUtils.jsonEscape("Next")).append(":");
        if (newStateJson != null) {
            sb.append(newStateJson); // Append raw JSON object
        } else {
            sb.append("null");
        }
        
        // NOTE: The 'next' field is omitted as it is not present in the base Action class.

        sb.append("}");
        return sb.toString();
    }

    /**
     * Manually deserializes a JSON string back into a concrete Action object.
     *
     * IMPORTANT: This method must use the Action subclass constructors
     * (e.g., CreateShapeAction, ModifyShapeAction) for correct object creation.
     */
    public static Action deserializeAction(final String json) {
        if (json == null || json.isEmpty() || "null".equals(json)) {
            return null;
        }

        try {
            final String content = json.substring(1, json.length() - 1); // Remove outer { }

            // 1. Extract Core Metadata Fields
            final String actionId = JsonUtils.extractString(content, "ActionId");
            final String actionTypeString = JsonUtils.extractString(content, "ActionType");

            final ActionType actionType = ActionType.valueOf(actionTypeString);
            
            // 2. Extract nested JSON strings for PrevState and NewState
            final String prevStateJson = JsonUtils.extractNestedJson(content, "Prev");
            final String newStateJson = JsonUtils.extractNestedJson(content, "Next");

            // 3. Deserialize nested ShapeState objects
            final ShapeState prevState = ShapeSerializer.deserializeShape(prevStateJson);
            final ShapeState newState = ShapeSerializer.deserializeShape(newStateJson);

            if (actionId == null || actionType == null || newState == null) {
                throw new SerializationException("Missing crucial action field during deserialization.");
            }


            final String userId = newState.getShape().getLastUpdatedBy();
            final long timestamp = newState.getLastModified();
            final ShapeId targetId = newState.getShape().getShapeId();

            // 4. Factory logic to create the correct concrete Action subclass
            switch (actionType) {
                case CREATE:
                    // CreateAction ignores prevState (should be null)
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

        } catch (final SerializationException e) {
            throw new SerializationException("Failed to manually deserialize Action: " + e.getMessage(), e);
        }
    }
}



