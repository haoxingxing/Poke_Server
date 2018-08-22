package process;

import dataprocess.json;
import network.network;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

public class tcpserverforward {
    //private HashMap<Long, Thread> threadssmap;
    private HashMap<Long, Socket> socketsmap;
    private HashMap<String, mode> modemap;
    private user u;

    //tcpserverforward(user u, HashMap<Long, Thread> threadsmap, HashMap<String, mode> modemap, HashMap<Long, Socket> socketsmap) {
    public tcpserverforward(user u, HashMap<String, mode> modemap, HashMap<Long, Socket> socketsmap) {
        this.modemap = modemap;
        this.socketsmap = socketsmap;
        this.u = u;
    }

    public void connect(String username) {
        mode m = new mode();
        m.tcpserverforwarding = true;
        m.tcpserverforwardingobject = username;
        m.tcpserverforwardthreadid = Thread.currentThread().getId();
        modemap.put(u.username, m);
        while (!modemap.containsKey(username)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                try {
                    new network(socketsmap.get(Thread.currentThread().getId())).send(json.jsonaesencrypet(json.makejson(new String[]{"status", "message", "class", "func"}, new String[]{"500", "Server Error", "tcpserverforward", "connect"}), u.getToken()).toString());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                return;
            }
        }

        while (!modemap.get(username).tcpserverforwarding) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                try {
                    new network(socketsmap.get(Thread.currentThread().getId())).send(json.jsonaesencrypet(json.makejson(new String[]{"status", "message", "class", "func"}, new String[]{"500", "Server Error", "tcpserverforward", "connect"}), u.getToken()).toString());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                return;
            }
        }
        if (!modemap.get(username).tcpserverforwardingobject.equals(u.username)) {
            try {
                new network(socketsmap.get(Thread.currentThread().getId())).send(json.jsonaesencrypet(json.makejson(new String[]{"status", "message", "class", "func"}, new String[]{"400", "The object doesn't request for you", "tcpserverforward", "connect"}), u.getToken()).toString());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return;
        }
        Socket a = socketsmap.get(Thread.currentThread().getId());
        Socket b = socketsmap.get(modemap.get(username).tcpserverforwardthreadid);
        try {
            new network(socketsmap.get(Thread.currentThread().getId())).send(json.jsonaesencrypet(json.makejson(new String[]{"status", "message", "class", "func"}, new String[]{"200", "P2P CONNECTED", "tcpserverforward", "connect"}), u.getToken()).toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                new network(a).send(new network(b).recv());
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    new network(socketsmap.get(Thread.currentThread().getId())).send(json.jsonaesencrypet(json.makejson(new String[]{"status", "message", "class", "func"}, new String[]{"200", "Socket Closed", "tcpserverforward", "datatransloop"}), u.getToken()).toString());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                break;
            }
        }
        mode bc = new mode();
        bc.tcpserverforwarding = false;
        bc.tcpserverforwardingobject = null;
        bc.tcpserverforwardthreadid = null;
        modemap.put(u.username, bc);
    }
}
