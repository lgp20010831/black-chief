package com.black.core.config.condition;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Log4j2
public class FastJsonConditional implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        try {
            Class.forName("com.alibaba.fastjson.JSON");
        } catch (ClassNotFoundException e) {
            if (log.isWarnEnabled()) {
                log.warn("lack of unnecessary dependencies: [{}], json component is defect",
                        "com.alibaba.fastjson");
            }
            return false;
        }
        return true;
    }
}
