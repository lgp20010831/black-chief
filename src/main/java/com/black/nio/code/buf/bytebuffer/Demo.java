package com.black.nio.code.buf.bytebuffer;

import com.black.nio.code.buf.PrimordialNioByteBuffer;

public class Demo {


    public static void main(String[] args) {
        PrimordialNioByteBuffer buffer = new PrimordialNioByteBuffer(10);
        buffer.writeBytes(new byte[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1});
        System.out.println(buffer);
        buffer.release();

    }
}
