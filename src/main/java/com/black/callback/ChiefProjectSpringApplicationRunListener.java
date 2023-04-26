package com.black.callback;

import com.black.scan.ChiefScanner;
import com.black.scan.ScannerManager;
import com.black.core.annotation.Sort;
import com.black.core.tools.BeanUtil;
import com.black.utils.ReflectionUtils;
import com.black.utils.ServiceUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.*;

@Log4j2
public class ChiefProjectSpringApplicationRunListener implements SpringApplicationRunListener {

    private final List<SpringApplicationRunListener> listeners = new ArrayList<>();

    public static final Set<String> ranges = new HashSet<>();

    static {
        ranges.add("com.black.callback");
    }

    public static void addInitRange(String range){
        ranges.add(range);
    }

    public ChiefProjectSpringApplicationRunListener(){
        init();
    }

    public ChiefProjectSpringApplicationRunListener(SpringApplication springApplication, String[] args){
        SpringApplicationHodler.setSpringApplication(springApplication);
        SpringApplicationHodler.setArgs(args);
        init();
    }

    private void init(){
        ChiefScanner instance = ScannerManager.getScanner();
        for (String range : ranges) {
            Set<Class<?>> classSet = instance.load(range);
            loadClasses(classSet);
        }
        sort();
        log.info("Initialization completed");
    }

    private void sort(){
        ServiceUtils.sort(listeners, ele -> {
            Class<SpringApplicationRunListener> primordialClass = BeanUtil.getPrimordialClass(ele);
            Sort annotation = primordialClass.getAnnotation(Sort.class);
            return annotation == null ? 0 : annotation.value();
        }, false);
    }


    private void loadClasses(Set<Class<?>> classSet){
        for (Class<?> clazz : classSet) {
            if (!ChiefProjectSpringApplicationRunListener.class.equals(clazz) &&
                    SpringApplicationRunListener.class.isAssignableFrom(clazz)
                    && BeanUtil.isSolidClass(clazz)){
                Object instanced = ReflectionUtils.instance(clazz);
                listeners.add((SpringApplicationRunListener) instanced);
            }
        }
    }

    @Override
    public void starting(ConfigurableBootstrapContext bootstrapContext) {
        for (SpringApplicationRunListener listener : listeners) {
            listener.starting(bootstrapContext);
        }
        SpringApplicationRunListener.super.starting(bootstrapContext);
    }

    @Override
    public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext, ConfigurableEnvironment environment) {
        for (SpringApplicationRunListener listener : listeners) {
            listener.environmentPrepared(bootstrapContext, environment);
        }
        SpringApplicationRunListener.super.environmentPrepared(bootstrapContext, environment);
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        for (SpringApplicationRunListener listener : listeners) {
            listener.contextPrepared(context);
        }
        SpringApplicationRunListener.super.contextPrepared(context);
    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        for (SpringApplicationRunListener listener : listeners) {
            listener.contextLoaded(context);
        }
        SpringApplicationRunListener.super.contextLoaded(context);
    }

    @Override
    public void started(ConfigurableApplicationContext context) {
        for (SpringApplicationRunListener listener : listeners) {
            listener.started(context);
        }
        SpringApplicationRunListener.super.started(context);
    }

    @Override
    public void running(ConfigurableApplicationContext context) {
        for (SpringApplicationRunListener listener : listeners) {
            listener.running(context);
        }
        SpringApplicationRunListener.super.running(context);
    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        for (SpringApplicationRunListener listener : listeners) {
            listener.failed(context, exception);
        }
        SpringApplicationRunListener.super.failed(context, exception);
    }
}
