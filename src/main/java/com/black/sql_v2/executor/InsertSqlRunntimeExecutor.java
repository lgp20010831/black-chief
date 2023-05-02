package com.black.sql_v2.executor;

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
import com.black.sql_v2.utils.VarcharIdType;
import com.black.sql_v2.with.GeneratePrimaryManagement;
import com.black.sql_v2.with.WaitGenerateWrapper;
import com.black.table.ColumnMetadata;
import com.black.table.PrimaryKey;
import com.black.table.TableMetadata;
import com.black.template.jdbc.JdbcType;
import com.black.utils.IdUtils;
import com.black.utils.ServiceUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringJoiner;

@SuppressWarnings("all")
public class InsertSqlRunntimeExecutor implements SqlRunntimeExecutor{

    public static boolean deepDebug = true;

    private GeneratedKeysResultSetHandler handler = new GeneratedKeysResultSetHandler();

    @Override
    public boolean support(SqlOutStatement statement) {
        return statement instanceof InsertStatement;
    }

    @Override
    public List<Object> runSql(SqlOutStatement statement, String sql) throws SQLException {
        Environment instance = JDBCEnvironmentLocal.getEnvironment();
        SqlExecutor executor = JDBCEnvironmentLocal.getPack().getExecutor();
        int insertBatch = instance.getInsertBatch();
        AliasColumnConvertHandler convertHandler = instance.getConvertHandler();
        IoLog log = instance.getLog();
        Object[] params = JDBCEnvironmentLocal.getParams();
        InsertCallBack<Object> insertCallBack = ServiceUtils.findInArray(params, InsertCallBack.class);
        Object attachment = JDBCEnvironmentLocal.attachment();
        if (attachment != null){
            GeneratePrimaryManagement.register(new WaitGenerateWrapper(attachment,
                    executor.getEnvironment().getConvertHandler(), JDBCEnvironmentLocal.getPack().getPassID(), statement.getTableName()));
        }
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
        Map<String, PrimaryKey> primaryKeyMap = tableMetadata.getPrimaryKeyMap();
        for (Object object : objects) {
            if (insertCallBack != null){
                insertCallBack.callback(object);
            }
            addDataSize++;
            Map<String, Object> map = executor.serialize(object);
            StringJoiner joiner = deepDebug ? new StringJoiner(",", addDataSize + " ==> ", ";") : null;
            for (SqlVariable variable : variables) {
                int index = variable.getIndex();
                String columnName = variable.getColumnName();
                boolean isPrimary = primaryKeyMap.containsKey(columnName);

                ColumnMetadata columnMetadata = tableMetadata.getColumnMetadata(columnName);
                Assert.notNull(columnMetadata, "null column is " + columnName);
                String alias = convertHandler.convertAlias(columnName);
                Object value = map.get(alias);
                if (isPrimary && value == null && instance.isAutoSetId()){
                    PrimaryKey primaryKey = primaryKeyMap.get(columnName);
                    if (primaryKey != null && !primaryKey.autoIncrement()){
                        //根据类型生成主键
                        value = autoCreateId(columnMetadata);
                        ServiceUtils.setProperty(object, alias, value);
                    }
                }
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
        sw.clearBatch();
        ResultSet generatedKeys = executeBody.getWrapper().getGeneratedKeys();
        return GeneratePrimaryManagement.handler(generatedKeys);
    }

    private Object autoCreateId(ColumnMetadata columnMetadata){
        Environment environment = JDBCEnvironmentLocal.getEnvironment();
        Class<?> javaClass = JdbcType.getByJdbcType(columnMetadata.getType()).getJavaClass();
        if (Number.class.isAssignableFrom(javaClass)){
            return new Random().nextInt(environment.getIncreasingRandomRange()) + 1;
        }else {
            VarcharIdType idType = environment.getVarcharIdType();
            switch (idType){
                case UUID:
                    return IdUtils.createId();
                case SHORT8:
                    return IdUtils.createShort8Id();
                case SHORT22:
                    return IdUtils.createShort22Id();
                default:
                    return IdUtils.createId();
            }
        }
    }
}
