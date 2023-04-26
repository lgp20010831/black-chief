package com.black.socket;

import com.black.io.in.JHexByteArrayInputStream;
import com.black.nio.code.buf.SocketReadCloseException;
import com.black.core.log.IoLog;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class OnerousLoopServer extends Server{

    @Override
    protected void acceptClient(Socket socket) throws IOException {
        InquiryClientRunnable clientRunnable = getInquiryClient();
        clientRunnable.registerClient(socket);
        log.info("accept client:[{}]", socket.getInetAddress());
    }

    protected abstract InquiryClientRunnable getInquiryClient();

    @SuppressWarnings("all")
    protected static class InquiryClientRunnable extends Parkable implements Runnable{

        private final IoLog log;

        private final LinkedBlockingQueue<SocketBoard> clientQueue = new LinkedBlockingQueue<>();

        private final OnerousLoopServer server;

        private int parkThreshold = 20;

        public InquiryClientRunnable(IoLog log, OnerousLoopServer server) {
            this.log = log;
            this.server = server;
        }

        public void registerClient(Socket socket) throws IOException {
            SocketBoard socketBoard = new SocketBoard(socket);
            SocketHandler handler = server.getHandler();
            if (handler != null){
                try {
                    handler.complete(socketBoard);
                } catch (Throwable e) {
                    server.closeClient(socketBoard, e);
                    return;
                }
            }
            clientQueue.add(socketBoard);
            unpark();
        }

        private void parkServer(){
            log.debug("park inquiry thread");
            park();
        }

        private void unparkServer(){
            unpark();
            log.debug("unpark inquiry thread");
        }

        @Override
        public void run() {
            for (;;){
                if (clientQueue.isEmpty()){
                   park();
                }
                //log.debug("onerous loop listen queue size: {}", clientQueue.size());
                Iterator<SocketBoard> iterator = clientQueue.iterator();
                while (iterator.hasNext()) {
                    SocketBoard soh = iterator.next();
                    String hostAddress = soh.getAddressString();
                    try {

                        InputStream in = soh.getInputStream();
                        int available = in.available();
                        if (available == 0){
                            if ((System.currentTimeMillis() - soh.getLastPingTime()) > 60 * 1000) {
                                log.debug("the client: {} does not send heartbeat for a long time", hostAddress);
                                iterator.remove();
                                server.closeClient(soh, new SocketReadCloseException("no heart"));
                            }
                            continue;
                        }
                        log.info("read for client: [{}]", hostAddress);
                        byte[] readBuffer = new byte[available];
                        int size = in.read(readBuffer);
                        if (size == -1){
                            iterator.remove();
                            server.closeClient(soh, new SocketReadCloseException("read -1"));
                            continue;
                        }
                        JHexByteArrayInputStream dataIn = new JHexByteArrayInputStream(readBuffer);
                        server.execute(soh, dataIn);
                    } catch (IOException e) {
                        iterator.remove();
                        server.closeClient(soh, e);
                    }
                }
            }
        }


    }
}
