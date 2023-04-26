package com.black.javassist;

import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import javassist.CannotCompileException;
import javassist.CtMethod;
import javassist.util.HotSwapper;

import java.io.IOException;

public class Demo {


    public static void main(String[] args) throws CannotCompileException, IOException, IllegalConnectorArgumentsException {
        //Class<Test> testClass = Test.class;
        new Test().say();
        HotSwapper swapper = new HotSwapper(8000);
        PartiallyCtClass partiallyCtClass = PartiallyCtClass.load("com.black.javassist.Test");
        System.out.println(partiallyCtClass);
        CtMethod ctMethod = partiallyCtClass.getLoadMethod("say");
        ctMethod.insertBefore("System.out.println(\"执行前执行\");");
        //Class<?> javaClass = partiallyCtClass.getJavaClass();
        //partiallyCtClass.writeFile();
        swapper.reload(Test.class.getName(), partiallyCtClass.toByteArray());
        new Test().say();
    }


}
