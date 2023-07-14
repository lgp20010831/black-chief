package com.black.core.aop.servlet.ui;

import com.black.core.aop.servlet.HttpMethodWrapper;

/**
 * @author 李桂鹏
 * @create 2023-07-12 9:42
 */
@SuppressWarnings("all")
public interface LogFrameUI {

    void log(HttpMethodWrapper methodWrapper) throws Throwable;

}
