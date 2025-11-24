package com.swe.ux.canvas.util;


import java.util.List;

import com.swe.canvas.datamodel.shape.Point;
import com.swe.canvas.datamodel.shape.Shape;
import com.swe.canvas.datamodel.shape.ShapeType;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;

/**
 * Utility for geometric calculations (bounding boxes, hit testing).
 */
public final class GeometryUtils {

    /**
     * Threshold for hit testing proximity.
     */
    private static final double HIT_THRESHOLD = 5.0;

    private GeometryUtils() {
        // Utility class should not be instantiated
    }

    /**
     * Function to return the bounds of a shape.
     * @param shape the current shape
     * @return Bounds
     */
    public static Bounds getBounds(final Shape shape) {
        final List<Point> points = shape.getPoints();
        if (points.isEmpty()) {
            return new BoundingBox(0, 0, 0, 0);
        }

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        for (Point p : points) {
            minX = Math.min(minX, p.getX());
            minY = Math.min(minY, p.getY());
            maxX = Math.max(maxX, p.getX());
            maxY = Math.max(maxY, p.getY());
        }

        return new BoundingBox(minX, minY, maxX - minX, maxY - minY);
    }

    /**
     * Test to see if the shape is hit or not.
     * @param shape Shape that is clicked
     * @param x x coordinate
     * @param y y coordinate
     * @return True/False
     */
    public static boolean hitTest(final Shape shape, final double x, final double y) {
        final Bounds b = getBounds(shape);
        final ShapeType shapeType = shape.getShapeType();
        
        // Simple bounding box hit test first for efficiency
        if (!b.contains(x, y) && shapeType != ShapeType.FREEHAND && shapeType != ShapeType.LINE) {
            return false;
        }

        if (shapeType == ShapeType.RECTANGLE || shapeType == ShapeType.ELLIPSE || shapeType == ShapeType.TRIANGLE) {
            return b.contains(x, y);
        } else if (shapeType == ShapeType.LINE) {
            return distanceToLine(shape.getPoints().get(0), shape.getPoints().get(1), x, y) < HIT_THRESHOLD;
        } else if (shapeType == ShapeType.FREEHAND) {
            return isFreehandHit(shape, x, y);
        }
        return false;
    }

    private static boolean isFreehandHit(final Shape shape, final double x, final double y) {
        for (int i = 0; i < shape.getPoints().size() - 1; i++) {
            if (distanceToLine(shape.getPoints().get(i), shape.getPoints().get(i + 1), x, y) < HIT_THRESHOLD) {
                return true;
            }
        }
        return false;
    }

    private static double distanceToLine(final Point p1, final Point p2, final double px, final double py) {
        final double x1 = p1.getX();
        final double y1 = p1.getY();
        final double x2 = p2.getX();
        final double y2 = p2.getY();
        final double deltaA = px - x1;
        final double deltaB = py - y1;
        final double deltaC = x2 - x1;
        final double deltaD = y2 - y1;

        final double dot = deltaA * deltaC + deltaB * deltaD;
        final double lenSq = deltaC * deltaC + deltaD * deltaD;

        double param = -1;

        if (lenSq != 0) {
            param = dot / lenSq;
        }

        final double xx;
        final double yy;

        if (param < 0) {
            xx = x1;
            yy = y1;
        } else if (param > 1) {
            xx = x2;
            yy = y2;
        } else {
            xx = x1 + param * deltaC;
            yy = y1 + param * deltaD;
        }

        final double dx = px - xx;
        final double dy = py - yy;
        return Math.sqrt(dx * dx + dy * dy);
    }

    
}


