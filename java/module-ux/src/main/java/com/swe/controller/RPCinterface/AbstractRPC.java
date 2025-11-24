package com.swe.controller.RPCinterface;

/*
 * Contributed by Pushti Vasoya.
 */

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/**
 * Interface for remote procedure call functionality.
 */
public interface AbstractRPC {

    /**
     * Subscribes a method to the RPC.
     *
     * @param methodName the method name
     * @param method the method function
     */
    void subscribe(String methodName, Function<byte[], byte[]> method);

    /**
     * Connects to the RPC server.
     *
     * @param portNumber the port number
     * @return the connection thread
     * @throws IOException if connection fails
     * @throws InterruptedException if interrupted
     * @throws ExecutionException if execution fails
     */
    Thread connect(int portNumber)
            throws IOException, InterruptedException, ExecutionException;

    /**
     * Makes a remote procedure call.
     *
     * @param methodName the method name
     * @param data the data to send
     * @return a future with the response
     */
    CompletableFuture<byte[]> call(String methodName, byte[] data);
}
