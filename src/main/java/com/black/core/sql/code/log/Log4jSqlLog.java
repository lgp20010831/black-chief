package com.black.core.sql.code.log;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class Log4jSqlLog implements Log{
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
        log.info(str);
    }

    @Override
    public void debug(String str) {
        log.debug(str);
    }

    @Override
    public void error(String str) {
        log.error(str);
    }
}
