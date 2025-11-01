package com.swe.screenNVideo;

import com.socketry.SocketryClient;
import com.swe.screenNVideo.AbstractRPC;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class DummyRPC implements AbstractRPC {
    HashMap<String, Function<byte[],byte[]>> procedures;
    SocketryClient server;

    private static DummyRPC instance;

    public static DummyRPC getInstance() {
        if (instance == null) {
            instance = new DummyRPC();
        }
        return instance;
    }

    private DummyRPC() {
        procedures = new HashMap<>();
    }

    @Override
    public void subscribe(String name, Function<byte[],byte[]> func) {
        System.out.println("Subscribed to " + name);
        procedures.put(name, func);
    }

    @Override
    public Thread connect() throws IOException, ExecutionException, InterruptedException {
        System.out.println("Connectiong to RPC");
        server = new SocketryClient(new byte[] {20}, 60000 ,procedures);
        Thread handler = new Thread(server::listenLoop);
        handler.start();
        System.out.println("Connected to RPC");
        return handler;
    }

    @Override
    public CompletableFuture<byte[]> call(String name, byte[] args) {
        byte funcId = server.getRemoteProcedureId(name);
        try {
            return server.makeRemoteCall(funcId, args,0 );
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
