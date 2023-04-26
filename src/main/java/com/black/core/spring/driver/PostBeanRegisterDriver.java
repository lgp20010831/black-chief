package com.black.core.spring.driver;

import com.black.core.spring.ChiefExpansivelyApplication;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

public interface PostBeanRegisterDriver extends Driver {

    void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry, ChiefExpansivelyApplication chiefExpansivelyApplication);
}
