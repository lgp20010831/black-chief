package com.black.nio.code.buf;

import java.util.Arrays;

public class BufferUtils {

    public static void clearBuf(byte[] bytes, int from, int to){
        Arrays.fill(bytes, from, to, (byte) 0);
    }

}
