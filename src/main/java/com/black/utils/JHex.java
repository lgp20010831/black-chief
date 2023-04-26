package com.black.utils;

import java.io.UnsupportedEncodingException;

@SuppressWarnings("all")
public class JHex {


    public static final char[] DIGITS_UPPER = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
            'E', 'F' };

    public static String limitInt16(int i){
        return limitInt16(i, 8);
    }

    public static String limitInt16(int i, int size){
        String hexString = Integer.toHexString(i);
        int length = hexString.length();
        if (length == size){
            return hexString;
        }
        if (length > size){
            throw new IllegalStateException("ill len :" + length + " - size:" + size);
        }
        for (int j = 0; j < size - length; j++) {
            hexString = "0" + hexString;
        }
        return hexString;
    }


    public static String castChineseToHex(String chinese){
        StringBuilder st = new StringBuilder();
        try {
            //这里要非常的注意,在将字符串转换成字节数组的时候一定要明确是什么格式的,这里使用的是gb2312格式的,还有utf-8,ISO-8859-1等格式
            byte[] by = chinese.getBytes("gb2312");
            for (int i = 0; i < by.length; i++) {
                String strs = Integer.toHexString(by[i]);
                if (strs.length() > 2) {
                    strs = strs.substring(strs.length() - 2);
                }
                st.append(strs);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return st.toString();
    }

    public static String castHexToChinese(String str16){
        byte[] bytes = decode(str16);
        try {
            return new String(bytes, "gb2312");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static int castInt(String hexString){
        return Integer.parseInt(hexString, 16);
    }

    public static String decodeToString(String str16){
        return new String(decode(str16));
    }

    public static byte[] decode(String str16){
        return decode(str16.toCharArray());
    }

    public static byte[] decode(char[] data) {
        int len = data.length;
        if ((len & 0x01) != 0) {
            throw new IllegalArgumentException("Odd number of characters.");
        }
        byte[] out = new byte[len >> 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; j < len; i++) {
            int f = toDigit(data[j], j) << 4;
            j++;
            f = f | toDigit(data[j], j);
            j++;
            out[i] = (byte) (f & 0xFF);
        }
        return out;
    }

    public static char[] encode(byte[] data) {
        return encode(data, DIGITS_UPPER);
    }

    public static char[] encode(byte[] data, char[] toDigits) {
        int l = data.length;
        char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }

    public static char[] doToDigits(byte b, char[] toDigits){
        char[] out = new char[2];
        out[0] = toDigits[(0xF0 & b) >>> 4];
        out[1] = toDigits[0x0F & b];
        return out;
    }

    public static String castStringToDigits(byte b){
        return castStringToDigits(b, DIGITS_UPPER);
    }

    public static String castStringToDigits(byte b, char[] toDigits){
        return new String(doToDigits(b, toDigits));
    }

    public static String encodeObject(Object source){
        if (source == null)
            return "";
        byte[] bytes = IoUtils.castToBytes(source);
        return encodeString(bytes);
    }

    public static String encodeString(byte[] data) {
        return new String(encode(data));
    }

    private static int toDigit(char ch, int index) {
        final int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new IllegalArgumentException("Illegal hexadecimal character " + ch + " at index " + index);
        }
        return digit;
    }

}
