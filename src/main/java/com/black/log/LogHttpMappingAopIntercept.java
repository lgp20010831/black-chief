package com.black.log;

import com.alibaba.fastjson.JSON;
import com.black.core.aop.AopMethodDirectAgent;
import com.black.core.aop.annotation.AopHybrid;
import com.black.core.aop.annotation.HybridSort;
import com.black.core.aop.code.AopMatchTargetClazzAndMethodMutesHandler;
import com.black.core.aop.code.AopTaskIntercepet;
import com.black.core.aop.code.AopTaskManagerHybrid;
import com.black.core.aop.code.HijackObject;
import com.black.core.servlet.HttpRequestUtil;
import com.black.core.util.AnnotationUtils;
import com.black.holder.SpringHodler;
import com.black.utils.ServiceUtils;
import org.springframework.beans.BeansException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author 李桂鹏
 * @create 2023-06-06 13:36
 */
@SuppressWarnings("all")
@AopHybrid
@HybridSort(13)
public class LogHttpMappingAopIntercept implements AopTaskManagerHybrid, AopTaskIntercepet {
    private final AtomicBoolean hook = new AtomicBoolean(false);

    @Override
    public AopMatchTargetClazzAndMethodMutesHandler obtainMatcher() {
        AopMethodDirectAgent instance = AopMethodDirectAgent.getInstance();
        instance.register(this, ((targetClazz, method) -> {
            return AnnotationUtils.isPertain(targetClazz, Lgwr.class) || AnnotationUtils.isPertain(method, Lgwr.class);
        }));
        return instance.getHandler(this);
    }

    @Override
    public AopTaskIntercepet obtainAopTaskIntercept() {
        return this;
    }

    @Override
    public Object processor(HijackObject hijack) throws Throwable {
        Method method = hijack.getMethod();
        Class<?> clazz = hijack.getClazz();
        Lgwr annotation = AnnotationUtils.obtainAnnotation(clazz, method, Lgwr.class);
        Object[] args = hijack.getArgs();
        HttpServletRequest request = LogHandler.getRequest();
        Object result = hijack.doRelease(args);
        if (annotation != null){
            SerializeGlobalLogConfiguration configuration = SerializeGlobalLogConfiguration.getInstance();
            configuration.setSerializeInAsync(false);
            if (!hook.get()){
                hook.set(true);
                configuration.addRecordCallbacks(record -> {
                    record.setControllerName(clazz.getSimpleName());
                    record.setJavaMethod(method.getName());
                    record.setModel(LogHandler.getModel(clazz, method));
                    record.setOperParam(Arrays.toString(args));
                    try {
                        String jsonString = JSON.toJSONString(result);
                        record.setJsonResult(jsonString);
                    }catch (RuntimeException e){
                        record.setJsonResult(String.valueOf(result));
                    }
                    if (request != null){
                        record.setUrl(request.getRequestURI());
                        record.setRequestMethod(request.getMethod());
                        record.setOperIp(HttpRequestUtil.getIpAddr(request));
                    }
                });
            }
            Map<String, Object> env = new LinkedHashMap<>();
            env.put("url", request == null ? "unknown host": request.getRequestURI());
            env.put("controllerName", clazz.getSimpleName());
            env.put("methodName", method.getName());
            env.put("request", request);
            env.put("controller", clazz);
            String prepareText = annotation.value();
            String logText = ServiceUtils.patternGetValue(env, prepareText);
            Logs.info(clazz, method, logText);
        }
        return result;
    }

}
