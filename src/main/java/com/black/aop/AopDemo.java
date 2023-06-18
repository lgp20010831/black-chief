package com.black.aop;

import com.black.core.factory.beans.annotation.NotNull;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 李桂鹏
 * @create 2023-06-06 15:30
 */
@SuppressWarnings("all") @AopIntercept
public class AopDemo {


    @InterceptOnAnnotation({RequestMapping.class})
    void myIntercept(@NotNull HttpServletRequest request){
        System.out.println("打印接口: " + request.getRequestURI());
    }

}
