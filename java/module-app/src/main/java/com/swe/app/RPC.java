package com.swe.app;

import com.socketry.SocketryServer;
import com.swe.app.RPCinterface.AbstractRPC;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class RPC implements AbstractRPC {
    HashMap<String, Function<byte[], byte[]>> methods;

    private SocketryServer socketryServer;
    private static RPC instance = null;

    private RPC() {
        methods = new HashMap<>();
    }

    public static RPC getInstance() {
        if (instance == null) {
            instance = new RPC();
        }
        return instance;
    }

    public void subscribe(String methodName, Function<byte[], byte[]> method) {
        methods.put(methodName, method);
    }

    public Thread connect(int portNumber) throws IOException, InterruptedException, ExecutionException {
        System.out.println("Connecting to port: " + portNumber + " " + methods.keySet());
        socketryServer = new SocketryServer(portNumber, methods);
        Thread rpcThread = new Thread(socketryServer::listenLoop);
        rpcThread.start();
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
