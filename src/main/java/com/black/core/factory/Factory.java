package com.black.core.factory;

public interface Factory {

    //工厂唯一 id 标识
    String id();

    //获取父类工厂
    Factory getParent();
}
