package com.black.core.log;

public abstract class AbstractIoLog implements IoLog{

    protected String prefix;


    @Override
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

    @Override
    public int info(Object msg, Object... params) {
      return 0;
    }

    @Override
    public int debug(Object msg, Object... params) {
        return 0;
    }

    @Override
    public int error(Throwable e, Object msg, Object... params) {
        return 0;
    }

    @Override
    public int trace(Object msg, Object... param) {
        return 0;
    }

    @Override
    public int flush(int index) {
        return 0;
    }

}
