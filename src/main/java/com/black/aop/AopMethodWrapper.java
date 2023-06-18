package com.black.aop;

import com.black.arg.MethodReflectionIntoTheParameterProcessor;
import com.black.arg.custom.SerlvetCustomParamterProcessor;
import com.black.core.query.MethodWrapper;
import com.black.core.tools.BeanUtil;
import com.black.core.util.AnnotationUtils;
import com.black.utils.IdUtils;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.core.Ordered;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 李桂鹏
 * @create 2023-06-06 16:34
 */
@SuppressWarnings("all")
public class AopMethodWrapper implements PointcutAdvisor, Pointcut, MethodInterceptor, Ordered {

    private int sort = 250;

    private final Method method;

    private final Object target;

    private final ClassInterceptCondition classInterceptCondition;

    private final MethodInterceptCondition methodInterceptCondition;

    private final String id;

    public AopMethodWrapper(Method method, Object target,
                            ClassInterceptCondition classInterceptCondition,
                            MethodInterceptCondition methodInterceptCondition) {
        this.id = IdUtils.createShort8Id();
        this.method = method;
        this.target = target;
        this.classInterceptCondition = classInterceptCondition;
        this.methodInterceptCondition = methodInterceptCondition;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public String getId() {
        return id;
    }

    @Override
    public Pointcut getPointcut() {
        return this;
    }

    @Override
    public Advice getAdvice() {
        return this;
    }

    @Override
    public boolean isPerInstance() {
        return false;
    }

    @Nullable
    @Override
    public Object invoke(@NotNull MethodInvocation invocation) throws Throwable {
        Object proxy = invocation.getThis();
        Method method = invocation.getMethod();
        boolean supprotHandle = !method.getReturnType().equals(void.class);
        MethodWrapper originMethod = MethodWrapper.get(method);
        MethodWrapper methodWrapper = MethodWrapper.get(this.method);
        Object[] arguments = invocation.getArguments();
        Class<Object> primordialClass = BeanUtil.getPrimordialClass(proxy);
        Handle handle = new Handle(primordialClass, method, arguments, primordialClass, invocation);
        MethodReflectionIntoTheParameterProcessor processor = new MethodReflectionIntoTheParameterProcessor();
        processor.addCustomParameterProcessor(new SerlvetCustomParamterProcessor());
        LinkedHashMap<String, Object> env = new LinkedHashMap<>();
        env.put("handle", handle);
        env.put("invocation", invocation);
        env.putAll(getMatchAttributes(method, primordialClass));
        Object[] args = processor.parse(methodWrapper, originMethod, arguments, (Object) env);
        try {
            if (supprotHandle){
                return fetchMethod(args);
            }else {
                fetchMethod(args);
                return invocation.proceed();
            }
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
    public ClassFilter getClassFilter() {
        return clazz -> {
            if (!classInterceptCondition.isConnectMethodWithAnd()) {
                return true;
            }else {
                Class<?>[] types = classInterceptCondition.getType();
                if (types != null){
                    if (!(classInterceptCondition.isTypeAnd() ? equalAnd(types, clazz)
                            : equalOr(types, clazz))){
                        return false;
                    }
                }

                Class<? extends Annotation>[] annAt = classInterceptCondition.getAnnAt();
                if (annAt != null){
                    if (!match(clazz, annAt, classInterceptCondition.isAnnAnd())){
                        return false;
                    }
                }
                return true;
            }
        };
    }

    @Override
    public MethodMatcher getMethodMatcher() {
        return new MethodMatcher() {
            @Override
            public boolean matches(Method method, Class<?> targetClass) {
                if (!Modifier.isPublic(method.getModifiers()) && methodInterceptCondition.isOpenMethod()){
                    return false;
                }
                Class<?>[] supportReturnType = methodInterceptCondition.getSupportReturnType();
                if (supportReturnType != null){
                    if (!equalOr(supportReturnType, method.getReturnType())){
                        return false;
                    }
                }
                Class<? extends Annotation>[] annAts = methodInterceptCondition.getAnnAt();
                if (annAts != null){
                    if (!match(method, annAts, methodInterceptCondition.isAnnAnd())) {
                        if (!classInterceptCondition.isConnectMethodWithAnd()){
                            if (!match(targetClass, annAts, methodInterceptCondition.isAnnAnd())) {
                                return false;
                            }
                        }else {
                            return false;
                        }

                    }

                }
                Class<? extends Annotation>[] paramAt = methodInterceptCondition.getParamAt();
                if (paramAt != null){
                    for (Parameter parameter : method.getParameters()) {
                        if (match(parameter, paramAt, methodInterceptCondition.isAnnAnd())){
                            return true;
                        }
                    }
                    return false;
                }
                return true;
            }

            @Override
            public boolean isRuntime() {
                return false;
            }

            @Override
            public boolean matches(Method method, Class<?> targetClass, Object... args) {
                return false;
            }
        };
    }

    public static boolean match(AnnotatedElement element, Class<? extends Annotation>[] annAts, boolean and){
        for (Class<? extends Annotation> annAt : annAts) {
            boolean pertain = AnnotationUtils.isPertain(element, annAt);
            if (!and && pertain){
                return true;
            }

            if (and && !pertain){
                return false;
            }
        }
        return and;
    }

    public static <T> boolean equalAnd(Class<?>[] clazzes, Class<?> clazz){
        for (Class<?> type : clazzes) {
            if (!type.equals(clazz)){
                return false;
            }
        }
        return true;
    }

    public static <T> boolean equalOr(Class<?>[] clazzes, Class<?> clazz){
        for (Class<?> type : clazzes) {
            if (type.equals(clazz)){
                return true;
            }
        }
        return false;
    }


    @Override
    public int getOrder() {
        return sort;
    }
}
