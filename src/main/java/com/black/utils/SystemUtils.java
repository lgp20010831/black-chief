package com.black.utils;

import java.io.IOException;

public class SystemUtils {


    public static Process systemCall(String cmd){
        try {
            return Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


}
