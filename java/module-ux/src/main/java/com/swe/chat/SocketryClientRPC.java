package com.swe.chat;

import com.swe.chat.AbstractRPC; // Using the interface from your repo
import com.socketry.SocketryClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class SocketryClientRPC implements AbstractRPC {
    HashMap<String, Function<byte[], byte[]>> procedures;
    SocketryClient client;

    private static SocketryClientRPC instance;

    public static SocketryClientRPC getInstance() {
        if (instance == null) {
            instance = new SocketryClientRPC();
        }
        return instance;
    }

    private SocketryClientRPC() {
        procedures = new HashMap<>();
    }

    @Override
    public void subscribe(String name, Function<byte[], byte[]> func) {
        System.out.println("[FRONT RPC] Subscribing to: " + name);
        procedures.put(name, func);
    }

    @Override
    public Thread connect() throws IOException, ExecutionException, InterruptedException {
        System.out.println("[FRONT RPC] Connecting to RPC");
        client = new SocketryClient(new byte[] {20},7000, procedures);
        Thread handler = new Thread(client::listenLoop);
        handler.start();
        System.out.println("Connected to RPC");
        return handler;
    }

    @Override
    public CompletableFuture<byte[]> call(String name, byte[] args) {
        byte funcId = client.getRemoteProcedureId(name);
        try{
            return client.makeRemoteCall(funcId, args,0);
        } catch (InterruptedException e){
            throw new RuntimeException(e);
        }
    }
}
