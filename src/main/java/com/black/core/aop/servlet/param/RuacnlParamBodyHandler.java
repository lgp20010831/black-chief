package com.black.core.aop.servlet.param;

import com.black.core.util.Av0;

//转驼峰式处理器
public class RuacnlParamBodyHandler extends AbstractMapParamBodyHandler{
    @Override
    String handlerKey(String originKey) {
        return Av0.ruacnl(originKey);
    }
}
