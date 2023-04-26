package com.black.core.log;

import com.black.core.util.TextUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;

@Log4j2
public class CommonLog4jLog extends AbstractIoLog{

    public CommonLog4jLog(){
        AnsiOutput.setEnabled(AnsiOutput.Enabled.ALWAYS);
    }

    @Override
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    protected String getMsg(Object msg){
        return (prefix == null ? "" : prefix) + msg;
    }

    @Override
    public int info(Object msg, Object... params) {
        if (!isInfoEnabled()) return -1;
        String content = TextUtils.parseContent(getMsg(msg), params);
        AnsiColor color = ArrayIoLog.getColor(ArrayIoLog.INFO);
        log.info(AnsiOutput.toString(color, content));
        return 0;
    }

    @Override
    public int debug(Object msg, Object... params) {
        if (!isDebugEnabled()) return -1;
        String content = TextUtils.parseContent(getMsg(msg), params);
        AnsiColor color = ArrayIoLog.getColor(ArrayIoLog.DEBUG);
        log.debug(AnsiOutput.toString(color, content));
        return 0;
    }

    @Override
    public int error(Throwable e, Object msg, Object... params) {
        if (!isErrorEnabled()) return -1;
        String content = TextUtils.parseContent(getMsg(msg), params);
        AnsiColor color = ArrayIoLog.getColor(ArrayIoLog.ERROR);
        log.error(AnsiOutput.toString(color, content));
        return 0;
    }

    @Override
    public int trace(Object msg, Object... params) {
        if (!isTraceEnabled()) return -1;
        String content = TextUtils.parseContent(getMsg(msg), params);
        AnsiColor color = ArrayIoLog.getColor(ArrayIoLog.TRACE);
        log.trace(AnsiOutput.toString(color, content));
        return 0;
    }

    @Override
    public int flush(int index) {
        return 0;
    }
}
