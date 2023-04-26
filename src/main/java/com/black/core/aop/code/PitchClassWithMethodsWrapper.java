package com.black.core.aop.code;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;

@Setter @Getter @NoArgsConstructor
public class PitchClassWithMethodsWrapper {

    Class<?> targetClass;

    Method pointMethod;

    Collection<Method> matchMethods = new HashSet<>();

    public void addMethod(Method method){
        matchMethods.add(method);
    }

    public PitchClassWithMethodsWrapper(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    @Override
    public String toString() {
        return "PitchClassWithMethodsWrapper{" +
                "targetClass=" + targetClass +
                ", matchMethods=" + matchMethods +
                '}';
    }
}
