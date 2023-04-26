package com.black.sql;

import com.black.core.log.CommonLog4jLog;
import com.black.core.log.IoLog;
import com.black.core.sql.HumpColumnConvertHandler;
import com.black.core.sql.SQLSException;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.DataSourceBuilder;
import com.black.core.sql.code.config.StatementValueSetDisplayConfiguration;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.sql.unc.SqlVariable;
import com.black.core.util.Assert;
import com.black.table.TableMetadata;
import com.black.table.TableUtils;
import lombok.NonNull;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

public class Insertor {


    private static Insertor insertor;


    public static Insertor getInstance() {
        if (insertor == null)
            insertor = new Insertor();
        return insertor;
    }

    private Insertor(){}

    private static final Map<String, DataSourceBuilder> dataSourceBuilderCache = new ConcurrentHashMap<>();

    public static AliasColumnConvertHandler convertHandler = new HumpColumnConvertHandler();

    public static final String DEF = "def";

    public static IoLog log = new CommonLog4jLog();

    public static void registerDataSource(@NonNull DataSourceBuilder builder){
        registerDataSource(DEF, builder);
    }

    public static void registerDataSource(@NonNull String key, @NonNull DataSourceBuilder builder){
        dataSourceBuilderCache.put(key, builder);
    }

    public static void setLog(IoLog log) {
        Insertor.log = log;
    }

    public static void setConvertHandler(AliasColumnConvertHandler convertHandler) {
        Insertor.convertHandler = convertHandler;
    }

    public InsertBuilder prepare(String tableName, Object arg){
        return new InsertBuilder(this, tableName, arg);
    }

    public static class InsertBuilder{

        private final Insertor insertor;
        private final String tableName;
        private Object arg;
        private String alias = DEF;
        private StatementValueSetDisplayConfiguration setDisplayConfiguration;
        public InsertBuilder(Insertor insertor, String tableName, Object arg) {
            this.insertor = insertor;
            this.tableName = tableName;
            this.arg = arg;
        }

        public InsertBuilder setArg(Object arg) {
            this.arg = arg;
            return this;
        }

        public InsertBuilder setAlias(String alias) {
            this.alias = alias;
            return this;
        }

        public InsertBuilder setSetDisplayConfiguration(StatementValueSetDisplayConfiguration setDisplayConfiguration) {
            this.setDisplayConfiguration = setDisplayConfiguration;
            return this;
        }

        public String single(){
            return SQLUtils.getSingle(list());
        }

        public List<String> list(){
            return insertor.insert(tableName, alias, arg, setDisplayConfiguration);
        }
    }


    public List<String> insert(String tableName, String alias, Object arg){
        return insert(tableName, alias, arg, null);
    }

    public List<String> insert(String tableName, String alias, Object arg, StatementValueSetDisplayConfiguration setDisplayConfiguration){
        DataSourceBuilder builder = dataSourceBuilderCache.get(alias);
        Assert.notNull(builder, "not find datasource: " + alias);
        List<Object> list = SQLUtils.wrapList(arg);
        DataSource dataSource = builder.getDataSource();
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            SqlOutStatement sql = SqlWriter.insert(tableName);
            TableMetadata metadata = TableUtils.getTableMetadata(tableName, connection);
            Assert.notNull(metadata, "无法找到: " + tableName + " 数据源");
            for (String column : metadata.getColumnNameSet()) {
                sql.insertVariable(column, "?");
            }
            sql.flush();
            String sqlTxt = sql.toString();
            if (log != null && log.isDebugEnabled()) {
                log.info("==> insertor insert sql: [" + sqlTxt + "]");
            }
            PreparedStatement statement = connection.prepareStatement(sqlTxt, Statement.RETURN_GENERATED_KEYS);
            for (Object obj : list) {
                Map<String, Object> json = SQLUtils.wrapMap(obj);
                StringJoiner joiner = new StringJoiner(",");
                for (SqlVariable variable : sql.getVariables()) {
                    int index = variable.getIndex();
                    String javaName = convertHandler.convertAlias(variable.getColumnName());
                    Object val = json.get(javaName);
                    int type = metadata.getColumnMetadata(variable.getColumnName()).getType();
                    SQLUtils.setStatementValue(statement, index, val, type, setDisplayConfiguration);
                    joiner.add("(" + index + ")" + val);
                }
                log.info("add batch: " + joiner);
                statement.addBatch();
            }
            statement.executeBatch();
            List<String> ids = new ArrayList<>();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            while (generatedKeys.next()) {
                ids.add(generatedKeys.getString(1));
            }
            SQLUtils.closeResultSet(generatedKeys);
            SQLUtils.closeStatement(statement);
            return ids;
        }catch (Throwable e){
            throw new SQLSException(e);
        }finally {
            SQLUtils.closeConnection(connection);
        }
    }
}
