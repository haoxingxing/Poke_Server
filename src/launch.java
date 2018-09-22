import network.server;
import process.log;
import process.mode;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class launch {
    public static void main(String[] args) {
        //------------------------  <INIT>  ---------------------------
        File userdir = new File("data/users");
        userdir.mkdirs();
        File queuesdir = new File("data/queues/");
        queuesdir.mkdirs();
        //-----------------------  </INIT>  ---------------------------
        //-----------------------  <MAPS>   ----------------------------
        HashMap<Long, Thread> threadmap = new HashMap<>();
        HashMap<Long, Socket> socketsmap = new HashMap<>();
        HashMap<Long, mode> modemap = new HashMap<>();
        //-----------------------  </MAPS>  -----------------------------

        AtomicBoolean isquit = new AtomicBoolean(false);
        try {
            ServerSocket serverSocket = new ServerSocket(8864);
            log.printf("ServerPort:" + 8864, Thread.currentThread().getStackTrace()[1].getFileName(), Thread.currentThread().getStackTrace()[1].getLineNumber());
            while (!isquit.get()) try {
                serverSocket.setSoTimeout(0);
                Socket stocs = serverSocket.accept();
                if (stocs.isConnected()) {
                    server s = new server(stocs.getLocalPort() + "", stocs, threadmap, socketsmap, modemap);
                    s.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
                isquit.set(true);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        log.printf("Server Stopped", Thread.currentThread().getStackTrace()[1].getFileName(), Thread.currentThread().getStackTrace()[1].getLineNumber());
    }
}