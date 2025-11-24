package com.swe.ux.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.swe.controller.RPCinterface.AbstractRPC;
import com.swe.controller.serialize.DataSerializer;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link MessageDataService}.
 */
class MessageDataServiceTest {

    private MessageDataService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new MessageDataService();
        clearStaticJsonList();
    }

    @Test
    void fetchNextData_returnsMessagesFromRpc() throws Exception {
        StubRpc rpc = new StubRpc();
        String payload = "[\"Task assigned\",\"Review pending\"]";
        rpc.setResponse(serialize(payload));

        String result = service.fetchNextData(rpc);

        assertEquals(payload, result);
    }

    @Test
    void parseJson_convertsPayloadToMessageList() {
        String json = "[\"First\",\"Second\",\"Third\"]";

        List<String> messages = service.parseJson(json);

        assertEquals(3, messages.size());
        assertEquals("First", messages.get(0));
        assertEquals("Third", messages.get(2));
    }

    @Test
    void parseJson_returnsEmptyListForInvalidPayload() {
        List<String> messages = service.parseJson("invalid");

        assertTrue(messages.isEmpty());
    }

    @SuppressWarnings("unchecked")
    private void clearStaticJsonList() throws Exception {
        Field listField = MessageDataService.class.getDeclaredField("jsonList");
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

