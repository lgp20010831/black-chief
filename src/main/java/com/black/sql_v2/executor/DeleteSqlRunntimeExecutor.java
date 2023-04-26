package com.black.sql_v2.executor;

import com.black.sql.SqlOutStatement;
import com.black.sql_v2.JDBCEnvironmentLocal;
import com.black.sql_v2.SqlExecutor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static com.black.core.sql.code.util.SQLUtils.closeStatement;

public class DeleteSqlRunntimeExecutor implements SqlRunntimeExecutor{
    @Override
    public boolean support(SqlOutStatement statement) {
        return statement.isDelete();
    }

    @Override
    public ResultSet runSql(SqlOutStatement statement, String sql) throws Throwable {
        Connection connection = JDBCEnvironmentLocal.getConnection();
        SqlExecutor executor = JDBCEnvironmentLocal.getPack().getExecutor();
        Statement javaStatement = null;
        try {
            executor.handlerJavaStatment(javaStatement = connection.createStatement(), JDBCEnvironmentLocal.getParams()).execute(sql);
        } catch (SQLException e) {
            closeStatement(javaStatement);
        }
        return null;
    }
}
