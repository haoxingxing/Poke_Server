package process;

import dataprocess.dom4j;
import dataprocess.json;
import network.network;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

public class user {
    String username;
    public Socket socket;
    private String token;
    private boolean islogin = false;
    public user(Socket socket) {
        this.socket = socket;
    }
    public void process(JSONObject doc) throws IOException {
        switch (doc.getString("func")) {
            case "login":
                if (this.login(doc.getJSONObject("parameter").getString("username"), doc.getJSONObject("parameter").getString("password"))) {
                    new network(socket).send(json.jsonaesencrypet(json.jsonaddjson(json.makejson(new String[]{"status", "message", "class", "func"}, new String[]{"200", "Successful Logged", "user", "login"}), "parameter", json.makejson(new String[]{"token"}, new String[]{this.getToken()})), "NULL").toString());
                } else {
                    new network(socket).send(json.jsonaesencrypet(json.jsonaddjson(json.makejson(new String[]{"status", "message", "class", "func"}, new String[]{"405", "Username or Password Error", "user", "login"}), "parameter", json.makejson(new String[]{"token"}, new String[]{this.getToken()})), this.getToken()).toString());
                }
                break;
            case "register":
                if (this.register(doc.getJSONObject("parameter").getString("username"), doc.getJSONObject("parameter").getString("password"))) {
                    new network(socket).send(json.jsonaesencrypet(json.jsonaddjson(json.makejson(new String[]{"status", "message", "class", "func"}, new String[]{"200", "Successful Registered", "user", "register"}), "parameter", json.makejson(new String[]{"token"}, new String[]{this.getToken()})), "NULL").toString());
                } else {
                    new network(socket).send(json.jsonaesencrypet(json.jsonaddjson(json.makejson(new String[]{"status", "message", "class", "func"}, new String[]{"405", "Username Repeated", "user", "register"}), "parameter", json.makejson(new String[]{"token"}, new String[]{this.getToken()})), this.getToken()).toString());
                }
                break;
            case "tokenlogin":
                if (this.loginwithtoken(doc.getJSONObject("parameter").getString("username"), doc.getJSONObject("parameter").getString("token"))) {
                    new network(socket).send(json.jsonaesencrypet(json.jsonaddjson(json.makejson(new String[]{"status", "message", "class", "func"}, new String[]{"200", "Successful Logged", "user", "tokenlogin"}), "parameter", json.makejson(new String[]{"token"}, new String[]{this.getToken()})), "NULL").toString());
                } else {
                    new network(socket).send(json.jsonaesencrypet(json.jsonaddjson(json.makejson(new String[]{"status", "message", "class", "func"}, new String[]{"405", "Username or Token Error", "user", "tokenlogin"}), "parameter", json.makejson(new String[]{"token"}, new String[]{this.getToken()})), this.getToken()).toString());
                }
                break;
            default:
                new network(socket).send(json.jsonaesencrypet(json.makejson(new String[]{"status", "message", "class", "func", "parammeter"}, new String[]{"404", "Function Not Find", "user", doc.getString("func"), json.makejson(new String[]{"token"}, new String[]{this.getToken()}).toString()}), this.getToken()).toString());
                break;
        }
    }

    /*
    static public Long getThreadID(String username) {
        Document rootdoc = dom4j.load("data/users/" + username + ".xml");
        if (rootdoc.getRootElement().element("isonline").getText() == "online")
            return Long.parseLong(Objects.requireNonNull(rootdoc).getRootElement().element("tid").getText());
        return 0L;
    }
    */
    private boolean login(String username, String password) {
        if (islogin) return false;
        File t = new File("data/users/" + username + ".xml");
        if (!t.exists())
            return false;
        Document document = dom4j.load("data/users/" + username + ".xml");
        Element rootElm = Objects.requireNonNull(document).getRootElement();
        String isonlne = Objects.requireNonNull(rootElm).element("isonline").getText();
        if (!isonlne.equals("offline"))
            return false;
        Objects.requireNonNull(rootElm).element("isonline").setText("online");
        String text = rootElm.element("password").getText();
        if (!text.equals(md5.md5_encode("PokePasswordMd5EncodeSalt" + password + "PokePasswordMd5EncodeSalt" + password)))
            return false;
        token = md5.md5_encode("PokePasswordMd5EncodeSalt" + password + "PokePasswordMd5EncodeSalt" + password + username + "UserTokenSalt");
        Element tokenElement = rootElm.element("token");
        tokenElement.setText(md5.md5_encode("PokePasswordMd5EncodeSalt" + password + "PokePasswordMd5EncodeSalt" + password + username + "UserTokenSalt"));
        dom4j.write(document, "data/users/" + username + ".xml");
        this.username = username;
        islogin = true;
        //writeThreadID();
        return true;
    }

    public String getToken() {
        if (islogin) {
            return token;
        } else {
            return "NULL";
        }
    }

    private boolean register(String username, String password) {
        if (islogin) return false;
        File xmlsaved = new File("data/users/" + username + ".xml");
        if (!xmlsaved.exists()) {
            Document document = DocumentHelper.createDocument();
            Element userrootElement = document.addElement("user");
            Element usernameElement = userrootElement.addElement("username");
            usernameElement.setText(username);
            Element userpwdElement = userrootElement.addElement("password");
            userpwdElement.setText(md5.md5_encode("PokePasswordMd5EncodeSalt" + password + "PokePasswordMd5EncodeSalt" + password));
            Element onlineElement = userrootElement.addElement("isonline");
            onlineElement.setText("online");
            Element tokenElement = userrootElement.addElement("token");
            tokenElement.setText(md5.md5_encode("PokePasswordMd5EncodeSalt" + password + "PokePasswordMd5EncodeSalt" + password + username + "UserTokenSalt"));
            token = md5.md5_encode("PokePasswordMd5EncodeSalt" + password + "PokePasswordMd5EncodeSalt" + password + username + "UserTokenSalt");
            dom4j.write(document, "data/users/" + username + ".xml");
            islogin = true;
            this.username = username;
            //writeThreadID();
            return true;
        } else {
            return false;
        }
    }

    public void logout() {
        if (islogin) {
            Document rootdoc = dom4j.load("data/users/" + username + ".xml");
            Element isonlineele = Objects.requireNonNull(rootdoc).getRootElement().element("isonline");
            isonlineele.setText("offline");
            Element tokenele = rootdoc.getRootElement().element("token");
            tokenele.setText("");
            dom4j.write(rootdoc, "data/users/" + username + ".xml");
            token = "";
            islogin = false;
        }
    }

    private boolean loginwithtoken(String username, String token) {
        if (islogin) {
            return false;
        } else {
            if (Objects.requireNonNull(dom4j.load("data/users/" + username + ".xml")).getRootElement().element("token").getText().equals(token)) {
                this.username = username;
                islogin = true;
                this.token = token;
                //writeThreadID();
                return true;
            } else {
                return false;
            }
        }
    }
    /*
    private void writeThreadID() {
        if (islogin) {
            Document rootdoc = dom4j.load("data/users/" + username + ".xml");
            Objects.requireNonNull(rootdoc).addElement("tid").setText(Thread.currentThread().getId() + "");
            dom4j.write(rootdoc, "data/users/" + username + ".xml");
        }
    }
    */
}
