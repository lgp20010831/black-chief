package com.black.core.spring;

import com.black.core.spring.driver.PostBeforeBeanInstantiationDriver;

import java.util.Map;

public class PreventDuplicationComponent implements PostBeforeBeanInstantiationDriver {


    private Map<Class<?>, Object> componentMutes;

    @Override
    public Object postBeforeBeanInstantiationLogic(Class<?> beanClass, String beanName,
                                                   ChiefExpansivelyApplication chiefExpansivelyApplication,
                                                   Object previousResultBean, PostBeforeBeanInstantiationDriver previousDriver) {
        if (componentMutes == null){
            componentMutes = chiefExpansivelyApplication.getComponentMutes();
        }
        if (!OpenComponent.class.isAssignableFrom(beanClass)){
            return previousResultBean;
        }
        for (Class<?> comClazz : componentMutes.keySet()) {
            if (comClazz.equals(beanClass) || beanClass.isAssignableFrom(comClazz)){
                return componentMutes.get(comClazz);
            }
        }

        return previousResultBean;
    }
}
