package com.black.core.entry;

import com.black.core.query.MethodWrapper;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

public class ProcessingItemConfiguration implements Configuration{

    //原始类
    private final Class<?> originClass;

    //原始方法
    private final MethodWrapper originMethod;

    //唯一条目
    private final String item;

    private final Object originObj;

    private List<String> paramterNameList;

    public ProcessingItemConfiguration(Class<?> originClass, Method originMethod,
                             String item, Object originObj) {
        this.originClass = originClass;
        this.originMethod = MethodWrapper.get(originMethod);
        this.item = item;
        this.originObj = originObj;
    }

    @Override
    public String getItem() {
        return item;
    }

    @Override
    public Object invoke(Object[] args) {
        return originMethod.invoke(args);
    }

    @Override
    public Collection<String> getParamterNameList() {
        return originMethod.getParameterNames();
    }
}
