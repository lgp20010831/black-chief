package com.black.core.aop.servlet;

import com.black.core.aop.annotation.HybridSort;
import com.black.core.aop.code.AopMatchTargetClazzAndMethodMutesHandler;
import com.black.core.aop.code.AopTaskManagerHybrid;
import com.black.core.aop.annotation.AopHybrid;
import com.black.core.aop.code.AopTaskIntercepet;
import com.black.core.tools.BeanUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;

@Log4j2
@AopHybrid @HybridSort(31)
public class AopEnhanceControllerHybrid implements AopTaskManagerHybrid {


    boolean debug = true;
    private AopControllerIntercept intercept;
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    @Override
    public AopMatchTargetClazzAndMethodMutesHandler obtainMatcher() {
        return new AopMatchTargetClazzAndMethodMutesHandler() {
            @Override
            public boolean matchClazz(Class<?> targetClazz) {
                Class<?> objectClass = BeanUtil.getPrimordialClass(targetClazz);
                return AnnotationUtils.getAnnotation(objectClass, GlobalEnhanceRestController.class) != null;
            }

            @Override
            public boolean matchMethod(Class<?> targetClazz, Method method) {
                Class<?> primordialClass = BeanUtil.getPrimordialClass(targetClazz);
                if (AnnotationUtils.getAnnotation(targetClazz, GlobalEnhanceRestController.class) == null){
                    return false;
                }
                return AnnotationUtils.getAnnotation(method, RequestMapping.class) != null &&
                        AnnotationUtils.getAnnotation(method, UnEnhancementRequired.class) == null;
            }
        };
    }

    @Override
    public AopTaskIntercepet obtainAopTaskIntercept() {
        if (intercept == null){
            intercept = new AopControllerIntercept();
        }
        return intercept;
    }
}
