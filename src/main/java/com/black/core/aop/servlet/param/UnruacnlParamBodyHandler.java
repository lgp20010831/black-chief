package com.black.core.aop.servlet.param;

import com.black.core.util.Av0;

//转下划线处理器
public class UnruacnlParamBodyHandler extends AbstractMapParamBodyHandler{

    @Override
    String handlerKey(String originKey) {
        return Av0.unruacnl(originKey);
    }

}
