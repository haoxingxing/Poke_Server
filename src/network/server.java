package network;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import process.aes;
import process.log;
import process.md5;
import process.user;
import xml.dom4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

import static xml.dom4j.makeOF;
import static xml.dom4j.xmltostring;

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
        while (socket.isConnected()) {
            try {
                String r = new network(socket).recv();
                Document re = new SAXReader().read(new ByteArrayInputStream(r.getBytes()));
                if (re.getRootElement().element("md5").getText().equals(md5.md5_encode(re.getRootElement().element("data").getText() + re.getRootElement().element("token")))) {
                    if (user.getToken().equals(re.getRootElement().element("token").getText())) {
                        Document encryoted = DocumentHelper.parseText(Objects.requireNonNull(aes.decrypt(re.getRootElement().element("data").getText(), Objects.requireNonNull(md5.md5_encode(re.getRootElement().element("token").getText() + "MakeTokenEnc")))));
                        if (encryoted.getRootElement().element("class").getText().equals("user")) {
                            user.process(encryoted);
                        }
                    } else {
                        Document doc = DocumentHelper.createDocument();
                        Element root = doc.addElement("reply");
                        root.addElement("status").setText("401");
                        root.addElement("message").setText("Unauthorized");
                        new network(socket).send(xmltostring(dom4j.retaesprocess(doc, user.getToken()), makeOF()));
                    }
                } else {
                    try {
                        new network(socket).send(xmltostring(dom4j.retaesprocess(dom4j.retxmlmodel("600", "Lossing Packet", "NULL", "NULL", DocumentHelper.createElement("")), user.getToken()), makeOF()));
                    } catch (IOException e) {
                        break;
                    }
                }
            } catch (IOException e2) {
                e2.printStackTrace();
                Document doc = DocumentHelper.createDocument();
                Element root = doc.addElement("reply");
                root.addElement("status").setText("500");
                root.addElement("message").setText("Server Error");
                try {
                    new network(socket).send(xmltostring(dom4j.retaesprocess(doc, user.getToken()), makeOF()));
                } catch (IOException e) {
                    break;
                }
            } catch (IllegalArgumentException | DocumentException | NullPointerException e3) {
                e3.printStackTrace();
                Document doc = DocumentHelper.createDocument();
                Element root = doc.addElement("reply");
                root.addElement("status").setText("400");
                root.addElement("message").setText("Bad Request");
                try {
                    new network(socket).send(xmltostring(dom4j.retaesprocess(doc, user.getToken()), makeOF()));
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