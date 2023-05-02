package com.black.sql_v2.executor;

import com.black.core.sql.code.StatementWrapper;
import com.black.core.sql.code.session.PrepareStatementFactory;
import com.black.core.sql.code.util.SQLUtils;
import com.black.sql.SqlOutStatement;
import com.black.sql.UpdateStatement;
import com.black.sql_v2.JDBCEnvironmentLocal;
import com.black.sql_v2.SqlExecutor;
import com.black.sql_v2.with.GeneratePrimaryManagement;

import java.sql.*;

public class UpdateSqlRunntimeExecutor implements SqlRunntimeExecutor{
    @Override
    public boolean support(SqlOutStatement statement) {
        return statement instanceof UpdateStatement;
    }

    @Override
    public ResultSet runSql(SqlOutStatement statement, String sql) throws Throwable {
        Connection connection = JDBCEnvironmentLocal.getConnection();
        SqlExecutor executor = JDBCEnvironmentLocal.getPack().getExecutor();
        StatementWrapper statementWrapper = PrepareStatementFactory.getStatement(sql, false, false, connection, false);
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = (PreparedStatement) executor.handlerJavaStatment(statementWrapper.getPreparedStatement(), JDBCEnvironmentLocal.getParams());
            preparedStatement.executeUpdate();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            GeneratePrimaryManagement.handler(generatedKeys);
        } catch (SQLException e) {
            SQLUtils.closeStatement(preparedStatement);
        }
        return null;
    }
}
