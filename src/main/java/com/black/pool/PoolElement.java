package com.black.pool;


import com.black.bin.ApplyProxyLayer;
import com.black.bin.ProxyTemplate;
import com.black.core.log.IoLog;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.tools.BeanUtil;

import java.lang.reflect.Method;

public class PoolElement<T> implements ApplyProxyLayer {

    private final AbstractPool<T> pool;

    private T origin;

    private T proxy0;

    private boolean core;

    //占用标记
    private boolean employ = false;


    public PoolElement(AbstractPool<T> pool) {
        this.pool = pool;
    }

    public AbstractPool<T> getPool() {
        return pool;
    }

    public boolean isCore() {
        return core;
    }

    public void setCore(boolean core) {
        this.core = core;
    }

    public boolean isEmploy() {
        return employ;
    }

    public void setEmploy(boolean employ) {
        this.employ = employ;
    }

    public T getOrigin() {
        return origin;
    }

    public void setOrigin(T origin) {
        this.origin = origin;
        if (!(origin instanceof Closeable)){
            checkAndGet(origin);
        }
    }

    public T getProxy0() {
        return proxy0;
    }

    public void setProxy0(T proxy0) {
        this.proxy0 = proxy0;
    }

    private MethodWrapper checkAndGet(Object origin){
        String closeMethodName = pool.getCloseMethodName();
        ClassWrapper<?> cw = BeanUtil.getPrimordialClassWrapper(origin);
        MethodWrapper mw = null;
        if (closeMethodName != null){
            mw = cw.getSingleMethod(closeMethodName);
            if (mw != null){
                if (!mw.getReturnType().equals(void.class) || mw.getParameterCount() != 0){
                    throw new PoolStateException("指定的关闭方法必须是无参且无返回值");
                }
                return mw;
            }
        }
        mw = cw.getSingleMethodByAnnotation(Shutdown.class);
        if (mw == null){
            throw new PoolStateException("can not find shutdown method");
        }
        return mw;
    }

    public void close(){
        IoLog log = pool.getLog();
        log.info("close element: {}", proxy0);
        try {
            if (origin instanceof Closeable){
                ((Closeable) origin).close();
            }else {
                MethodWrapper methodWrapper = checkAndGet(origin);
                methodWrapper.invoke(origin);
            }
        } catch (Throwable e) {
            //ignore
        }
    }



    @Override
    public Object proxy(Object[] args, Method method, Class<?> beanClass, ProxyTemplate template) throws Throwable {
        String name = method.getName();
        Shutdown annotation = method.getAnnotation(Shutdown.class);
        if (method.getReturnType().equals(void.class) && (name.equals("close") || annotation != null)){
            pool.releaseConnection(this);
            return null;
        }
        return template.invokeOriginal(args);
    }
}
