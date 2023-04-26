package com.black.core.entry;


import com.black.core.cache.TypeConvertCache;
import com.black.core.convert.TypeHandler;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

//每一个条目方法都会生成对应的 configuration 类
public class ItemConfiguration implements Configuration{

    //原始类
    private final Class<?> originClass;

    //原始方法
    private final Method originMethod;

    //唯一条目
    private final String item;

    private final Object originObj;

    private List<String> paramterNameList;

    public ItemConfiguration(Class<?> originClass, Method originMethod,
                             String item, Object originObj) {
        this.originClass = originClass;
        this.originMethod = originMethod;
        this.item = item;
        this.originObj = originObj;
    }

    public List<String> getParamterNameList() {
        if (paramterNameList == null){
            paramterNameList = new ArrayList<>();
            for (Parameter parameter : originMethod.getParameters()) {
                paramterNameList.add(parameter.getName());
            }
        }
        return paramterNameList;
    }

    //执行条目方法, 传递参数列表
    public Object invoke(Object[] args){
        try {

            int parameterCount = originMethod.getParameterCount();
            Object[] iArgs = new Object[parameterCount];
            if (args == null || args.length == 0){
                return originMethod.invoke(originObj, iArgs);
            }else {

                if (args.length > parameterCount){
                    throw new RuntimeException("参数列表 size > 方法参数列表 szie");
                }else

                //copy args element to iArgs
                if (args.length < parameterCount){
                    System.arraycopy(args, 0, iArgs, 0, args.length);
                }else

                if (args.length == parameterCount){
                    iArgs = args;
                }
                handlerArgs(iArgs);
                //handler iArgs
                return originMethod.invoke(originObj, iArgs);
            }

        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

    protected void handlerArgs(Object[] args){
        Parameter[] parameters = originMethod.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Class<?> type = parameter.getType();
            Object arg = args[i];
            if (arg != null){
                if (!type.isAssignableFrom(arg.getClass())){
                    TypeHandler typeHandler = TypeConvertCache.initAndGet();
                    if (typeHandler != null){
                        args[i] = typeHandler.convert(type, arg);
                    }
                }
            }
        }
    }

    public String getItem() {
        return item;
    }
}
