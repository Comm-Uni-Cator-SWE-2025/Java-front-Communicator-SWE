package com.swe.canvas.model;

import com.swe.ux.model.analytics.ShapeCount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for ShapeCount analytics tracking.
 * Tests increment functionality and counter integrity.
 */
class ShapeCountTest {

    private ShapeCount shapeCount;

    @BeforeEach
    void setUp() {
        shapeCount = new ShapeCount(0, 0, 0, 0, 0);
    }

    @Test
    void testInitialCountsAreZero() {
        assertEquals(0, shapeCount.getFreeHand(), "Initial freehand count should be 0");
        assertEquals(0, shapeCount.getStraightLine(), "Initial line count should be 0");
        assertEquals(0, shapeCount.getRectangle(), "Initial rectangle count should be 0");
        assertEquals(0, shapeCount.getEllipse(), "Initial ellipse count should be 0");
        assertEquals(0, shapeCount.getTriangle(), "Initial triangle count should be 0");
    }

    @Test
    void testInitialCountsWithValues() {
        ShapeCount count = new ShapeCount(5, 3, 2, 1, 4);
        assertEquals(5, count.getFreeHand());
        assertEquals(3, count.getStraightLine());
        assertEquals(2, count.getRectangle());
        assertEquals(1, count.getEllipse());
        assertEquals(4, count.getTriangle());
    }

    @Test
    void testIncrementFreeHand() {
        assertEquals(0, shapeCount.getFreeHand());
        
        shapeCount.incrementFreeHand();
        assertEquals(1, shapeCount.getFreeHand(), "Freehand count should increment to 1");
        
        shapeCount.incrementFreeHand();
        assertEquals(2, shapeCount.getFreeHand(), "Freehand count should increment to 2");
    }

    @Test
    void testIncrementStraightLine() {
        assertEquals(0, shapeCount.getStraightLine());
        
        shapeCount.incrementStraightLine();
        assertEquals(1, shapeCount.getStraightLine(), "Line count should increment to 1");
        
        shapeCount.incrementStraightLine();
        assertEquals(2, shapeCount.getStraightLine(), "Line count should increment to 2");
    }

    @Test
    void testIncrementRectangle() {
        assertEquals(0, shapeCount.getRectangle());
        
        shapeCount.incrementRectangle();
        assertEquals(1, shapeCount.getRectangle(), "Rectangle count should increment to 1");
        
        shapeCount.incrementRectangle();
        assertEquals(2, shapeCount.getRectangle(), "Rectangle count should increment to 2");
    }

    @Test
    void testIncrementEllipse() {
        assertEquals(0, shapeCount.getEllipse());
        
        shapeCount.incrementEllipse();
        assertEquals(1, shapeCount.getEllipse(), "Ellipse count should increment to 1");
        
        shapeCount.incrementEllipse();
        assertEquals(2, shapeCount.getEllipse(), "Ellipse count should increment to 2");
    }

    @Test
    void testIncrementTriangle() {
        assertEquals(0, shapeCount.getTriangle());
        
        shapeCount.incrementTriangle();
        assertEquals(1, shapeCount.getTriangle(), "Triangle count should increment to 1");
        
        shapeCount.incrementTriangle();
        assertEquals(2, shapeCount.getTriangle(), "Triangle count should increment to 2");
    }

    @Test
    void testMultipleIncrements() {
        // Increment each type multiple times
        for (int i = 0; i < 5; i++) {
            shapeCount.incrementFreeHand();
        }
        for (int i = 0; i < 3; i++) {
            shapeCount.incrementStraightLine();
        }
        for (int i = 0; i < 7; i++) {
            shapeCount.incrementRectangle();
        }
        for (int i = 0; i < 2; i++) {
            shapeCount.incrementEllipse();
        }
        for (int i = 0; i < 4; i++) {
            shapeCount.incrementTriangle();
        }

        assertEquals(5, shapeCount.getFreeHand());
        assertEquals(3, shapeCount.getStraightLine());
        assertEquals(7, shapeCount.getRectangle());
        assertEquals(2, shapeCount.getEllipse());
        assertEquals(4, shapeCount.getTriangle());
    }

