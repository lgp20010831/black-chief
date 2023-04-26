package com.black.core.mybatis;

import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.executor.statement.StatementHandler;

import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;


import java.sql.Connection;
import java.util.concurrent.locks.ReentrantLock;

@Log4j2
@Intercepts({@Signature(type = StatementHandler.class, method = MybatisConfig.PREPARE,
                args = {Connection.class, Integer.class})})
public class IbatisSqlLogPrintIntercept implements Interceptor {

    private final ReentrantLock reentrantLock = new ReentrantLock();

    private boolean lock = false;

    public void addLock(boolean addLock) {
        this.lock = addLock;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        try {
            if (lock){
                reentrantLock.lock();
            }

            Object target = invocation.getTarget();
            if (target instanceof StatementHandler){
                StatementHandler statementHandler = (StatementHandler) target;
                if (log.isInfoEnabled()) {
                    log.info("执行 sql ====>:\n {}", statementHandler.getBoundSql().getSql());
                }
            }
        }catch (Throwable e){
            if (log.isWarnEnabled()) {
                log.warn("尝试打印sql时发送异常:{}", e.getMessage());
            }
        }finally {
            if (lock){
                reentrantLock.unlock();
            }
        }
        return invocation.proceed();
    }
}
