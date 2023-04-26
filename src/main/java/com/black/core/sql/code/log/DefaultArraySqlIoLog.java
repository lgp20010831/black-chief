package com.black.core.sql.code.log;

import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;


public class DefaultArraySqlIoLog implements Log {

    private final IoLog arrayIoLog;

    public DefaultArraySqlIoLog() {
        arrayIoLog = LogFactory.getArrayLog();
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public void info(String str) {
        if (str != null && str.startsWith("===>>")){
            arrayIoLog.debug(str);
        }
    }

    @Override
    public void debug(String str) {
        if (str != null && str.startsWith("==>")){
            arrayIoLog.info(str);
        }else if (str != null && str.startsWith("<==")){
            arrayIoLog.error(null, str);
        }else if (str != null && str.startsWith("-->")){
            arrayIoLog.debug(str);
        }
    }

    @Override
    public void error(String str) {
        arrayIoLog.error(null, str);
    }
}
