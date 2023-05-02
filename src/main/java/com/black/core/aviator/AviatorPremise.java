package com.black.core.aviator;

import com.black.core.aop.code.AbstractAopTaskQueueAdapter;
import com.black.core.aop.code.Premise;
import com.black.core.aviator.annotation.EnabledGlobalAviatorLayer;
import com.black.core.spring.ChiefApplicationRunner;

public class AviatorPremise implements Premise {
    @Override
    public boolean condition(AbstractAopTaskQueueAdapter aopTaskQueueAdapter) {
        return doJudge();
    }

    public boolean doJudge(){
        if (ChiefApplicationRunner.isPertain(EnabledGlobalAviatorLayer.class)){

            try {
                Class.forName("com.googlecode.aviator.AviatorEvaluator");
                return true;
            } catch (ClassNotFoundException e) {
                System.out.println("你需要添加条件解析器的依赖, 并遵循其语法规范");
                System.out.println("<dependency>");
                System.out.println("    <groupId>com.googlecode.aviator</groupId>");
                System.out.println("    <artifactId>aviator</artifactId>");
                System.out.println("    <version>5.2.5</version>");
                System.out.println("</dependency>");
                return false;
            }
        }
        return false;
    }
}
