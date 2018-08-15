import network.server;
import process.log;

import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class launch {
    public static void main(String[] args) {
        //------------------------<INIT>---------------------------
        File userdir = new File("data/users");
        userdir.mkdirs();
        File queuesdir = new File("data/queues/");
        queuesdir.mkdirs();
        //-----------------------</INIT>---------------------------
        HashMap<Long, Thread> threadmap = new HashMap<>();
        HashMap<Integer, Socket> socketsmap = new HashMap<>();
        HashMap<Long, PipedInputStream> pipeinmap = new HashMap<>();
        HashMap<Long, PipedOutputStream> pipeoutmap = new HashMap<>();
        AtomicBoolean isquit = new AtomicBoolean(false);
        while (!isquit.get()) try {
            ServerSocket serverSocket = new ServerSocket(8864);
            log.printf("ServerPort:" + 8864);
            serverSocket.setSoTimeout(0);
            Socket stocs = serverSocket.accept();
            if (stocs.isConnected()) {
                server s = new server(stocs.getLocalPort() + "", stocs, threadmap, socketsmap, pipeinmap, pipeoutmap);
                s.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            isquit.set(true);
        }
    }
}