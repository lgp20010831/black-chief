package com.black.socket;

import com.black.io.in.DataByteBufferArrayInputStream;
import com.black.io.in.JHexByteArrayInputStream;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client {

    final InetSocketAddress address;

    IoLog log;

    volatile State state;

    SocketHandler handler;

    SocketBoard board;

    final ClientRunnable clientRunnable;

    final HeartRunnable heartRunnable;

    public Client(int port){
        this("127.0.0.1", port);
    }

    public Client(String host, int port){
        address = new InetSocketAddress(host, port);
        log = LogFactory.getLog4j();
        state = State.WAIT;
        clientRunnable = new ClientRunnable(this);
        new Thread(clientRunnable).start();
        heartRunnable = new HeartRunnable(this);
        new Thread(heartRunnable).start();
    }

    public SocketBoard connect() throws IOException {
        checkClose();
        return doConnect();
    }

    private void checkClose(){
        if (getState() == State.CLOSE){
            throw new IllegalStateException("client is closed");
        }
    }

    private SocketBoard doConnect() throws IOException {
        Socket socket = new Socket();
        socket.connect(address);
        log.debug("成功连接到服务器");
        board = new SocketBoard(socket);
        state = State.OPEN;
        SocketHandler handler = getHandler();
        if (this.handler != null){
            try {

                handler.complete(board);
            } catch (Throwable e) {
                closeSocket(board, e);
                return board;
            }
        }
        clientRunnable.wakeUp();
        heartRunnable.wakeUp();
        return board;
    }

    public void setHandler(SocketHandler handler) {
        this.handler = handler;
    }

    private void closeSocket(SocketBoard board, Throwable e){
        log.error(null, "close client: [{}]", board.getAddress());
        if (handler != null){
            board.close();
            handler.close(board, e);
        }
        state = State.WAIT;
    }

    public State getState() {
        return state;
    }

    public SocketBoard getBoard() {
        return board;
    }

    public IoLog getLog() {
        return log;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public SocketHandler getHandler() {
        return handler;
    }

    public void closeClient(){
        state = State.CLOSE;
        if (board != null){
            if (!board.isClosed()) {
                board.close();
            }
        }
        clientRunnable.wakeUp();
        heartRunnable.wakeUp();
    }

    protected static class ClientRunnable extends Parkable implements Runnable{

        private final Client client;

        public ClientRunnable(Client client) {
            this.client = client;
        }

        public void wakeUp(){
            IoLog log = client.getLog();
            log.debug("唤醒客户端监听线程");
            unpark();
        }

        @Override
        public void run() {
            for (;;){
                IoLog log = client.getLog();
                if (client.getState() == State.CLOSE){
                    break;
                }

                if (client.getState() == State.WAIT){
                    log.debug("客户端监听线程休眠");
                    park();
                }


                SocketBoard board = client.getBoard();
                if (board == null || board.isClosed()){
                    continue;
                }
                DataByteBufferArrayInputStream in = board.getDataInputStream();
                try {

                    int type = in.readInt();
                    if (type == 200){
                        byte[] bytes = new byte[in.available()];
                        in.read(bytes);
                        SocketHandler handler = client.getHandler();
                        if (handler != null){
                            try {
                                handler.read(board, new JHexByteArrayInputStream(bytes));
                            } catch (Throwable e) {
                                client.closeSocket(board, e);
                            }
                        }
                    }else {
                        log.debug("丢弃无法识别的消息类型:{}", type);
                    }

                } catch (IOException e) {
                    client.closeSocket(board, e);
                }
            }
            IoLog log = client.getLog();
            log.debug("监听服务器线程关闭");
        }
    }

    protected static class HeartRunnable extends Parkable implements Runnable{

        private final Client client;

        public HeartRunnable(Client client) {
            this.client = client;
        }

        public void wakeUp(){
            IoLog log = client.getLog();
            log.debug("唤醒客户端心跳线程");
            unpark();
        }

        @Override
        public void run() {

            for (;;){
                IoLog log = client.getLog();
                if (client.getState() == State.CLOSE){
                    break;
                }
                if (client.getState() == State.WAIT){
                    log.debug("客户端心跳线程休眠");
                    park();
                }
                SocketBoard board = client.getBoard();
                if (board.isClosed()){
                    continue;
                }
                try {

                    board.sendHeart();
                } catch (IOException e) {
                    client.closeSocket(board, e);
                }
                try {
                    Thread.sleep(30 * 1000);
                } catch (InterruptedException e) {}
            }
            IoLog log = client.getLog();
            log.debug("发送心跳线程关闭");
        }
    }

}
