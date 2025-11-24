package com.swe.ux.testutil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import com.swe.controller.RPCinterface.AbstractRPC;

/**
 * Mock implementation of AbstractRPC for testing purposes.
 */
public class MockAbstractRPC implements AbstractRPC {

    private final Map<String, Function<byte[], CompletableFuture<byte[]>>> methodHandlers = new HashMap<>();
    private final Map<String, Function<byte[], byte[]>> subscriptions = new HashMap<>();

    @Override
    public void subscribe(final String methodName, final Function<byte[], byte[]> method) {
        subscriptions.put(methodName, method);
    }

    @Override
    public Thread connect(final int portNumber) throws IOException, InterruptedException, ExecutionException {
        return new Thread();
    }

    @Override
    public CompletableFuture<byte[]> call(final String methodName, final byte[] data) {
        final Function<byte[], CompletableFuture<byte[]>> handler = methodHandlers.get(methodName);
        if (handler != null) {
            return handler.apply(data);
        }
        // Default: return empty future
        return CompletableFuture.completedFuture(new byte[0]);
    }

    /**
     * Helper method for tests to register mock responses.
     *
     * @param methodName the method name
     * @param handler the handler function
     */
    public void registerHandler(final String methodName,
                                final Function<byte[], CompletableFuture<byte[]>> handler) {
        methodHandlers.put(methodName, handler);
    }

    /**
     * Helper method for tests to register simple responses.
     *
     * @param methodName the method name
     * @param response the response data
     */
    public void registerResponse(final String methodName, final byte[] response) {
        methodHandlers.put(methodName, data -> CompletableFuture.completedFuture(response));
    }

    /**
     * Helper method for tests to register exceptions.
     *
     * @param methodName the method name
     * @param exception the exception to throw
     */
    public void registerException(final String methodName, final Exception exception) {
        methodHandlers.put(methodName, data -> {
            final CompletableFuture<byte[]> future = new CompletableFuture<>();
            future.completeExceptionally(exception);
            return future;
        });
    }
}

