package com.black.core.mybatis;

import com.black.core.util.CentralizedExceptionHandling;
import com.zaxxer.hikari.pool.HikariProxyPreparedStatement;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.executor.resultset.DefaultResultSetHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;

import java.sql.SQLException;

@Log4j2
@MybatisIntercept({"prepare", "result"})
public class SqlLogIntercept implements IbatisIntercept{

    @Override
    public Object doIntercept(MybatisLayerObject layer) {
        if (layer.isStatementHandler()) {
            String sql = layer.getStatementHandler().getBoundSql().getSql();
            if (log.isInfoEnabled()) {
                log.info("执行 sql ====>: {}", sql);
            }
        }else if (layer.isResultSetHandler()){
            ResultSetHandler resultSetHandler = layer.getResultSetHandler();
            if (resultSetHandler instanceof DefaultResultSetHandler){
                DefaultResultSetHandler handler = (DefaultResultSetHandler) resultSetHandler;
            }
            Object arg = layer.interceptsArgs()[0];
            if (arg instanceof HikariProxyPreparedStatement){
                HikariProxyPreparedStatement proxyPreparedStatement = (HikariProxyPreparedStatement) arg;
                if (log.isDebugEnabled()) {
                    try {
                        log.debug("result rows:{}", proxyPreparedStatement.getMaxRows());
                    } catch (SQLException e) {
                        if (log.isDebugEnabled()) {
                            log.debug("不合格的操作");
                            CentralizedExceptionHandling.handlerException(e);
                        }
                    }
                }
            }

        }
        return layer.doChain();
    }
}
