package com.black.core.factory.beans.component;

import com.black.core.factory.beans.BeanFactory;

public class BeanFactoryHolder {

    static BeanFactory factory;

    public static void setFactory(BeanFactory factory) {
        BeanFactoryHolder.factory = factory;
    }

    public static BeanFactory getFactory() {
        return factory;
    }
}
