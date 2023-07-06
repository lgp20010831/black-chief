package com.black.compile;

import com.black.asm.User;
import com.black.core.query.ClassWrapper;
import com.black.javassist.PartiallyCtClass;
import javassist.CannotCompileException;
import javassist.CtMethod;
import lombok.extern.log4j.Log4j2;

import java.lang.annotation.Annotation;

@Log4j2
public class Demo {


    public static void main(String[] args) throws InstantiationException, IllegalAccessException, CannotCompileException, ClassNotFoundException {
        PartiallyCtClass partiallyCtClass = PartiallyCtClass.load("com.black.asm.User");
        Object[] annotations = partiallyCtClass.getCtClass().getAnnotations();
        System.out.println(((Annotation)annotations[0]).annotationType());
        partiallyCtClass.addMethod("test", void.class, "{System.out.println(1);}");
        CtMethod ctMethod = partiallyCtClass.getMethod("say");
        System.out.println(ctMethod.getSignature());
        ctMethod.insertBefore("System.out.println(this);");
        CtMethod get = partiallyCtClass.getMethod("may");
        get.insertBefore("com.black.javassist.WeaverManager.callback(\"8woafRaA\",com.black.core.util.CurrentLineUtils.loadMethod(), this, new Object[]{name, age});");

        Class<?> javaClass = partiallyCtClass.getJavaClass();
        Object instance = javaClass.newInstance();
        ClassWrapper<?> wrapper = ClassWrapper.get(javaClass);
        wrapper.getSingleMethod("test").invoke(instance);
        wrapper.getSingleMethod("say").invoke(instance);
        new User().may("lgp", 1);
    }

    public static <A, B> Object[] as(Object v1, Object v2){
        return new Object[]{v1, v2};
    }

}
