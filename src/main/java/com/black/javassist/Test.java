package com.black.javassist;

import com.black.aop.InterceptOnAnnotation;
import com.black.asm.User;
import com.black.core.json.Trust;
import com.black.core.util.Av0;

@PreloadWeaver
public class Test {
    public void say(){
        System.out.println("hello");
    }

    @InterceptOnAnnotation(Trust.class)
    void logs(Object target){
        System.out.println(target);
    }


    public static void main(String[] args) {
        PreloadInterceptionModule.loadByClassName(Av0.set("com.black.asm"), "com.black.javassist.Test");
        new User().yet(null);
    }
}
