package com.black.core.config;

import com.black.core.spring.ChiefApplicationRunner;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.SpringVersion;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Log4j2
public class ProjectDependencyResolver implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        SpringApplication springApplication = ChiefApplicationRunner.getSpringApplication();
        String version = SpringVersion.getVersion();
        if (log.isInfoEnabled()) {
            log.info("spring current version is [{}]", version);
        }
        if (!"2.5.5".equals(version)){
            if (log.isWarnEnabled()) {
                log.warn("当前服务器最适配的 spring 版本是: [{}], 但是项目此时依赖的 spring 版本是: [{}]", "2.5.5", version);
            }
        }

        return false;
    }
}
