package com.black.core.aop.listener;
import com.black.core.aop.code.AopApplicationContext;
import com.black.core.spring.ChiefApplicationRunner;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;

@Log4j2
public class EnableAopStartListener implements GenericApplicationListener {

    @Override
    public boolean supportsEventType(ResolvableType eventType) {
        return ApplicationContextInitializedEvent.class.isAssignableFrom(eventType.resolve());
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        SpringApplication application = (SpringApplication) event.getSource();
        SpringApplicationStaticHodler.springApplication = application;
        //获取启动类
        Class<?> mainApplicationClass = application.getMainApplicationClass();
        if (AnnotationUtils.getAnnotation(mainApplicationClass, EnableGlobalAopChainWriedModular.class) != null){
            //开启 aop 注入模块
            if (log.isInfoEnabled()) {
                log.info("start aop chain modular");
            }
            runAop(application);
        }
    }

    public static boolean isOpen(){
        return ChiefApplicationRunner.isPertain(EnableGlobalAopChainWriedModular.class);
    }

    private void runAop(SpringApplication application){
        AopApplicationContext.run(application);
    }
}
