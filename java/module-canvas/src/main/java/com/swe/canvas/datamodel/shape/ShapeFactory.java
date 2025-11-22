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
     * @param shapeType_ The enum type of shape to create.
     * @param shapeId_   The unique ID for the new shape.
     * @param points_    The geometric points for the new shape.
     * @param thickness_ The stroke thickness.
     * @param color_     The shape color.
     * @param userId_    The user creating the shape.
     * @return A new, concrete {@link Shape} instance.
     * @throws IllegalArgumentException if the shapeType is unrecognized.
     */
    public Shape createShape(final ShapeType shapeType_, final ShapeId shapeId_, final List<Point> points_,
                             final double thickness_, final Color color_, final String userId_) {

        switch (shapeType_) {
            case FREEHAND:
                return new FreehandShape(shapeId_, points_, thickness_, color_, userId_, userId_);
            case RECTANGLE:
                return new RectangleShape(shapeId_, points_, thickness_, color_, userId_, userId_);
            case ELLIPSE:
                return new EllipseShape(shapeId_, points_, thickness_, color_, userId_, userId_);
            case TRIANGLE:
                return new TriangleShape(shapeId_, points_, thickness_, color_, userId_, userId_);
            case LINE:
                return new LineShape(shapeId_, points_, thickness_, color_, userId_, userId_);
            default:
                throw new IllegalArgumentException("Unknown shape type: " + shapeType_);
        }
    }
}