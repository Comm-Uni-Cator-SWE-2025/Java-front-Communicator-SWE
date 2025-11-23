package com.swe.ux.testutil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import com.swe.app.RPCinterface.AbstractRPC;

/**
 * Mock implementation of AbstractRPC for testing purposes.
 */
public class MockAbstractRPC implements AbstractRPC {

    private final Map<String, Function<byte[], CompletableFuture<byte[]>>> methodHandlers = new HashMap<>();
    private final Map<String, Function<byte[], byte[]>> subscriptions = new HashMap<>();

    @Override
    public void subscribe(String methodName, Function<byte[], byte[]> method) {
        subscriptions.put(methodName, method);
    }

    @Override
    public Thread connect(int portNumber) throws IOException, InterruptedException, ExecutionException {
        return new Thread();
    }

    @Override
    public CompletableFuture<byte[]> call(String methodName, byte[] data) {
        Function<byte[], CompletableFuture<byte[]>> handler = methodHandlers.get(methodName);
        if (handler != null) {
            return handler.apply(data);
        }
        // Default: return empty future
        return CompletableFuture.completedFuture(new byte[0]);
    }

    // Helper method for tests to register mock responses
    public void registerHandler(String methodName, Function<byte[], CompletableFuture<byte[]>> handler) {
        methodHandlers.put(methodName, handler);
    }

    // Helper method for tests to register simple responses
    public void registerResponse(String methodName, byte[] response) {
        methodHandlers.put(methodName, data -> CompletableFuture.completedFuture(response));
    }

    // Helper method for tests to register exceptions
    public void registerException(String methodName, Exception exception) {
        methodHandlers.put(methodName, data -> {
            CompletableFuture<byte[]> future = new CompletableFuture<>();
            future.completeExceptionally(exception);
            return future;
        });
    }
}

