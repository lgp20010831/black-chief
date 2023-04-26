package com.black.core.spring.driver;

import com.black.core.spring.ChiefExpansivelyApplication;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

public interface PostBeanFactoryDriver extends Driver {

    void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory, ChiefExpansivelyApplication chiefExpansivelyApplication);
}
