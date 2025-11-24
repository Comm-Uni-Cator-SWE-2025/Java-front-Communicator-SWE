package com.swe.canvas.datamodel.shape;

// import com.swe.canvas.datamodel.canvas.CanvasState;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete shape for a triangle, defined by a bounding box (two diagonal points).
 *
 * <p>The rendering team will be responsible for inscribing a triangle
 * (e.g., isosceles) within this bounding box.
 * </p>
 *
 * <p><b>Thread Safety:</b> Not thread-safe. Designed to be managed by
 * {@link CanvasState}.
 * </p>
 *
 * @author Gajjala Bhavani Shankar
 
 
 */
public class TriangleShape extends Shape {

    /**
     * Used for Java serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new TriangleShape.
     *
     * @param shapeId       The unique ID.
     * @param points        List containing 2 diagonal corner points of the bounding box.
     * @param thickness     The stroke thickness.
     * @param color         The shape color.
     * @param createdBy     The creating user's ID.
     * @param lastUpdatedBy The last modifying user's ID.
     * @throws IllegalArgumentException if points list does not contain exactly 2 points.
     */
    public TriangleShape(final ShapeId shapeId, final List<Point> points, final double thickness,
                         final Color color, final String createdBy, final String lastUpdatedBy) {
        super(shapeId, ShapeType.TRIANGLE, points, thickness, color, createdBy, lastUpdatedBy);
        if (points.size() != 2) {
            throw new IllegalArgumentException("TriangleShape must be defined by exactly 2 points.");
        }
    }

    @Override
    public Shape copy() {
        return new TriangleShape(
                this.getShapeId(),
                new ArrayList<>(this.getPoints()), // Deep copy
                this.getThickness(),
                this.getColor(),
                this.getCreatedBy(),
                this.getLastUpdatedBy()
        );
    }
}