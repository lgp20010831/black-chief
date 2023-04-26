package com.black.core.config.condition;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Log4j2
public class SaveAopConditional implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        try {
            Class.forName("org.aspectj.lang.ProceedingJoinPoint");
        } catch (ClassNotFoundException e) {
            if (log.isWarnEnabled()) {
                log.warn("lack of unnecessary dependencies: [{}], aop component is defect",
                        "org.aspectj");
            }
            return false;
        }
        return true;
    }
}
