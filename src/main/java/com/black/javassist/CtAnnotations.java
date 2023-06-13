package com.black.javassist;

import com.black.function.Consumer;
import javassist.bytecode.annotation.Annotation;


import java.util.*;

public class CtAnnotations {

    private final List<CtAnnotation> annotationList = new ArrayList<>();

    public void addAnnotationArray(CtAnnotation... annotations){
        for (CtAnnotation annotation : annotations) {
            addAnnotation(annotation);
        }
    }

    public void addAnnotation(CtAnnotation annotation){
        annotationList.add(annotation);
    }

    public void addAnnotations(Collection<CtAnnotation> annotations){
        annotationList.addAll(annotations);
    }

    public static CtAnnotations group(CtAnnotation... annotations){
        CtAnnotations ctAnnotations = new CtAnnotations();
        for (CtAnnotation annotation : annotations) {
            ctAnnotations.addAnnotation(annotation);
        }
        return ctAnnotations;
    }

    public boolean isEmpty(){
        return annotationList.isEmpty();
    }

    public List<CtAnnotation> getAnnotationList() {
        return annotationList;
    }

    public Map<Class<? extends java.lang.annotation.Annotation>,
            Consumer<Annotation>> getAnnotationCallback(){
        Map<Class<? extends java.lang.annotation.Annotation>,
                Consumer<Annotation>> map = new LinkedHashMap<>();
        for (CtAnnotation ctAnnotation : annotationList) {
            Map<Class<? extends java.lang.annotation.Annotation>,
                    Consumer<Annotation>> annotationCallback = ctAnnotation.getAnnotationCallback();
            map.putAll(annotationCallback);
        }
        return map;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("\n", "", "");
        for (CtAnnotation ctAnnotation : annotationList) {
            joiner.add(ctAnnotation.toString());
        }
        return joiner.toString();
    }

    public CtAnnotations copy(){
        return CtAnnotations.group(annotationList.toArray(new CtAnnotation[0]));
    }
}
