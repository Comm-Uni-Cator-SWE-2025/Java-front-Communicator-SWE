/**
 *  Contributed by Ram Charan.
 */

package com.swe.ux.model.analytics;

public class ShapeCount {
    private int freeHand;
    private int straightLine;
    private int rectangle;
    private int ellipse;
    private int triangle;

    public ShapeCount(int freeHand, int straightLine, int rectangle, int ellipse, int triangle) {
        this.freeHand = freeHand;
        this.straightLine = straightLine;
        this.rectangle = rectangle;
        this.ellipse = ellipse;
        this.triangle = triangle;
    }

    public int getFreeHand() {
        return freeHand;
    }

    public int getStraightLine() {
        return straightLine;
    }

    public int getRectangle() {
        return rectangle;
    }

    public int getEllipse() {
        return ellipse;
    }

    public int getTriangle() {
        return triangle;
    }

    // Increment methods for analytics tracking
    public void incrementFreeHand() {
        this.freeHand++;
    }

    public void incrementStraightLine() {
        this.straightLine++;
    }

    public void incrementRectangle() {
        this.rectangle++;
    }

    public void incrementEllipse() {
        this.ellipse++;
    }

    public void incrementTriangle() {
        this.triangle++;
    }
}
