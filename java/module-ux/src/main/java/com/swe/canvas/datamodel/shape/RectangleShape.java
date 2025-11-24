package com.swe.canvas.datamodel.shape;

// import com.swe.canvas.datamodel.canvas.CanvasState;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete shape for a rectangle, defined by two diagonal corner points.
 *
 * <p><b>Thread Safety:</b> Not thread-safe. Designed to be managed by
 * {@link CanvasState}.
 * </p>
 *
 * @author Gajjala Bhavani Shankar
 
 
 */
public class RectangleShape extends Shape {

    /**
     * Used for Java serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new RectangleShape.
     *
     * @param shapeId       The unique ID.
     * @param points        List containing 2 diagonal corner points.
     * @param thickness     The stroke thickness.
     * @param color         The shape color.
     * @param createdBy     The creating user's ID.
     * @param lastUpdatedBy The last modifying user's ID.
     * @throws IllegalArgumentException if points list does not contain exactly 2 points.
     */
    public RectangleShape(final ShapeId shapeId, final List<Point> points, final double thickness,
                          final Color color, final String createdBy, final String lastUpdatedBy) {
        super(shapeId, ShapeType.RECTANGLE, points, thickness, color, createdBy, lastUpdatedBy);
        if (points.size() != 2) {
            throw new IllegalArgumentException("RectangleShape must be defined by exactly 2 points.");
        }
    }

    @Override
    public Shape copy() {
        return new RectangleShape(
                this.shapeId,
                new ArrayList<>(this.points), // Deep copy
                this.thickness,
                this.color,
                this.createdBy,
                this.lastUpdatedBy
        );
    }
}