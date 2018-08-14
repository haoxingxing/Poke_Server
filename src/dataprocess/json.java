package dataprocess;

import org.json.JSONException;
import org.json.JSONObject;
import process.base64;
import process.md5;

public class json {
    public static JSONObject makejson(String[] name, String[] value) {
        JSONObject obj = new JSONObject();
        try {
            for (int x = 0; x < name.length; x++)
                obj.put(name[x], value[x]);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static JSONObject jsonaesencrypet(JSONObject json, String Token) {
        //String encrypet = aes.encrypt(json.toString(), Objects.requireNonNull(md5.md5_encode(Token + "MakeTokenEnc")));
        String encrypet = base64.encode(json.toString());
        return makejson(new String[]{"token", "data", "md5"}, new String[]{Token, encrypet, md5.md5_encode(encrypet + Token)});
    }
}
