package com.black.sql_v2.javassist;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

public class test {


    public static void main(String[] args) {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        AbstractBeanDefinition definition = BeanDefinitionBuilder.genericBeanDefinition(TestController.class).getBeanDefinition();
        factory.registerBeanDefinition("testController", definition);
        TestController controller = factory.getBean(TestController.class);
        System.out.println(controller);
    }


}
