package com.black.share;

/**
 * @author 李桂鹏
 * @create 2023-05-04 11:47
 */
@SuppressWarnings("all")
public interface MethodInvoker {

    Object invokeMethod(String name, Object... args) throws Throwable;

}
