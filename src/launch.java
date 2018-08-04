import process.aes;

import java.io.File;

public class launch {
    public static void main(String[] args) {
        //------------------------<INIT>---------------------------
        File userdir = new File("data/users");
        userdir.mkdirs();
        //-----------------------</INIT>---------------------------
        String mi = aes.encrypt("233", "qwq");
        System.out.println(mi);
        String de = aes.decrypt(mi, "qwqd");
        System.out.println(de);
    }
}