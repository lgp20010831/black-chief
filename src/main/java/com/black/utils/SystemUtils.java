package com.black.utils;

import com.black.io.in.JHexByteArrayInputStream;

import java.io.IOException;
import java.nio.charset.Charset;

public class SystemUtils {


    public static Process systemCall(String cmd){
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            try {
                process.waitFor();
            } catch (InterruptedException e) {

            }
            return process;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


    public static String callAndRece(String cmd){
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            try {
                process.waitFor();
            } catch (InterruptedException e) {

            }
            JHexByteArrayInputStream stream = new JHexByteArrayInputStream(process.getInputStream());
            return stream.readNewString(Charset.forName("GBK"));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


}
