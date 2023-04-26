package com.black.utils;

import com.black.core.sql.code.log.Log;

public class CachableLog implements Log {
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
        System.out.println(str);
    }

    @Override
    public void debug(String str) {
        System.out.println(str);
    }

    @Override
    public void error(String str) {
        System.out.println(str);
    }
}
