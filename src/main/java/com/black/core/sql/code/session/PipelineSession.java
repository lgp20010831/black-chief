package com.black.core.sql.code.session;

import com.black.core.sql.SQLSException;
import com.black.core.sql.code.*;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.code.log.Log;
import com.black.core.sql.code.pattern.ExecuteBody;
import com.black.core.sql.code.sqls.SqlValueGroup;

import java.sql.Connection;
import java.util.List;

public class PipelineSession extends TransactionSQLSession{

    final BaseSessionExecutor baseExecutor;

    final BatchStrategySessionExecutor strategyExecutor ;

    public PipelineSession(GlobalSQLConfiguration configuration) {
        super(configuration);
        baseExecutor = new BaseSessionExecutor(this);
        strategyExecutor = new BatchStrategySessionExecutor(this);
    }

    @Override
    public ExecuteBody pipelineSelect(String sql, List<SqlValueGroup> valueGroups) {
        return doPipelineExecute(sql, valueGroups, true);
    }

    @Override
    public ExecuteBody pipelineUpdates(String sql, List<SqlValueGroup> valueGroups) {
        return doPipelineExecute(sql, valueGroups, false);
    }

    @Override
    public ExecuteBody pipelineInserts(String sql, List<SqlValueGroup> valueGroups) {
        return doPipelineExecute(sql, valueGroups, false);
    }

    @Override
    public ExecuteBody pipelineDeletes(String sql, List<SqlValueGroup> valueGroups) {
        return doPipelineExecute(sql, valueGroups, false);
    }

    private ExecuteBody doPipelineExecute(String sql, List<SqlValueGroup> valueGroups, boolean query){
        Connection connection = getConnection();
        ExecuteBody result ;
        try {
            if (query){
                result = baseExecutor.doPipelineBatchExecute(sql, valueGroups, query);
            }else {
                result = strategyExecutor.doPipelineBatchExecute(sql, valueGroups, query);
                int count = result.getUpdateCount();
                Log log = configuration.getLog();
                if (log.isDebugEnabled()) {
                    log.debug("<== total:" + count);
                }
            }
        }catch (SQLSException sqlex){
            for (GlobalSQLRunningListener listener : configuration
                    .getApplicationContext()
                    .getSQLRunningListeners()) {
                listener.processorThrowable(sqlex, connection);
            }
            throw sqlex;
        }
        return result;
    }
}
