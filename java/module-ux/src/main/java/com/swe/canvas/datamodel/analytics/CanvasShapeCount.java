/*
 * -----------------------------------------------------------------------------
 * File: CanvasShapeCount.java
 * Owner: Darla Manohar
 * Roll Number: 112201034
 * Module: Canvas
 *
 * -----------------------------------------------------------------------------
 */

package com.swe.canvas.datamodel.analytics;

import com.swe.ux.model.analytics.ShapeCount;

public class CanvasShapeCount extends ShapeCount {

    public CanvasShapeCount(int freeHand, int straightLine, int rectangle, int ellipse, int triangle) {
        super(freeHand, straightLine, rectangle, ellipse, triangle);
    }

    // Increment methods for analytics tracking
    public void incrementFreeHand() {
        this.setFreeHand(this.getFreeHand() + 1);
    }

    public void incrementStraightLine() {
        this.setStraightLine(this.getStraightLine() + 1);
    }

    public void incrementRectangle() {
        this.setRectangle(this.getRectangle() + 1);
    }

    public void incrementEllipse() {
        this.setEllipse(this.getEllipse() + 1);
    }

    public void incrementTriangle() {
        this.setTriangle(this.getTriangle() + 1);
    }
}

