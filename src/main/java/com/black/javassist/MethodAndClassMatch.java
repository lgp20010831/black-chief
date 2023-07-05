package com.black.javassist;

import javassist.CtClass;
import javassist.CtMethod;

@SuppressWarnings("all")
public interface MethodAndClassMatch {

    boolean match(CtMethod method, CtClass ctClass);
}
