package com.swe.canvas.ui.util;


import com.swe.canvas.datamodel.shape.Point;
import com.swe.canvas.datamodel.shape.Shape;
import com.swe.canvas.datamodel.shape.ShapeType;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;

import java.util.List;

/**
 * Utility for geometric calculations (bounding boxes, hit testing).
 */
public class GeometryUtils {

    private static final double HIT_THRESHOLD = 5.0;

    public static Bounds getBounds(Shape shape) {
        List<Point> points = shape.getPoints();
        if (points.isEmpty()) return new BoundingBox(0, 0, 0, 0);

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

    public static boolean hitTest(Shape shape, double x, double y) {
        Bounds b = getBounds(shape);
        // Simple bounding box hit test first for efficiency
        if (!b.contains(x, y) && shape.getShapeType() != ShapeType.FREEHAND && shape.getShapeType() != ShapeType.LINE) {
            return false;
        }

        switch (shape.getShapeType()) {
            case RECTANGLE:
            case ELLIPSE: // Simplified as rect for now
            case TRIANGLE: // Simplified as rect for now
                return b.contains(x, y);
            case LINE:
                return distanceToLine(shape.getPoints().get(0), shape.getPoints().get(1), x, y) < HIT_THRESHOLD;
            case FREEHAND:
                for (int i = 0; i < shape.getPoints().size() - 1; i++) {
                    if (distanceToLine(shape.getPoints().get(i), shape.getPoints().get(i+1), x, y) < HIT_THRESHOLD) {
                        return true;
                    }
                }
                return false;
            default:
                return false;
        }
    }

    private static double distanceToLine(Point p1, Point p2, double px, double py) {
        double x1 = p1.getX(), y1 = p1.getY(), x2 = p2.getX(), y2 = p2.getY();
        double A = px - x1;
        double B = py - y1;
        double C = x2 - x1;
        double D = y2 - y1;

        double dot = A * C + B * D;
        double len_sq = C * C + D * D;
        double param = (len_sq != 0) ? dot / len_sq : -1;

        double xx, yy;

        if (param < 0) {
            xx = x1; yy = y1;
        } else if (param > 1) {
            xx = x2; yy = y2;
        } else {
            xx = x1 + param * C;
            yy = y1 + param * D;
        }

        double dx = px - xx;
        double dy = py - yy;
        return Math.sqrt(dx * dx + dy * dy);
    }
}