package com.black.core.data;

import com.black.core.aop.code.AopTaskIntercepet;
import com.black.core.aop.code.HijackObject;
import com.black.core.data.annotation.DataMethod;
import com.black.core.ill.GlobalThrowableCentralizedHandling;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AopDataIntercept implements AopTaskIntercepet {

    private final Map<Method, String[]> cache = new ConcurrentHashMap<>();

    @Override
    public Object processor(HijackObject hijack) throws Throwable {
        Method method = hijack.getMethod();
        String[] names = cache.computeIfAbsent(method, md -> AnnotationUtils.getAnnotation(md, DataMethod.class).value());
        Object release = hijack.doRelease(hijack.getArgs());
        Data<?> data;
        if (!(release instanceof Data<?>)){
            data = DataWrapper.createData(release);
        }else {
            data = (Data<?>) release;
        }

        for (String name : names) {
            try {

                TransferStationManager.pushData(data, name);
            }catch (RuntimeException ex){
                GlobalThrowableCentralizedHandling.resolveThrowable(ex);
            }
        }
        return release;
    }
}
