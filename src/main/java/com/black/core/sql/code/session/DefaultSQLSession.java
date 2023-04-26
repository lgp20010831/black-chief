package com.black.core.sql.code.session;

import com.black.core.sql.SQLSException;
import com.black.core.sql.code.*;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.sql.code.log.Log;
import com.black.core.sql.code.pattern.ExecuteBody;
import com.black.core.sql.code.sqls.SqlValueGroup;
import lombok.extern.log4j.Log4j2;

import java.sql.*;
import java.util.List;



@Log4j2
public class DefaultSQLSession implements SQLSignalSession {

    protected final GlobalSQLConfiguration configuration;
    private volatile boolean close;

    public DefaultSQLSession(GlobalSQLConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void isClosed() {
        if (close){
            try {
                getConnection();
            }catch (SQLSException e){
                Log log = configuration.getLog();
                if (log.isDebugEnabled()) {
                    log.debug("close -" + configuration.getDataSourceAlias() + "- connection");
                }
            }
        }
    }

    @Override
    public void close() {
        close = true;
    }

    @Override
    public GlobalSQLConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public Connection getConnection(){
        return ConnectionManagement.getConnection(configuration.getDataSourceAlias());
    }

    @Override
    public ExecuteBody pipelineInserts(String sql, List<SqlValueGroup> valueGroups) {
        throw new UnsupportedOperationException("not a pipeline session");
    }

    @Override
    public ExecuteBody pipelineSelect(String sql, List<SqlValueGroup> valueGroups) {
        throw new UnsupportedOperationException("not a pipeline session");
    }

    @Override
    public ExecuteBody pipelineUpdates(String sql, List<SqlValueGroup> valueGroups) {
        throw new UnsupportedOperationException("not a pipeline session");
    }

    @Override
    public ExecuteBody pipelineDeletes(String sql, List<SqlValueGroup> valueGroups) {
        throw new UnsupportedOperationException("not a pipeline session");
    }

    protected StatementWrapper createStatementWrapper(Statement statement){
        return new StatementWrapper(statement);
    }
}
