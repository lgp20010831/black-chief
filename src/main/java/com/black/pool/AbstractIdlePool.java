package com.black.pool;

import com.black.core.log.IoLog;
import com.black.utils.ServiceUtils;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("all")
public abstract class AbstractIdlePool<E> extends AbstractPool<E>{

    private final IdleListener<E> idleSocketListener;

    private final Map<PoolElement<E>, ElementAndTimeWrapper<E>> cache = new ConcurrentHashMap<>();

    public AbstractIdlePool(Configuration configuration) {
        super(configuration);
        idleSocketListener = new IdleListener<E>(this);
        new Thread(idleSocketListener, "idle socket" + getSort()).start();
    }

    @Override
    protected void shutdown0() {
        idleSocketListener.setClose(true);
        super.shutdown0();
        cache.clear();
    }


    @Override
    protected void closeElementIfUnused(PoolElement<E> poolElement) {
        //重写, 不在立即销毁闲置连接池中的连接
    }


    @Override
    protected void callbackGetElement(PoolElement<E> poolElement) {
        if (!poolElement.isCore()){
            ElementAndTimeWrapper<E> wrapper = cache.get(poolElement);
            if (wrapper != null){
                log.info("重置连接过期时间: {}", poolElement);
                wrapper.setLeaveTime(getLeaveTime());
                idleSocketListener.sort();
            }
        }
    }

    @Override
    protected void addQueue(PoolElement<E> poolElement, boolean core) {
        super.addQueue(poolElement, core);
        if (!poolElement.isCore()){
            //如果不是核心连接, 则设置过期时间
            ElementAndTimeWrapper<E> wrapper = new ElementAndTimeWrapper<>(poolElement, getLeaveTime());
            cache.put(poolElement, wrapper);
            idleSocketListener.register(wrapper);
            log.info("监听闲置连接: {}", poolElement);
        }
    }

    private long getLeaveTime(){
        Configuration configuration = getConfiguration();
        long unusedSocketKeepAlive = configuration.getUnusedSocketKeepAlive();
        TimeUnit unusedSocketTimeUnit = configuration.getUnusedSocketTimeUnit();
        long millis = unusedSocketTimeUnit.toMillis(unusedSocketKeepAlive);
        return System.currentTimeMillis() + millis;
    }

    protected void idleCloseElement(ElementAndTimeWrapper<E> wrapper){
        PoolElement<E> poolElement = wrapper.getPoolElement();
        if (!poolElement.isCore()) {
            log.info("关闭闲置过期连接: {}", poolElement);
            closeElement(poolElement);
        }
    }

    static class ElementAndTimeWrapper<E>{
        final PoolElement<E> poolElement;
        long leaveTime;

        ElementAndTimeWrapper(PoolElement<E> poolElement, long leaveTime) {
            this.poolElement = poolElement;
            this.leaveTime = leaveTime;
        }


        public long getLeaveTime() {
            return leaveTime;
        }

        public PoolElement<E> getPoolElement() {
            return poolElement;
        }

        public void setLeaveTime(long leaveTime) {
            this.leaveTime = leaveTime;
        }
    }

    static class IdleListener<E> implements Runnable{

        private final AbstractIdlePool<E> pool;

        private LinkedList<ElementAndTimeWrapper<E>> socketAndTimeWrapperArrayList = new LinkedList<>();

        private ReentrantLock lock = new ReentrantLock();

        private Thread currentThread;

        private boolean close = false;

        public IdleListener(AbstractIdlePool<E> pool) {
            this.pool = pool;
        }

        public void register(ElementAndTimeWrapper<E> satw){
            lock.lock();
            try {
                socketAndTimeWrapperArrayList.add(satw);
                if (socketAndTimeWrapperArrayList.size() != 1){
                    sort();
                }
                LockSupport.unpark(currentThread);
            }finally {
                lock.unlock();
            }
        }

        public boolean isClose() {
            return close;
        }

        public void setClose(boolean close) {
            this.close = close;
            if (close){
                LockSupport.unpark(currentThread);
            }
        }

        private void sort(){
            ServiceUtils.sort(socketAndTimeWrapperArrayList, ele -> ele.getLeaveTime(), true);
        }

        @Override
        public void run() {
            IoLog log = pool.log;
            currentThread = Thread.currentThread();
            for (;;){
                if (isClose()){
                    break;
                }
                if (socketAndTimeWrapperArrayList.isEmpty()){
                    log.info("park 连接监听器");
                    LockSupport.park();
                    continue;
                }
                long sleep = 0;
                lock.lock();
                try {
                    long now = System.currentTimeMillis();
                    ElementAndTimeWrapper<E> wrapper = socketAndTimeWrapperArrayList.peek();
                    long leaveTime = wrapper.getLeaveTime();
                    sleep = leaveTime - now;
                    if (sleep <= 0){
                        closeRightNow();
                    }
                }finally {
                    lock.unlock();
                }
                if (sleep > 0){
                    sleep(sleep);
                }
            }
            log.info("连接监听器关闭");
        }

        private void sleep(long time){
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                Thread.interrupted();
                pool.log.error("Clean up idle connection thread is interrupted");
            }
        }

        private void closeRightNow(){
            ElementAndTimeWrapper<E> poll = socketAndTimeWrapperArrayList.poll();
            pool.idleCloseElement(poll);
        }
    }


}
