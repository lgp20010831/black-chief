package com.black.pool;

import com.black.bin.ApplyProxyFactory;
import com.black.socket.pool.SocketPoolException;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;

import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("all")
public abstract class AbstractPool<E> implements Pool<E>{

    protected IoLog log;

    private final int sort;

    private static AtomicInteger poolSort = new AtomicInteger(0);

    private final Configuration configuration;

    private boolean init = false;

    private boolean shutdown = false;

    private final BlockingQueue<PoolElement<E>> coreSocketQueue;

    private final BlockingQueue<PoolElement<E>> unusedSocketQueue;

    private final ReentrantLock lock = new ReentrantLock();

    private final LinkedList<Thread> waitForIdleThreadList = new LinkedList<>();

    public AbstractPool(Configuration configuration) {
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
        if (!configuration.isLazyInit() && structureInit()){
            init();
        }
        registerShutdown();
    }

    public BlockingQueue<PoolElement<E>> getCoreSocketQueue() {
        return coreSocketQueue;
    }

    public BlockingQueue<PoolElement<E>> getUnusedSocketQueue() {
        return unusedSocketQueue;
    }

    protected boolean structureInit(){
        return true;
    }

    protected void registerShutdown(){
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            shutdown();
        }, "socketPoolShutdown" + getSort()));
    }

    protected void checkShutdown(){
        if (shutdown){
            throw new PoolStateException("pool is shutdown");
        }
    }
    
    //从池中获取一个 socket 连接
    public E getElement(){
        checkShutdown();
        boolean waitForIdle = false;
        lock.lock();
        PoolElement<E> poolElement;
        try {
            if (!init){
                init();
            }
            poolElement = tryGetFormCorePool();
            if (poolElement == null){
                poolElement = tryGetFormUnusedPool();
            }
            if (poolElement == null){
                if (allowCreateUnused()){
                    poolElement = createUnusedElement();
                }else {
                    waitForIdle = true;
                }
            }
        }finally {
            lock.unlock();
        }

        if (poolElement != null){
            callbackGetElement(poolElement);
            poolElement.setEmploy(true);
            return poolElement.getProxy0();
        }

        if (waitForIdle){
            return waitForIdleAndGetNewConnection();
        }
        throw new PoolStateException("Connection pool status is abnormal");
    }

    protected void callbackGetElement(PoolElement<E> poolElement){

    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public int getSort() {
        return sort;
    }

    public void shutdown(){
        log.info("Pool[{}] shutdown start...", sort);
        shutdown = true;
        shutdown0();
        log.info("Pool[{}] shutdown finish...", sort);
    }

    protected void shutdown0(){
        for (PoolElement<E> poolElement : coreSocketQueue) {
            poolElement.close();
        }
        coreSocketQueue.clear();

        for (PoolElement<E> poolElement : unusedSocketQueue) {
            poolElement.close();
        }
        unusedSocketQueue.clear();
    }

    //向连接池中初始化核心连接数
    private void init(){
        if (init){
            return;
        }
        try {
            log.info("Pool[{}] Initialization start...", sort);
            init0();
            log.info("Pool[{}] Initialization finish...", sort);
        } catch (Throwable e) {
            log.error("Unable to get element during initialization");
            throw new SocketPoolException(e);
        } finally {
            init = true;
        }
    }

    private void init0() throws Throwable {
        int corePoolSize = configuration.getCorePoolSize();
        for (int i = 0; i < corePoolSize; i++) {
            PoolElement<E> poolElement = createElement();
            addQueue(poolElement, true);
        }
    }

    protected void addQueue(PoolElement<E> poolElement, boolean core){
        if (poolElement != null){
            int maxPoolSize = configuration.getMaxPoolSize();
            BlockingQueue<PoolElement<E>> target = core ? coreSocketQueue : unusedSocketQueue;
            if (maxPoolSize > 0 && target.size() >= maxPoolSize){
                throw new IllegalStateException("deny listing, the maximum number of connections has been reached");
            }
            poolElement.setCore(core);
            target.add(poolElement);
        }
    }

    //开启一个 socket 连接
    private PoolElement<E> createElement() throws Throwable {
        E e = create();
        log.info("创建新的元素: {}", e);
        return wrapperElement(e);
    }

    protected E create() throws Throwable {
        PoolElementFactory elementFactory = configuration.getPoolElementFactory();
        if (elementFactory != null){
            return (E) elementFactory.create();
        }
        E e = create0();
        if (e == null){
            throw new PoolStateException("create element must is not null");
        }
        return e;
    }

    protected abstract E create0() throws Throwable;

    protected String getCloseMethodName(){
        return null;
    }

    //对开启的 socket 连接进行一个代理
    protected PoolElement<E> wrapperElement(E e){
        PoolElement<E> element = new PoolElement<E>(this);
        E proxyE = ApplyProxyFactory.proxy(e, element);
        element.setProxy0(proxyE);
        element.setOrigin(e);
        return element;
    }

    protected synchronized void releaseConnection(PoolElement<E> poolElement){
        poolElement.setEmploy(false);
        if (!waitForIdleThreadList.isEmpty()){
            waitForIdleThreadList.get(0).interrupt();
        }else {
            closeElementIfUnused(poolElement);
        }
    }

    protected void closeElementIfUnused(PoolElement<E> poolElement){
        if (!poolElement.isCore()) {
            closeElement(poolElement);
        }
    }

    public IoLog getLog() {
        return log;
    }

    protected void closeElement(PoolElement<E> poolElement){
        poolElement.close();
        unusedSocketQueue.remove(poolElement);
    }

    protected E waitForIdleAndGetNewConnection(){
        log.info("等待闲置元素");
        long waitForIdleTime = configuration.getWaitForIdleTime();
        Thread thread = Thread.currentThread();
        waitForIdleThreadList.addFirst(thread);
        try {

            Thread.sleep(waitForIdleTime);
        } catch (InterruptedException e) {
            thread.interrupted();
            return getElement();
        }finally {
            waitForIdleThreadList.remove(thread);
        }
        throw new PoolStateException("Unable to get a new element after waiting for " + waitForIdleTime + " ms");
    }

    protected PoolElement<E> createUnusedElement(){
        PoolElement<E> poolElement = null;
        try {
            poolElement = createElement();
        } catch (Throwable e) {
            throw new PoolStateException(e);
        }
        addQueue(poolElement, false);
        return poolElement;
    }

    protected boolean allowCreateUnused(){
        if (configuration.getMaxPoolSize() < 0){
            return true;
        }
        int i = configuration.getMaxPoolSize() - configuration.getCorePoolSize();
        return unusedSocketQueue.size() < i;
    }

    protected PoolElement<E> tryGetFormUnusedPool(){
        return tryGetFromPool(unusedSocketQueue);
    }

    protected PoolElement<E> tryGetFormCorePool(){
        return tryGetFromPool(coreSocketQueue);
    }

    private static <E> PoolElement<E> tryGetFromPool(BlockingQueue<PoolElement<E>> queue){
        for (PoolElement<E> poolElement : queue) {
            if (!poolElement.isEmploy()) {
                return poolElement;
            }
        }
        return null;
    }


}
