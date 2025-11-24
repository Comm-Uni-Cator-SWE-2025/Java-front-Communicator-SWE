/**
 * 
 */

package com.swe.canvas.datamodel.collaboration;

import com.swe.controller.RPCinterface.AbstractRPC;
import com.swe.networking.ClientNode;
import com.swe.networking.NetworkFront;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CanvasNetworkService ensuring RPC calls are invoked.
 *
 * These tests use a small hand-written `FakeRPC` instead of Mockito.
 */
class CanvasNetworkServiceTest {

    private static final class FakeRPC implements AbstractRPC {
        final List<String> calledMethods = new ArrayList<>();
        final List<byte[]> calledData = new ArrayList<>();

        @Override
        public void subscribe(final String methodName, final java.util.function.Function<byte[], byte[]> method) {
            // no-op for tests
        }

        @Override
        public Thread connect(final int portNumber) {
            throw new UnsupportedOperationException("connect not used in tests");
        }

        @Override
        public CompletableFuture<byte[]> call(final String methodName, final byte[] data) {
            calledMethods.add(methodName);
            calledData.add(data == null ? null : data.clone());
            return CompletableFuture.completedFuture(new byte[0]);
        }
    }

    @Test
    void sendMessageToHost_doesNotCallRpc_whenHostUnknown() {
        final FakeRPC rpc = new FakeRPC();
        final NetworkFront network = null; // not used by CanvasNetworkService for this test

        final CanvasNetworkService svc = new CanvasNetworkService(rpc, network);

        final NetworkMessage msg = new NetworkMessage(MessageType.NORMAL, new byte[]{1});
        svc.sendMessageToHost(msg);

        assertEquals(0, rpc.calledMethods.size(), "RPC should not be called when hostNode is unknown");
    }

    @Test
    void sendMessageToHost_callsRpc_whenHostKnown() throws Exception {
        final FakeRPC rpc = new FakeRPC();
        final NetworkFront network = null;
        final CanvasNetworkService svc = new CanvasNetworkService(rpc, network);

        // set private hostNode field using reflection
        final Field hostField = CanvasNetworkService.class.getDeclaredField("hostNode");
        hostField.setAccessible(true);
        hostField.set(svc, new ClientNode("127.0.0.1", 9999));

        final NetworkMessage msg = new NetworkMessage(MessageType.NORMAL, new byte[]{5, 6, 7});

        svc.sendMessageToHost(msg);

        assertEquals(1, rpc.calledMethods.size(), "RPC.call should be invoked once");
        assertEquals("canvas:sendToHost", rpc.calledMethods.get(0));

        final byte[] sent = rpc.calledData.get(0);
        assertNotNull(sent);
        final String sentStr = new String(sent);
        assertTrue(sentStr.contains("\"type\":\"NORMAL\""));
    }

    @Test
    void broadcastMessage_callsRpc() {
        final FakeRPC rpc = new FakeRPC();
        final NetworkFront network = null;
        final CanvasNetworkService svc = new CanvasNetworkService(rpc, network);

        final NetworkMessage msg = new NetworkMessage(MessageType.REDO, new byte[]{9});
        svc.broadcastMessage(msg);

        assertEquals(1, rpc.calledMethods.size());
        assertEquals("canvas:broadcast", rpc.calledMethods.get(0));

        final byte[] sent = rpc.calledData.get(0);
        assertNotNull(sent);
        final String sentStr = new String(sent);
        assertTrue(sentStr.contains("\"type\":\"REDO\""));
    }
}
