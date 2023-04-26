package com.black.core.proxy;

public class TargetWrapper {

    boolean lazyForSpring;

    Class<?> targetClazz;

    public TargetWrapper(boolean lazyForSpring, Class<?> targetClazz) {
        this.lazyForSpring = lazyForSpring;
        this.targetClazz = targetClazz;
    }

    public boolean isLazyForSpring() {
        return lazyForSpring;
    }

    public Class<?> getTargetClazz() {
        return targetClazz;
    }
}
