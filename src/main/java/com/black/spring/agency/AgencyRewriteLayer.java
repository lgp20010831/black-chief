package com.black.spring.agency;

import com.black.arg.MethodReflectionIntoTheParameterProcessor;
import com.black.arg.custom.SerlvetCustomParamterProcessor;
import com.black.bin.ApplyProxyLayer;
import com.black.bin.ProxyTemplate;
import com.black.pattern.MethodInvoker;
import com.black.core.util.Utils;
import com.black.utils.ReflectionUtils;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class AgencyRewriteLayer implements ApplyProxyLayer {


    private final MethodReflectionIntoTheParameterProcessor parameterProcessor;

    public AgencyRewriteLayer() {
        parameterProcessor = new MethodReflectionIntoTheParameterProcessor();
        parameterProcessor.setNullValueGetFromSpring(true);
        parameterProcessor.setNullValueCreateByFactory(true);
        parameterProcessor.addCustomParameterProcessor(new SerlvetCustomParamterProcessor());
    }

    @Override
    public Object proxy(Object[] args, Method method, Class<?> beanClass, ProxyTemplate template) throws Throwable {
        ObjectAgencyRegister register = ObjectAgencyRegister.getInstance();
        List<Class<?>> supperClasses = ReflectionUtils.getUseSupperClasses(beanClass);
        List<MethodInvoker> methodInvokers = null;
        for (Class<?> supperClass : supperClasses) {
            methodInvokers = register.getMethodInvoker(supperClass, method);
            if (methodInvokers != null){
                break;
            }
        }
        if (!Utils.isEmpty(methodInvokers)){
            List<MethodInvoker> copy = new ArrayList<>(methodInvokers);
            Invocation invocation = new Invocation(copy, template, args);
            invocation.setParameterProcessor(parameterProcessor);
            return invocation.invoke();
        }
        return template.invokeOriginal(args);
    }
}
