package com.kytomaki.jracetracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Janne Kytömäki on 16.5.2016.
 */
public class SerialReader implements Runnable {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private InputStream in;

    private BufferedReader bin;

    private SerialEventListener serialEventListener;

    public SerialReader(InputStream in, SerialEventListener serialEventListener) {
        this.in = in;
        this.bin = new BufferedReader(new InputStreamReader(in));
        this.serialEventListener = serialEventListener;
    }

    public void run() {
        this.withBuf();
    }

    private char lastChar;

    private void withBuf() {
        byte[] buffer = new byte[1024];
        int len = -1;
        String line = "";
        boolean started = false;
        try {
            while ((len = this.in.read(buffer)) > -1) {
                for (int i = 0; i < len; i++) {
                    //ch = (char)buffer[i];

                    char ch = (char) buffer[i];
                    System.out.print(ch);

                    if (!started) {
                        if (ch == 1) {
                            started = true;
                        }
                    } else {
                        if (ch == 1) {
                            System.err.println("Line start encountered before previous line CRLF");
                        }
                        if (ch == 13) {
                            if (lastChar != 10) {
                                //System.err.println("LF encountered without CR");
                            }
                            //System.out.println("Read line '" + line + "'");
                            this.serialEventListener.onReadEvent(line);
                            started = false;
                            line = "";
                        } else if (ch != 10 && ch != 1) {
                            line += ch;
                        }
                    }

                    lastChar = ch;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*private void readLines() {
        int readByte;
        String line = "";
        try {
            while ((readByte = this.in.read()) > -1) {
        System.out.print((char)readByte);
        System.out.print(" " + readByte);
                if ((char) readByte == '\n' || (char) readByte == '\r') {
                    System.out.println(line);
                    line = "";
                } else {
                    line += (char) readByte;
                }
                //System.out.print(new String(buffer,0,readByte));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void withReader() {
        String line = null;
        try {
            while ((line = this.bin.readLine()) != null) {
                System.out.println("> " + line);
                //System.out.print(new String(buffer,0,len));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

}
