package com.swe.ux.model.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link DashboardModel}.
 */
class DashboardModelTest {

    @Test
    void gettersReturnConstructorValues() {
        DashboardModel model = new DashboardModel(42, 7, "Summary");

        assertEquals(42, model.getUsersPresent());
        assertEquals(7, model.getUsersLoggedOut());
        assertEquals("Summary", model.getMeetingSummary());
    }
}

