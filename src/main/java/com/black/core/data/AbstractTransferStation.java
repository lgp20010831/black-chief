package com.black.core.data;

import com.black.core.asyn.AsynGlobalExecutor;
import com.black.core.ill.GlobalThrowableCentralizedHandling;
import lombok.NonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;


@SuppressWarnings("all")
public abstract class AbstractTransferStation implements TransferStation{

    protected AtomicInteger consumerSize = new AtomicInteger(0);
    protected final String name;
    protected boolean shutdown = false;
    protected volatile boolean park = false;
    protected DataLog log;
    protected final ReentrantLock lock = new ReentrantLock();
    protected final Collection<DataConsumer> consumers = new HashSet<>();
    protected final LinkedBlockingQueue<Data<?>> dataQueue = new LinkedBlockingQueue<>();
    protected Thread currentThread;

    public AbstractTransferStation(String name){
        this.name = name;
        log = initLog();
        start();
    }

    @NonNull
    protected DataLog initLog(){
        return new SystemLog();
    }

    @Override
    public String getName() {
        return name;
    }

    protected void start(){
        AsynGlobalExecutor.execute(() ->{
            run();
        });
    }

    protected void run(){
        if (shutdown){
            throw new IllegalStateException("transfer station is already shutdown");
        }

        if (log.isInfoEnabled()) {
            log.info("启动分发服务器[" + getName() + "]");
        }
        currentThread = Thread.currentThread();
        for (;;){
            if (shutdown){
                break;
            }

            if (dataQueue.isEmpty()){
                park();
                continue;
            }

            Data<?> data = dataQueue.poll();
            if (log.isDebugEnabled()) {
                log.debug("消费数据, 已经消费了 {" + consumerSize.incrementAndGet() + "} 个数据");
            }
            data.setStatus(DataStatus.LOOP);
            for (DataConsumer consumer : consumers) {
                try {

                    if (consumer.support(data)) {
                        if (!DataStatus.IN_HAND.equals(data.getStatus())){
                            data.setStatus(DataStatus.IN_HAND);
                        }
                        AsynGlobalExecutor.execute(() -> {
                            consumer.handler(data);
                        });
                    }
                }catch (Throwable ex){
                    GlobalThrowableCentralizedHandling.resolveThrowable(ex);
                }
            }
        }
        log.info("关闭分发服务器[{" + getName() +"}]");
        end();
    }

    protected void end(){
        dataQueue.clear();
        consumers.clear();
    }

    protected void park(){
        park = true;
        if (log.isInfoEnabled()) {
            log.info("无数据处理, 挂起分发服务器[{" + getName() +"}]");
        }
        LockSupport.park();
    }

    protected void unpark(){
        if (log.isInfoEnabled()) {
            log.info("唤醒分发服务器[{" + getName() + "}]");
        }
        LockSupport.unpark(currentThread);
        park = false;
    }

    @Override
    public void push(Data<?> data) {
        if (data != null){
            dataQueue.add(data);
            data.setStatus(DataStatus.WAIT);
            if (park){
                unpark();
            }
        }
    }

    @Override
    public void registerConsumer(DataConsumer consumer) {
        consumers.add(consumer);
    }

    @Override
    public Collection<DataConsumer> getConsumers() {
        return consumers;
    }

    @Override
    public void removeConsumer(DataConsumer consumer) {
        consumers.remove(consumer);
    }

    @Override
    public void shutdown() {
        shutdown = true;
        currentThread.interrupt();
    }
}
