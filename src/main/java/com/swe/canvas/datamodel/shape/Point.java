package com.swe.canvas.datamodel.shape;

import com.swe.canvas.datamodel.canvas.ShapeState;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents an immutable 2D coordinate (x, y).
 *
 * <p>This class is a simple data holder used to define the geometry of shapes.
 * It is immutable to ensure thread safety and predictability when used in
 * {@link ShapeState} snapshots.
 * </p>
 *
 * <p><b>Thread Safety:</b> This class is immutable and therefore thread-safe.</p>
 *
 * @author Gajjala Bhavani Shankar
 
 
 */
public final class Point implements Serializable {

    /**
     * Used for Java serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The x-coordinate.
     */
    private final double x;

    /**
     * The y-coordinate.
     */
    private final double y;

    /**
     * Constructs a new Point.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     */
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the x-coordinate.
     *
     * @return The x-coordinate.
     */
    public double getX() {
        return x;
    }

    /**
     * Gets the y-coordinate.
     *
     * @return The y-coordinate.
     */
    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        return "Point(" + x + ", " + y + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return Double.compare(point.x, x) == 0 && Double.compare(point.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}