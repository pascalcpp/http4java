package com.xpcf.http4java.log;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 * @author XPCF
 * @version 1.0
 * @date 12/27/2021 7:51 AM
 */
public class Logger {

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("127.0.0.1", 4396);
            Thread.sleep(5000);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * haven't complete
     * @param str
     */
    public static void println(String str) {
        System.out.println(str);
    }

    public static void printError(String str) {
        System.err.println(str);
    }
}
