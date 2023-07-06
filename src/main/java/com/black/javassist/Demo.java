package com.black.javassist;


import com.black.asm.User;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class Demo {


    public static void main(String[] args) throws CannotCompileException, IOException, NotFoundException {
        testproxy();
    }


    static void testproxy(){
        WeaverManager.loadClass("com.black.asm.User", ctMethod -> {
            if (ctMethod.getName().equals("may")){
                return Arrays.asList(new Weaver() {
                    @Override
                    public void braid(Method method, Object target, Object[] args) throws Throwable {
                        System.out.println("前置执行");
                    }
                });
            }
            return null;
        });
        new User().may("lgp", 2);
    }

    static void testint() throws NotFoundException {
        PartiallyCtClass partiallyCtClass = PartiallyCtClass.load("com.black.asm.User");
        CtMethod ctMethod = partiallyCtClass.getMethod("yet");
        CtClass[] parameterTypes = ctMethod.getParameterTypes();
        System.out.println(parameterTypes);
    }

}
