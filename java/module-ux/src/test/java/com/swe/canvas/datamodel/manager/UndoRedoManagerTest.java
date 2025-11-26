/*
 * -----------------------------------------------------------------------------
 * File: UndoRedoManagerTest.java
 * Owner: Gajjala Bhavani Shankar
 * Roll Number : 112201026
 * Module: Canvas
 * -----------------------------------------------------------------------------
 */
package com.swe.canvas.datamodel.manager;

import com.swe.canvas.datamodel.action.Action;
import com.swe.canvas.datamodel.action.ActionType;
import com.swe.canvas.datamodel.canvas.ShapeState;
import com.swe.canvas.datamodel.shape.Point;
import com.swe.canvas.datamodel.shape.Shape;
import com.swe.canvas.datamodel.shape.ShapeId;
import com.swe.canvas.datamodel.shape.ShapeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class UndoRedoManagerTest {

    private UndoRedoManager manager;
    private Action stubAction1;
    private Action stubAction2;

    static class TestShape extends Shape {
        private static final long serialVersionUID = 1L;
        public TestShape(ShapeId id) {
            super(id, ShapeType.FREEHAND, new ArrayList<>(Arrays.asList(new Point(0, 0))), 1, Color.BLACK, "u", "u");
        }
        @Override public Shape copy() { return new TestShape(getShapeId()); }
    }

    static class TestAction extends Action {
        private static final long serialVersionUID = 1L;
        public TestAction(String actionId) {
            super(actionId, "user", 1L, ActionType.MODIFY, new ShapeId("s1"),
                    new ShapeState(new TestShape(new ShapeId("s1")), false, 1L),
                    new ShapeState(new TestShape(new ShapeId("s1")), false, 2L));
        }
    }

    @BeforeEach
    void setUp() {
        manager = new UndoRedoManager();
        stubAction1 = new TestAction("act-1");
        stubAction2 = new TestAction("act-2");
    }

    @Test
    void testStandardFlow() {
        assertFalse(manager.canUndo());
        
        manager.push(stubAction1);
        assertTrue(manager.canUndo());
        assertEquals(stubAction1, manager.getActionToUndo());
        
        manager.applyHostUndo();
        assertTrue(manager.canRedo());
        assertEquals(stubAction1, manager.getActionToRedo());
        
        manager.applyHostRedo();
        assertFalse(manager.canRedo());
    }

    @Test
    void testRedoClearance() {
        manager.push(stubAction1);
        manager.applyHostUndo();
        
        // Pushing new action should clear redo history
        manager.push(stubAction2);
        assertFalse(manager.canRedo());
        assertEquals(stubAction2, manager.getActionToUndo());
    }

    @Test
    void testClear() {
        manager.push(stubAction1);
        manager.clear();
        assertFalse(manager.canUndo());
    }
}