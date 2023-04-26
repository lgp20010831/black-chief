package com.black.utils;

import java.util.HashSet;
import java.util.UUID;

public class IdUtils {

    public static String[] chars8 = new String[] { "a", "b", "c", "d", "e", "f",
            "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
            "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z" };

    public static String[] chars22 = new String[] { "a", "b", "c", "d", "e", "f",
            "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
            "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z", "-", "_" };

    public static String createId(){
        return UUID.randomUUID().toString();
    }

    public static String createSimpleId(){
        return createId().replace("-", "");
    }

    public static String createShort8Id(){
        String simpleId = createSimpleId();
        //调用Java提供的生成随机字符串的对象：32位，十六进制，中间包含-
        StringBuilder shortBuffer = new StringBuilder();
        for (int i = 0; i < 8; i++) {                       //分为8组
            String str = simpleId.substring(i * 4, i * 4 + 4);  //每组4位
            int x = Integer.parseInt(str, 16);              //将4位str转化为int 16进制下的表示
            //用该16进制数取模62（十六进制表示为314（14即E）），结果作为索引取出字符
            shortBuffer.append(chars8[x % 0x3E]);
        }
        return shortBuffer.toString();
    }



    public static String createShort22Id() {
        StringBuilder shortBuffer = new StringBuilder();
        String uuid = createSimpleId();
        // 每3个十六进制字符转换成为2个字符
        for (int i = 0; i < 10; i++) {
            String str = uuid.substring(i * 3, i * 3 + 3);
            int x = Integer.parseInt(str, 16);      //转成十六进制
            shortBuffer.append(chars22[x / 0x40]);    //除64得到前面6个二进制数的
            shortBuffer.append(chars22[x % 0x40]);    //对64求余得到后面6个二进制数1
        }
        //加上后面两个没有改动的
        shortBuffer.append(uuid.charAt(30));
        shortBuffer.append(uuid.charAt(31));
        return shortBuffer.toString();
    }

    public static void main(String[] args) {
        HashSet<Object> set = new HashSet<>();
        for (int i = 0; i < 20000; i++) {
            String shortId = createShort22Id();
            System.out.println(shortId);
            set.add(shortId);
        }
        System.out.println(set.size());
    }
}
