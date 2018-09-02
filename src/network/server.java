package network;

import dataprocess.json;
import org.json.JSONException;
import org.json.JSONObject;
import process.*;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Objects;

public class server extends Thread {
    private Socket socket;
    private Thread t;
    private String threadName;
    private HashMap<Long, Thread> threadmap;
    private HashMap<Long, Socket> socketsmap;
    private HashMap<String, mode> modemap;

    public server(String name, Socket socketaccept, HashMap<Long, Thread> threadmap, HashMap<Long, Socket> socketsmap, HashMap<String, mode> modemap) {
        threadName = name;
        socket = socketaccept;
        this.socketsmap = socketsmap;
        this.threadmap = threadmap;
        this.modemap = modemap;
        log.printf("ACCEPT CONNECTION:[" + socket.getRemoteSocketAddress().toString() + "]");
    }

    public void run() {
        threadmap.put(Thread.currentThread().getId(), Thread.currentThread());
        socketsmap.put(Thread.currentThread().getId(), socket);
        user user = new user(socket);
        while (socket.isConnected()) {
            try {
                String r = new network(socket).recv();
                JSONObject re = new JSONObject(r);
                if (re.getString("md5").equals(md5.md5_encode(re.getString("data") + re.getString("token")))) {
                    if (user.getToken().equals(re.getString("token"))) {
                        JSONObject encryoted = new JSONObject(Objects.requireNonNull(aes.decrypt(re.getString("data"), Objects.requireNonNull(md5.md5_encode(re.getString("token") + "MakeTokenEnc")))));
                        switch (encryoted.getString("class")) {
                            case "user": {
                                user.process(encryoted);
                                continue;
                            }
                            case "tcpserverforward": {
                                new tcpserverforward(user, modemap, socketsmap).connect(encryoted.getString("object"));
                                continue;
                            }
                            case "matchqueue":
                                new matchqueue(user, encryoted.getJSONObject("parameter").getString("queuename"), Integer.parseInt(encryoted.getJSONObject("parameter").getString("queueall"))).process(encryoted);
                                continue;
                            default:
                                new network(socket).send(json.jsonaesencrypet(json.makejson(new String[]{"status", "message", "class", "func"}, new String[]{"404", "Class Not Find", "main", "server"}), user.getToken()).toString());
                        }
                    } else {
                        new network(socket).send(json.jsonaesencrypet(json.makejson(new String[]{"status", "message", "class", "func"}, new String[]{"401", "Unauthorized", "main", "server"}), user.getToken()).toString());
                    }
                } else {
                    try {
                        new network(socket).send(json.jsonaesencrypet(json.makejson(new String[]{"status", "message", "class", "func"}, new String[]{"600", "Lossing Packet", "main", "server"}), user.getToken()).toString());
                    } catch (IOException e) {
                        break;
                    }
                }
            } catch (IOException e2) {
                e2.printStackTrace();
                e2.printStackTrace();
                try {
                    new network(socket).send(json.jsonaesencrypet(json.makejson(new String[]{"status", "message", "class", "func"}, new String[]{"500", "Server IO Error", "main", "server"}), user.getToken()).toString());
                } catch (IOException e) {
                    break;
                }
            } catch (IllegalArgumentException | JSONException | NullPointerException e3) {
                e3.printStackTrace();
                try {
                    new network(socket).send(json.jsonaesencrypet(json.makejson(new String[]{"status", "message", "class", "func"}, new String[]{"400", "Bad Request", "main", "server"}), user.getToken()).toString());
                } catch (IOException e) {
                    break;
                }
            }
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        user.logout();
        threadmap.remove(Thread.currentThread().getId());
        socketsmap.remove(Thread.currentThread().getId());
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