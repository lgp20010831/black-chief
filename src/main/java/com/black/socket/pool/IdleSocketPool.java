package com.black.socket.pool;


import com.black.core.log.IoLog;
import com.black.utils.ServiceUtils;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("all")
public class IdleSocketPool extends SocketWrapperPool{

    private final IdleSocketListener idleSocketListener;

    private final Map<SocketProxy, SocketAndTimeWrapper> cache = new ConcurrentHashMap<>();

    public IdleSocketPool(SocketPoolConfiguration configuration) {
        super(configuration);
        idleSocketListener = new IdleSocketListener(this);
        new Thread(idleSocketListener, "idle socket" + getSort()).start();
    }

    @Override
    protected void shutdown0() {
        idleSocketListener.setClose(true);
        super.shutdown0();
        cache.clear();
    }

    @Override
    protected void closeSocketIfUnused(SocketProxy socketProxy) {

        //重写, 不在立即销毁闲置连接池中的连接
    }

    @Override
    protected void callbackGetSocket(SocketProxy socketProxy) {
        if (!socketProxy.isCore()){
            SocketAndTimeWrapper wrapper = cache.get(socketProxy);
            if (wrapper != null){
                log.info("重置连接过期时间: {}", socketProxy);
                wrapper.setLeaveTime(getLeaveTime());
                idleSocketListener.sort();
            }
        }
    }

    @Override
    protected void addQueue(SocketProxy socketProxy, boolean core) {
        super.addQueue(socketProxy, core);
        if (!socketProxy.isCore()){
            //如果不是核心连接, 则设置过期时间
            SocketAndTimeWrapper wrapper = new SocketAndTimeWrapper(socketProxy, getLeaveTime());
            cache.put(socketProxy, wrapper);
            idleSocketListener.register(wrapper);
            log.info("监听闲置连接: {}", socketProxy);
        }
    }

    private long getLeaveTime(){
        SocketPoolConfiguration configuration = getConfiguration();
        long unusedSocketKeepAlive = configuration.getUnusedSocketKeepAlive();
        TimeUnit unusedSocketTimeUnit = configuration.getUnusedSocketTimeUnit();
        long millis = unusedSocketTimeUnit.toMillis(unusedSocketKeepAlive);
        return System.currentTimeMillis() + millis;
    }

    protected void idleCloseSocket(SocketAndTimeWrapper wrapper){
        SocketProxy socketProxy = wrapper.getSocketProxy();
        if (!socketProxy.isCore()) {
            log.info("关闭闲置过期连接: {}", socketProxy);
            closeSocket(socketProxy);
        }
    }

    static class SocketAndTimeWrapper{
        final SocketProxy socketProxy;
        long leaveTime;
        SocketAndTimeWrapper(SocketProxy socketProxy, long leaveTime) {
            this.socketProxy = socketProxy;
            this.leaveTime = leaveTime;
        }

        public long getLeaveTime() {
            return leaveTime;
        }

        public SocketProxy getSocketProxy() {
            return socketProxy;
        }

        public void setLeaveTime(long leaveTime) {
            this.leaveTime = leaveTime;
        }
    }

    static class IdleSocketListener implements Runnable{

        private final IdleSocketPool pool;

        private LinkedList<SocketAndTimeWrapper> socketAndTimeWrapperArrayList = new LinkedList<>();

        private ReentrantLock lock = new ReentrantLock();

        private Thread currentThread;

        private boolean close = false;

        public IdleSocketListener(IdleSocketPool pool) {
            this.pool = pool;
        }

        public void register(SocketAndTimeWrapper satw){
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
                    SocketAndTimeWrapper wrapper = socketAndTimeWrapperArrayList.peek();
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
            SocketAndTimeWrapper poll = socketAndTimeWrapperArrayList.poll();
            pool.idleCloseSocket(poll);
        }
    }

}
