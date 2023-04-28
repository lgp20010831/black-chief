package com.black.sql_v2;

import com.alibaba.fastjson.JSONObject;
import com.black.core.log.IoLog;
import com.black.core.sql.SQLSException;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.DataSourceBuilder;
import com.black.core.sql.code.SpringDataSourceBuilder;
import com.black.core.sql.code.TransactionSQLManagement;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.tools.BeanUtil;
import com.black.core.util.Assert;
import com.black.core.util.Av0;
import com.black.core.util.StreamUtils;
import com.black.json.JsonParser;
import com.black.sql.NativeSqlAdapter;
import com.black.sql.QueryResultSetParser;
import com.black.sql.SqlOutStatement;
import com.black.sql_v2.callback.UpdateCallBack;
import com.black.sql_v2.executor.SqlRunntimeExecutor;
import com.black.sql_v2.handler.SqlStatementHandler;
import com.black.sql_v2.listener.SqlListener;
import com.black.sql_v2.result.ResultSetParserCreator;
import com.black.sql_v2.result.SqlResultHandler;
import com.black.sql_v2.serialize.SerializeUtils;
import com.black.sql_v2.sql_statement.JavaSqlStatementHandler;
import com.black.sql_v2.statement.SqlStatementBuilder;
import com.black.sql_v2.transaction.NestedTransactionControlManager;
import com.black.sql_v2.utils.SqlV2Utils;
import com.black.table.PrimaryKey;
import com.black.table.TableMetadata;
import com.black.table.TableUtils;
import com.black.utils.ServiceUtils;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import static com.black.utils.ServiceUtils.*;

@SuppressWarnings("all")
public class SqlExecutor implements NativeSqlAdapter {

    private final DataSourceBuilder dataSourceBuilder;

    private final String name;

    private Environment environment;

    public SqlExecutor(String name){
        this(null, name);
    }

    public SqlExecutor(DataSourceBuilder dataSourceBuilder, String name) {

        GlobalEnvironment globalEnvironment = GlobalEnvironment.getInstance();
        environment = new Environment(globalEnvironment);
        if (dataSourceBuilder == null){
            dataSourceBuilder = GlobalEnvironment.getDataSourceBuilder(name);
            if (dataSourceBuilder == null){
                dataSourceBuilder = globalEnvironment.getDataSourceBuilder();
            }
        }
        this.dataSourceBuilder = dataSourceBuilder;
        DataSource dataSource = dataSourceBuilder.getDataSource();
        this.name = name;
        ConnectionManagement.registerDataSource(name, dataSource);
        //保证事务
        ConnectionManagement.registerListeners(name,
                new TransactionSQLManagement.TransactionConnectionListener(name));
        BeanUtil.mappingBean(globalEnvironment, environment);
    }

    public boolean isManageConnectionWithSpring(){
        return dataSourceBuilder instanceof SpringDataSourceBuilder;
    }

    public boolean springTransactionEnabled(){
        Connection connection = getConnection();
        DataSource dataSource = dataSourceBuilder.getDataSource();
        return DataSourceUtils.isConnectionTransactional(connection, dataSource);
    }

    public TableMetadata getTableMetadata(String tableName){
        Connection connection = getConnection();
        try {
            return TableUtils.getTableMetadata(tableName, connection);
        }finally {
            closeConnection();
        }
    }

    public Environment getEnvironment() {
        return environment;
    }

    public String getName() {
        return name;
    }

    public DataSourceBuilder getDataSourceBuilder() {
        return dataSourceBuilder;
    }

    @Override
    public Connection getNativeFetchConnection() {
        return getConnection();
    }

    @Override
    public void closeNativeFetchConnection(Connection connection) {
        ConnectionManagement.closeCurrentConnection(name);
    }

    public Connection getConnection(){
        return ConnectionManagement.getConnection(name);
    }

    protected void closeConnection(Connection connection){
        //如果事务不存活 或者 联结事务不存活
        if (!(TransactionSQLManagement.isActivity(name) || NestedTransactionControlManager.isAtive(name))) {

            if (!isManageConnectionWithSpring()){
                ConnectionManagement.closeCurrentConnection(name);
            }else {
                //如果 spring 事务不存活
                if (!springTransactionEnabled()){
                    Connection fetchConnection = getConnection();
                    DataSource dataSource = dataSourceBuilder.getDataSource();
                    //关闭 spring 维护的连接
                    DataSourceUtils.releaseConnection(fetchConnection, dataSource);
                    //关闭自己的连接
                    ConnectionManagement.closeCurrentConnection(name);
                }
            }

        }
    }

    public void closeConnection(){
        closeConnection(null);
    }

    public String findPrimaryKeyAliasName(String tableName){
        return getEnvironment().getConvertHandler().convertAlias(findPrimaryKeyName(tableName));
    }

