package com.black.nio.code.buf;

import java.io.FileInputStream;

public class BUFFERDOME {


    public static void main(String[] args) throws Exception{
        t1();
    }

    public static void t1() throws Exception {
        PoolByteBufferGroup group = new PoolByteBufferGroup();
        for (int i = 1; i < 4; i++) {
            FileInputStream stream = new FileInputStream("E:\\ideaSets\\SpringAutoThymeleaf\\docs\\" + i + ".txt");
            byte[] bytes = new byte[stream.available()];
            System.out.println(stream.read(bytes));
            PoolByteBuffer buffer = group.allotment(bytes.length);
            buffer.writeByte(bytes);
            System.out.println(buffer);
            buffer.release();
            stream.close();
        }
    }

}
