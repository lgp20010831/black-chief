package com.black.nio.code;

import com.black.io.out.JHexByteArrayOutputStream;
import com.black.nio.code.buf.*;
import com.black.nio.code.run.Future;
import com.black.throwable.IOSException;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

@Log4j2
public abstract class AbstractNioChannel implements NioChannel{

    //远程命名
    protected final String nameAddress;

    //维护的唯一 channel
    protected final SelectableChannel channel;

    //配置类
    protected final Configuration configuration;

    //维护唯一的管道
    protected final Pipeline pipeline;

    //关联的兴趣子健
    protected SelectionKey key;

    //绑定的 event loop
    protected EventLoop loop;

    //连接 / 绑定的地址
    protected SocketAddress address;
    private NioByteBuffer readByteBuffer;
    private NioByteBuffer writeByteBuffer;
    private JHexByteArrayOutputStream outputStream;

    protected AbstractNioChannel(SelectableChannel channel,
                                 Configuration configuration,
                                 String nameAddress){
        this(channel, configuration, null, null, nameAddress);
    }

    /***
     * 构造方法
     * @param channel java channel
     * @param configuration 全局配置类
     * @param key 绑定的字键, 所有兴趣的更换将操作这一个字键
     * @param loop 处理 io 事件的线程
     * @param nameAddress 别名, 通常代表地址
     */
    protected AbstractNioChannel(SelectableChannel channel,
                                 Configuration configuration,
                                 SelectionKey key, EventLoop loop,
                                 String nameAddress) {
        this.channel = channel;
        this.nameAddress = nameAddress;
        this.configuration = configuration;
        this.key = key;
        this.pipeline = new Pipeline(this);
        //为每个 channel 配置业务链
        ChannelInitialization initialization = configuration.getChannelInitialization();
        if (initialization != null){
            initialization.init(pipeline);
        }
        this.loop = loop;
        //创建两个 buffer 一个用来写, 一个用来读
        readByteBuffer = BufferFactory.createBuffer(configuration);
        writeByteBuffer = BufferFactory.createBuffer(configuration);
        if (log.isDebugEnabled()) {
            log.debug("[buffer configuration: {}]", readByteBuffer);
        }
        NioChannelOutputStream nioChannelOutputStream = new NioChannelOutputStream(this);
        outputStream = new JHexByteArrayOutputStream(nioChannelOutputStream);
    }

    public void setLoop(EventLoop loop) {
        this.loop = loop;
    }

    public void setKey(SelectionKey key) {
        this.key = key;
    }

    /** 判断当前现成是否为绑定的事件线程 */
    @Override
    public boolean inEventLoop() {
        return Thread.currentThread().equals(loop.getRunnableThread());
    }

    @Override
    public String nameAddress() {
        return nameAddress;
    }

    public NioByteBuffer getReadByteBuffer() {
        return readByteBuffer;
    }

    public NioByteBuffer getWriteByteBuffer() {
        return writeByteBuffer;
    }

    //channel 在关闭的时候要释放调自己占用的缓存
    //并清除调所有引用
    @Override
    public void close() {
        ChannelHandlerContext tail = pipeline.getTail();
        tail.fireClose(tail);
    }

    /** 关闭 channel 方法, 有管道 head 调用 */
    public void close0() throws IOException {
        if (log.isInfoEnabled()) {
            log.info("channel close:{}", nameAddress());
        }
        channel.close();
        if (key != null)
            key.cancel();
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public SelectableChannel channel() {
        return channel;
    }

    @Override
    public EventLoop getEventLoop() {
        return loop;
    }

    @Override
    public Future<?> addTaskInLoop(Runnable runnable) {
        return getEventLoop().addTask(runnable);
    }

    /** 流动异常事件 */
    @Override
    public void error(Throwable e) {
        ChannelHandlerContext head = pipeline.getHead();
        try {
            head.fireError(head, e);
        } catch (IOException ex) {
            close();
        }
    }

    /** 流动连接完成事件, 这个事件只会出现在客户端 */
    @Override
    public void connectComplete() {
        ChannelHandlerContext head = pipeline.getHead();
        head.fireConnectComplete(head);
    }

    /** 流动接受连接事件, 这个事件只会出现在服务端 */
    @Override
    public void acceptComplete() {
        ChannelHandlerContext head = pipeline.getHead();
        head.fireAcceptComplete(head);
    }

    /** 流动 flush 事件, 会将缓存区内数据发给远程 */
    @Override
    public void flush() {
        ChannelHandlerContext tail = pipeline.getTail();
        tail.fireFlush(tail);
    }


    public void flush0(){
        if (key.isValid()){
            key.interestOps(SelectionKey.OP_WRITE);
            SelectorWrapper loopSelector = (SelectorWrapper) loop.getSelector();
            if (loopSelector.getRunningState().get()) {
                log.info("will wake up running selector");
                loopSelector.wakeup();
            }
        }
    }

    @Override
    public Pipeline getPipeline() {
        return pipeline;
    }

    public void addBuffer(byte[] bytes)  {
        writeByteBuffer.writeBytes(bytes);
    }

    public void active(){
        ChannelHandlerContext head = pipeline.getHead();
        head.fireActive(head);
    }

    public void readChannel() throws IOException {

        try {
            byte[] readBytes = readByteBuffer.read(this);
            ChannelHandlerContext head = pipeline.getHead();
            head.fireRead(head, readBytes);
        } catch (SocketReadCloseException e) {
            //将异常抛出, 然后经过流动关闭
          throw new IOException("客户端已经关闭", e);
        }
    }

    public void writeChannel(SelectionKey key) throws IOException {
        //将缓冲区里的数据写到 channel 里
        writeByteBuffer.write(this);

        //情况缓冲去里的数据
        writeByteBuffer.clear();
        key.interestOps(SelectionKey.OP_READ);
    }

    @Override
    public void write(Object source) {
        if (configuration.isWriteInCurrentLoop() && !inEventLoop()) {
            loop.addTask(() -> {
                doWrite(source);
            });
        }else {
            doWrite(source);
        }
    }

    private void doWrite(Object source){
        ChannelHandlerContext tail = pipeline.getTail();
        try {
            tail.fireWrite(tail, source);
        } catch (IOException e) {
            throw new IOSException(e);
        }
    }

    @Override
    public void writeAndFlush(Object source) {
        if (configuration.isWriteInCurrentLoop() && !inEventLoop()) {
            loop.addTask(() ->{
                write(source);
                flush();
            });
        }else {
            write(source);
            flush();
        }
    }

    @Override
    public JHexByteArrayOutputStream getOutputStream() {
       return outputStream;
    }
}
