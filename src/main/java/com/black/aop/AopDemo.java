package com.black.aop;

import com.black.core.annotation.ChiefServlet;
import com.black.core.annotation.Sort;
import com.black.log.Lgwr;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author 李桂鹏
 * @create 2023-06-06 15:30
 */
@SuppressWarnings("all")
public class AopDemo {

    @Sort
    @InterceptOnAnnotation(Lgwr.class)
    @InterceptClass(annAt = ChiefServlet.class)
    @InterceptMethod(annAt = RequestMapping.class)
    Object myIntercept(Handle handle, Lgwr lgwr){
        return handle.invoke();
    }

}
