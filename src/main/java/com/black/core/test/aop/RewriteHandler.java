package com.black.core.test.aop;


import com.black.nest.NestDictController;
import com.black.spring.agency.Invocation;
import com.black.spring.agency.Rewrite;
import com.black.spring.agency.RewriteOnAnnotation;
import com.black.spring.mapping.RewriteMapping;
import com.black.core.annotation.Sort;
import com.black.core.api.ApiService;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

//@Agency
public class RewriteHandler {


    @Sort(50)
    @Rewrite(target = NestDictController.class, name = "init")
    Object init(Invocation invocation) throws Throwable {
        System.out.println("重写 invocation 方法");
        return invocation.invoke();
    }

    /*
        request -> agency -> (子链路: all -> init -> init2) -> cglib proxy -> apply ->
        aop Task chain -> aopControllerIntercept -> controller

     */
    @Sort(10)
    @RewriteMapping("**/init_nest_dict")
    Object init2(Invocation invocation, HttpServletRequest request) throws Throwable {
        System.out.println("重写 init 方法");
        return invocation.invoke();
    }

    @Sort(1000)
    @RewriteMapping("/**")
    Object all(Invocation invocation, HttpServletRequest request) throws Throwable {
        System.out.println("全局控制器日志: " + request.getRequestURI());
        return invocation.invoke();
    }

    @Sort(1200)
    @RewriteOnAnnotation(RequestMapping.class)
    Object web(Invocation invocation) throws Throwable {
        System.out.println("代理所有 web");
        return invocation.invoke();
    }

    @Rewrite(target = ApiService.class, name = "api")
    void letApi(){
        System.out.println("拦截 api 接口");
    }
}
