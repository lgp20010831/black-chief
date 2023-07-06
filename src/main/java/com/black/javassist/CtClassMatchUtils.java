package com.black.javassist;

import com.black.core.util.AnnotationUtils;
import javassist.CtClass;
import javassist.CtMethod;

import java.lang.annotation.Annotation;

@SuppressWarnings("all")
public class CtClassMatchUtils {


    public static boolean pertainOnClass(CtClass ctClass, Class<? extends Annotation> type){
        try {
            Object[] annotations = ctClass.getAnnotations();
            for (Object annotation : annotations) {
                Annotation javaAnn = (Annotation) annotation;
                Class<? extends Annotation> javaAnnType = javaAnn.annotationType();
                if (javaAnnType.equals(type)){
                    return true;
                }
                if (AnnotationUtils.isPertain(javaAnnType, type)) {
                    return true;
                }
            }
            return false;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    public static boolean pertainOnMethod(CtMethod ctMethod, Class<? extends Annotation> type){
        try {
            Object[] annotations = ctMethod.getAnnotations();
            for (Object annotation : annotations) {
                Annotation javaAnn = (Annotation) annotation;
                Class<? extends Annotation> javaAnnType = javaAnn.annotationType();
                if (javaAnnType.equals(type)){
                    return true;
                }
                if (AnnotationUtils.isPertain(javaAnnType, type)) {
                    return true;
                }
            }
            return false;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> boolean equalAnd(Class<?>[] clazzes, String clazzName){
        for (Class<?> type : clazzes) {
            if (!type.getName().equals(clazzName)){
                return false;
            }
        }
        return true;
    }

    public static <T> boolean equalOr(Class<?>[] clazzes, String clazzName){
        for (Class<?> type : clazzes) {
            if (type.getName().equals(clazzName)){
                return true;
            }
        }
        return false;
    }

    public static boolean matchByClass(CtClass ctClass, Class<? extends Annotation>[] annAts, boolean and){
        for (Class<? extends Annotation> annAt : annAts) {
            boolean pertain = pertainOnClass(ctClass, annAt);
            if (!and && pertain){
                return true;
            }

            if (and && !pertain){
                return false;
            }
        }
        return and;
    }


    public static boolean matchByMethod(CtMethod ctMethod, Class<? extends Annotation>[] annAts, boolean and){
        for (Class<? extends Annotation> annAt : annAts) {
            boolean pertain = pertainOnMethod(ctMethod, annAt);
            if (!and && pertain){
                return true;
            }

            if (and && !pertain){
                return false;
            }
        }
        return and;
    }
}
