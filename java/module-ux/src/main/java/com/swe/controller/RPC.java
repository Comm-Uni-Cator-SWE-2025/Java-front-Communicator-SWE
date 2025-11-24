package com.swe.core;

import com.socketry.SocketryClient;
import com.swe.core.RPCinterface.AbstractRPC;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/**
 * Remote Procedure Call implementation for client-side communication.
 * Uses {@link SocketryClient} to perform remote calls and handle subscriptions.
 */
public class RPC implements AbstractRPC {

    /**
     * Module code for chat features.
     */
    private static final byte MODULE_CHAT = 1;

    /**
     * Module code for networking features.
     */
    private static final byte MODULE_NETWORKING = 2;

    /**
     * Module code for screen sharing features.
     */
    private static final byte MODULE_SCREENSHARING = 5;

    /**
     * Module code for canvas features.
     */
    private static final byte MODULE_CANVAS = 2;

    /**
     * Module code for controller features.
     */
    private static final byte MODULE_CONTROLLER = 1;

    /**
     * Module code for miscellaneous features.
     */
    private static final byte MODULE_MISC = 1;

    /**
     * Registered RPC methods mapped by method name.
     */
    private final HashMap<String, Function<byte[], byte[]>> methods;

    /**
     * Underlying Socketry client for communication.
     */
    private SocketryClient socketryClient;

    /**
     * Indicates whether this RPC client is currently connected.
     */
    private Boolean isConnected = false;

    /**
     * Creates an instance of {@link RPC} with an empty method registry.
     */
    public RPC() {
        this.methods = new HashMap<>();
    }

    /**
     * Returns the connection status of this RPC client.
     *
     * @return {@code true} if connected, {@code false} otherwise
     */
    public Boolean isConnected() {
        return isConnected;
    }

    /**
     * Subscribes a local method implementation to a given method name.
     *
     * @param methodName the remote method name
     * @param method     the implementation that handles the call
     */
    @Override
    public void subscribe(final String methodName, final Function<byte[], byte[]> method) {
        methods.put(methodName, method);
    }

    /**
     * Connects to the remote server using the given port number and starts the listen loop
     * in a separate thread.
     *
     * @param portNumber the port number of the remote server
     * @return the thread that runs the listen loop
     * @throws IOException          if the connection fails
     * @throws InterruptedException if the thread is interrupted
     * @throws ExecutionException   if an asynchronous task fails
     */
    public Thread connect(final int portNumber)
            throws IOException, InterruptedException, ExecutionException {

        final byte[] moduleCodes = new byte[] {
            MODULE_CHAT,         // Chat
            MODULE_NETWORKING,   // Networking
            MODULE_SCREENSHARING,// Screensharing
            MODULE_CANVAS,       // Canvas
            MODULE_CONTROLLER,   // Controller
            MODULE_MISC          // Misc
        };

        socketryClient = new SocketryClient(moduleCodes, portNumber, methods);

        final Thread rpcThread = new Thread(socketryClient::listenLoop);
        rpcThread.start();
        isConnected = true;
        return rpcThread;
    }

    /**
     * Invokes a remote procedure by name with the given payload.
     *
     * @param methodName the name of the remote method
     * @param data       the payload to send
     * @return a {@link CompletableFuture} that completes with the response bytes
     */
    @Override
    public CompletableFuture<byte[]> call(final String methodName, final byte[] data) {
        if (socketryClient == null) {
            System.err.println("Server is null");
            return CompletableFuture.supplyAsync(() -> new byte[0]);
        }

        final byte methodId = socketryClient.getRemoteProcedureId(methodName);
        try {
            return socketryClient.makeRemoteCall(methodId, data, 0);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
