package com.black;

import com.black.core.util.Av0;
import com.black.utils.ServiceUtils;

public class DemoList {


    static void testMethod(){
        String txt = "${str.toLowerCase()} -- ${str.length()}";
        Object param = Av0.of("str", "HELLO");
        Object val = ServiceUtils.patternGetValue(param, txt);
        System.out.println(val);
    }


    public static void main(String[] args) throws Throwable{
        testMethod();
    }
}
