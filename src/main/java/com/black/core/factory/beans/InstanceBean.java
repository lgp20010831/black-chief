package com.black.core.factory.beans;

public interface InstanceBean extends BeanCycle {

    void instanceComplete(BeanFactory factory);
}
