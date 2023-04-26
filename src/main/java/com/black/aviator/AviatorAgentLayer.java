package com.black.aviator;

import com.black.aviator.annotation.BooleanExpress;
import com.black.aviator.annotation.ObjectExpress;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.cache.TypeConvertCache;
import com.black.core.convert.TypeHandler;
import com.black.core.query.MethodWrapper;
import com.black.core.spring.factory.AgentLayer;
import com.black.core.spring.factory.AgentObject;
import com.black.core.sql.code.MapArgHandler;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Getter @Setter
public class AviatorAgentLayer implements AgentLayer {

    private ObjectEnv objectEnv;

    @Override
    public Object proxy(AgentObject layer) throws Throwable {
        Object[] args = layer.getArgs();
        MethodWrapper mw = MethodWrapper.get(layer.getProxyMethod());
        final Map<String, Object> objectMap = MapArgHandler.parse(args, mw);
        final Map<String, Object> env = createParamEnv(objectMap);
        if (interceptMethod(layer)){
            BooleanExpress annotation = mw.getAnnotation(BooleanExpress.class);
            Boolean execute = (Boolean) AviatorManager.getInstance().execute(annotation.express(), env);
            if (!execute){
                //表示拦截, 不会执行原生方法
                return AviatorManager.getInstance().execute(annotation.otherwise(), env);
            }
        }

        Collection<ParameterWrapper> wrappers = mw.getParameterWrappersSet();
        for (ParameterWrapper wrapper : wrappers) {
            if (processorObjectParam(wrapper)){
                doProcessorObject(env, args, wrapper, objectMap);
            }

            if (processorBooleanParam(wrapper)){
                doProcessorBoolean(env, args, wrapper, objectMap);
            }
        }
        Object result = layer.doFlow(args);
        if (processorResult(mw)){
            Map<String, Object> resultEnv = createResultEnv(result);
            result = doProcessorResult(mw, resultEnv);
        }

        return result;
    }

    public Object doProcessorResult(MethodWrapper mw, Map<String, Object> env){
        ObjectExpress annotation = mw.getAnnotation(ObjectExpress.class);
        return AviatorManager.getInstance().execute(annotation.value(), env);
    }


    public boolean processorResult(MethodWrapper mw){
        return mw.hasAnnotation(ObjectExpress.class);
    }

    public void doProcessorObject(Map<String, Object> env, Object[] args, ParameterWrapper pw, Map<String, Object> objectMap){
        ObjectExpress annotation = pw.getAnnotation(ObjectExpress.class);
        Object execute = AviatorManager.getInstance().execute(annotation.value(), env);
        TypeHandler typeHandler = TypeConvertCache.initAndGet();
        if (typeHandler != null){
            execute = typeHandler.convert(pw.getType(), execute);
        }
        args[pw.getIndex()] = execute;
        objectMap.put(pw.getName(), execute);
    }

    public void doProcessorBoolean(Map<String, Object> env, Object[] args, ParameterWrapper pw, Map<String, Object> objectMap){
        BooleanExpress annotation = pw.getAnnotation(BooleanExpress.class);
        Boolean execute = (Boolean) AviatorManager.getInstance().execute(annotation.express(), env);
        Object newValue;
        if (execute){
            newValue = AviatorManager.getInstance().execute(annotation.then(), env);
        }else {
            newValue = AviatorManager.getInstance().execute(annotation.otherwise(), env);
        }

        TypeHandler typeHandler = TypeConvertCache.initAndGet();
        if (typeHandler != null){
            newValue = typeHandler.convert(pw.getType(), newValue);
        }
        args[pw.getIndex()] = newValue;
        objectMap.put(pw.getName(), newValue);
    }

    public boolean processorObjectParam(ParameterWrapper pw){
        return pw.hasAnnotation(ObjectExpress.class);
    }

    public boolean processorBooleanParam(ParameterWrapper pw){
        return pw.hasAnnotation(BooleanExpress.class);
    }

    public boolean interceptMethod(AgentObject object){
        Method method = object.getProxyMethod();
        return method.isAnnotationPresent(BooleanExpress.class);
    }

    public Map<String, Object> getBaseEnv(){
        Map<String, Object> env = new HashMap<>();
        synchronized (AviatorContext.initialEnvNames){
            for (String initialEnvName : AviatorContext.initialEnvNames) {
                env.put(initialEnvName, objectEnv.getCreateParam());
            }
        }

        synchronized (AviatorContext.fieldEnvNames){
            for (String fieldEnvName : AviatorContext.fieldEnvNames) {
                env.put(fieldEnvName, objectEnv.getFieldParam());
            }
        }
        return env;
    }

    public Map<String, Object> createResultEnv(Object result){
        Map<String, Object> baseEnv = getBaseEnv();
        synchronized (AviatorContext.methodResultEnvNames){
            for (String paramEnvName : AviatorContext.methodResultEnvNames) {
                baseEnv.put(paramEnvName, result);
            }
        }
        return baseEnv;
    }

    public Map<String, Object> createParamEnv(Map<String, Object> objectMap){
        Map<String, Object> baseEnv = getBaseEnv();
        synchronized (AviatorContext.paramEnvNames){
            for (String paramEnvName : AviatorContext.paramEnvNames) {
                baseEnv.put(paramEnvName, objectMap);
            }
        }
        return baseEnv;
    }



}
