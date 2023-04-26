package com.black.core.spring.pureness;

import com.black.core.spring.ChiefApplicationConfigurer;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.ChooseScanRangeHolder;
import com.black.core.spring.PettyApplicationConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;

public class CrutchSpringApplicationConfiguration extends PettyApplicationConfiguration {

    private ApplicationContext applicationContext;

    private boolean addSpringScanPackages;

    public CrutchSpringApplicationConfiguration(ApplicationContext context,
                                                ChiefExpansivelyApplication chiefExpansivelyApplication) {
        super(new ChooseScanRangeHolder(), chiefExpansivelyApplication);
        applicationContext = context;
    }

    @Override
    public void init() {
        super.init();
        if (addSpringScanPackages && applicationContext != null){
            setScanPackages(AutoConfigurationPackages.get(applicationContext).toArray(new String[0]));
        }
    }


    public void setAddSpringScanPackages(boolean addSpringScanPackages) {
        this.addSpringScanPackages = addSpringScanPackages;
    }

    @Override
    protected void handlerConfig(ChiefApplicationConfigurer chiefApplicationConfigurer) {
        //获取启动类
        Class<?> startUpClazz = chiefExpansivelyApplication.getStartUpClazz();
        SpringBootApplication bootApplication = startUpClazz == null ? null : AnnotationUtils.findAnnotation(startUpClazz, SpringBootApplication.class);
        if (bootApplication != null){

            //将 spring 的作用范围加载到 chiefExpansivelyApplication 中
            String[] basePackages = bootApplication.scanBasePackages();
            setScanPackages(basePackages);
        }
        setAddSpringScanPackages(chiefApplicationConfigurer.addSpringScanPackages());
        super.handlerConfig(chiefApplicationConfigurer);
    }

    @Override
    protected void initDefaultConfig() {
        super.initDefaultConfig();
        setScanPackages(AutoConfigurationPackages.get(applicationContext).toArray(new String[0]));
    }
}
