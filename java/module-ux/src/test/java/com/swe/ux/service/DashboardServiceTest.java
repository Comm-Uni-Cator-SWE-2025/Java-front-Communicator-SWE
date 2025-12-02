package com.swe.ux.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.swe.cloud.datastructures.CloudResponse;
import com.swe.cloud.datastructures.Entity;
import com.swe.ux.model.analytics.DashboardModel;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link DashboardService}.
 */
class DashboardServiceTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void fetchDashboardData_parsesCloudPayload() {
        final ObjectNode dataNode = MAPPER.createObjectNode()
                .put("usersPresent", 42)
                .put("usersLoggedOut", 8)
                .put("meetingSummary", "Sprint review covered backlog grooming.");
        final ObjectNode wrapper = MAPPER.createObjectNode().set("data", dataNode);
        final ArrayNode responseArray = MAPPER.createArrayNode().add(wrapper);

        final Function<Entity, CompletableFuture<CloudResponse>> fetcher =
                entity -> CompletableFuture.completedFuture(new CloudResponse(200, "ok", responseArray));

        final DashboardService service = new DashboardService(fetcher);
        final DashboardModel model = service.fetchDashboardData("demo@example.com").join();

        assertNotNull(model);
        assertEquals(42, model.getUsersPresent());
        assertEquals(8, model.getUsersLoggedOut());
        assertEquals("Sprint review covered backlog grooming.", model.getMeetingSummary());
        assertEquals(responseArray.toPrettyString(), model.getRawPayload());
    }

    @Test
    void fetchDashboardData_returnsDefaultsWhenCloudFails() {
        final Function<Entity, CompletableFuture<CloudResponse>> fetcher =
                entity -> CompletableFuture.failedFuture(new RuntimeException("boom"));

        final DashboardService service = new DashboardService(fetcher);
        final DashboardModel model = service.fetchDashboardData("demo@example.com").join();

        assertNotNull(model);
        assertEquals(0, model.getUsersPresent());
        assertEquals(0, model.getUsersLoggedOut());
        assertEquals("We will surface historical insights once your meetings generate activity.",
                model.getMeetingSummary());
        assertEquals("No cloud data returned yet.", model.getRawPayload());
    }
}