    public String findPrimaryKeyName(String tableName){
        return ConnectionManagement.employConnection(name, connection -> {
            TableMetadata metadata = TableUtils.getTableMetadata(tableName, connection);
            Assert.notNull(metadata, "unknown table of:[" + tableName + "]");
            PrimaryKey primaryKey = metadata.firstPrimaryKey();
            return primaryKey == null ? null : primaryKey.getName();
        });
    }

    public List<String> findPrimaryKeyNames(String tableName){
        return ConnectionManagement.employConnection(name, connection -> {
            TableMetadata metadata = TableUtils.getTableMetadata(tableName, connection);
            Assert.notNull(metadata, "unknown table of:[" + tableName + "]");
            Collection<PrimaryKey> primaryKeys = metadata.getPrimaryKeys();
            return StreamUtils.mapList(primaryKeys, PrimaryKey::getName);
        });
    }

    public List<String> findPrimaryKeyAliasNames(String tableName){
        AliasColumnConvertHandler convertHandler = getEnvironment().getConvertHandler();
        return StreamUtils.mapList(findPrimaryKeyNames(tableName), name -> convertHandler.convertAlias(name));
    }

    public void deleteById(String tableName, Object idValue, Object... params){
        String primaryKeyName = findPrimaryKeyName(tableName);
        if (primaryKeyName == null){
            throw new SQLSException("unknown primary key of table: " + tableName);
        }
        params = SqlV2Utils.addParams(params, Av0.of(findPrimaryKeyAliasName(tableName), idValue));
        delete(tableName, params);
    }

    public void delete(String tableName, Object... params){
        prepare(params);
        GlobalEnvironment instance = GlobalEnvironment.getInstance();
        SqlStatementBuilder sqlBuilder = instance.getDefaultDeleteSqlBuilder();
        SqlOutStatement statement = sqlBuilder.build(tableName, false, params);
        doExecute(statement, params);
    }

    public <T> void saveBatch(String tableName, Collection<T> collection, Object... params){
        for (T t : collection) {
            save(tableName, t, params);
        }
    }

    public void save(Object bean, Object... params){
        String tableName = SqlV2Utils.findTableName(bean, environment.getConvertHandler());
        if (tableName == null){
            throw new SQLSException("CAN NOT FIND TABLE NAME OF: " + bean);
        }
        save(tableName, bean, params);
    }

    public <T> void save(String tableName, T param, Object... params){
        saveAndEffect(tableName, param, false, params);
    }

    public <T> void saveAndEffectBatch(String tableName, Collection<T> collection, boolean effect, Object... params){
        for (T t : collection) {
            saveAndEffect(tableName, t, effect, params);
        }
    }

    public <T> void saveAndEffect(String tableName, T param, boolean effect, Object... params){
        List<String> primaryKeyAliasNames = findPrimaryKeyAliasNames(tableName);
        //JSONObject json = JsonUtils.letJson(param);
        Map<String, Object> json = SerializeUtils.serialize(param);
        if (ServiceUtils.containKeys(json, primaryKeyAliasNames.toArray(new String[0]))){
            Map<String, Object> effectCondition = createEffectCondition(json, primaryKeyAliasNames);
            if (effect){
                //效验
                int count = queryCount(tableName, effectCondition);
                if (count <= 0){
                    //go insert
                    insert(tableName, addArray(params, json, true));
                    return;
                }
            }
            //go update
            update(tableName, param, addArray(params, effectCondition, true));
        }else {
            //go insert
            insert(tableName, addArray(params, json, true));
        };
    }

    protected Map<String, Object> createEffectCondition(Map<String, Object> param, List<String> primaryKeyAliasNames){
        return filterNewMap(param, primaryKeyAliasNames);
    }

    public void updateById(String tableName, Object setMap, Object idValue, Object... params){
        String name = findPrimaryKeyAliasName(tableName);
        if (name == null){
            throw new SQLSException("unknown primary key of table: " + tableName);
        }
        update(tableName, setMap, addArray(params, Av0.of(name, idValue), true));
    }

    public void prepareUdpate(String tableName, String setMap, Object bean, Object... params){
        JsonParser jsonParser = getEnvironment().getJsonParser();
        JSONObject json = SqlV2Utils.prepareJson(jsonParser, setMap, bean);
        update(tableName, json, params);
    }

    public <T> void update(String tableName, T setMap, Object... params){
        prepare(params);
        GlobalEnvironment instance = GlobalEnvironment.getInstance();
        SqlStatementBuilder sqlBuilder = instance.getDefaultUpdateSqlBuilder();
        SqlOutStatement statement = sqlBuilder.build(tableName, false, params);

        //find call back
        UpdateCallBack<T> updateCallBack = findInArray(params, UpdateCallBack.class);
        if (updateCallBack != null){
            updateCallBack.callback(setMap);
        }
        //JSONObject json = JsonUtils.letJson(setMap);
        Map<String, Object> json = SerializeUtils.serialize(setMap);
        //处理 setMap 里的值
        Connection connection = getConnection();
        TableMetadata tableMetadata = TableUtils.getTableMetadata(tableName, connection);
        SqlV2Utils.setMapToStatement(statement, environment.getConvertHandler(), json, tableMetadata);
        doExecute(statement, params);
    }


