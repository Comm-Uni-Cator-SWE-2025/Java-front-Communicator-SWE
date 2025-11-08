package com.swe.canvas.datamodel.shape;

// import com.swe.canvas.datamodel.canvas.CanvasState;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete shape for a straight line, defined by two endpoints.
 *
 * <p><b>Thread Safety:</b> Not thread-safe. Designed to be managed by
 * {@link CanvasState}.
 * </p>
 *
 * @author Gajjala Bhavani Shankar
 
 
 */
public class LineShape extends Shape {

    /**
     * Used for Java serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new LineShape.
     *
     * @param shapeId       The unique ID.
     * @param points        List containing 2 endpoints.
     * @param thickness     The stroke thickness.
     * @param color         The shape color.
     * @param createdBy     The creating user's ID.
     * @param lastUpdatedBy The last modifying user's ID.
     * @throws IllegalArgumentException if points list does not contain exactly 2 points.
     */
    public LineShape(final ShapeId shapeId, final List<Point> points, final double thickness,
                     final Color color, final String createdBy, final String lastUpdatedBy) {
        super(shapeId, ShapeType.LINE, points, thickness, color, createdBy, lastUpdatedBy);
        if (points.size() != 2) {
            throw new IllegalArgumentException("LineShape must be defined by exactly 2 points.");
        }
    }

    @Override
    public Shape copy() {
        return new LineShape(
                this.shapeId,
                new ArrayList<>(this.points), // Deep copy
                this.thickness,
                this.color,
                this.createdBy,
                this.lastUpdatedBy
        );
    }
}