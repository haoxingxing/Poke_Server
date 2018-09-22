package process;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class log {
    public static void printf(String log, String file, int line) {
        String logdata = "[" + Thread.currentThread().getId() + "]{" + file + ":" + line + "} " + log;
        System.out.println(logdata);
        try {
            File f = new File("logs");
            FileWriter fw = new FileWriter(f, true);
            PrintWriter pw = new PrintWriter(fw);
            pw.println(logdata);
            pw.flush();
            fw.flush();
            pw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}