package com.black.javassist;

import com.black.aop.AopSecondaryInterceptionException;
import com.black.aop.ClassInterceptCondition;
import com.black.aop.MethodInterceptCondition;
import com.black.arg.MethodReflectionIntoTheParameterProcessor;
import com.black.arg.custom.SerlvetCustomParamterProcessor;
import com.black.core.query.MethodWrapper;
import com.black.core.tools.BeanUtil;
import com.black.core.util.AnnotationUtils;
import com.black.utils.IdUtils;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import lombok.Getter;
import lombok.Setter;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.black.javassist.CtClassMatchUtils.*;

@SuppressWarnings("all") @Setter @Getter
public class PreloadMethodWrapperWeaver implements Weaver, MethodAndClassMatch{

    private int sort = 250;

    private final Method method;

    private final Object target;

    private final ClassInterceptCondition classInterceptCondition;

    private final MethodInterceptCondition methodInterceptCondition;

    private final String id;

    public PreloadMethodWrapperWeaver(Method method, Object target,
                            ClassInterceptCondition classInterceptCondition,
                            MethodInterceptCondition methodInterceptCondition) {
        this.id = IdUtils.createShort8Id();
        this.method = method;
        this.target = target;
        this.classInterceptCondition = classInterceptCondition;
        this.methodInterceptCondition = methodInterceptCondition;
    }

    @Override
    public void braid(Method method, Object target, Object[] arguments) throws Throwable {
        MethodWrapper originMethod = MethodWrapper.get(method);
        MethodWrapper methodWrapper = MethodWrapper.get(this.method);
        Class<Object> primordialClass = BeanUtil.getPrimordialClass(target);
        MethodReflectionIntoTheParameterProcessor processor = new MethodReflectionIntoTheParameterProcessor();
        processor.addCustomParameterProcessor(new SerlvetCustomParamterProcessor());
        LinkedHashMap<String, Object> env = new LinkedHashMap<>();
        env.put("method", method);
        env.put("target", target);
        env.put("args", arguments);
        env.put("class", primordialClass);
        env.putAll(getMatchAttributes(method, primordialClass));
        Object[] args = processor.parse(methodWrapper, originMethod, arguments, (Object) env);
        try {
            fetchMethod(args);
        }catch (RuntimeException e){
            Throwable cause = e.getCause();
            if (cause != null){
                if (cause instanceof IllegalAccessException){
                    throw e;
                }

                if (cause instanceof InvocationTargetException){
                    Throwable throwable = cause.getCause();
                    if (throwable != null){
                        throw new AopSecondaryInterceptionException(throwable);
                    }
                }
                throw new AopSecondaryInterceptionException(cause);
            }else {
                throw e;
            }
        }
    }

    public String getId() {
        return id;
    }

    protected Object fetchMethod(Object[] args){
        MethodWrapper methodWrapper = MethodWrapper.get(method);
        return methodWrapper.invoke(target, args);
    }

    protected Map<String, Object> getMatchAttributes(Method mappingMethod, Class<?> proxyClass){
        Map<String, Object> env = new LinkedHashMap<>();
        Map<Class<?>, Annotation> matchAnnotationAttibutes = new LinkedHashMap<>();
        Class<? extends Annotation>[] annAt = methodInterceptCondition.getAnnAt();
        if (annAt != null){
            for (Class<? extends Annotation> type : annAt) {
                Annotation annotation = AnnotationUtils.findAnnotation(mappingMethod, type);
                if (annotation != null){
                    matchAnnotationAttibutes.put(type, annotation);
                }

            }
        }

        Class<? extends Annotation>[] paramAt = methodInterceptCondition.getParamAt();
        if (paramAt != null){
            for (Class<? extends Annotation> type : paramAt) {
                List<Annotation> annotations = new ArrayList<>();
                for (Parameter parameter : method.getParameters()) {
                    Annotation annotation = AnnotationUtils.findAnnotation(parameter, type);
                    annotations.add(annotation);
                }
                env.put(type.getSimpleName(), annotations);
            }
        }

        Class<? extends Annotation>[] classAnnAt = classInterceptCondition.getAnnAt();
        if (classAnnAt != null){
            for (Class<? extends Annotation> type : classAnnAt) {
                if (!matchAnnotationAttibutes.containsKey(type)){
                    Annotation annotation = AnnotationUtils.findAnnotation(proxyClass, type);
                    if (annotation != null){
                        matchAnnotationAttibutes.put(type, annotation);
                    }
                }
            }
        }

        matchAnnotationAttibutes.forEach((k, v) -> {
            env.put(k.getSimpleName(), v);
        });
        return env;
    }

    @Override
    public boolean match(CtMethod method, CtClass ctClass) {
        if (!matchClass(ctClass)){
            return false;
        }
        return matchMethod(method, ctClass);
    }

    protected boolean matchClass(CtClass ctClass){
        String ctClassName = ctClass.getName();
        if (!classInterceptCondition.isConnectMethodWithAnd()) {
            return true;
        }else {
            Class<?>[] types = classInterceptCondition.getType();
            if (types != null){
                if (!(classInterceptCondition.isTypeAnd() ? equalAnd(types, ctClassName)
                        : equalOr(types, ctClassName))){
                    return false;
                }
            }

            Class<? extends Annotation>[] annAt = classInterceptCondition.getAnnAt();
            if (annAt != null){
                if (!matchByClass(ctClass, annAt, classInterceptCondition.isAnnAnd())){
                    return false;
                }
            }
            return true;
        }
    }


    protected boolean matchMethod(CtMethod method, CtClass ctClass){
        if (!Modifier.isPublic(method.getModifiers()) && methodInterceptCondition.isOpenMethod()){
            return false;
        }
        String returnTypeName;
        try {
             returnTypeName = method.getReturnType().getName();
        } catch (NotFoundException e) {
            throw new IllegalStateException(e);
        }
        Class<?>[] supportReturnType = methodInterceptCondition.getSupportReturnType();
        if (supportReturnType != null){
            if (!equalOr(supportReturnType, returnTypeName)){
                return false;
            }
        }
        Class<? extends Annotation>[] annAts = methodInterceptCondition.getAnnAt();
        if (annAts != null){
            if (!matchByMethod(method, annAts, methodInterceptCondition.isAnnAnd())) {
                if (!classInterceptCondition.isConnectMethodWithAnd()){
                    if (!matchByClass(ctClass, annAts, methodInterceptCondition.isAnnAnd())) {
                        return false;
                    }
                }else {
                    return false;
                }

            }

        }
        return true;
    }









}
