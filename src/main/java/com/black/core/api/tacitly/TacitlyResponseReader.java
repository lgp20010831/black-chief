package com.black.core.api.tacitly;

import com.black.core.api.handler.ExampleStreamAdapter;
import com.black.core.api.handler.ResponseExampleReader;
import com.black.core.mvc.response.Response;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class TacitlyResponseReader implements ResponseExampleReader {
    @Override
    public String handlerApiResponseExample(Class<?> responseClass, Map<String, String> params,
                                            Method method, Class<?> controllerClass, List<Class<?>> pojoClasses,
                                            String example, ExampleStreamAdapter adapter) {
        final String name = method.getName();
        if (responseClass.equals(Response.class)){
            //主要处理这个类
            adapter.addParam("code", "int")
                    .addParam("message")
                    .addParam("total", "int")
                    .addParam("successful", "boolean");

            if (name.startsWith("query") || name.startsWith("select") || name.startsWith("get")){
                adapter.addAttributeListParam("result", pojoClasses.toArray(new Class[0]));
            }
            return adapter.rewrite();
        }
        return example;
    }
}
