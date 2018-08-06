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

public class user {
    private String username;
    private boolean islogin = false;
    private String token;
    private Socket socket;

    public user(Socket socket) {
        this.socket = socket;
    }

    public void process(JSONObject doc) throws IOException {
        switch (doc.getString("func")) {
            case "login":
                if (this.login(doc.getJSONObject("parameter").getString("username"), doc.getJSONObject("parameter").getString("password"))) {
                    new network(socket).send(json.jsonaesencrypet(json.makejson(new String[]{"status", "message", "class", "func", "parammeter"}, new String[]{"200", "Successful Logged", "user", "login", json.makejson(new String[]{"token"}, new String[]{this.getToken()}).toString()}), "NULL").toString());
                } else {
                    new network(socket).send(json.jsonaesencrypet(json.makejson(new String[]{"status", "message", "class", "func", "parammeter"}, new String[]{"405", "Username or Password Error", "user", "login", json.makejson(new String[]{"token"}, new String[]{this.getToken()}).toString()}), this.getToken()).toString());
                }
                break;
            case "register":
                if (this.register(doc.getJSONObject("parameter").getString("username"), doc.getJSONObject("parameter").getString("password"))) {
                    new network(socket).send(json.jsonaesencrypet(json.makejson(new String[]{"status", "message", "class", "func", "parammeter"}, new String[]{"200", "Successful Registered", "user", "register", json.makejson(new String[]{"token"}, new String[]{this.getToken()}).toString()}), "NULL").toString());
                } else {
                    new network(socket).send(json.jsonaesencrypet(json.makejson(new String[]{"status", "message", "class", "func", "parammeter"}, new String[]{"405", "Username Repeated", "user", "register", json.makejson(new String[]{"token"}, new String[]{this.getToken()}).toString()}), this.getToken()).toString());
                }
                break;
            case "tokenlogin":
                if (this.loginwithtoken(doc.getJSONObject("parameter").getString("username"), doc.getJSONObject("parameter").getString("password"))) {
                    new network(socket).send(json.jsonaesencrypet(json.makejson(new String[]{"status", "message", "class", "func", "parammeter"}, new String[]{"200", "Successful Logged", "user", "login", json.makejson(new String[]{"token"}, new String[]{this.getToken()}).toString()}), "NULL").toString());
                } else {
                    Element r = DocumentHelper.createElement("parameter");
                    r.addElement("token").setText(this.getToken());
                    new network(socket).send(json.jsonaesencrypet(json.makejson(new String[]{"status", "message", "class", "func", "parammeter"}, new String[]{"405", "Username or Token Error", "user", "login", json.makejson(new String[]{"token"}, new String[]{this.getToken()}).toString()}), this.getToken()).toString());
                }
                break;
            default:
                new network(socket).send(json.jsonaesencrypet(json.makejson(new String[]{"status", "message", "class", "func", "parammeter"}, new String[]{"404", "Function Not Find", "user", doc.getString("func"), json.makejson(new String[]{"token"}, new String[]{this.getToken()}).toString()}), this.getToken()).toString());
                break;
        }
    }

    private boolean login(String username, String password) {
        if (islogin) return false;
        File t = new File("data/users/" + username + ".dataprocess");
        if (!t.exists())
            return false;
        Document document = dom4j.load("data/users/" + username + ".dataprocess");
        Element rootElm = document.getRootElement();
        String isonlne = rootElm.element("isonline").getText();
        if (isonlne.equals("true"))
            return false;
        String text = rootElm.element("password").getText();
        if (!text.equals(md5.md5_encode("PokePasswordMd5EncodeSalt" + password + "PokePasswordMd5EncodeSalt" + password)))
            return false;
        token = md5.md5_encode("PokePasswordMd5EncodeSalt" + password + "PokePasswordMd5EncodeSalt" + password + username + "UserTokenSalt");
        Element tokenElement = rootElm.element("token");
        tokenElement.setText(md5.md5_encode("PokePasswordMd5EncodeSalt" + password + "PokePasswordMd5EncodeSalt" + password + username + "UserTokenSalt"));
        dom4j.write(document, "data/users/" + username + ".dataprocess");
        this.username = username;
        islogin = true;
        return true;
    }

    private boolean register(String username, String password) {
        if (islogin) return false;
        File xmlsaved = new File("data/users/" + username + ".dataprocess");
        if (!xmlsaved.exists()) {
            Document document = DocumentHelper.createDocument();
            Element userrootElement = document.addElement("user");
            Element usernameElement = userrootElement.addElement("username");
            usernameElement.setText(username);
            Element userpwdElement = userrootElement.addElement("password");
            userpwdElement.setText(md5.md5_encode("PokePasswordMd5EncodeSalt" + password + "PokePasswordMd5EncodeSalt" + password));
            Element onlineElement = userrootElement.addElement("isonline");
            onlineElement.setText("true");
            Element tokenElement = userrootElement.addElement("token");
            tokenElement.setText(md5.md5_encode("PokePasswordMd5EncodeSalt" + password + "PokePasswordMd5EncodeSalt" + password + username + "UserTokenSalt"));
            token = md5.md5_encode("PokePasswordMd5EncodeSalt" + password + "PokePasswordMd5EncodeSalt" + password + username + "UserTokenSalt");
            dom4j.write(document, "data/users/" + username + ".dataprocess");
            islogin = true;
            this.username = username;
            return true;
        } else {
            return false;
        }
    }

    public String getToken() {
        if (islogin) {
            return token;
        } else {
            return "NULL";
        }
    }

    private boolean loginwithtoken(String username, String token) {
        if (islogin) {
            return false;
        } else {
            if (dom4j.load("data/users/" + username + ".dataprocess").getRootElement().element("token").getText().equals(token)) {
                this.username = username;
                islogin = true;
                this.token = token;
                return true;
            } else {
                return false;
            }
        }
    }
    public void logout() {
        if (islogin) {
            Document rootdoc = dom4j.load("data/users/" + username + ".dataprocess");
            Element isonlineele = rootdoc.getRootElement().element("isonline");
            isonlineele.setText("offline");
            Element tokenele = rootdoc.getRootElement().element("token");
            tokenele.setText("");
            dom4j.write(rootdoc, "data/users/" + username + ".dataprocess");
            token = "";
            islogin = false;
        }
    }
}
