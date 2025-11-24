package com.swe.ux.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.swe.ux.model.analytics.DashboardModel;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link DashboardService}.
 */
class DashboardServiceTest {

    private final DashboardService dashboardService = new DashboardService();

    @Test
    void fetchDashboardData_returnsExpectedHardcodedModel() {
        DashboardModel model = dashboardService.fetchDashboardData();

        assertNotNull(model);
        assertEquals(120, model.getUsersPresent());
        assertEquals(15, model.getUsersLoggedOut());
        assertEquals("The previous meeting was very productive and tasks were assigned.",
                model.getMeetingSummary());
    }
}

