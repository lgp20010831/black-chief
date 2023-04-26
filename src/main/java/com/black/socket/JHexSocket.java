package com.black.socket;

import com.black.function.Callback;
import com.black.io.in.JHexByteArrayInputStream;
import com.black.io.out.JHexByteArrayOutputStream;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.core.util.Utils;
import com.black.throwable.IOSException;
import com.black.utils.IoUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

@Getter @Setter @SuppressWarnings("all")
public class JHexSocket {

    /**
     * 封装的 socket
     */
    private Socket socket;

    /**
     * 连接目标的 ip 地址
     */
    private String hostCache;

    /**
     * 打印日志的工具
     */
    private IoLog log;

    /**
     * 连接目标的端口
     */
    private int portCache;

    /**
     * 对于 socket 的输入流的封装
     * 提供了各种增强方法
     */
    private JHexByteArrayInputStream inputStream;

    /**
     * 对于 socket 输出流的封装
     * 提供了各种增强方法
     */
    private JHexByteArrayOutputStream outputStream;

    /**
     * 核心处理器
     * resolveBytes 方法主要处理来自于服务端发送的数据
     * handlerReadEvent 方法主要处理业务程序所抛出的异常, 也就是 resolveBytes 执行过程
     *                  中所抛出的异常
     * handlerIoEvent 方法主要处理在与服务端交互中可能出现的无法控制的 io 异常
     */
    private JHexBytesHandler jHexBytesHandler;

    /**
     * 当 socket 关闭后的回调函数
     */
    private Runnable closeCallback;

    /**
     * 针对如何读取 socket 数据的核心处理器
     * 提供的几个核心处理器:
     * {@link SpecifyBufferSocketPolling} 准备一个指定容量的缓冲区接收数据
     * {@link AvailableSocketPolling} 无需要指定缓冲区大小, 可接收大量数据
     */
    private ReadSocketPolling polling;

    /**
     * 当 socket 被构造出来以后(连接之前)的回调函数
     */
    private Callback<Socket> socketCallback;

    /**
     * 监听 socket 读状态的线程
     */
    private ReadThread readThread;

    /**
     * 每次重连时间间隔 单位毫秒
     */
    private long reconnectInterval = 5000;

    /**
     * 当发生 io 异常时是否尝试重连
     */
    private boolean tryReconnect = true;

    /**
     * true: 无限重连, false: 重连一次
     */
    private boolean reconnectLoop = true;

    /**
     * 内部缓冲区, 当需要串行处理数据, 而不是采用
     * 回调函数的形式处理数据时, 数据会被写到该缓冲区
     */
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    /**
     * 串行处理数据时, 该变量指向等待数据的线程
     */
    private Thread waitThread = null;

    public JHexSocket(int port){
        this("0.0.0.0", port);
    }

    public JHexSocket(String host, int port){
        hostCache = host;
        portCache = port;
    }

    public JHexSocket(Socket socket) throws IOException {
        this(socket, null);
    }

    public JHexSocket(Socket socket, JHexBytesHandler handler) throws IOException {
        setJHexBytesHandler(handler);
        this.socket = socket;
        connect0(socket, true);
    }

    /**
     * 该方法由 readThread 对象进行调用
     * 为私有方法, 主要是将数据写入缓冲区
     * @param bytes 数据
     */
    void writeBuffer(byte[] bytes){
        try {
            buffer.write(bytes);
        } catch (IOException e) {

        }
        if (waitThread != null){
            waitThread.interrupt();
        }
    }


    public JHexByteArrayInputStream writeAndWaitReadIn(Object source) throws IOException {
        return writeAndWaitReadIn(source, -1);
    }

