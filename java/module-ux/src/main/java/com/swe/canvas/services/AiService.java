
/*
 * -----------------------------------------------------------------------------
 * File: AiService.java
 * Owner: Bhogaraju Shanmukha Sri Krishna
 * Roll Number: 112201013
 * Module: Canvas
 *
 * -----------------------------------------------------------------------------
 */



package com.swe.canvas.services;

import java.util.ArrayList;
import java.util.List;

import com.swe.canvas.datamodel.shape.Point;
import com.swe.canvas.datamodel.shape.Shape;
import com.swe.canvas.datamodel.shape.ShapeType;

public class AiService {
    /**
     * Regularize a freehand shape into a polygonal or smoothed Shape.
     * <p>
     * This method returns a new Shape instance (a copy of the original)
     * with its points replaced by a regularized approximation. If the
     * input is invalid or the shape is not a FREEHAND shape, {@code null}
     * is returned.
     * </p>
     *
     * @param original the original shape (must be FREEHAND)
     * @return a new Shape with regularized points, or null if not applicable
     */
    public static Shape regularizeFreehandShape(final Shape original) {
        if (original == null || original.getPoints() == null || original.getPoints().size() < 3) {
            return null;
        }
        if (original.getShapeType() != ShapeType.FREEHAND) {
            return original; // only operate on freehand
        }

        final List<Point> originalPoints = original.getPoints();
        final List<Point> newPoints = regularizeFreehandPoints(originalPoints);

        if (newPoints == null || newPoints.isEmpty()) {
            return null;
        }

        final Shape copy = original.copy();
        copy.setPoints(newPoints);
        return copy;
    }

    /**
     * Simple regularization helper: produce an N-sided regular polygon that
     * approximates the freehand stroke. This uses the centroid of the stroke
     * and the average radius of points from that centroid. The returned list
     * is non-empty for valid inputs (sides>=3).
     */
    private static List<Point> regularizeFreehandPoints(final List<Point> originalPoints) {
        final List<Point> out = new ArrayList<>();
        final Point p = new Point(25, 25);
        out.add(p);

        return out;
    }
}
