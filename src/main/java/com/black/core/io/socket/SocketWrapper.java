package com.black.core.io.socket;

import com.black.core.query.Wrapper;
import lombok.NonNull;

import java.io.*;
import java.net.Socket;


public class SocketWrapper implements Wrapper<Socket> {

    private final Socket socket;

    private final SocketConfiguration configuration;

    private final BufferedReader reader;

    private final BufferedWriter writer;

    public SocketWrapper(@NonNull Socket socket, SocketConfiguration configuration) {
        this.socket = socket;
        this.configuration = configuration;
        if (socket.isClosed()){
            throw new SocketsException("socket is aleary close");
        }

        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), configuration.getCharset()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), configuration.getCharset()));
        } catch (IOException e) {
            close();
            throw new SocketsException(e);
        }
    }

    public boolean isVaild(){
        return !socket.isClosed();
    }

    public SocketWrapper write(String data){
        try {
            writer.write(data);
            writer.newLine();
            return this;
        } catch (IOException e) {
            throw new SocketsException(e);
        }
    }

    public SocketWrapper flush(){
        try {
            writer.flush();
            return this;
        } catch (IOException e) {
            throw new SocketsException(e);
        }
    }

    public SocketWrapper writeAndFlush(String data){
        return write(data).flush();
    }

    public String readLine(){
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new SocketsException(e);
        }
    }

    public String readAll(){
        StringBuilder buffer = new StringBuilder();
        String line;
        while ((line = readLine()) != null){
            buffer.append(line);
        }
        return buffer.toString();
    }

    public void close(){
        try {
            if (reader != null){
                reader.close();
            }

            if (writer != null){
                writer.close();
            }
            socket.close();
        } catch (IOException e) {
            throw new SocketsException(e);
        }
    }


    @Override
    public Socket get() {
        return socket;
    }
}
