package com.swe.canvas.datamodel.collaboration;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.List;

import com.swe.canvas.datamodel.manager.ActionManager;
import com.swe.canvas.datamodel.action.ActionFactory;
import com.swe.canvas.datamodel.canvas.CanvasState;
import com.swe.canvas.datamodel.manager.UndoRedoManager;
import org.junit.jupiter.api.Test;

/**
 * Tests NetworkSimulator behavior using reflection to set internal references.
 * Uses small hand-written `SimpleActionManager` instances instead of Mockito.
 */
class NetworkSimulatorReflectionTest {

    private static final class SimpleActionManager implements ActionManager {
        NetworkMessage lastMessage;

        @Override
        public void initialize() {
            // no-op
        }

        @Override
        public com.swe.canvas.datamodel.action.ActionFactory getActionFactory() {
            return new ActionFactory();
        }

        @Override
        public CanvasState getCanvasState() {
            return new CanvasState();
        }

        @Override
        public UndoRedoManager getUndoRedoManager() {
            return new UndoRedoManager();
        }

        @Override
        public void setOnUpdate(final Runnable callback) {
            // no-op
        }

        @Override
        public void requestCreate(final com.swe.canvas.datamodel.shape.Shape newShape) {
            // no-op
        }

        @Override
        public void requestModify(final com.swe.canvas.datamodel.canvas.ShapeState prevState,
                                  final com.swe.canvas.datamodel.shape.Shape modifiedShape) {
            // no-op
        }

        @Override
        public void requestDelete(final com.swe.canvas.datamodel.canvas.ShapeState shapeToDelete) {
            // no-op
        }

        @Override
        public void requestUndo() {
            // no-op
        }

        @Override
        public void requestRedo() {
            // no-op
        }

        @Override
        public String saveMap() {
            return "{}";
        }

        @Override
        public void restoreMap(final String json) {
            // no-op
        }

        @Override
        public void processIncomingMessage(final NetworkMessage message) {
            this.lastMessage = message;
        }

        @Override
        public byte[] handleUpdate(final byte[] data) {
            return new byte[0];
        }

        @Override
        public void handleUserJoined(final String userId) {
            // no-op
        }
    }

    @Test
    void sendMessageToHost_callsHost_whenHostSetViaReflection() throws Exception {
        final NetworkSimulator sim = new NetworkSimulator();
        final SimpleActionManager host = new SimpleActionManager();
        final SimpleActionManager client = new SimpleActionManager();

        // Set private hostManager
        final Field hostField = NetworkSimulator.class.getDeclaredField("hostManager");
        hostField.setAccessible(true);
        hostField.set(sim, host);

        // Add a client into private clientManagers list
        final Field clientsField = NetworkSimulator.class.getDeclaredField("clientManagers");
        clientsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        final List<ActionManager> clients = (List<ActionManager>) clientsField.get(sim);
        clients.add(client);

        final NetworkMessage msg = new NetworkMessage(MessageType.NORMAL, new byte[]{9});

        sim.sendMessageToHost(msg);

        assertNotNull(host.lastMessage);
        assertArrayEquals(msg.getSerializedAction(), host.lastMessage.getSerializedAction());
        assertNull(client.lastMessage);
    }

    @Test
    void broadcastMessage_callsAllClients() throws Exception {
        final NetworkSimulator sim = new NetworkSimulator();
        final SimpleActionManager c1 = new SimpleActionManager();
        final SimpleActionManager c2 = new SimpleActionManager();

        final Field clientsField = NetworkSimulator.class.getDeclaredField("clientManagers");
        clientsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        final List<ActionManager> clients = (List<ActionManager>) clientsField.get(sim);
        clients.add(c1);
        clients.add(c2);

        final NetworkMessage msg = new NetworkMessage(MessageType.REDO, new byte[]{2});

        sim.broadcastMessage(msg);

        assertNotNull(c1.lastMessage);
        assertNotNull(c2.lastMessage);
        assertArrayEquals(msg.getSerializedAction(), c1.lastMessage.getSerializedAction());
        assertArrayEquals(msg.getSerializedAction(), c2.lastMessage.getSerializedAction());
    }
}
