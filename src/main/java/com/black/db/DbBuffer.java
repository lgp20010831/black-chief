package com.black.db;

import com.black.core.log.IoLog;
import com.black.core.sql.code.StatementWrapper;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.util.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class DbBuffer {

    private final DBGlobalConfiguration configuration;

    private final SqlPrepareHandler sqlPrepareHandler;

    public DbBuffer(DBGlobalConfiguration configuration) {
        this.configuration = configuration;
        sqlPrepareHandler = new SqlPrepareHandler(configuration);
    }

    public DBGlobalConfiguration getConfiguration() {
        return configuration;
    }

    public <T> T querySimpleSingle(String sql, Object... params){
        return SQLUtils.getSingle(queryList(sql, null, params));
    }

    public <T> T querySingle(String sql, Map<String, Object> env, Object... params){
        return SQLUtils.getSingle(queryList(sql, env, params));
    }

    public <T> List<T> querySimpleList(String sql, Object... params){
        return queryList(sql, null, params);
    }

    public <T> List<T> queryList(String sql, Map<String, Object> env, Object... params){
        if (!StringUtils.hasText(sql)){
            throw new DBSqlException("sql is not lenght");
        }
        if (!DBUtils.idioticJudgeIsSelectSql(sql)){
            throw new DBSqlException("sql is not a select sql");
        }
        IoLog log = configuration.getLog();
        sql = prepareSql(sql, env, params);
        if (log.isInfoEnabled()) {
            log.info("[DB] query sql: {}", sql);
        }
        //获取 jdbc 预编译语句
        StatementWrapper jdbcStatement = getJdbcStatement(sql, true);
        if (!configuration.isUseJpaFictitiousStrategy()){
            doPrepareStatement(jdbcStatement, params);
        }
        try {
            ResultSet resultSet = jdbcStatement.executeQuery();
            return (List<T>) parseResultSet(resultSet);
        }catch (Throwable e){
            throw new DBSqlException(e);
        }
    }

    public String updateSimpleSingle(String sql, Object... params){
        return SQLUtils.getSingle(update(sql, null, params));
    }

    public String updateSingle(String sql, Map<String, Object> env, Object... params){
        return SQLUtils.getSingle(update(sql, env, params));
    }

    public List<String> updateSimple(String sql, Object... params){
        return update(sql, null, params);
    }

    public List<String> update(String sql, Map<String, Object> env, Object... params){
        if (!StringUtils.hasText(sql)){
            throw new DBSqlException("sql is not lenght");
        }
        if (DBUtils.idioticJudgeIsSelectSql(sql)){
            throw new DBSqlException("sql is a select sql");
        }
        IoLog log = configuration.getLog();
        sql = prepareSql(sql, env, params);
        if (log.isInfoEnabled()) {
            log.info("[DB] update sql: {}", sql);
        }
        //获取 jdbc 预编译语句
        StatementWrapper jdbcStatement = getJdbcStatement(sql, false);
        if (!configuration.isUseJpaFictitiousStrategy()){
            doPrepareStatement(jdbcStatement, params);
        }
        try {
            jdbcStatement.executeUpdate();
            ResultSet generatedKeys = jdbcStatement.getGeneratedKeys();
            return DBUtils.parseGeneratedKeys(generatedKeys);
        }catch (Throwable e){
            throw new DBSqlException(e);
        }
    }

    protected Object parseResultSet(ResultSet resultSet){
        return SQLUtils.parseJavaResult(resultSet, configuration.getReturnType(), configuration.getConvertHandler());
    }

    protected void doPrepareStatement(StatementWrapper jdbcStatement, Object... params){
        IoLog log = configuration.getLog();
        PreparedStatement preparedStatement = jdbcStatement.getPreparedStatement();
        for (int i = 0; i < params.length; i++) {
            try {
                preparedStatement.setObject(i + 1, params[i]);
            } catch (SQLException e) {
                throw new DBSqlException(e);
            }
        }
    }

    protected StatementWrapper getJdbcStatement(String sql, boolean query){
        DBConnection dbConnection = configuration.getDbConnection();
        //获取数据库连接
        Connection connection = dbConnection.getFetchConnection();
        return DBStatmentFactory.getStatement(sql, query, configuration.isAllowScroll(), connection);
    }

    protected String prepareSql(String sql, Map<String, Object> env, Object... params){
        return sqlPrepareHandler.prepareSql(sql, env, params);
    }
}
