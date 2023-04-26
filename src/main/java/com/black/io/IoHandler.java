package com.black.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@SuppressWarnings("all")
public interface IoHandler extends Handler{

    default void throwsIoException() throws IOException{
        throw new IOException();
    }

    Object handlerInputStream(InputStream in) throws IOException;

    void handlerOutputStream(OutputStream out) throws IOException;

    void attach(Object source);

    Object attachment();

    void flush() throws IOException;

    void close() throws IOException;


}
