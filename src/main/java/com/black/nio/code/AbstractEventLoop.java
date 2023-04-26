package com.black.nio.code;

import com.black.nio.code.run.*;
import com.black.nio.code.util.SelectorUtils;
import com.black.core.util.CentralizedExceptionHandling;
import com.black.utils.IoUtils;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.channels.*;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
public abstract class AbstractEventLoop implements EventLoop{

    protected static final AtomicInteger threadSort = new AtomicInteger(0);

    //关闭标识符
    protected boolean shutdown = false;

    //事件组关联
    private final EventLoopGroup loopGroup;

    //维护一个 selector
    protected final Selector selector;

    //任务线程
    private final Thread runnableThread;

    //配置类
    private final Configuration configuration;

    //保存 channel 的引用
    private final Map<String, NioChannel> channelQuotes = new ConcurrentHashMap<>();

    //其他任务, 任务队列
    private final LinkedBlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();

    public AbstractEventLoop(EventLoopGroup loopGroup, Configuration configuration) throws IOException {
        this.loopGroup = loopGroup;
        this.configuration = configuration;
        selector = SelectorUtils.openSelector();
        runnableThread  = createThread(new EventLoopRunnable());
        runnableThread.start();
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public EventLoopGroup getGroup() {
        return loopGroup;
    }

    @Override
    public Selector getSelector() {
        return selector;
    }

    public Map<String, NioChannel> getChannelQuotes() {
        return channelQuotes;
    }

    protected Thread createThread(Runnable runnable){
        return new Thread(runnable, "nio-event-" + threadSort.incrementAndGet());
    }

    public Thread getRunnableThread() {
        return runnableThread;
    }

    @Override
    public Future<?> removeAndCloseChannel(NioChannel channel) {
        return addTask(() ->{
            //主要关闭 channel 的方法
            String address = channel.nameAddress();
            //移出缓存
            channelQuotes.remove(address);
            try {

                ((AbstractNioChannel)channel).close0();
            } catch (IOException e) {
                //ignore
            }finally {
                //回收当前 channel 占用的资源
                channel.getReadByteBuffer().release();
                channel.getWriteByteBuffer().release();
            }
        });
    }

    @Override
    public Future<?> close() {
        Future<?> future = addTask(() -> {
            for (NioChannel nioChannel : getChannelQuotes().values()) {
                removeAndCloseChannel(nioChannel);
            }
            shutdown = true;
            //这里可能出现问题, 绑定的 channel 并没有关闭完全就中止了线程
            runnableThread.interrupt();
        });
        for (;;){
            if (taskQueue.isEmpty()) {
                break;
            }
        }
        try {

            selector.close();
        } catch (IOException e) {
            //ignore
        }
        return future;
    }

    @Override
    public Future<NioChannel> registerChannel(NioChannel channel, int keyOps) throws AttysNioException {
        return addTask(() -> {
            String clientName = channel.nameAddress();
            SelectionKey selectionKey;
            SelectableChannel selectableChannel = channel.channel();
            try {

                //将 channel 绑定到 selector 上
                 selectionKey = selectableChannel.register(((SelectorWrapper)selector).getSelector(), keyOps);
            } catch (IOException e) {
                try {
                    if (log.isInfoEnabled()) {
                        log.info("Failed to register the channel. The reason may " +
                                "be the shutdown of the remote service or other IO " +
                                "exceptions, closing channel: [{}]", selectableChannel);
                    }
                    //获取 channel 地址时, 出现异常, 然后吧 channel 关闭
                    selectableChannel.close();
                } catch (IOException ex) {}
                throw new AttysNioException(e);
            }


            if (log.isInfoEnabled()) {
                log.info("successfully created channel object {}", clientName);
            }

            AbstractNioChannel abstractNioChannel = (AbstractNioChannel) channel;
            abstractNioChannel.setLoop(this);
            abstractNioChannel.setKey(selectionKey);
            //System.out.println("bind key:" + selectionKey + " on channel");
            //将 channel 绑定到 selector key
            selectionKey.attach(channel);
            channelQuotes.put(clientName, channel);
            //传播连接
            abstractNioChannel.active();
            return abstractNioChannel;
        });
    }

    @Override
    public Future<?> addTask(Runnable task) {
        DefaultFuture future = null;
        if (task != null){
            if(!shutdown){
                future = new DefaultFuture();
                taskQueue.add(new FutureRunnable(future, task));

                //唤醒 selector
                selector.wakeup();
            }else
                throw new AttysNioException("event loop is shutdown");
        }
        return future;
    }

    @Override
    public <V> Future<V> addTask(Callable<V> callable) {
        CallableFuture<V> future = null;
        if (callable != null){
            if(!shutdown){
                future = new CallableFuture<>();
                taskQueue.add(new FutureCallable<>(future, callable));

                //唤醒 selector
                selector.wakeup();
            }else
                throw new AttysNioException("event loop is shutdown");
        }
        return future;
    }

    /***
     * 处理 io 事件
     * @throws IOException 抛出发生的 io 异常
     */
    public abstract void handlerIoEvent() throws IOException;


    /** io 任务处理器 */
    public class EventLoopRunnable implements Runnable{

        @Override
        public void run() {
            log.info("任务工作线程启动");
            while (!Thread.interrupted()){
                int eventSize = 0;
                try {
                    if (taskQueue.isEmpty()) {
                        eventSize = selector.select();
                    }else {
                        eventSize = selector.selectNow();
                    }
                }catch (IOException e){
                    //这里要做更换 seletor 操作, 目前不具备
                }
                if (eventSize > 0){
                    //处理 io 事件
                    try {
                        handlerIoEvent();
                    } catch (Throwable e) {
                        //CentralizedExceptionHandling.handlerException(e);
                        log.warn("processor io event has error: {}", IoUtils.getThrowableMessage(e));
                    }
                }
                runTask();
            }
            runTask();
            if (log.isDebugEnabled()) {
                log.debug("关闭任务工作线程: " + Thread.currentThread().getName());
            }
        }

        protected void runTask(){
            Runnable task;
            while ((task = taskQueue.poll()) != null){
                try {
                    task.run();
                }catch (Throwable re){
                    CentralizedExceptionHandling.handlerException(re);
                }
            }
        }
    }
}
