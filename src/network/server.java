package network;

import java.net.Socket;

public class server extends Thread {
    Socket socket;
    private Thread t;
    private String threadName;

    server(String name, Socket socketaccept) {
        threadName = name;
        socket = socketaccept;
    }

    public void run() {

    }

    public void start() {
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
    }
}