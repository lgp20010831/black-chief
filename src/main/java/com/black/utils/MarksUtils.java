package com.black.utils;

import com.black.core.util.Av0;

import java.lang.reflect.AnnotatedElement;
import java.util.HashSet;
import java.util.Set;

public class MarksUtils {


    public static boolean matching(AnnotatedElement element){
        return matching(element, null);
    }

    public static boolean matching(AnnotatedElement element, String world){
        Marks annotation = element.getAnnotation(Marks.class);
        if (annotation != null){
            if (world != null){
                for (String val :annotation.value()){
                    if (world.equals(val)){
                        return true;
                    }
                }
                return false;
            }
            return true;
        }
        return false;
    }


    public static Set<String> getMarks(AnnotatedElement element){
        Marks annotation = element.getAnnotation(Marks.class);
        if (annotation == null) return new HashSet<>();
        return Av0.set(annotation.value());
    }
}
