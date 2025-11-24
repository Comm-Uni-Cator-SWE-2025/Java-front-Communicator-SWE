package com.swe.canvas.datamodel.shape;

import java.awt.Color;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Abstract base class representing a drawable geometric shape on a canvas.
 *
 * <p>This class defines the common properties and behaviors shared by all shapes,
 * such as position points, color, stroke thickness, and creator metadata.
 * Concrete shape types (e.g., Rectangle, Circle, Line) extend this class to
 * implement specific geometry and drawing logic.</p>
 *
 * <p><b>Thread Safety:</b> Not thread-safe. Instances are typically managed by
 * higher-level components like {@code CanvasState} or {@code ShapeManager}.</p>
 *
 * @author 
 *     Gajjala Bhavani Shankar
 */
public abstract class Shape implements Serializable {

    /** Used for Java serialization. */
    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique identifier for this shape. */
    private final ShapeId shapeId;

    /** List of defining points for the shape (e.g., corners or control points). */
    private List<Point> points;

    /** Thickness of the shape's outline. */
    private double thickness;

    /** Color used to render the shape. */
    private Color color;

    /** Identifier of the user who created this shape. */
    private final String createdBy;

    /** Identifier of the user who last modified this shape. */
    private String lastUpdatedBy;

    /** Type of the shape (e.g., RECTANGLE, LINE, CIRCLE). */
    private final ShapeType shapeType;

    /**
     * Constructs a new {@code Shape} instance with the given properties.
     *
     * @param shapeIdParam       The unique shape identifier (non-null).
     * @param shapeTypeParam     The shape type enumeration (non-null).
     * @param pointsParam        The list of defining points (non-null).
     * @param thicknessParam     The line thickness.
     * @param colorParam         The color used to render the shape (non-null).
     * @param createdByParam     The identifier of the user who created this shape (non-null).
     * @param lastUpdatedByParam The identifier of the user who last modified this shape (non-null).
     * @throws NullPointerException if any required argument is {@code null}.
     */
    protected Shape(final ShapeId shapeIdParam, final ShapeType shapeTypeParam, final List<Point> pointsParam,
                    final double thicknessParam, final Color colorParam, final String createdByParam, 
                    final String lastUpdatedByParam) {
        this.shapeId = Objects.requireNonNull(shapeIdParam, "shapeId cannot be null");
        this.shapeType = Objects.requireNonNull(shapeTypeParam, "shapeType cannot be null");
        this.points = Objects.requireNonNull(pointsParam, "points list cannot be null");
        this.thickness = Objects.requireNonNull(thicknessParam, "thickness cannot be null");
        this.color = Objects.requireNonNull(colorParam, "color cannot be null");
        this.createdBy = Objects.requireNonNull(createdByParam, "createdBy cannot be null");
        this.lastUpdatedBy = Objects.requireNonNull(lastUpdatedByParam, "lastUpdatedBy cannot be null");
    }

    /**
     * Translates (moves) this shape by the given offset.
     *
     * <p>This operation shifts all points in the shape by the specified delta values
     * along the X and Y axes. The transformation is applied in-place by updating
     * the {@code points} list.</p>
     *
     * @param dx The horizontal displacement.
     * @param dy The vertical displacement.
     */
    public void translate(final double dx, final double dy) {
        final List<Point> newPoints = new ArrayList<>(points.size());
        for (final Point p : points) {
            newPoints.add(new Point(p.getX() + dx, p.getY() + dy));
        }
        this.points = newPoints;
    }

    /**
     * Creates and returns a deep copy of this shape.
     *
     * <p>Subclasses must implement this method to ensure that all relevant
     * fields (such as points and color) are duplicated correctly.</p>
     *
     * @return A deep copy of this shape.
     */
    public abstract Shape copy();

    /**
     * Gets the unique identifier of this shape.
     * @return The unique identifier of this shape. 
     */
    public ShapeId getShapeId() {
        return shapeId;
    }

    /**
     * Gets the list of defining points for this shape.
     * @return The list of defining points for this shape. 
     */
    public List<Point> getPoints() {
        return points;
    }

    /**
     * Gets the line thickness of the shape.
     * @return The line thickness of the shape. 
     */
    public double getThickness() {
        return thickness;
    }

    /**
     * Gets the color used to render this shape.
     * @return The color used to render this shape. 
     */
    public Color getColor() {
        return color;
    }

    /**
     * Gets the user ID of the creator of this shape.
     * @return The user ID of the creator of this shape.
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * Gets the user ID of the last person who updated this shape.
     * @return The user ID of the last person who updated this shape.
     */
    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    /**
     * Gets the type of this shape.
     * @return The type of this shape. 
     */
    public ShapeType getShapeType() {
        return shapeType;
    }

    /**
     * Updates the defining points of this shape.
     *
     * <p>Updates the defining points of this shape.</p>
     *
     * @param pointsParam The new list of points.
     */
    public void setPoints(final List<Point> pointsParam) {
        this.points = pointsParam;
    }

    /**
     * Updates the thickness of the shape's outline.
     *
     * @param thicknessParam The new line thickness.
     */
    public void setThickness(final double thicknessParam) {
        this.thickness = thicknessParam;
    }

    /**
     * Updates the color used to render this shape.
     *
     * @param colorParam The new color.
     */
    public void setColor(final Color colorParam) {
        this.color = colorParam;
    }

    /**
     * Updates the user who last modified this shape.
     *
     * @param lastUpdatedByParam The last updating user ID.
     */
    public void setLastUpdatedBy(final String lastUpdatedByParam) {
        this.lastUpdatedBy = lastUpdatedByParam;
    }

    /**
     * Compares this shape to another object for equality.
     *
     * <p>Two shapes are considered equal if all of their defining attributes
     * (ID, points, thickness, color, creators, and type) match exactly.</p>
     *
     * @param o The object to compare with.
     * @return {@code true} if the objects are equal; {@code false} otherwise.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
            
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
            
        final Shape shape = (Shape) o;
        return Double.compare(shape.thickness, thickness) == 0 
                && shapeId.equals(shape.shapeId)
                && points.equals(shape.points) 
                && color.equals(shape.color) 
                && createdBy.equals(shape.createdBy) 
                && lastUpdatedBy.equals(shape.lastUpdatedBy) 
                && shapeType == shape.shapeType;
    }

    /**
     * Computes a hash code for this shape.
     *
     * @return The computed hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(shapeId, points, thickness, color, createdBy, lastUpdatedBy, shapeType);
    }

    /** Length of the shape ID substring used in string representation. */
    private static final int SUBSTRING_LENGTH = 8;

    /**
     * Returns a concise string representation of this shape, useful for debugging.
     *
     * @return A string summarizing the shape type, ID, number of points, and color.
     */
    @Override
    public String toString() {
        return String.format("%s[id=%s, points=%d, color=%s]",
                shapeType, shapeId.getValue().substring(0, SUBSTRING_LENGTH), points.size(), color);
    }
}
