/**
 * Implements the <b>Command Pattern</b> for all canvas operations.
 *
 * <p>This package includes:
 * <ul>
 * <li>{@link com.swe.canvas.datamodel.action.Action}: The abstract base class
 * for all commands.</li>
 * <li>Concrete action classes ({@link com.swe.canvas.datamodel.action.CreateShapeAction},
 * {@link com.swe.canvas.datamodel.action.DeleteShapeAction}, etc.).</li>
 * <li>{@link com.swe.canvas.datamodel.action.ActionFactory}: A factory for
 * creating new action objects.</li>
 * </ul>
 * </p>
 *
 * <p>Each action encapsulates the full <b>Memento</b> ({@link com.swe.canvas.datamodel.canvas.ShapeState})
 * of the shape both before (`prevState`) and after (`newState`) the operation.
 * This is critical for conflict detection and undo/redo.
 * </p>
 *
 * @author Gajula Sri Siva Sai Shashank
 
 
 */
package com.swe.canvas.datamodel.action;