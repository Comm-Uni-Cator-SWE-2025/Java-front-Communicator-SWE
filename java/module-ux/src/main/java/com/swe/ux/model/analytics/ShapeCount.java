/*
 * Contributed by Ram Charan.
 */

package com.swe.ux.model.analytics;

/**
 * Model representing counts of different shape types.
 */
public class ShapeCount {

    /** Count of freehand shapes. */
    private int freeHand;

    /** Count of straight line shapes. */
    private int straightLine;

    /** Count of rectangle shapes. */
    private int rectangle;

    /** Count of ellipse shapes. */
    private int ellipse;

    /** Count of triangle shapes. */
    private int triangle;

    /**
     * Creates a new shape count model.
     *
     * @param freeHandCount count of freehand shapes
     * @param straightLineCount count of straight lines
     * @param rectangleCount count of rectangles
     * @param ellipseCount count of ellipses
     * @param triangleCount count of triangles
     */
    public ShapeCount(final int freeHandCount,
                      final int straightLineCount,
                      final int rectangleCount,
                      final int ellipseCount,
                      final int triangleCount) {
        this.freeHand = freeHandCount;
        this.straightLine = straightLineCount;
        this.rectangle = rectangleCount;
        this.ellipse = ellipseCount;
        this.triangle = triangleCount;
    }

    /**
     * Gets the freehand shape count.
     *
     * @return freehand count
     */
    public int getFreeHand() {
        return freeHand;
    }

    /**
     * Gets the straight line count.
     *
     * @return straight line count
     */
    public int getStraightLine() {
        return straightLine;
    }

    /**
     * Gets the rectangle count.
     *
     * @return rectangle count
     */
    public int getRectangle() {
        return rectangle;
    }

    /**
     * Gets the ellipse count.
     *
     * @return ellipse count
     */
    public int getEllipse() {
        return ellipse;
    }

    /**
     * Gets the triangle count.
     *
     * @return triangle count
     */
    public int getTriangle() {
        return triangle;
    }

    /** Increments the freehand count. */
    public void incrementFreeHand() {
        setFreeHand(this.freeHand + 1);
    }

    /** Increments the straight line count. */
    public void incrementStraightLine() {
        setStraightLine(this.straightLine + 1);
    }

    /** Increments the rectangle count. */
    public void incrementRectangle() {
        setRectangle(this.rectangle + 1);
    }

    /** Increments the ellipse count. */
    public void incrementEllipse() {
        setEllipse(this.ellipse + 1);
    }

    /** Increments the triangle count. */
    public void incrementTriangle() {
        setTriangle(this.triangle + 1);
    }

    /**
     * Sets the freehand shape count.
     *
     * @param count new freehand count
     */
    public void setFreeHand(final int count) {
        this.freeHand = count;
    }

    public void setStraightLine(final int count) {
        this.straightLine = count;
    }

    public void setRectangle(final int count) {
        this.rectangle = count;
    }

    public void setEllipse(final int count) {
        this.ellipse = count;
    }

    public void setTriangle(final int count) {
        this.triangle = count;
    }
}
