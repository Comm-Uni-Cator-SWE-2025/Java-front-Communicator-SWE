package com.swe.canvas.datamodel.shape;

import com.swe.canvas.datamodel.canvas.CanvasState;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete shape for a freehand line, defined by a list of many points.
 *
 * <p><b>Thread Safety:</b> Not thread-safe. Designed to be managed by
 * {@link CanvasState}.
 * </p>
 *
 * @author Gajjala Bhavani Shankar
 
 
 */
public class FreehandShape extends Shape {

    /**
     * Used for Java serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new FreehandShape.
     *
     * @param shapeId       The unique ID.
     * @param points        The list of points in the line.
     * @param thickness     The stroke thickness.
     * @param color         The shape color.
     * @param createdBy     The creating user's ID.
     * @param lastUpdatedBy The last modifying user's ID.
     */
    public FreehandShape(ShapeId shapeId, List<Point> points, double thickness,
                         Color color, String createdBy, String lastUpdatedBy) {
        super(shapeId, ShapeType.FREEHAND, points, thickness, color, createdBy, lastUpdatedBy);
    }

    @Override
    public Shape copy() {
        // Return a deep copy
        return new FreehandShape(
                this.shapeId,
                new ArrayList<>(this.points), // Deep copy of points list
                this.thickness,
                this.color,
                this.createdBy,
                this.lastUpdatedBy
        );
    }
}