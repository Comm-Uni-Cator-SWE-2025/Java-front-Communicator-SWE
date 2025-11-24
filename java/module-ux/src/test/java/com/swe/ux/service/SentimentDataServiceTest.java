package com.swe.ux.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.swe.controller.RPCinterface.AbstractRPC;
import com.swe.controller.serialize.DataSerializer;
import com.swe.ux.model.analytics.SentimentPoint;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SentimentDataService}.
 */
class SentimentDataServiceTest {

    private SentimentDataService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new SentimentDataService();
        clearStaticJsonList();
    }

    @Test
    void fetchNextData_returnsDataProvidedByRpc() throws Exception {
        StubRpc rpc = new StubRpc();
        String payload = "[{\"time\":\"2023-11-20T10:00\",\"sentiment\":0.65}]";
        rpc.setResponse(serialize(payload));

        String result = service.fetchNextData(rpc);

        assertEquals(payload, result);
    }

    @Test
    void fetchNextData_returnsEmptyStringWhenRpcReturnsEmptyPayload() throws Exception {
        StubRpc rpc = new StubRpc();
        rpc.setResponse(serialize(""));

        String result = service.fetchNextData(rpc);

        assertEquals("", result);
    }

    @Test
    void parseJson_convertsJsonIntoSentimentPoints() {
        String json = """
                [{"time":"2023-11-20T10:00","sentiment":0.5},
                 {"time":"2023-11-20T10:01","sentiment":-0.2}]
                """;

        List<SentimentPoint> points = service.parseJson(json);

        assertEquals(2, points.size());
        assertEquals("2023-11-20T10:00", points.get(0).getTime());
        assertEquals(0.5, points.get(0).getSentiment());
        assertEquals(-0.2, points.get(1).getSentiment());
    }

    @SuppressWarnings("unchecked")
    private void clearStaticJsonList() throws Exception {
        Field listField = SentimentDataService.class.getDeclaredField("jsonList");
        listField.setAccessible(true);
        List<String> list = (List<String>) listField.get(null);
        list.clear();
    }

    private byte[] serialize(String payload) throws JsonProcessingException {
        return DataSerializer.serialize(payload);
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

