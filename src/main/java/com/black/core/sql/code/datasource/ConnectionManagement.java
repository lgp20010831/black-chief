package com.black.core.sql.code.datasource;

import com.black.core.sql.SQLSException;
import com.black.core.sql.code.GlobalSQLRunningListener;
import com.black.core.sql.code.SQLApplicationContext;
import com.black.core.sql.code.TransactionSQLManagement;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.util.Assert;
import com.black.utils.LocalMap;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.log4j.Log4j2;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Function;

@Log4j2 @SuppressWarnings("all")
public class ConnectionManagement {

    private static final Map<String, DataSource> dataSourceManager = new ConcurrentHashMap<>();

    private static final LocalMap<String, Connection> connectionManager = new LocalMap<>();

    private static final Map<String, Collection<GlobalSQLRunningListener>> listeners = new ConcurrentHashMap<>();

    public static void registerApplicationContext(SQLApplicationContext context){
        GlobalSQLConfiguration configuration = context.getConfiguration();
        registerListener(configuration.getDataSourceAlias(), context.getSQLRunningListeners());
    }

    public static void registerListeners(String alias, GlobalSQLRunningListener... listeners){
        Collection<GlobalSQLRunningListener> queue = ConnectionManagement.listeners.computeIfAbsent(alias, al -> new LinkedBlockingQueue());
        queue.addAll(Arrays.asList(listeners));
    }

    public static void registerListener(String alias, Collection<GlobalSQLRunningListener> listeners){
        Collection<GlobalSQLRunningListener> queue = ConnectionManagement.listeners.computeIfAbsent(alias, al -> new LinkedBlockingQueue());
        queue.addAll(listeners);
    }

    public static Map<String, DataSource> getDataSourceManager() {
        return dataSourceManager;
    }


    public static DataSource getDataSource(String alias){
        return getDataSourceManager().get(alias);
    }

    public static Set<String> getAliasSet(){
        return listeners.keySet();
    }

    public static boolean existDataSource(String alias){
        return !dataSourceManager.containsKey(alias);
    }

    public static Map<String, Collection<GlobalSQLRunningListener>> getListeners() {
        return listeners;
    }

    public static void registerDataSource(String alias, DataSource dataSource){
        if (dataSourceManager.containsKey(alias)){
            if (log.isWarnEnabled()) {
                log.warn("The data source of this alias has been " +
                        "registered and managed, existing data source: [{}]", alias);
            }
            return;
        }
        dataSourceManager.put(alias, dataSource);
    }

    public static void shutdown(String alias){
        listeners.remove(alias);
        DataSource dataSource = dataSourceManager.remove(alias);
        if (dataSource != null){
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
                log.info("shutdown hikariDataSource: [{}]", alias);
                hikariDataSource.close();
            }
        }
    }

    public static void unregister(String alias){
        dataSourceManager.remove(alias);
    }

    public static void clearAll(){
        dataSourceManager.clear();
    }

    public static String getDefaultAlias(){
        if (listeners.size() == 1){
            return listeners.keySet().toArray(new String[0])[0];
        }
        return null;
    }

    //关闭数据源并通知监听者
    public static void closeConnection(Connection connection, String alias){
        Collection<GlobalSQLRunningListener> globalSQLRunningListeners = listeners.get(alias);
        if (globalSQLRunningListeners != null){
            for (GlobalSQLRunningListener runningListener : globalSQLRunningListeners) {
                runningListener.abandonConnection(connection, alias);
            }
        }

        //关闭连接
        SQLUtils.closeConnection(connection);
    }


    public static void employConnection(String alias, Consumer<Connection> consumer){
        Connection connection = getConnection(alias);
        try {
             consumer.accept(connection);
        }finally {
            if (!TransactionSQLManagement.isActivity(alias)) {
                SQLUtils.closeConnection(connection);
            }
        }
    }

    public static <R> R employConnection(String alias, Function<Connection, R> function){
        Connection connection = getConnection(alias);
        try {
            return function.apply(connection);
        }finally {
            if (!TransactionSQLManagement.isActivity(alias)) {
                SQLUtils.closeConnection(connection);
            }
        }
    }

    //检查数据源
    private static void checkDatasource(String alias){
        Assert.notNull(alias, "alias is not be null");
        if (!dataSourceManager.containsKey(alias)){
            if (log.isErrorEnabled()) {
                log.error("Connection cannot be obtained if " +
                        "specified data source is not managed, Nonexistent data source:[{}]", alias);
            }

            Connection connection = connectionManager.get(alias);
            if (connection != null){
                try {
                    connection.close();
                } catch (SQLException e) {
                    //ignore
                }
                connectionManager.remove(alias);
            }
            throw new SQLSException("nonexistent data source: " + alias);
        }
    }

    //关闭当前现成连接
    public static void closeCurrentConnection(String alias){
        checkDatasource(alias);
        Connection connection = connectionManager.remove(alias);
        if (connection != null){
            closeConnection(connection, alias);
        }
    }

    //获取连接
    public static Connection getConnection(String alias){

        //检查数据源是否存在
        checkDatasource(alias);

        DataSource dataSource = dataSourceManager.get(alias);
        if (dataSource instanceof DataSourceCacheWrapper){
            return openNewConnection(dataSource, alias);
        }
        if (!connectionManager.containsKey(alias)){
            connectionManager.put(alias, openNewConnection(dataSource, alias));
        }else {
            Connection connection = connectionManager.get(alias);
            if (!checkConnection(connection)){
                //移除连接
                connectionManager.remove(alias);
                //关闭连接
                closeConnection(connection, alias);
                return getConnection(alias);
            }
        }
        return connectionManager.get(alias);
    }

    //检查连接是否可用
    public static boolean checkConnection(Connection connection){
        try {
            return !connection.isClosed() && connection.isValid(2);
        } catch (SQLException e) {
            if (log.isWarnEnabled()) {
                log.warn("check connection is closed ?", e);
            }
            return false;
        }
    }

    //开启一个新的连接
    public static Connection openNewConnection(DataSource dataSource, String alias){
        try {
            if (log.isDebugEnabled()) {
                log.debug("open connection: {}", alias);
            }
            Connection connection = dataSource.getConnection();
            Collection<GlobalSQLRunningListener> globalSQLRunningListeners = listeners.get(alias);
            if (globalSQLRunningListeners != null){
                for (GlobalSQLRunningListener runningListener : globalSQLRunningListeners) {
                    runningListener.createNewConnection(connection, alias);
                }
            }
            return connection;
        } catch (SQLException e) {
            if (log.isErrorEnabled()) {
                log.error("An exception occurred when opening a new connection");
            }
            throw new SQLSException(e);
        }
    }
}
