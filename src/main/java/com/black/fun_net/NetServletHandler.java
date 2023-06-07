package com.black.fun_net;

import com.black.bin.ApplyProxyLayer;
import com.black.bin.ProxyTemplate;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.tools.BeanUtil;
import lombok.NonNull;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.Method;

@SuppressWarnings("all")
public class NetServletHandler implements ApplyProxyLayer {

    private final Net servletParent;

    private final ClassWrapper<?> servletParentClass;

    public NetServletHandler(@NonNull Net servletParent) {
        this.servletParent = servletParent;
        servletParentClass = BeanUtil.getPrimordialClassWrapper(servletParent);
    }

    @Override
    public Object proxy(Object[] args, Method method, Class<?> beanClass, ProxyTemplate template) throws Throwable {
        String name = method.getName();
        if (!isServletMethod(name)){
            return template.invokeOriginal(args);
        }

        Servlet servlet = findServlet(name);
        if (isPostMethod(servlet)){
            handlerRequestBody(args, method);
        }
        try {
            if (servlet != null){
                servlet.fetch();
            }
            Object response = servletParent.obtainWriteResult();
            return response;
        }finally {
            servletParent.fetchFinishCallback();
        }
    }


    protected void handlerRequestBody(Object[] args, Method method){
        MethodWrapper methodWrapper = MethodWrapper.get(method);
        ParameterWrapper bodyPw = methodWrapper.getSingleParameterByAnnotation(RequestBody.class);
        if (bodyPw != null){
            Object body = args[bodyPw.getIndex()];
            servletParent.setBody(body);
        }
    }

    protected boolean isServletMethod(String name){
        return servletParentClass.getField(name) != null;
    }

    protected boolean isPostMethod(Servlet servlet){
        return servlet instanceof Post;
    }


    protected Servlet findServlet(String name){
        FieldWrapper fw = servletParentClass.getField(name);
        return fw.getValue(servletParent, Servlet.class);
    }
}
