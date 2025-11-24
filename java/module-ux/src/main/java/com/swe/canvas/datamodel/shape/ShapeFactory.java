package com.swe.canvas.datamodel.shape;

// import com.swe.canvas.datamodel.action.ActionFactory;

import java.awt.Color;
import java.util.List;

/**
 * Factory for creating concrete {@link Shape} instances.
 *
 * <p>This class abstracts the instantiation logic for different shape types,
 * simplifying the creation process for the UI layer and {@link ActionFactory}.
 * </p>
 *
 * <p><b>Thread Safety:</b> This class is stateless and therefore thread-safe.</p>
 *
 * <p><b>Design Pattern:</b> Factory Pattern</p>
 *
 * @author Gajjala Bhavani Shankar
 */


 
public class ShapeFactory {

    /**
     * Creates a new shape instance.
     *
     * @param shapeType The enum type of shape to create.
     * @param shapeId   The unique ID for the new shape.
     * @param points    The geometric points for the new shape.
     * @param thickness The stroke thickness.
     * @param color     The shape color.
     * @param userId    The user creating the shape.
     * @return A new, concrete {@link Shape} instance.
     * @throws IllegalArgumentException if the shapeType is unrecognized.
     */
    public Shape createShape(final ShapeType shapeType, final ShapeId shapeId, final List<Point> points,
                             final double thickness, final Color color, final String userId) {

        switch (shapeType) {
            case FREEHAND:
                return new FreehandShape(shapeId, points, thickness, color, userId, userId);
            case RECTANGLE:
                return new RectangleShape(shapeId, points, thickness, color, userId, userId);
            case ELLIPSE:
                return new EllipseShape(shapeId, points, thickness, color, userId, userId);
            case TRIANGLE:
                return new TriangleShape(shapeId, points, thickness, color, userId, userId);
            case LINE:
                return new LineShape(shapeId, points, thickness, color, userId, userId);
            default:
                throw new IllegalArgumentException("Unknown shape type: " + shapeType);
        }
    }
}