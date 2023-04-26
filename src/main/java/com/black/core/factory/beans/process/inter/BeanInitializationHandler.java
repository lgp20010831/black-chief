package com.black.core.factory.beans.process.inter;

import com.black.core.factory.beans.BeanFactory;
import com.black.core.query.FieldWrapper;

//当对象初始化的执行, 处理对象的每一个属性
public interface BeanInitializationHandler extends BeanFactoryProcessor {


    boolean support(FieldWrapper fw, BeanFactory factory, Object bean);


    void doHandler(FieldWrapper fw, BeanFactory factory, Object bean);
}
