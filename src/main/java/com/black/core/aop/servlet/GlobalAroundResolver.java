package com.black.core.aop.servlet;

import com.black.core.aop.code.HijackObject;
import com.black.core.chain.Order;


public interface GlobalAroundResolver extends Order {

    /***
     * 处理参数, 不管有没有拦截, 都会执行此方法回调
     * @param args 返回作为控制器方法的参数
     * @param mw 对控制器方法的封装
     * @return 返回加工后的参数
     */
    default Object[] handlerArgs(Object[] args, HttpMethodWrapper mw){
        return args;
    }

    /**
     * 在即将执行主要方法前执行, 如果拦截方法拦截生效, 则不会执行此方法
     * @param args 返回作为控制器方法的参数
     * @param mw 对控制器方法的封装
     * @return 返回加工后的参数
     */
    default Object[] beforeInvoke(Object[] args, HttpMethodWrapper mw){
        return args;
    }

    /***
     * 当业务方法执行完成以后执行, 如果拦截方法拦截生效, 则不会执行此方法
     * @param result 业务
     * @param httpMethodWrapper 对控制器方法的封装
     * @param responseClass 响应类 class 对象
     * @return 返回响应结果, 可以不用去实例化响应类
     */
    default Object handlerAfterInvoker(Object result, HttpMethodWrapper httpMethodWrapper, Class<? extends RestResponse> responseClass){
        return result;
    }

    /***
     * 拦截方法, 如果返回 true, 则不会走业务方法, false 表示放行
     * @param args 返回作为控制器方法的参数
     * @param mw 对控制器方法的封装
     * @return 是否拦截
     */
    default boolean intercept(Object[] args, HttpMethodWrapper mw){
        return false;
    }

    /***
     * 如果拦截成功, intercept 返回 true 时回调此方法, 如果放行, 则不会执行此方法
     * @param hijack  可以执行主体方法的封装对象
     * @param args 返回作为控制器方法的参数
     * @param mw 对控制器方法的封装
     * @param chainResult 经过拦截链的响应结果
     * @return 响应结果
     */
    default Object interceptCallBack(HijackObject hijack, Object[] args, HttpMethodWrapper mw, Object chainResult){
        return chainResult;
    }

    /***
     * 当业务方法发生异常, 并抛出后, 回调此方法
     * @param e 异常
     * @param responseClass 响应类 class 对象
     * @param mw 对控制器方法的封装
     * @return 返回处理异常过后的响应结果
     * @throws Throwable 如果不想处理异常, 则将异常抛出, 由接下来的处理器处理
     */
    default Object handlerException(Throwable e, Class<? extends RestResponse> responseClass, HttpMethodWrapper mw)
            throws Throwable{
        throw e;
    }

    /***
     * 创建响应类, 当程序正常执行的时候
     * @param emtryBean 会现根据这个响应类型的无参构造创建一个没有加载的对象
     * @param result 正常结束的结果
     * @param mw 方法
     * @return 如果此处给他进行过属性填充了, 返回true, 则不会继续询问其他处理器了
     *          如果所有处理器都返回 false, 则会有自己的默认值
     */
    default boolean createResponseByRegular(RestResponse emtryBean, Object result, HttpMethodWrapper mw){
        return false;
    }

    /***
     * 创建响应类当程序发生异常时
     * @param emtryBean 会现根据这个响应类型的无参构造创建一个没有加载的对象
     * @param ex 发生的异常
     * @param mw 方法
     * @return 如果此处给他进行过属性填充了, 返回true, 则不会继续询问其他处理器了
     *          如果所有处理器都返回 false, 则会有自己的默认值
     */
    default boolean createResponseByError(RestResponse emtryBean, Throwable ex, HttpMethodWrapper mw){
        return false;
    }

}