    @Test
    void testIndependentCounters() {
        // Verify each counter is independent
        shapeCount.incrementFreeHand();
        
        assertEquals(1, shapeCount.getFreeHand());
        assertEquals(0, shapeCount.getStraightLine(), 
                     "Line counter should not change when freehand increments");
        assertEquals(0, shapeCount.getRectangle(), 
                     "Rectangle counter should not change when freehand increments");
        assertEquals(0, shapeCount.getEllipse(), 
                     "Ellipse counter should not change when freehand increments");
        assertEquals(0, shapeCount.getTriangle(), 
                     "Triangle counter should not change when freehand increments");
    }

    @Test
    void testCountersPersistAcrossIncrements() {
        shapeCount.incrementFreeHand();
        shapeCount.incrementRectangle();
        
        assertEquals(1, shapeCount.getFreeHand());
        assertEquals(1, shapeCount.getRectangle());
        
        // Increment again
        shapeCount.incrementFreeHand();
        
        assertEquals(2, shapeCount.getFreeHand(), 
                     "Freehand should increment to 2");
        assertEquals(1, shapeCount.getRectangle(), 
                     "Rectangle should remain at 1");
    }

    @Test
    void testLargeIncrementValues() {
        // Test with large number of increments
        for (int i = 0; i < 1000; i++) {
            shapeCount.incrementFreeHand();
        }
        
        assertEquals(1000, shapeCount.getFreeHand(), 
                     "Should handle 1000 increments correctly");
    }

    @Test
    void testGettersDoNotModifyState() {
        shapeCount.incrementFreeHand();
        
        // Call getter multiple times
        int count1 = shapeCount.getFreeHand();
        int count2 = shapeCount.getFreeHand();
        int count3 = shapeCount.getFreeHand();
        
        assertEquals(1, count1);
        assertEquals(count1, count2, "Multiple getter calls should return same value");
        assertEquals(count2, count3, "Multiple getter calls should return same value");
    }

    @Test
    void testMixedOperations() {
        // Simulate real usage pattern
        shapeCount.incrementFreeHand();      // User draws freehand
        shapeCount.incrementRectangle();     // User draws rectangle
        shapeCount.incrementFreeHand();      // User draws another freehand
        shapeCount.incrementEllipse();       // User draws ellipse
        shapeCount.incrementRectangle();     // User draws another rectangle
        shapeCount.incrementTriangle();      // User draws triangle
        
        assertEquals(2, shapeCount.getFreeHand());
        assertEquals(0, shapeCount.getStraightLine());
        assertEquals(2, shapeCount.getRectangle());
        assertEquals(1, shapeCount.getEllipse());
        assertEquals(1, shapeCount.getTriangle());
    }

    @Test
    void testTotalShapeCount() {
        shapeCount.incrementFreeHand();
        shapeCount.incrementStraightLine();
        shapeCount.incrementRectangle();
        shapeCount.incrementEllipse();
        shapeCount.incrementTriangle();
        
        int total = shapeCount.getFreeHand() + 
                    shapeCount.getStraightLine() + 
                    shapeCount.getRectangle() + 
                    shapeCount.getEllipse() + 
                    shapeCount.getTriangle();
        
        assertEquals(5, total, "Total count should be sum of all shapes");
    }

    @Test
    void testZeroAfterConstruction() {
        ShapeCount newCount = new ShapeCount(0, 0, 0, 0, 0);
        
        int total = newCount.getFreeHand() + 
                    newCount.getStraightLine() + 
                    newCount.getRectangle() + 
                    newCount.getEllipse() + 
                    newCount.getTriangle();
        
        assertEquals(0, total, "Newly constructed counter should have 0 total");
    }
}
