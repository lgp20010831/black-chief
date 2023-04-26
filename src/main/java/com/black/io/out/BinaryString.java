package com.black.io.out;

import com.black.io.in.JHexByteArrayInputStream;
import lombok.NoArgsConstructor;

import java.io.IOException;

@SuppressWarnings("all") @NoArgsConstructor
public class BinaryString implements BinaryPartElement{

    private String name;

    private byte[] buf;

    public BinaryString(String name, String context){
        this(name, context.getBytes());
    }

    public BinaryString(String name, byte[] buf) {
        this.name = name;
        this.buf = buf;
    }

    public String readContext() throws IOException {
        return new String(getInputStream().readAll());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JHexByteArrayInputStream getInputStream() {
        return new JHexByteArrayInputStream(buf);
    }

    @Override
    public int size() throws IOException {
        return buf.length;
    }

    @Override
    public byte[] buf() {
        return buf;
    }
}
