package com.conferencing;

import com.socketry.SocketryClient;
import com.socketry.SocketryServer;
import com.conferencing.AbstractRPC;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class DummyRPC implements AbstractRPC {
    HashMap<String, Function<byte[],byte[]>> procedures;
    SocketryClient server;

    public DummyRPC() {
        procedures = new HashMap<>();
    }

    @Override
    public void subscribe(String name, Function<byte[],byte[]> func) {
        procedures.put(name, func);
    }

    @Override
    public Thread connect() throws IOException, ExecutionException, InterruptedException {
        server = new SocketryClient(new byte[] {5}, 60000 ,procedures);
        Thread handler = new Thread(server::listenLoop);
        handler.start();
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
