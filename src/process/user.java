package process;

import network.network;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import xml.dom4j;

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

    public void process(Document doc) throws IOException {
        if (doc.getRootElement().element("func").getText().equals("login")) {
            if (this.login(doc.getRootElement().element("parameter").element("username").getText(), doc.getRootElement().element("parameter").element("password").getText())) {
                Element r = DocumentHelper.createElement("parameter");
                r.addElement("token").setText(this.getToken());
                new network(socket).send(dom4j.xmltostring(dom4j.retaesprocess(dom4j.retxmlmodel("200", "Successful Logged", "user", "login", r), "NULL"), dom4j.makeOF()));
            } else {
                Element r = DocumentHelper.createElement("parameter");
                r.addElement("token").setText(this.getToken());
                new network(socket).send(dom4j.xmltostring(dom4j.retaesprocess(dom4j.retxmlmodel("405", "Username or Password Error", "user", "login", r), this.getToken()), dom4j.makeOF()));
            }
        }
        if (doc.getRootElement().element("func").getText().equals("register")) {
            if (this.register(doc.getRootElement().element("parameter").element("username").getText(), doc.getRootElement().element("parameter").element("password").getText())) {
                Element r = DocumentHelper.createElement("parameter");
                r.addElement("token").setText(this.getToken());
                new network(socket).send(dom4j.xmltostring(dom4j.retaesprocess(dom4j.retxmlmodel("200", "Successful Registered", "user", "register", r), "NULL"), dom4j.makeOF()));
            } else {
                Element r = DocumentHelper.createElement("parameter");
                r.addElement("token").setText(this.getToken());
                new network(socket).send(dom4j.xmltostring(dom4j.retaesprocess(dom4j.retxmlmodel("405", "Username Repeated", "user", "register", r), this.getToken()), dom4j.makeOF()));
            }
        }
        if (doc.getRootElement().element("func").getText().equals("tokenlogin")) {
            if (this.loginwithtoken(doc.getRootElement().element("parameter").element("username").getText(), doc.getRootElement().element("parameter").element("token").getText())) {
                Element r = DocumentHelper.createElement("parameter");
                r.addElement("token").setText(this.getToken());
                new network(socket).send(dom4j.xmltostring(dom4j.retxmlmodel("200", "Successful Logged", "user", "loginwithtoken", r), dom4j.makeOF()));
            } else {
                Element r = DocumentHelper.createElement("parameter");
                r.addElement("token").setText(this.getToken());
                new network(socket).send(dom4j.xmltostring(dom4j.retxmlmodel("405", "Username or Token Error", "user", "loginwithtoken", r), dom4j.makeOF()));
            }
        }
    }

    private boolean login(String username, String password) {
        if (islogin) return false;
        File t = new File("data/users/" + username + ".xml");
        if (!t.exists())
            return false;
        Document document = dom4j.load("data/users/" + username + ".xml");
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
        dom4j.write(document, "data/users/" + username + ".xml");
        this.username = username;
        islogin = true;
        return true;
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
            onlineElement.setText("true");
            Element tokenElement = userrootElement.addElement("token");
            tokenElement.setText(md5.md5_encode("PokePasswordMd5EncodeSalt" + password + "PokePasswordMd5EncodeSalt" + password + username + "UserTokenSalt"));
            token = md5.md5_encode("PokePasswordMd5EncodeSalt" + password + "PokePasswordMd5EncodeSalt" + password + username + "UserTokenSalt");
            dom4j.write(document, "data/users/" + username + ".xml");
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
            if (dom4j.load("data/users/" + username + ".xml").getRootElement().element("token").getText().equals(token)) {
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
            Document rootdoc = dom4j.load("data/users/" + username + ".xml");
            Element isonlineele = rootdoc.getRootElement().element("isonline");
            isonlineele.setText("offline");
            Element tokenele = rootdoc.getRootElement().element("token");
            tokenele.setText("");
            dom4j.write(rootdoc, "data/users/" + username + ".xml");
            token = "";
            islogin = false;
        }
    }
}
