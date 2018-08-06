package network;

import process.base64;

import java.io.*;
import java.net.Socket;

public class network {
    private Socket socket;
    private BufferedWriter send;
    private BufferedReader recv;

    public network(Socket socket) {
        this.socket = socket;
        try {
            recv = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            send = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(String data) throws IOException {
        send.write(base64.encode(data));
        send.write("\r\n");
        send.flush();
    }

    public String recv() throws IOException {
        String y = recv.readLine();
        return base64.decode(y);
    }
}
