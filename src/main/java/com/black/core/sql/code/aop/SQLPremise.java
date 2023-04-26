package com.black.core.sql.code.aop;

import com.black.core.aop.code.AbstractAopTaskQueueAdapter;
import com.black.core.aop.code.Premise;
import com.black.core.spring.ChiefApplicationRunner;
import com.black.core.sql.annotation.EnabledMapSQLApplication;
import org.springframework.core.annotation.AnnotationUtils;

public class SQLPremise implements Premise {
    @Override
    public boolean condition(AbstractAopTaskQueueAdapter aopTaskQueueAdapter) {
        Class<?> mainClass = ChiefApplicationRunner.getMainClass();
        if (mainClass != null){
            return AnnotationUtils.getAnnotation(mainClass, EnabledMapSQLApplication.class) != null;
        }
        return false;
    }
}
