package com.black.aop;

import com.black.arg.MethodReflectionIntoTheParameterProcessor;
import com.black.core.query.MethodWrapper;
import com.black.core.tools.BeanUtil;
import com.black.core.util.AnnotationUtils;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;

/**
 * @author 李桂鹏
 * @create 2023-06-06 16:34
 */
@SuppressWarnings("all")
public class AopMethodWrapper implements PointcutAdvisor, Pointcut, MethodInterceptor {

    private final Method method;

    private final Object target;

    private final ClassInterceptCondition classInterceptCondition;

    private final MethodInterceptCondition methodInterceptCondition;

    public AopMethodWrapper(Method method, Object target,
                            ClassInterceptCondition classInterceptCondition,
                            MethodInterceptCondition methodInterceptCondition) {
        this.method = method;
        this.target = target;
        this.classInterceptCondition = classInterceptCondition;
        this.methodInterceptCondition = methodInterceptCondition;
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
        MethodWrapper methodWrapper = MethodWrapper.get(method);
        Object[] arguments = invocation.getArguments();
        Class<Object> primordialClass = BeanUtil.getPrimordialClass(proxy);
        Handle handle = new Handle(primordialClass, method, arguments, primordialClass, invocation);
        MethodReflectionIntoTheParameterProcessor processor = new MethodReflectionIntoTheParameterProcessor();
        Object[] args = processor.parse(methodWrapper, arguments, invocation);
        try {
            return methodWrapper.invoke(target, args);
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
                        return false;
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


}
