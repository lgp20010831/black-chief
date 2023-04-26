package com.black.core.aop.servlet.security;

import com.black.core.aop.servlet.HttpMethodWrapper;

public interface SecurityHandler {

    boolean doIntercept(Object[] args, HttpMethodWrapper mw);


}
