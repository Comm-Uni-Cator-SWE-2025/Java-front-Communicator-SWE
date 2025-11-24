package com.swe.controller;

import com.socketry.SocketryServer;
import com.swe.controller.RPCinterface.AbstractRPC;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class RPC implements AbstractRPC {
    HashMap<String, Function<byte[], byte[]>> methods;

    private SocketryServer socketryServer;
    private boolean isConnected = false;

    public RPC() {
        methods = new HashMap<>();
    }

    public void subscribe(String methodName, Function<byte[], byte[]> method) {
        // if (isConnected) {
        //     throw new RuntimeException("Already connected to a port");
        // }
        methods.put(methodName, method);
    }

    public Thread connect(int portNumber) throws IOException, InterruptedException, ExecutionException {
        if (isConnected) {
            return null;
        }
        System.out.println("Connecting to port: " + portNumber + " " + methods.keySet());
        socketryServer = new SocketryServer(portNumber, methods);
        Thread rpcThread = new Thread(socketryServer::listenLoop);
        rpcThread.start();
        isConnected = true;
        return rpcThread;
    }

    public CompletableFuture<byte[]> call(String methodName, byte[] data) {
        System.out.println("Calling method: " + methodName);
        byte methodId = socketryServer.getRemoteProcedureId(methodName);
        System.out.println("Method ID: " + methodId);
        try {
            return socketryServer.makeRemoteCall(methodId, data, 0);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
