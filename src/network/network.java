package network;

import org.json.JSONObject;
import process.aes;
import process.base64;
import process.md5;
import process.user;

import java.io.*;
import java.net.Socket;
import java.util.Objects;

public class network {
    private BufferedWriter send;
    private BufferedReader recv;

    public network(Socket socket) {
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
    static public JSONObject decrypet(user u, String data)
    {
        JSONObject re = new JSONObject(data);
        if (re.getString("md5").equals(md5.md5_encode(re.getString("data") + re.getString("token")))) {
            if (u.getToken().equals(re.getString("token"))) {
                return new JSONObject(Objects.requireNonNull(aes.decrypt(re.getString("data"), Objects.requireNonNull(md5.md5_encode(re.getString("token") + "MakeTokenEnc")))));
            } else {
                return new JSONObject();
            }
        } else {
            return new JSONObject();
        }
    }
}