    public String insert(String tableName, Object... params){
        return SQLUtils.getSingle(insertBatch(tableName, params));
    }

    public List<String> insertBatch(String tableName, Object... params){
        prepare(params);
        GlobalEnvironment environment = GlobalEnvironment.getInstance();
        SqlStatementBuilder statementBuilder = environment.getDefaultInsertSqlBuilder();
        SqlOutStatement statement = statementBuilder.build(tableName, false, null);
        QueryResultSetParser parser = doExecute(statement, params);
        return parser.stringList();
    }

    public QueryResultSetParser queryPrimary(String tableName, Object target, Object... params){
        List<String> aliasNames = findPrimaryKeyAliasNames(tableName);
        //JSONObject json = JsonUtils.letJson(target);
        Map<String, Object> json = SerializeUtils.serialize(target);
        Map<String, Object> condition = createEffectCondition(json, aliasNames);
        params = SqlV2Utils.addParams(params, condition);
        return query(tableName, params);
    }

    public QueryResultSetParser queryById(String tableName, Object idValue, Object... params){
        String primaryKeyName = findPrimaryKeyName(tableName);
        if (primaryKeyName == null){
            throw new SQLSException("unknown primary key of table: " + tableName);
        }
        params = SqlV2Utils.addParams(params, Av0.of(findPrimaryKeyAliasName(tableName), idValue));
        return query(tableName, params);
    }

    public QueryResultSetParser query(String tableName, Object... params){
        prepare(params);
        SqlOutStatement statement = createSelectStatement(tableName, false, params);
        return doExecute(statement, params);
    }

    public int queryCount(String tableName, Object... params){
        prepare(params);
        GlobalEnvironment globalEnvironment = GlobalEnvironment.getInstance();
        SqlStatementBuilder statementBuilder = globalEnvironment.getDefaultSelectCountSqlBuilder();
        SqlOutStatement statement = statementBuilder.build(tableName, false, null);
        return doExecute(statement, params).intVal();
    }

    public QueryResultSetParser doExecute(SqlOutStatement sqlOutStatement, Object... params){
        Connection connection = getConnection();
        try {
            return doExecute0(sqlOutStatement, connection, params);
        }catch (Throwable e){
            if (e instanceof SQLSException){
                throw e;
            }else {
                throw new SQLSException(e);
            }
        }
    }

    public QueryResultSetParser doExecute0(SqlOutStatement sqlOutStatement, Connection connection, Object... params){
        GlobalEnvironment globalEnvironment = GlobalEnvironment.getInstance();
        LinkedBlockingQueue<SqlListener> listeners = globalEnvironment.getListeners();
        IoLog log = environment.getLog();
        try {
            sqlOutStatement = handlerStatement(sqlOutStatement, params);
            for (SqlListener listener : listeners) {
                listener.beforeFlush(sqlOutStatement, this);
            }
            flush(sqlOutStatement, connection);
            String sql = sqlOutStatement.toString();

            for (SqlListener listener : listeners) {
                sql = listener.postInvokeSql(sql, sqlOutStatement, this);
            }
            QueryResultSetParser parser = runSql(sqlOutStatement, sql);
            if (parser != null){
                parser.setFinish(() -> {
                    closeConnection(connection);
                });
                return parser;
            }else {
                closeConnection(connection);
                return null;
            }
        }finally {
            end();
        }
    }

    public QueryResultSetParser runSql(SqlOutStatement sqlOutStatement, String sql){
        GlobalEnvironment globalEnvironment = GlobalEnvironment.getInstance();
        IoLog log = environment.getLog();
        log.info("[SQL] -- execute sql -- {}", sql);
        ResultSet resultSet = null;
        boolean invoke = false;
        LinkedBlockingQueue<SqlRunntimeExecutor> sqlRunntimeExecutors = globalEnvironment.getSqlRunntimeExecutors();
        for (SqlRunntimeExecutor runntimeExecutor : sqlRunntimeExecutors) {
            if (runntimeExecutor.support(sqlOutStatement)) {
                try {
                    resultSet = runntimeExecutor.runSql(sqlOutStatement, sql);
                    invoke = true;
                    break;
                } catch (Throwable throwable) {
                    throw new SQLSException(throwable);
                }
            }
        }
        if (!invoke){
            throw new SQLSException("No processor specified");
        }
        if (resultSet == null){
            return null;
        }
        return ResultSetParserCreator.create(sqlOutStatement, resultSet, this);
    }

