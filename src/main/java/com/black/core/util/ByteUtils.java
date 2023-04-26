package com.black.core.util;

import com.black.core.spring.util.ApplicationUtil;

/**
 * ^: 相同则为0, 不同为1
 * &: 都为1 则1， 否则0
 * |： 只要有一个为 1, 则为1
 * ~ 位取反, 1 -> 0, 0 -> 1
 */
public class ByteUtils {


    public static int read(byte b){

        //0xff 二进制: 1111 1111
        //byte -> int 8位 -> 32位
        //高 24位补1
        //0xff -> 32位 高 24 位补 0
        return b & 0xff;
    }

    public static int uc(String str){
        int h = 0;
        if (str == null){
            return -1;
        }
        char[] charArray = str.toCharArray();
        byte[] bytes = new byte[charArray.length * 2];
        int offset = 0;
        for (char c : charArray) {
            bytes[offset ++] = (byte) ((c & 0xFF00) >> 8);
            bytes[offset ++] = (byte) (c & 0xFF);
        }
        for (byte b : bytes) {
            h = h + 31 * b;
        }
        return h;
    }

    public static void main(String[] args) {
        ApplicationUtil.programRunMills(() ->{
            System.out.println(uc("hello"));
            System.out.println(uc("poll"));
            System.out.println(uc(""));
            System.out.println(uc(null));
            System.out.println(uc("hello"));
        });
    }

}
