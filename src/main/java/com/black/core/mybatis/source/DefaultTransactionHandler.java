package com.black.core.mybatis.source;

import com.black.core.util.CentralizedExceptionHandling;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.session.SqlSession;

import java.sql.Connection;
import java.sql.SQLException;

@Log4j2
public class DefaultTransactionHandler implements TransactionIbtaisSessionHandler {

    private final String alias;

    private final SqlSession session;
    public DefaultTransactionHandler(String alias, SqlSession session) {
        this.alias = alias;
        this.session = session;
    }

    @Override
    public void commit(boolean force) {
        try {
            session.commit(force);
        } catch (RuntimeException e) {
            CentralizedExceptionHandling.handlerException(e);
            if (log.isErrorEnabled()) {
                log.error("commit fail");
            }
        }
        if (log.isInfoEnabled()) {
            log.info("commit successful");
        }
    }

    @Override
    public void rollback() {
        try {
            session.rollback();
            if (log.isInfoEnabled()) {
                log.info("回滚数据源: {} 成功", alias);
            }
        }catch (RuntimeException e){
            CentralizedExceptionHandling.handlerException(e);
            if (log.isErrorEnabled()) {
                log.error("回滚操作失败");
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public SqlSession getSqlsession() {
        return session;
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public void openTransaction() {
        Connection connection = session.getConnection();
        try {
            if (connection != null && connection.getAutoCommit()){
                connection.setAutoCommit(false);
            }
        } catch (SQLException e) {
            CentralizedExceptionHandling.handlerException(e);
        }
    }
}
