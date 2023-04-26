package com.black.core.factory.beans;

public interface InitializeBean extends BeanCycle {

    void initializeComplete(BeanFactory factory);
}
