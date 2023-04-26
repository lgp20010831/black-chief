package com.black.core.io.bio;

import com.black.core.io.socket.SocketWrapper;

import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class SocketConnection implements Connection{

    private final SocketWrapper sw;

    private final LinkedBlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

    public SocketConnection(SocketWrapper sw) {
        this.sw = sw;
        new Thread(() ->{
           for (;;){
               if (!sw.isVaild()) {
                   break;
               }
               try {
                   String line = sw.readLine();
                   if (line != null){
                       messageQueue.add(line);
                   }
               }catch (Throwable e){
                   break;
               }
           }
        });
    }

    @Override
    public String read(){
        return messageQueue.poll();
    }

    @Override
    public String read(long timeout, TimeUnit unit){
        String msg = null;
        try {
            msg = messageQueue.poll(timeout, unit);
        } catch (InterruptedException e) {}
        return msg;
    }

    @Override
    public void close() {
        sw.close();
    }

    @Override
    public boolean isVaild() {
        return sw.isVaild();
    }

    @Override
    public Socket getSocket() {
        return sw.get();
    }

    @Override
    public void write(String message) {
        sw.write(message);
    }

    @Override
    public void writeAndFlush(String message) {
        sw.writeAndFlush(message);
    }
}
