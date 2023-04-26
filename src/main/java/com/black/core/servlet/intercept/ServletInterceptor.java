package com.black.core.servlet.intercept;


import lombok.extern.log4j.Log4j2;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

@Log4j2
public class ServletInterceptor implements HandlerInterceptor {

    private final ArrayList<HandlerInterceptor> handlerInterceptorQueue = new ArrayList<>();

    public void registerInterceptor(HandlerInterceptor interceptor){
        if (interceptor != null && !handlerInterceptorQueue.contains(interceptor)){
            handlerInterceptorQueue.add(interceptor);
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("ServletInterceptor- preHandler, springMvc.handler:{}", handler);
        }
        for (HandlerInterceptor handlerInterceptor : handlerInterceptorQueue) {
            if (handlerInterceptor.preHandle(request, response, handler)) {
                if (log.isDebugEnabled()) {
                    log.debug("拦截器:{} 处理结束,放行", handlerInterceptor.getClass().getSimpleName());
                }
                continue;
            }
            if (log.isInfoEnabled()) {
                log.info("拦截器: {}, 拦截请求,\n 请求信息:{}", handlerInterceptor.getClass().getSimpleName(), handler);
            }
            return false;
        }
        return true;
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("ServletInterceptor-postHandler, springMvc.handler:{};\n modelAndView:{}", handler, modelAndView);
        }
        for (HandlerInterceptor handlerInterceptor : handlerInterceptorQueue) {
            handlerInterceptor.postHandle(request, response, handler, modelAndView);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        for (HandlerInterceptor handlerInterceptor : handlerInterceptorQueue) {
            handlerInterceptor.afterCompletion(request, response, handler, ex);
        }
    }

}
