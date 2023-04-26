package com.black.sql_v2.executor;

import com.alibaba.fastjson.JSONObject;
import com.black.core.json.JsonUtils;
import com.black.core.log.IoLog;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.StatementWrapper;
import com.black.core.sql.code.pattern.ExecuteBody;
import com.black.core.sql.code.session.PrepareStatementFactory;
import com.black.core.sql.code.sqls.GeneratedKeysResultSetHandler;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.sql.unc.OperationType;
import com.black.core.sql.unc.SqlVariable;
import com.black.core.util.Assert;
import com.black.sql.InsertStatement;
import com.black.sql.SqlOutStatement;
import com.black.sql_v2.Environment;
import com.black.sql_v2.JDBCEnvironmentLocal;
import com.black.sql_v2.SqlExecutor;
import com.black.sql_v2.callback.InsertCallBack;
import com.black.sql_v2.utils.SqlV2Utils;
import com.black.table.ColumnMetadata;
import com.black.table.TableMetadata;
import com.black.utils.ServiceUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.StringJoiner;

public class InsertSqlRunntimeExecutor implements SqlRunntimeExecutor{

    public static boolean deepDebug = true;

    private GeneratedKeysResultSetHandler handler = new GeneratedKeysResultSetHandler();

    @Override
    public boolean support(SqlOutStatement statement) {
        return statement instanceof InsertStatement;
    }

    @Override
    public ResultSet runSql(SqlOutStatement statement, String sql) throws SQLException {
        Environment instance = JDBCEnvironmentLocal.getEnvironment();
        SqlExecutor executor = JDBCEnvironmentLocal.getPack().getExecutor();
        int insertBatch = instance.getInsertBatch();
        AliasColumnConvertHandler convertHandler = instance.getConvertHandler();
        IoLog log = instance.getLog();
        Object[] params = JDBCEnvironmentLocal.getParams();
        InsertCallBack<Object> insertCallBack = ServiceUtils.findInArray(params, InsertCallBack.class);
        Object attachment = JDBCEnvironmentLocal.attachment();
        Connection connection = JDBCEnvironmentLocal.getConnection();
        TableMetadata tableMetadata = SqlV2Utils.tryGetMetadata(statement.getTableName());
        Assert.notNull(connection, "not find connection");
        Assert.notNull(attachment, "not data to insert");
        //-----------------------------------------------------------------

        List<Object> objects = SQLUtils.wrapList(attachment);
        StatementWrapper sw = PrepareStatementFactory.getStatement(sql, false, false, connection, false);
        ExecuteBody executeBody = new ExecuteBody(sw);
        PreparedStatement preparedStatement = sw.getPreparedStatement();
        preparedStatement = (PreparedStatement) executor.handlerJavaStatment(preparedStatement, JDBCEnvironmentLocal.getParams());
        List<SqlVariable> variables = statement.getVariables(OperationType.INSERT);
        int addDataSize = 0;
        for (Object object : objects) {
            if (insertCallBack != null){
                insertCallBack.callback(object);
            }
            addDataSize++;
            JSONObject map = JsonUtils.toJson(object);
            StringJoiner joiner = deepDebug ? new StringJoiner(",", addDataSize + " ==> ", ";") : null;
            for (SqlVariable variable : variables) {
                int index = variable.getIndex();
                String columnName = variable.getColumnName();
                ColumnMetadata columnMetadata = tableMetadata.getColumnMetadata(columnName);
                Assert.notNull(columnMetadata, "null column is " + columnName);
                String alias = convertHandler.convertAlias(columnName);
                Object value = map.get(alias);
                SQLUtils.setStatementValue(preparedStatement, index, value, columnMetadata.getType(), instance.getDisplayConfiguration());
                if (joiner != null){
                    joiner.add(value + "(" + index + ")");
                }
            }

            if (joiner != null){
                log.trace("[SQL] -- {}", joiner);
            }
            sw.addBatch();
            if (addDataSize % insertBatch == 0){
                log.debug("[SQL] ==> batch submission:[" + insertBatch + "], how many times:[" + addDataSize / insertBatch + "]");
                sw.executeBatch();
                sw.clearBatch();
            }
        }
        sw.executeAndExecuteBatch(false, addDataSize, executeBody);
        return executeBody.getWrapper().getGeneratedKeys();
    }
}