    /**
     * 向 socket 写入数据并且冲刷到服务端
     * 之后堵塞等待指定时间, 如果未超出指定时间则返回数据输入流
     * 若超出时间则抛出超时异常
     * 等同于 socket.getInputStream().read() 形式
     * 不过由于 jhex socket 设计之处是异步回调, 所以为了支持原生功能
     * 该方法会比 socket 的 读多进行两次 io 操作, 效率比不上 socket 原生读
     * 原生 socket：
     *                       read
     *              socket  --> -->  用户层面
     * jhex socket:
     *                      read            write           read
     *              socket --> --> buffer1 --> --> buffer2 --> --> 用户层面
     * @param source 写数据对象
     * @param timeout 超时时间 <0 则表示无限时, 单位毫秒
     * @return 读出的字节封装输入流
     * @throws IOException io error
     */
    public JHexByteArrayInputStream writeAndWaitReadIn(Object source, long timeout) throws IOException {
        writeAndFlush(source);
        return waitReadIn(timeout);
    }

    public JHexByteArrayInputStream waitReadIn(){
        return waitReadIn(-1);
    }

    public JHexByteArrayInputStream waitReadIn(long timeout){
        return new JHexByteArrayInputStream(waitRead(timeout));
    }

    public byte[] waitRead(){
        return waitRead(-1);
    }

    public byte[] waitRead(long timeout){
        waitThread = Thread.currentThread();
        if (readThread == null){
            throw new IOSException("socket is not connection");
        }else {
            readThread.setWriteBuffer(true);
        }
        try {
            if (timeout < 0){
                //则一直堵塞等待数据缓冲
                LockSupport.park();
            }else {
                boolean timeoutThrowable = true;
                //在指定的时间内等待数据缓冲
                try {
                    Thread.sleep(timeout);
                } catch (InterruptedException e) {
                    timeoutThrowable = false;
                }
                if (timeoutThrowable){
                    throw new IOSException("read time out in " + timeout);
                }
            }
            //恢复线程标识
            Thread.interrupted();
            byte[] bytes = buffer.toByteArray();
            buffer.reset();
            return bytes;
        }finally {
            waitThread = null;
        }
    }

    public String getServerName(){
        return hostCache + "|" + portCache;
    }

    public String getLocalName(){
        if (isClosed()){
            return "unactive";
        }
        int localPort = socket.getLocalPort();
        InetAddress address = socket.getLocalAddress();
        if (address == null){
            return "unlocal";
        }
        return address.getHostAddress() + "|" + localPort;
    }

    public ReadSocketPolling getPolling() {
        if (polling == null)
            polling = new AvailableSocketPolling();
        return polling;
    }

    public IoLog getLog() {
        if (log == null)
            log = LogFactory.getLog4j();
        return log;
    }

    public void connect() throws IOException{
        connect(true);
    }

    public void connect(boolean startReadThread) throws IOException {
        try {
            doConnect(startReadThread);
        }catch (IOException e){
            closeSocket();
            JHexBytesHandler jHexBytesHandler = getJHexBytesHandler();
            if (jHexBytesHandler != null){
                jHexBytesHandler.handlerIoEvent(e, this);
            }
        }

    }

    private void doConnect(boolean startReadThread) throws IOException {
        socket = new Socket();
        if (socketCallback != null){
            try {
                socketCallback.call(socket);
            } catch (Throwable e) {
                log.error(e, "call back socket has error");
            }
        }
        connect0(socket, startReadThread);
    }

    private void connect0(Socket socket, boolean startReadThread) throws IOException {
        if (!socket.isConnected()){
            socket.connect(new InetSocketAddress(hostCache, portCache));
            getLog().debug("connect to host:{} | port:{} completed", hostCache, portCache);
        }
        inputStream = new JHexByteArrayInputStream(socket.getInputStream());
        outputStream = new JHexByteArrayOutputStream(socket.getOutputStream());
        if (startReadThread && readThread == null){
            readThread = new ReadThread(getPolling(), this);
            readThread.start();
        }
    }

    public boolean isConnected(){
        if (socket == null || inputStream == null || outputStream == null)
            return false;
        return socket.isConnected();
    }

    public boolean isClosed(){
        if (socket == null || inputStream == null || outputStream == null)
            return true;
        return socket.isClosed();
    }

    public Socket socket(){
        return socket;
    }

    public void reconnect() throws IOException{
        reconnect(true);
    }

