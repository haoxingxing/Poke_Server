package process;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class base64 {
    public static String encode(String str) {
        final Base64.Encoder encoder = Base64.getEncoder();
        final byte[] textByte = str.getBytes(StandardCharsets.UTF_8);
        return encoder.encodeToString(textByte);
    }

    public static String decode(String str) {
        final Base64.Decoder decoder = Base64.getDecoder();
        return new String(decoder.decode(str), StandardCharsets.UTF_8);
    }
}
