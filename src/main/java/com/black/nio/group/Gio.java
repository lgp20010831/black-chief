package com.black.nio.group;

import com.black.io.out.JHexByteArrayOutputStream;

public interface Gio {

    void write(Object source);

    void flush();

    void writeAndFlush(Object source);

    void shutdown();

    JHexByteArrayOutputStream getOutputStream();

    String bindAddress();

    String remoteAddress();

}
