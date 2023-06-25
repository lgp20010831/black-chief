package com.black.core.query;

import java.lang.reflect.Modifier;

/**
 * @author 李桂鹏
 * @create 2023-06-25 13:44
 */
@SuppressWarnings("all")
public interface ModifierInformationist {

    default int getModifiers(){
        throw new UnsupportedOperationException("getModifiers");
    }

    default boolean isStatic(){
        return Modifier.isStatic(getModifiers());
    }

    default boolean isAbstract(){
        return Modifier.isAbstract(getModifiers());
    }

    default boolean isFinal(){
        return Modifier.isFinal(getModifiers());
    }

    default boolean isPrivate(){
        return Modifier.isPrivate(getModifiers());
    }

    default boolean isInterface(){
        return Modifier.isInterface(getModifiers());
    }

    default boolean isNative(){
        return Modifier.isNative(getModifiers());
    }

    default boolean isSynchronized(){
        return Modifier.isSynchronized(getModifiers());
    }

    default boolean isProtected(){
        return Modifier.isProtected(getModifiers());
    }

    default boolean isTransient(){
        return Modifier.isTransient(getModifiers());
    }

    default boolean isVolatile(){
        return Modifier.isVolatile(getModifiers());
    }
}
