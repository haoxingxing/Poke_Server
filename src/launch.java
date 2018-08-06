import network.server;
import process.aes;
import process.log;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class launch {
    public static void main(String[] args) {
        //------------------------<INIT>---------------------------
        File userdir = new File("data/users");
        userdir.mkdirs();
        //-----------------------</INIT>---------------------------
        try {
            ServerSocket serverSocket = new ServerSocket(8864);
            log.printf("ServerPort:" + 8864);
            serverSocket.setSoTimeout(0);
            System.out.println(aes.decrypt("8D94AB470AE2634D6FA0E25EFC7A661FD6B43CDD14AF2AFD058E89C833D4CDC88039B65738C3CC772CEEE0A9DE7DE378A2D55F6FAB37C1E3A37DDDB70EC271138C012BE243E220C6C5F6F0BC37A613CBC970FCB7C84C0F3E3D5192F5B771F496A33BB4213C2B44ED0968D9506C962299", "NULL"));
            //noinspection InfiniteLoopStatement
            while (true) {
                Socket stocs = serverSocket.accept();
                if (stocs.isConnected()) {
                    server s = new server(stocs.getLocalPort() + "", stocs);
                    s.start();
                }
            }
        } catch (IOException ignored) {
        }
    }
}