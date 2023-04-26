package com.black.core.aop.ibatis;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface DynamicallyTransactionClient {


    //错误之一
    //current transaction is aborted, commands ignored until end of transaction block
    //当在一个事务中出错, 且没有提交和回滚, 那么之后的事务都会报错, 不管正不正确
    //此问题出现在标注了此注解的类上, 但是查询方法上没有标注
    // DynamicallySimpleRollBackTransactional 注解, 导致查询方法出错以后, 没有提交和回滚
    //之后在调当前线程的此方法后,会抛出  current transaction is aborted, commands ignored until end of transaction block
    //错误
}
