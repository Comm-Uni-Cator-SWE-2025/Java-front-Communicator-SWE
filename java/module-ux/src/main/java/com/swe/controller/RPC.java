package com.swe.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import com.socketry.SocketryServer;
import com.swe.controller.RPCinterface.AbstractRPC;

/**
 * Remote Procedure Call implementation using Socketry.
 */
public class RPC implements AbstractRPC {

    /** Map of method names to their implementations. */
    private final HashMap<String, Function<byte[], byte[]>> methods;

    /** The Socketry server instance. */
    private SocketryServer socketryServer;

    /**
     * Creates a new RPC instance.
     */
    public RPC() {
        methods = new HashMap<>();
    }

    private static RPC instance = null;
    public static RPC getInstance() {
        if (instance == null) {
            instance = new RPC();
        }
        return instance;
    }

    /**
     * Subscribes a method to the RPC server.
     *
     * @param methodName the name of the method
     * @param method the method implementation
     */
    @Override
    public void subscribe(final String methodName,
                          final Function<byte[], byte[]> method) {
        methods.put(methodName, method);
    }

    /**
     * Connects to the RPC server on the specified port.
     *
     * @param portNumber the port number to connect to
     * @return the server thread
     * @throws IOException if connection fails
     * @throws InterruptedException if interrupted
     * @throws ExecutionException if execution fails
     */
    @Override
    public Thread connect(final int portNumber)
            throws IOException, InterruptedException, ExecutionException {
        System.out.println("Connecting to port: " + portNumber + " "
                + methods.keySet());
        socketryServer = new SocketryServer(portNumber, methods);
        final Thread rpcThread = new Thread(socketryServer::listenLoop);
        rpcThread.start();
        return rpcThread;
    }

    /**
     * Makes a remote procedure call.
     *
     * @param methodName the name of the method to call
     * @param data the data to send
     * @return a future containing the response
     */
    @Override
    public CompletableFuture<byte[]> call(final String methodName,
                                          final byte[] data) {
        System.out.println("Calling method: " + methodName);
        final byte methodId = socketryServer.getRemoteProcedureId(methodName);
        System.out.println("Method ID: " + methodId);
        try {
            return socketryServer.makeRemoteCall(methodId, data, 0);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
