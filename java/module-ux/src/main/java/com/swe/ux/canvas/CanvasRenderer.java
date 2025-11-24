package com.swe.ux.canvas;

import java.util.List;

import com.swe.canvas.datamodel.canvas.CanvasState;
import com.swe.canvas.datamodel.canvas.ShapeState;
import com.swe.canvas.datamodel.shape.Point;
import com.swe.canvas.datamodel.shape.Shape;
import com.swe.canvas.datamodel.shape.ShapeId;
import com.swe.canvas.datamodel.shape.ShapeType;
import com.swe.ux.canvas.util.ColorConverter;
import com.swe.ux.canvas.util.GeometryUtils;

import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

/**
 * Renderer for canvas shapes.
 */
public class CanvasRenderer {

    /**
     * Opacity for ghost shapes.
     */
    private static final double GHOST_OPACITY = 0.5;
    /**
     * Full opacity for normal shapes.
     */
    private static final double FULL_OPACITY = 1.0;
    /**
     * Margin around selection box.
     */
    private static final double SELECTION_MARGIN = 5.0;
    /**
     * Additional size to add to selection box.
     */
    private static final double SELECTION_SIZE_ADDITION = 10.0;
    /**
     * Size of selection box dashes.
     */
    private static final int DASHES_SIZE = 5;
    /**
     * Divisor for calculating triangle center point.
     */
    private static final double TRIANGLE_CENTER_DIVISOR = 2.0;
    /**
     * Number of vertices in a triangle.
     */
    private static final int TRIANGLE_VERTICES = 3;

    /**
     * The canvas to render on.
     */
    private final Canvas canvas;

    /**
     * Graphics context for drawing.
     */
    private final GraphicsContext gc;

    /**
     * Constructor for renderer.
     * @param canvasParam Instance of the canvas
     */
    public CanvasRenderer(final Canvas canvasParam) {
        this.canvas = canvasParam;
        this.gc = canvas.getGraphicsContext2D();
    }

    /**
     * Main Rendering logic.
     * @param state current state
     * @param transientShape The "ghost" shape awaiting network confirmation
     * @param selectedShapeId id of the shape selected
     * @param isDraggingSelection dragging or not (Note: isDragging is now part of transientShape logic)
     */
    public void render(final CanvasState state, final Shape transientShape,
                       final ShapeId selectedShapeId, final boolean isDraggingSelection) {
        // We must use the canvas's fixed width/height, not the container's
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // 1. Draw all committed shapes
        for (final Shape shape : state.getVisibleShapes()) {
            // If we are ghosting this shape (e.g., dragging it), don't draw the original.
            if (transientShape != null && shape.getShapeId().equals(transientShape.getShapeId())) {
                continue;
            }
            drawShape(shape, FULL_OPACITY); // Draw fully opaque
        }

        // 2. Draw ghost shape (either new drawing OR shape being moved)
        if (transientShape != null) {
            // Draw ghost with 50% opacity
            drawShape(transientShape, GHOST_OPACITY);
        }

        // 3. Draw selection box
        if (selectedShapeId != null) {
            final ShapeState selectedState = state.getShapeState(selectedShapeId);
            if (selectedState != null && !selectedState.isDeleted()) {
                // If we are dragging the selected shape, draw box around the ghost
                if (transientShape != null && transientShape.getShapeId().equals(selectedShapeId)) {
                    drawBoundingBox(transientShape);
                } else {
                    // Otherwise draw around the shape in the main state
                    drawBoundingBox(selectedState.getShape());
                }
            }
        }
    }

    private void drawShape(final Shape shape, final double alpha) {
        setupGraphicsContext(shape, alpha);

        final List<Point> p = shape.getPoints();
        if (p.isEmpty()) {
            return;
        }

        drawShapeGeometry(shape.getShapeType(), p);
        gc.setGlobalAlpha(FULL_OPACITY); // Reset alpha
    }

    private void setupGraphicsContext(final Shape shape, final double alpha) {
        gc.setStroke(ColorConverter.toFx(shape.getColor()));
        gc.setLineWidth(shape.getThickness());
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);
        gc.setGlobalAlpha(alpha);
    }

    private void drawShapeGeometry(final ShapeType shapeType, final List<Point> points) {
        if (shapeType == ShapeType.FREEHAND) {
            drawFreehand(points);
        } else if (points.size() >= 2) {
            drawTwoPointShape(shapeType, points.get(0), points.get(1));
        }
    }

    private void drawTwoPointShape(final ShapeType shapeType, final Point p1, final Point p2) {
        if (shapeType == ShapeType.LINE) {
            gc.strokeLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
        } else if (shapeType == ShapeType.RECTANGLE) {
            drawRect(p1, p2);
        } else if (shapeType == ShapeType.ELLIPSE) {
            drawEllipse(p1, p2);
        } else if (shapeType == ShapeType.TRIANGLE) {
            drawTriangle(p1, p2);
        }
    }

    private void drawFreehand(final List<Point> points) {
        gc.beginPath();
        gc.moveTo(points.get(0).getX(), points.get(0).getY());
        for (int i = 1; i < points.size(); i++) {
            gc.lineTo(points.get(i).getX(), points.get(i).getY());
        }
        gc.stroke();
    }

    private void drawBoundingBox(final Shape shape) {
        final Bounds b = GeometryUtils.getBounds(shape);
        gc.setStroke(Color.CORNFLOWERBLUE);
        gc.setLineWidth(1);
        gc.setLineDashes(DASHES_SIZE);
        // Draw slightly larger than the shape
        gc.strokeRect(b.getMinX() - SELECTION_MARGIN, b.getMinY() - SELECTION_MARGIN,
                b.getWidth() + SELECTION_SIZE_ADDITION, b.getHeight() + SELECTION_SIZE_ADDITION);
        gc.setLineDashes((double[]) null);
    }

    private void drawRect(final Point p1, final Point p2) {
        gc.strokeRect(Math.min(p1.getX(), p2.getX()), Math.min(p1.getY(), p2.getY()),
                Math.abs(p1.getX() - p2.getX()), Math.abs(p1.getY() - p2.getY()));
    }

    private void drawEllipse(final Point p1, final Point p2) {
        gc.strokeOval(Math.min(p1.getX(), p2.getX()), Math.min(p1.getY(), p2.getY()),
                Math.abs(p1.getX() - p2.getX()), Math.abs(p1.getY() - p2.getY()));
    }

    private void drawTriangle(final Point p1, final Point p2) {
        final double minX = Math.min(p1.getX(), p2.getX());
        final double minY = Math.min(p1.getY(), p2.getY());
        final double maxX = Math.max(p1.getX(), p2.getX());
        final double maxY = Math.max(p1.getY(), p2.getY());
        gc.strokePolygon(new double[] {minX + (maxX - minX) / TRIANGLE_CENTER_DIVISOR, minX, maxX},
                new double[] {minY, maxY, maxY}, TRIANGLE_VERTICES);
    }
}


