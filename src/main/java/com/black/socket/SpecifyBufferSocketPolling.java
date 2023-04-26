package com.black.socket;

import com.black.io.in.JHexByteArrayInputStream;
import com.black.io.out.JHexByteArrayOutputStream;

import java.io.IOException;

public class SpecifyBufferSocketPolling implements ReadSocketPolling{

    private final int bufferSize;

    public SpecifyBufferSocketPolling(int bufferSize) {
        this.bufferSize = bufferSize;
        if (bufferSize <= 0){
            throw new IllegalArgumentException("buffer size must be not <= 0");
        }
    }

    @Override
    public JHexByteArrayInputStream doRead(JHexByteArrayInputStream socketIn, JHexSocket socket) throws IOException {
        JHexByteArrayOutputStream out = new JHexByteArrayOutputStream();
        byte[] buffer = new byte[bufferSize];
        int size = socketIn.read(buffer);
        if (size == -1){
            throw new IOException("read -1");
        }

        while (size == buffer.length){
            out.write(buffer);
            size = socketIn.read(buffer);
            if (size == -1){
                throw new IOException("read -1");
            }
        }
        out.write(buffer, 0, size);
        return new JHexByteArrayInputStream(out.toByteArray());
    }
}
