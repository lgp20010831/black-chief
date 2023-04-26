package com.black.spring;

import org.springframework.boot.ApplicationContextFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.web.reactive.context.AnnotationConfigReactiveWebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.ResourceLoader;

public class ChiefSpringApplication extends SpringApplication {

    private boolean detailedInventory = false;

    private final ApplicationContextFactory chiefApplicationContextFactory = new ApplicationContextFactory() {
        @Override
        public ConfigurableApplicationContext create(WebApplicationType webApplicationType) {
            try {
                switch (webApplicationType) {
                    case SERVLET:
                        return new ChiefAutoProxyAnnotationConfigServletWebServerApplicationContext();
                    case REACTIVE:
                        return new AnnotationConfigReactiveWebServerApplicationContext();
                    default:
                        return new AnnotationConfigApplicationContext();
                }
            } catch (Exception var2) {
                throw new IllegalStateException("Unable create a default ApplicationContext instance, you may need a custom ApplicationContextFactory", var2);
            }
        }
    };

    public void setDetailedInventory(boolean detailedInventory) {
        this.detailedInventory = detailedInventory;
    }

    public boolean isDetailedInventory() {
        return detailedInventory;
    }

    @Override
    protected ConfigurableApplicationContext createApplicationContext() {
        return this.chiefApplicationContextFactory.create(getWebApplicationType());
    }

    public ChiefSpringApplication(Class<?>... primarySources) {
        super(primarySources);
    }

    public ChiefSpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
        super(resourceLoader, primarySources);
    }

    public static ConfigurableApplicationContext run(Class<?> primarySource, String... args) {
        return run(new Class[]{primarySource}, args);
    }

    public static ConfigurableApplicationContext run(Class<?>[] primarySources, String[] args) {
        return (new ChiefSpringApplication(primarySources)).run(args);
    }

}
