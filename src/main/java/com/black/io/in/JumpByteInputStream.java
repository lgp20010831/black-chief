package com.black.io.in;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;

public class JumpByteInputStream extends ByteArrayInputStream{

    private ByteArrayOutputStream out;

    public JumpByteInputStream(byte[] buf) {
        super(buf);
        out = new ByteArrayOutputStream();
    }


    /*
        0|1|1|2|0|3|4|a|v|2|0

     */
    public int jumpRead(int n) throws IOException {
        if (pos + n >= count){
            //can not skip
            throw new EOFException("can not skip pos: " + pos + "; skip: " + n + "; count: " + count);
        }
        int b = pos;
        pos += n;
        int read = read();
        out.write(read);
        buf[pos - 1] = -2;
        return read;
    }

    @Override
    public synchronized int read() {
        for (;;){
            int i = (pos < count) ? (buf[pos++] & 0xff) : -1;
            if (i == -1)
                return i;
            if (i == -2){
                continue;
            }
            return i;
        }
    }
}
