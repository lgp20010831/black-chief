package com.black.nio.group;

import com.black.function.Callable;
import com.black.function.Runnable;
import com.black.throwable.IOSException;

public abstract class AbstractGioContext implements GioContext{

    protected final NioType type;

    protected final Configuration configuration;

    protected final ContextType contextType;
    public AbstractGioContext(NioType type, Configuration configuration, ContextType contextType) {
        this.type = type;
        this.configuration = configuration;
        this.contextType = contextType;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public ContextType getContextType() {
        return contextType;
    }

    @Override
    public NioType getType() {
        return type;
    }

    @Override
    public void writeAndFlush(Object source) {
        write(source);
        flush();
    }

    protected void castIosTask(Runnable runnable){
        try {
            runnable.run();
        } catch (Throwable e) {
            throw new IOSException(e);
        }
    }


    protected <C> C castIosCall(Callable<C> cCallable){
        try {
            return cCallable.call();
        } catch (Throwable e) {
            throw new IOSException(e);
        }
    }
}