    private void end(){
        JDBCEnvironmentLocal.remove();
    }

    private SqlV2Pack prepare(Object... params){
        SqlV2Pack pack = new SqlV2Pack();
        pack.setExecutor(this);
        pack.setParams(params);
        pack.setConnection(getConnection());
        pack.setEnvironment(environment);
        JDBCEnvironmentLocal.set(pack);
        return pack;
    }


    private void flush(SqlOutStatement statement, Connection connection){
        String tableName = statement.getTableName();
        TableMetadata tableMetadata = TableUtils.getTableMetadata(tableName, connection);
        if (tableMetadata == null){
            throw new SQLSException("can not find table: "+ tableName);
        }
        statement.calibration(tableMetadata);
        statement.flush();
    }

    public Statement handlerJavaStatment(Statement statement, Object... params){
        GlobalEnvironment globalEnvironment = GlobalEnvironment.getInstance();
        IoLog ioLog = getEnvironment().getLog();
        LinkedBlockingQueue<JavaSqlStatementHandler> statementHandlers = globalEnvironment.getJavaSqlStatementHandlers();
        P: for (Object param : params) {
            for (JavaSqlStatementHandler statementHandler : statementHandlers) {
                String handlerName = SqlV2Utils.getName(statementHandler);
                if (statementHandler.support(param)) {
                    try {
                        ioLog.debug("[SQL] -- java statement handler: [{}] processing ...",
                                handlerName);
                        statement = statementHandler.handlerJavaStatement(statement, param);
                    } catch (Throwable e) {
                        ioLog.error("[SQL] -- An error occurred in the processor: {} " +
                                "processing statement", handlerName);
                        throw new SQLSException(e);
                    }
                    if (statement == null){
                        throw new IllegalStateException("Processor exception handling statement " +
                                "returned null -- " + handlerName);
                    }
                    continue P;
                }
            }
        }

        return statement;
    }

    private SqlOutStatement handlerStatement(SqlOutStatement sqlOutStatement, Object... params){
        GlobalEnvironment globalEnvironment = GlobalEnvironment.getInstance();
        LinkedBlockingQueue<SqlStatementHandler> sqlStatementHandlers = globalEnvironment.getSqlStatementHandlers();
        for (Object param : params) {
            for (SqlStatementHandler statementHandler : sqlStatementHandlers) {
                if (!statementHandler.supportStatement(sqlOutStatement)) {
                    continue;
                }
                if (statementHandler.support(param)) {
                    long start = System.currentTimeMillis();
                    sqlOutStatement = statementHandler.handleStatement(sqlOutStatement, param);
                    environment.getLog().debug("[SQL] -- statement handler: [{}] processing finish ... take: {} ms",
                            SqlV2Utils.getName(statementHandler), System.currentTimeMillis() - start);

                    break;
                }
            }
        }
        return sqlOutStatement;
    }

    public void handlerResult(List<Map<String, Object>> dataList, SqlOutStatement statement, SqlV2Pack pack){
        GlobalEnvironment globalEnvironment = GlobalEnvironment.getInstance();
        LinkedBlockingQueue<SqlResultHandler> resultHandlers = globalEnvironment.getResultHandlers();
        Object[] params = pack.getParams();
        for (Object param : params) {
            for (SqlResultHandler statementHandler : resultHandlers) {
                if (!statementHandler.supportStatement(statement)) {
                    continue;
                }
                if (statementHandler.support(param)) {
                    long start = System.currentTimeMillis();
                    statementHandler.handlerResultList(statement, dataList, param, pack);
                    environment.getLog().debug("[SQL] -- result handler: [{}] processing finish ... take: {} ms",
                            SqlV2Utils.getName(statementHandler), System.currentTimeMillis() - start);

                    break;
                }
            }
        }
    }


    public SqlOutStatement createSelectStatement(String tableName, boolean alias, Object... params){
        GlobalEnvironment globalEnvironment = GlobalEnvironment.getInstance();
        SqlStatementBuilder builder = null;
        Object targetParam = null;
        LinkedBlockingQueue<SqlStatementBuilder> selectSqlBuilders = globalEnvironment.getSelectSqlBuilders();
        loop: for (SqlStatementBuilder selectSqlBuilder : selectSqlBuilders) {
            for (Object param : params) {
                if (selectSqlBuilder.support(param)) {
                    builder = selectSqlBuilder;
                    targetParam = param;
                    break loop;
                }
            }
        }
        if (builder == null){
            builder = globalEnvironment.getDefaultSelectSqlBuilder();
        }
        environment.getLog().debug("[SQL] -- select statement builder is [{}]", SqlV2Utils.getName(builder));
        return builder.build(tableName, alias, targetParam);
    }


}
