package com.black.core.sql.code.session;

import com.black.core.spring.util.ApplicationUtil;
import com.black.core.sql.SQLSException;
import com.black.core.sql.code.GlobalSQLRunningListener;
import com.black.core.sql.code.log.Log;
import com.black.core.sql.code.StatementWrapper;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.code.pattern.ExecuteBody;
import com.black.core.sql.code.sqls.SqlValueGroup;
import com.black.core.sql.unc.SqlValue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractSessionExecutor {

    final SQLSignalSession session;

    protected AbstractSessionExecutor(SQLSignalSession session) {
        this.session = session;
    }

    abstract void addBatch(StatementWrapper sw, int batchSize, int batchCount) throws SQLException;


    public ExecuteBody doPipelineBatchExecute(String sql, List<SqlValueGroup> valueGroups, boolean query){
        session.isClosed();
        GlobalSQLConfiguration configuration = session.getConfiguration();
        int batchSize = 0, batchCount = 0;
        Log log = configuration.getLog();
        if (log.isDebugEnabled()) {
            log.debug("==> " + sql);
            log.debug("==> group batch: " + (valueGroups == null ? batchCount : (batchCount = valueGroups.size())));
        }
        boolean emtryBatch = valueGroups == null || valueGroups.isEmpty();
        Connection connection = session.getConnection();
        StatementWrapper statementWrapper = PrepareStatementFactory.getStatement(sql, configuration, connection, query);
        try {

            ExecuteBody executeBody = new ExecuteBody(statementWrapper);
            if (valueGroups != null){
                Iterator<SqlValueGroup> groupIterator = valueGroups.iterator();
                groupLoop: while (groupIterator.hasNext()){
                    SqlValueGroup valueGroup = groupIterator.next();
                    try {
                        for (GlobalSQLRunningListener runningListener : configuration
                                .getApplicationContext()
                                .getSQLRunningListeners()) {

                            if (runningListener.interceptBatchs(sql, valueGroup)) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Group data blocked by " + runningListener.getClass().getSimpleName());
                                }
                                runningListener.intercptCallback(sql, valueGroup, executeBody);
                                continue groupLoop;
                            }
                        }

                        StringBuilder paramBuidler = new StringBuilder("==> ");
                        List<SqlValue> valueList = valueGroup.getSqlValueList();
                        if(valueList.isEmpty()){
                            paramBuidler.append("no param!!! ");
                        }else {
                            for (SqlValue sqlValue : valueList) {
                                paramBuidler.append(sqlValue.getValue())
                                        .append("(")
                                        .append(sqlValue.getVariable().getIndex())
                                        .append(");");
                                sqlValue.setValue(statementWrapper);
                            }
                        }

                        if (log.isDebugEnabled()) {
                            log.debug(paramBuidler.toString());
                        }
                        if (!query){
                            addBatch(statementWrapper, ++batchSize, batchCount);
                            //Notify the listener to add batch post-processing
                            for (GlobalSQLRunningListener runningListener : configuration
                                    .getApplicationContext()
                                    .getSQLRunningListeners()) {

                                runningListener.afterAddBatch(statementWrapper, connection);
                            }
                        }
                    }finally {
                        try {
                            groupIterator.remove();
                        }catch (Throwable e){}
                    }
                }
            }

            if (!query){
                for (GlobalSQLRunningListener runningListener : configuration
                        .getApplicationContext()
                        .getSQLRunningListeners()) {

                    runningListener.afterProcessorOfBatch();
                }
            }

            int fbs = batchSize;
            ApplicationUtil.programRunMills(() ->{
                try {
                    if (query){
                        ResultSet executeQuery = statementWrapper.executeQuery();
                        executeBody.setQueryResult(executeQuery);
                    }
                    else {
                        statementWrapper.executeAndExecuteBatch(emtryBatch, fbs, executeBody);
                        statementWrapper.clearBatch();
                    }
                }catch (SQLException e){
                    throw new SQLSException(e);
                }
            }, "jdbc io task", log, "===>> ");
            return executeBody;
        } catch (SQLException e) {
            throw new SQLSException(e);
        }finally {
            try {
                statementWrapper.getPreparedStatement().clearParameters();
            } catch (SQLException e) {}
        }
    }

}
