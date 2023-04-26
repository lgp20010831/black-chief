package com.black.core.ill.aop;

import com.black.core.chain.Order;

public interface IllHandler extends Order {

    boolean support(IllSourceWrapper sourceWrapper);

    /***
     * 处理异常
     * @param sourceWrapper 对异常起源地的封装
     * @return 返回 true, 向下流动,且如果最后一层, 会将异常跑出, false：不抛出异常
     */
    boolean intercept(IllSourceWrapper sourceWrapper);

    /***
     * 当 intercept 返回 false 时,才会调用
     * @param sourceWrapper source
     * @return 返回代替此方法的结果
     */
    default Object handler(IllSourceWrapper sourceWrapper){
        return null;
    }
}
