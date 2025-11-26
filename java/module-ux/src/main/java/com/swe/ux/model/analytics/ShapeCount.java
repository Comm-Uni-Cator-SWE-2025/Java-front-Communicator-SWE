/*
 * Contributed by Ram Charan.
 */

package com.swe.ux.model.analytics;

/**
 * Model representing counts of different shape types.
 */
public class ShapeCount {

    /** Count of freehand shapes. */
    private final int freeHand;

    /** Count of straight line shapes. */
    private final int straightLine;

    /** Count of rectangle shapes. */
    private final int rectangle;

    /** Count of ellipse shapes. */
    private final int ellipse;

    /** Count of triangle shapes. */
    private final int triangle;

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

    /**
     * Sets the freehand shape count.
     *
     * @param count new freehand count
     */
    public void setFreeHand(int count) {
        // This method is intentionally left blank to maintain immutability
        // Subclasses can override this method to provide mutability
    }

    public void setStraightLine(int count) {
        // This method is intentionally left blank to maintain immutability
        // Subclasses can override this method to provide mutability
    }

    public void setRectangle(int count) {
        // This method is intentionally left blank to maintain immutability
        // Subclasses can override this method to provide mutability
    }

    public void setEllipse(int count) {
        // This method is intentionally left blank to maintain immutability
        // Subclasses can override this method to provide mutability
    }

    public void setTriangle(int count) {
        // This method is intentionally left blank to maintain immutability
        // Subclasses can override this method to provide mutability
    }


    
}
