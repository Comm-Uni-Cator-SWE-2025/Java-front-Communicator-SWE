package com.swe.canvas.datamodel.manager;

import com.swe.canvas.datamodel.serialization.SerializedAction;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A thread-safe queue for network messages.
 *
 * <p>This class wraps a {@link BlockingQueue} to provide a simple, thread-safe
 * FIFO mechanism for passing {@link SerializedAction} objects between the
 * network layer and the {@link ActionManager}.
 * </p>
 *
 * <p>This decouples the network I/O threads from the action processing thread.
 * </p>
 *
 * <p><b>Thread Safety:</b> This class is thread-safe.
 * It is internally backed by {@link LinkedBlockingQueue}.
 * </p>
 *
 * @author Gajjala Bhavani Shankar
 
 
 */
public class MessageQueue {

    /**
     * The underlying thread-safe queue.
     */
    private final BlockingQueue<SerializedAction> queue;

    /**
     * Constructs a new MessageQueue with default (unbounded) capacity.
     */
    public MessageQueue() {
        this.queue = new LinkedBlockingQueue<>();
    }

    /**
     * Adds a serialized action to the end of the queue.
     *
     * <p>This method is non-blocking and thread-safe.
     * </p>
     *
     * @param action The action to add.
     * @throws InterruptedException if the thread is interrupted while waiting
     * (only if a bounded queue is used and full).
     */
    public void post(SerializedAction action) throws InterruptedException {
        queue.put(action);
    }

    /**
     * Retrieves and removes the head of the queue, waiting if necessary.
     *
     * <p>This method is blocking and thread-safe. It will wait until an
     * element becomes available.
     * </p>
     *
     * @return The {@link SerializedAction} from the front of the queue.
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    public SerializedAction take() throws InterruptedException {
        return queue.take();
    }

    /**
     * @return The current number of messages in the queue.
     */
    public int size() {
        return queue.size();
    }
}