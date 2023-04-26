package com.black.spring.mapping;

import com.black.pattern.MethodInvoker;
import com.black.spring.agency.Agency;
import com.black.spring.agency.AgencyScanClass;
import com.black.spring.agency.ObjectAgencyRegister;
import com.black.core.chain.GroupKeys;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UrlMappingHandler {

    private static final AntPathMatcher pathMatcher = new AntPathMatcher("\\");

    private static final Set<Class<?>> agencyClassCache = new HashSet<>();

    public static void deposit(Class<?> type){
        if (isAgency(type)){
            agencyClassCache.add(type);
        }
    }

    public static boolean isAgency(Class<?> type){
        return type.isAnnotationPresent(Agency.class);
    }

    public static void flushHttpMethod(RequestMappingInfo requestMappingInfo, Class<?> controllerType, Method method){
        for (Class<?> clazz : agencyClassCache) {
            handlerClass(requestMappingInfo, controllerType, method, clazz);
        }
    }

    public static void handlerClass(RequestMappingInfo requestMappingInfo, Class<?> controllerType, Method method, Class<?> type){
        Agency annotation = type.getAnnotation(Agency.class);
        if (annotation == null){
            return;
        }
        ClassWrapper<?> classWrapper = ClassWrapper.get(type);
        List<MethodWrapper> methodWrappers = classWrapper.getMethodByAnnotation(RewriteMapping.class);
        Object instancedBean = AgencyScanClass.instanceBean(type, annotation.lazy());
        handlerMethods(requestMappingInfo, controllerType, method, methodWrappers, instancedBean);
    }

    public static void handlerMethods(RequestMappingInfo requestMappingInfo, Class<?> controllerType, Method method,
                               Collection<MethodWrapper> methodWrappers, Object instance){

        ObjectAgencyRegister register = ObjectAgencyRegister.getInstance();
        for (MethodWrapper methodWrapper : methodWrappers) {
            RewriteMapping annotation = methodWrapper.getAnnotation(RewriteMapping.class);
            if (annotation == null){
                continue;
            }

            String[] patterns = annotation.value();
            if (match(requestMappingInfo, patterns)){
                MethodInvoker methodInvoker = new MethodInvoker(methodWrapper);
                methodInvoker.setInvokeBean(instance);
                register.register(new GroupKeys(controllerType, method), methodInvoker);
            }
        }
    }

    public static boolean match(RequestMappingInfo mappingInfo, String[] patterns){
        Set<String> patternValues = mappingInfo.getPatternValues();
        for (String patternValue : patternValues) {
            for (String pattern : patterns) {
                if (pathMatcher.match(pattern, patternValue)){
                    return true;
                }
            }
        }
        return false;
    }

}
