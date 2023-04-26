package com.black.core.spring.component;

import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.driver.PostLoadBeanMutesDriver;
import lombok.extern.log4j.Log4j2;

import java.util.Map;

@Log4j2
public class BeanMutesHandler implements PostLoadBeanMutesDriver {

    @Override
    public void postLoadBeanMutes(Map<Class<?>, Object> loadBeanMutes,
                                  Map<String, Object> springMutes,
                                  Map<Class<? extends OpenComponent>, Object> springLoadComponentMutes,
                                  ChiefExpansivelyApplication pettySpringApplication) {

        loadBeanMutes.forEach((k, v) ->{

            if (!OpenComponent.class.isAssignableFrom(k))
                return;

            boolean join = true;
            for (Class<? extends OpenComponent> clazz : springLoadComponentMutes.keySet()) {

                if (k.equals(clazz)){
                    join = false;
                    break;
                }
                if (k.isAssignableFrom(clazz)){
                    join = false;
                    break;
                }
                if (clazz.isAssignableFrom(k)){
                    if (log.isDebugEnabled()) {
                        log.debug("整合 bean 缓存时, 发现了子类对象," +
                                " 即将替换组件中的父类对象, 新组件: {}, 被替换的组件: {}", v, springLoadComponentMutes.get(clazz));
                    }
                    springLoadComponentMutes.remove(clazz);
                }
            }
            if(join){
                pettySpringApplication.registerComponentInstance((OpenComponent) v);
            }
        });
    }
}
