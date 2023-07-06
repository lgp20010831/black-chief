package com.black.callback;

import com.black.core.annotation.Sort;
import com.black.core.util.Av0;
import com.black.core.util.ClassUtils;
import com.black.javassist.EnabledPreloadConfigurer;
import com.black.javassist.PreloadInterceptionModule;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;

import java.util.Set;

@SuppressWarnings("all") @Sort(150000)
public class PrecompiledLoader implements SpringApplicationRunListener {

    @Override
    public void starting(ConfigurableBootstrapContext bootstrapContext) {
        SpringApplication springApplication = ChiefProjectSpringApplicationRunListener.springApplication;
        if (springApplication == null){
            return;
        }
        Set<Object> allSources = springApplication.getAllSources();
        EnabledPreloadConfigurer preloadConfigurer = null;
        Class<?> mainClass = null;
        for (Object source : allSources) {
            if (source instanceof Class){
                mainClass = (Class<?>) source;
                EnabledPreloadConfigurer annotation = ((Class<?>) source).getAnnotation(EnabledPreloadConfigurer.class);
                if (annotation != null){
                    preloadConfigurer = annotation;
                    break;
                }
            }
        }
        if (preloadConfigurer == null || mainClass == null){
            return;
        }
        String packageName = ClassUtils.getPackageName(mainClass);
        String[] modifiedScope = preloadConfigurer.modifyScope();
        String[] weaverScope = preloadConfigurer.weaverScope();
        if (modifiedScope.length == 0){
            modifiedScope = new String[]{packageName};
        }

        if (weaverScope.length == 0){
            weaverScope = new String[]{packageName};
        }

        PreloadInterceptionModule.load(Av0.set(modifiedScope), weaverScope);
    }

}
