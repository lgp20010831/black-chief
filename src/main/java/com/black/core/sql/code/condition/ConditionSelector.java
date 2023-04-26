package com.black.core.sql.code.condition;

import com.black.core.sql.code.condition.aviator.SqlAviatorManager;

import java.util.Map;

public class ConditionSelector {


    public static boolean excCondition(String expression, Map<String, Object> sourceMap){
        if (SqlAviatorManager.isOpen()) {
            SqlAviatorManager.init();
            return SqlAviatorManager.execute(expression, sourceMap);
        }
        System.out.println("你需要添加条件解析器的依赖, 并遵循其语法规范");
        System.out.println("<dependency>");
        System.out.println("    <groupId>com.googlecode.aviator</groupId>");
        System.out.println("    <artifactId>aviator</artifactId>");
        System.out.println("    <version>5.2.5</version>");
        System.out.println("</dependency>");
        throw new IllegalStateException("没有足够的条件处理器了");
    }


}
