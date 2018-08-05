package network;

import process.log;
import process.user;

import java.io.IOException;
import java.net.Socket;
public class server extends Thread {
    private Socket socket;
    private Thread t;
    private String threadName;

    public server(String name, Socket socketaccept) {
        threadName = name;
        socket = socketaccept;
        log.printf("ACCEPT CONNECTION:[" + socket.getRemoteSocketAddress().toString() + "]");
    }

    public void run() {
        user user = new user(socket);
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.printf("CLOSED CONNECTION:[" + socket.getRemoteSocketAddress().toString() + "]");
        this.interrupt();
    }
    public void start() {
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
    }
}