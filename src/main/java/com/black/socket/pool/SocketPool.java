package com.black.socket.pool;

import com.black.bin.ApplyProxyFactory;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("ALL")
public class SocketPool {

    protected IoLog log;

    private final int sort;

    private static AtomicInteger poolSort = new AtomicInteger(0);

    private final SocketPoolConfiguration configuration;

    private boolean init = false;

    private boolean shutdown = false;

    private final BlockingQueue<SocketProxy> coreSocketQueue;

    private final BlockingQueue<SocketProxy> unusedSocketQueue;

    private final ReentrantLock lock = new ReentrantLock();

    private final LinkedList<Thread> waitForIdleThreadList = new LinkedList<>();

    public SocketPool(SocketPoolConfiguration configuration) {
        this.configuration = configuration;
        log = LogFactory.getLog4j();
        sort = poolSort.incrementAndGet();
        int corePoolSize = configuration.getCorePoolSize();
        int maxPoolSize = configuration.getMaxPoolSize();
        if (corePoolSize < 0 || (maxPoolSize > 0 && maxPoolSize < corePoolSize)){
            throw new IllegalStateException("ill state");
        }
        coreSocketQueue = new ArrayBlockingQueue<>(corePoolSize);
        //根据最大连接数, 选择创建什么类型的队列
        if (maxPoolSize < 0){
            unusedSocketQueue = new LinkedBlockingQueue<>();
        }else {
            unusedSocketQueue = new ArrayBlockingQueue<>(maxPoolSize - corePoolSize);
        }
        if (!configuration.isLazyInit()){
            init();
        }
        registerShutdown();
    }

    protected void registerShutdown(){
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            shutdown();
        }, "socketPoolShutdown" + getSort()));
    }

    protected void checkShutdown(){
        if (shutdown){
            throw new SocketPoolException("pool is shutdown");
        }
    }

    //从池中获取一个 socket 连接
    public Socket getConnection(){
        checkShutdown();
        boolean waitForIdle = false;
        lock.lock();
        SocketProxy socketProxy;
        try {
            if (!init){
                init();
            }
            socketProxy = tryGetFormCorePool();
            if (socketProxy == null){
                socketProxy = tryGetFormUnusedPool();
            }
            if (socketProxy == null){
                if (allowCreateUnused()){
                    socketProxy = createUnusedConnection();
                }else {
                    waitForIdle = true;
                }
            }
        }finally {
            lock.unlock();
        }

        if (socketProxy != null){
            callbackGetSocket(socketProxy);
            socketProxy.setEmploy(true);
            return socketProxy.getProxy0();
        }

        if (waitForIdle){
            return waitForIdleAndGetNewConnection();
        }
        throw new SocketPoolException("Connection pool status is abnormal");
    }

    protected void callbackGetSocket(SocketProxy socketProxy){

    }

    public SocketPoolConfiguration getConfiguration() {
        return configuration;
    }

    public int getSort() {
        return sort;
    }

    public void shutdown(){
        log.info("socketPool[{}] shutdown start...", sort);
        shutdown = true;
        shutdown0();
        log.info("socketPool[{}] shutdown finish...", sort);
    }

    protected void shutdown0(){
        for (SocketProxy socketProxy : coreSocketQueue) {
            socketProxy.closeSocket();
        }
        coreSocketQueue.clear();

        for (SocketProxy socketProxy : unusedSocketQueue) {
            socketProxy.closeSocket();
        }
        unusedSocketQueue.clear();
    }

    //向连接池中初始化核心连接数
    private void init(){
        if (init){
            return;
        }
        try {
            log.info("socketPool[{}] Initialization start...", sort);
            init0();
            log.info("socketPool[{}] Initialization finish...", sort);
        } catch (IOException e) {
            log.error("Unable to get connection during initialization");
            throw new SocketPoolException(e);
        } finally {
            init = true;
        }
    }

    private void init0() throws IOException {
        int corePoolSize = configuration.getCorePoolSize();
        for (int i = 0; i < corePoolSize; i++) {
            SocketProxy socketProxy = openConnection();
            addQueue(socketProxy, true);
        }
    }

    protected void addQueue(SocketProxy socketProxy, boolean core){
        if (socketProxy != null){
            int maxPoolSize = configuration.getMaxPoolSize();
            BlockingQueue<SocketProxy> target = core ? coreSocketQueue : unusedSocketQueue;
            if (maxPoolSize > 0 && target.size() >= maxPoolSize){
                throw new IllegalStateException("deny listing, the maximum number of connections has been reached");
            }
            socketProxy.setCore(core);
            target.add(socketProxy);
        }
    }

    //开启一个 socket 连接
    private SocketProxy openConnection() throws IOException {
        Socket socket = createSocket();
        log.info("开启新的连接: {}", socket);
        return openConnection0(socket);
    }

    protected Socket createSocket() throws IOException {
        Socket socket = new Socket();
        InetSocketAddress address = new InetSocketAddress(configuration.getServerHost(), configuration.getServerPort());
        socket.connect(address);
        return socket;
    }

    //对开启的 socket 连接进行一个代理
    protected SocketProxy openConnection0(Socket socket){
        SocketProxy socketProxy = new SocketProxy(this);
        Socket proxy0 = ApplyProxyFactory.proxy(socket, socketProxy);
        socketProxy.setProxy0(proxy0);
        socketProxy.setOrigin(socket);
        return socketProxy;
    }

    protected synchronized void releaseConnection(SocketProxy socketProxy){
        socketProxy.setEmploy(false);
        if (!waitForIdleThreadList.isEmpty()){
            waitForIdleThreadList.get(0).interrupt();
        }else {
            closeSocketIfUnused(socketProxy);
        }
    }

    protected void closeSocketIfUnused(SocketProxy socketProxy){
        if (!socketProxy.isCore()) {
            closeSocket(socketProxy);
        }
    }

    public IoLog getLog() {
        return log;
    }

    protected void closeSocket(SocketProxy socketProxy){

        socketProxy.closeSocket();
        unusedSocketQueue.remove(socketProxy);
    }

    protected Socket waitForIdleAndGetNewConnection(){
        log.info("等待闲置连接");
        long waitForIdleTime = configuration.getWaitForIdleTime();
        Thread thread = Thread.currentThread();
        waitForIdleThreadList.addFirst(thread);
        try {

            Thread.sleep(waitForIdleTime);
        } catch (InterruptedException e) {
            thread.interrupted();
            return getConnection();
        }finally {
            waitForIdleThreadList.remove(thread);
        }
        throw new SocketPoolException("Unable to get a new connection after waiting for " + waitForIdleTime + " ms");
    }

    protected SocketProxy createUnusedConnection(){
        SocketProxy socketProxy = null;
        try {
            socketProxy = openConnection();
        } catch (IOException e) {
            throw new SocketPoolException(e);
        }
        addQueue(socketProxy, false);
        return socketProxy;
    }

    protected boolean allowCreateUnused(){
        if (configuration.getMaxPoolSize() < 0){
            return true;
        }
        int i = configuration.getMaxPoolSize() - configuration.getCorePoolSize();
        return unusedSocketQueue.size() < i;
    }

    protected SocketProxy tryGetFormUnusedPool(){
        return tryGetFromPool(unusedSocketQueue);
    }

    protected SocketProxy tryGetFormCorePool(){
        return tryGetFromPool(coreSocketQueue);
    }

    private static SocketProxy tryGetFromPool(BlockingQueue<SocketProxy> queue){
        for (SocketProxy socketProxy : queue) {
            if (!socketProxy.isEmploy()) {
                return socketProxy;
            }
        }
        return null;
    }

}
