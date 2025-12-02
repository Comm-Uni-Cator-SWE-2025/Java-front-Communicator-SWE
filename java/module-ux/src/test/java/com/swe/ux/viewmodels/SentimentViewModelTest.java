package com.swe.ux.viewmodels;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.swe.controller.RPCinterface.AbstractRPC;
import com.swe.controller.serialize.DataSerializer;
import com.swe.ux.model.analytics.SentimentPoint;
import com.swe.ux.service.SentimentDataService;

/**
 * Unit tests for {@link SentimentViewModel}.
 */
class SentimentViewModelTest {

    private SentimentViewModel viewModel;
    private StubRpc rpc;

    @BeforeEach
    void setUp() throws Exception {
        clearSentimentBuffer();
        rpc = new StubRpc();
        viewModel = new SentimentViewModel(rpc);
    }

    @Test
    void fetchAndUpdateDataPopulatesObservableLists() throws Exception {
        String payload = "[{\"time\":\"2023-11-20T10:00\",\"sentiment\":0.5}]";
        rpc.setResponse(serialize(payload));

        viewModel.fetchAndUpdateData();

        List<SentimentPoint> points = viewModel.getAllData();
        assertEquals(1, points.size());
        assertEquals("2023-11-20T10:00", points.get(0).getTime());
        assertEquals(0.5, points.get(0).getSentiment());
        assertEquals(0, viewModel.getCurrentStartIndex());
        assertEquals(0, viewModel.lowerBoundProperty().get());
        assertEquals(viewModel.windowSizeProperty().get() - 1, viewModel.upperBoundProperty().get());
    }

    @Test
    void moveNextAndMovePreviousAdjustWindow() throws Exception {
        String payload = buildPayload(12);
        rpc.setResponse(serialize(payload));

        viewModel.windowSizeProperty().set(6);
        viewModel.fetchAndUpdateData();
        assertTrue(viewModel.getAllData().size() >= 12);

        viewModel.autoModeProperty().set(false);
        viewModel.currentStartIndexProperty().set(0);

        viewModel.moveNext();
        assertEquals(viewModel.windowSizeProperty().get() / 2, viewModel.getCurrentStartIndex());

        viewModel.movePrevious();
        assertEquals(0, viewModel.getCurrentStartIndex());
    }

    @Test
    void moveWindowToProvidesDraggableWindow() throws Exception {
        String payload = buildPayload(15);
        rpc.setResponse(serialize(payload));

        viewModel.windowSizeProperty().set(5);
        viewModel.fetchAndUpdateData();

        viewModel.moveWindowTo(4);
        assertEquals(4, viewModel.getCurrentStartIndex());
        assertFalse(viewModel.autoModeProperty().get());

        final int maxStart = viewModel.getMaxStartIndex();
        viewModel.moveWindowTo(100);
        assertEquals(maxStart, viewModel.getCurrentStartIndex());

        viewModel.moveWindowTo(-5);
        assertEquals(0, viewModel.getCurrentStartIndex());
    }

    private String buildPayload(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> String.format("{\"time\":\"T%02d\",\"sentiment\":%s}", i, i * 0.1))
                .collect(Collectors.joining(",", "[", "]"));
    }

    private byte[] serialize(String payload) throws JsonProcessingException {
        return DataSerializer.serialize(payload);
    }

    @SuppressWarnings("unchecked")
    private void clearSentimentBuffer() throws Exception {
        Field field = SentimentDataService.class.getDeclaredField("JSON_LIST");
        field.setAccessible(true);
        ((List<String>) field.get(null)).clear();
    }

    private static final class StubRpc implements AbstractRPC {
        private byte[] response = new byte[0];

        void setResponse(byte[] response) {
            this.response = response;
        }

        @Override
        public void subscribe(String methodName, Function<byte[], byte[]> method) {
            // no-op
        }

        @Override
        public Thread connect(int portNumber) {
            return new Thread();
        }

        @Override
        public CompletableFuture<byte[]> call(String methodName, byte[] data) {
            return CompletableFuture.completedFuture(response);
        }
    }
}
