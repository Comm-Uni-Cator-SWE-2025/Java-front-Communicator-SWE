package com.swe.ux.model.analytics;

public class ShapeCount {
    private final int freeHand;
    private final int straightLine;
    private final int rectangle;
    private final int ellipse;
    private final int triangle;

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
}