    public void reconnect(boolean startReadThread) throws IOException {
        reconnect(hostCache, portCache, startReadThread);
    }

    public void reconnect(String newHost, int newPort, boolean startReadThread) throws IOException {
        hostCache = newHost;
        portCache = newPort;
        boolean f = true;
        boolean doreconn = true;
        while (doreconn){
            doreconn = reconnectLoop;
            if (!f){
                Utils.sleep(reconnectInterval);
            }
            f = false;
            getLog().info("reconnect to host:{} | port:{}", hostCache, portCache);
            try {
                doConnect(startReadThread);
            }catch (Throwable e){
                closeSocket();
                getLog().info("reconnect fair by " + IoUtils.getThrowableMessage(e));
                continue;
            }
            break;
        }
    }

    public JHexByteArrayInputStream getInputStream() {
        return inputStream;
    }

    public JHexByteArrayOutputStream getOutputStream() {
        return outputStream;
    }

    public void write(Object source) throws IOException {
        if (!isConnected()){
            throw new IOException("lost connect");
        }
        byte[] bytes = IoUtils.getBytes(source, false);
        outputStream.write(bytes);
    }

    public void flush() throws IOException {
        if (!isConnected()){
            throw new IOException("lost connect");
        }
        outputStream.flush();
    }

    public void close() throws IOException {
        if (isConnected()){
            closeSocket();
            readThread.shutdown();
            readThread = null;
        }
    }

    public void closeSocket() throws IOException {
        getLog().info("close socket");
        if (outputStream != null)
        outputStream.close();
        if (inputStream != null)
        inputStream.close();
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        socket = null;
        outputStream = null;
        inputStream = null;
        if (closeCallback != null){
            closeCallback.run();
        }
    }

    public void writeAndFlush(Object source) throws IOException {
        if (!isConnected()){
            throw new IOException("lost connect");
        }
        write(source);
        flush();
    }

    protected static class ReadThread extends Thread{

        private final ReadSocketPolling polling;

        private final JHexSocket socket;

        private boolean shutdown = false;

        private boolean writeBuffer = false;

        private static final AtomicInteger index = new AtomicInteger(1);

        public ReadThread(ReadSocketPolling polling, JHexSocket socket) {
            super("polling-read-" + index.getAndIncrement());
            this.polling = polling;
            this.socket = socket;
        }

        public void shutdown(){
            shutdown = true;
        }


        public void setWriteBuffer(boolean writeBuffer) {
            this.writeBuffer = writeBuffer;
        }

        @Override
        public void run() {
            IoLog log = socket.getLog();
            log.debug("run socket:{} listen read event thread");
            JHexBytesHandler bytesHandler;
            while (!shutdown){
                if (socket.isClosed()) {
                    continue;
                }
                bytesHandler = socket.getJHexBytesHandler();
                JHexByteArrayInputStream inputStream = socket.getInputStream();
                try {
                    JHexByteArrayInputStream readIn = polling.doRead(inputStream, socket);
                    if (readIn == null){
                        throw new IOException("null return value of read");
                    }
                    //如果需要直接写入 jhexsocket的缓冲区
                    if (writeBuffer){
                        writeBuffer = false;
                        socket.writeBuffer(readIn.readAll());
                        continue;
                    }

                    if (bytesHandler != null){
                        try {

                            bytesHandler.resolveBytes(readIn, socket);
                        } catch (Throwable e) {
                            if (bytesHandler != null){
                                bytesHandler.handlerReadEvent(e, socket);
                            }
                        }
                    }
                } catch (IOException e) {
                    if (e instanceof SocketTimeoutException){
                        log.error(null, "socket:{} read time out", socket.getLocalName());
                    }else {
                        try {
                            socket.closeSocket();
                        } catch (IOException ex) {}
                        if (bytesHandler != null){
                            try {
                                bytesHandler.handlerIoEvent(e, socket);
                            } catch (IOException ex) {
                                //ignore
                            }
                        }
                    }
                }
            }
            log.debug("socket read thread shutdown");
        }
    }
}
