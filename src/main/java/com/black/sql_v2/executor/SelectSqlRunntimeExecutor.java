package com.black.sql_v2.executor;

import com.black.core.sql.SQLSException;
import com.black.core.util.Assert;
import com.black.database.calcite.SchemaDataSource;
import com.black.sql.SqlOutStatement;
import com.black.sql_v2.Environment;
import com.black.sql_v2.JDBCEnvironmentLocal;
import com.black.sql_v2.SqlExecutor;
import com.black.sql_v2.utils.SqlV2Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

public class SelectSqlRunntimeExecutor implements SqlRunntimeExecutor{
    @Override
    public boolean support(SqlOutStatement statement) {
        return SqlV2Utils.isSelectStatement(statement);
    }

    @Override
    public ResultSet runSql(SqlOutStatement statement, String sql) throws Throwable {
        Connection connection = JDBCEnvironmentLocal.getConnection();
        SqlExecutor executor = JDBCEnvironmentLocal.getPack().getExecutor();
        Environment environment = executor.getEnvironment();

        PreparedStatement prepareStatement = null;
        try {
            if (environment.isOpenMemorySelect()){
                Set<String> memoryTables = environment.getMemoryTables();
                if (memoryTables.contains(statement.getTableName())){
                    String schame = environment.getCurrentSchame();
                    Assert.notNull(schame, "can not find current schema");
                    SchemaDataSource dataSource = new SchemaDataSource(schame);
                    Connection memoryConnection = dataSource.getConnection();
                    prepareStatement = memoryConnection.prepareStatement(sql);
                }
            }

            if (prepareStatement == null){
                prepareStatement = connection.prepareStatement(sql);
            }
            prepareStatement = (PreparedStatement) executor.handlerJavaStatment(prepareStatement, JDBCEnvironmentLocal.getParams());
            return prepareStatement.executeQuery();
        } catch (SQLException e) {
            throw new SQLSException(e);
        }
    }
}
