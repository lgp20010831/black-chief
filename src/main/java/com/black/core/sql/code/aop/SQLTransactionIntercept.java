package com.black.core.sql.code.aop;

import com.black.core.aop.code.AopTaskIntercepet;
import com.black.core.aop.code.HijackObject;
import com.black.core.sql.annotation.OpenTransactional;
import com.black.core.sql.code.TransactionSQLManagement;
import com.black.core.util.AnnotationUtils;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Method;

@Log4j2
public class SQLTransactionIntercept implements AopTaskIntercepet {

    @Override
    public Object processor(HijackObject hijack) throws Throwable {
        Method method = hijack.getMethod();
        OpenTransactional transactional = getTransactional(hijack);
        return TransactionSQLManagement.transactionCall(() ->{
            try {
                return hijack.doRelease(hijack.getArgs());
            } catch (Throwable e) {
                throw e;
            }
        }, transactional == null ? new String[0] : transactional.value());
    }

    OpenTransactional getTransactional(HijackObject hijackObject){
        Method method = hijackObject.getMethod();
        OpenTransactional annotation = AnnotationUtils.findAnnotation(method, OpenTransactional.class);
        if (annotation == null){
            annotation = AnnotationUtils.findAnnotation(hijackObject.getClazz(), OpenTransactional.class);
        }
        if (annotation == null){
            annotation = AnnotationUtils.findAnnotation(method.getDeclaringClass(), OpenTransactional.class);
        }
        return annotation;
    }
}
