package com.black.core.sql.code.session;

import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.code.pattern.ExecuteBody;
import com.black.core.sql.code.sqls.SqlValueGroup;

import java.sql.Connection;
import java.util.List;

public interface SQLSignalSession {

    void isClosed();

    ExecuteBody pipelineSelect(String sql, List<SqlValueGroup> valueGroups);

    ExecuteBody pipelineInserts(String sql, List<SqlValueGroup> valueGroups);

    ExecuteBody pipelineUpdates(String sql, List<SqlValueGroup> valueGroups);

    ExecuteBody pipelineDeletes(String sql, List<SqlValueGroup> valueGroups);

    GlobalSQLConfiguration getConfiguration();

    Connection getConnection();

    void close();
}
