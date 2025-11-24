package com.swe.ux.viewmodels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ShapeViewModel}.
 */
class ShapeViewModelTest {

    private ShapeViewModel viewModel;

    @BeforeEach
    void setUp() {
        viewModel = new ShapeViewModel();
    }

    @Test
    void fetchAndUpdateDataAddsNewSnapshots() {
        viewModel.fetchAndUpdateData();
        viewModel.fetchAndUpdateData();

        assertFalse(viewModel.getAllData().isEmpty());
        assertEquals(viewModel.getAllData().size(), viewModel.getWindowData().size());
    }

    @Test
    void movePreviousAndMoveNextAdjustCurrentWindow() {
        for (int i = 0; i < 4; i++) {
            viewModel.fetchAndUpdateData();
        }

        int startBefore = viewModel.getCurrentStartIndex();
        viewModel.movePrevious();
        int afterPrevious = viewModel.getCurrentStartIndex();
        assertEquals(Math.max(0, startBefore - 1), afterPrevious);

        viewModel.moveNext();
        int maxIndex = Math.max(0, viewModel.getAllData().size() - viewModel.windowSizeProperty().get());
        int expectedAfterNext = Math.min(maxIndex, afterPrevious + 1);
        assertEquals(expectedAfterNext, viewModel.getCurrentStartIndex());
    }
}

