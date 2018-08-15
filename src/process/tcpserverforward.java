package process;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.HashMap;

public class tcpserverforward {
    private user u;
    private HashMap<Long, PipedInputStream> pipein;
    private HashMap<Long, PipedOutputStream> pipeout;

    tcpserverforward(user u, HashMap<Long, PipedInputStream> pipein, HashMap<Long, PipedOutputStream> pipeout) {
        this.u = u;
        this.pipein = pipein;
        this.pipeout = pipeout;
    }

    void connect(String username) {
        pipein.put(Thread.currentThread().getId(), new PipedInputStream());
        pipeout.put(Thread.currentThread().getId(), new PipedOutputStream());
        while (pipein.get(user.getThreadID(username)) == null || pipeout.get(user.getThreadID(username)) == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            pipein.get(Thread.currentThread().getId()).connect(pipeout.get(user.getThreadID(username)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
