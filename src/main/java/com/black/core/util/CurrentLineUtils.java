package com.black.core.util;

import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;

import java.lang.reflect.Method;

public class CurrentLineUtils {

    private static final int stackIndex = 3;

    public static StackTraceElement[] getStackTraceArray(){
        return Thread.currentThread().getStackTrace();
    }

    private static void checkArray(StackTraceElement[] stackTraceElements, int index){
        if(stackTraceElements.length <= index){
            throw new IllegalStateException("array out of bounds, array size:" + stackTraceElements.length + "; index:" + index);
        }
    }


    public static String getOriginFileName(){
        return getFileName(stackIndex);
    }

    //result of xxx.java
    public static String getFileName(int index){
        StackTraceElement[] stackTraceArray = getStackTraceArray();
        checkArray(stackTraceArray, index);
        return stackTraceArray[index].getFileName();
    }

    public static String getOriginClassName(){
        return getClassName(stackIndex);
    }

    public static String getClassName(int index){
        StackTraceElement[] stackTraceArray = getStackTraceArray();
        checkArray(stackTraceArray, index);
        return stackTraceArray[index].getClassName();
    }

    public static String getOriginMethodName(){
        return getMethodName(stackIndex + 1);
    }

    public static String getMethodName(int index){
        StackTraceElement[] stackTraceArray = getStackTraceArray();
        checkArray(stackTraceArray, index);
        return stackTraceArray[index].getMethodName();
    }

    public static int getOriginLine(){
        return getLine(stackIndex);
    }

    public static int getLine(int index){
        StackTraceElement[] stackTraceArray = getStackTraceArray();
        checkArray(stackTraceArray, index);
        return stackTraceArray[index].getLineNumber();
    }

    public static Class<?> loadClass(){
        return loadClass(4);
    }

    public static Class<?> loadClass(int index){
        String className = getClassName(index);
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("can not find class: " + className);
        }
    }

    //new Object(){}.getClass().getEnclosingMethod()
    public static Method loadMethod(){
        Class<?> loadClass = loadClass(5);
        ClassWrapper<?> cw = ClassWrapper.get(loadClass);
        String methodName = getMethodName(4);
        MethodWrapper mw = cw.getSingleMethod(methodName);
        return mw == null ? null : mw.get();
    }

    public static Method loadMethod(int up){
        Class<?> loadClass = loadClass(5 + up);
        ClassWrapper<?> cw = ClassWrapper.get(loadClass);
        String methodName = getMethodName(4 + up);
        MethodWrapper mw = cw.getSingleMethod(methodName);
        return mw == null ? null : mw.get();
    }

}
