package com.black.socket;

import com.black.io.in.JHexByteArrayInputStream;
import com.black.io.out.JHexByteArrayOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class AvailableSocketPolling implements ReadSocketPolling{

    @Override
    public JHexByteArrayInputStream doRead(JHexByteArrayInputStream socketIn, JHexSocket socket) throws IOException {
        JHexByteArrayOutputStream out = new JHexByteArrayOutputStream();
        byte[] smallBytes = new byte[25];
        int size = socketIn.read(smallBytes);
        if (size == -1)
            throw new IOException("read -1");
        int available = socketIn.available();
        byte[] buffer = new byte[available];
        int read = socketIn.read(buffer);
        if (read == -1)
            throw new IOException("read -1");
        out.write(smallBytes, 0, size);
        out.write(buffer, 0, read);
        return new JHexByteArrayInputStream(out.toByteArray());
    }

    public static JHexByteArrayInputStream read0(InputStream socketIn, Socket socket) throws IOException {
        JHexByteArrayOutputStream out = new JHexByteArrayOutputStream();
        byte[] smallBytes = new byte[25];
        int size = socketIn.read(smallBytes);
        if (size == -1)
            throw new IOException("read -1");
        int available = socketIn.available();
        byte[] buffer = new byte[available];
        int read = socketIn.read(buffer);
        if (read == -1)
            throw new IOException("read -1");
        out.write(smallBytes, 0, size);
        out.write(buffer, 0, read);
        return new JHexByteArrayInputStream(out.toByteArray());
    }
}
