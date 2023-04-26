package com.black.pattern;

import com.black.core.sql.SQLSException;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.log4j.Log4j2;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class GlobalConnectionManagement {

    public static final String DEFAULT = "master";

    private static final Map<String, DataSource> dataSourceManager = new ConcurrentHashMap<>();

    private static final Map<String, Collection<ConnectionLifeCycleListener>> listenerCache = new ConcurrentHashMap<>();

    private static final ThreadLocal<Map<String, Connection>> connectionManager = new ThreadLocal<>();

    public static boolean containDataSource(String alias){
        return dataSourceManager.containsKey(alias);
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() ->{
            for (String alias : dataSourceManager.keySet()) {
                shutdown(alias);
            }
        }, "connection-shutdown"));
    }

    public static void registerListener(ConnectionLifeCycleListener listener){
        registerListener(DEFAULT, listener);
    }

    public static void registerListener(String alias, ConnectionLifeCycleListener listener){
        if (!containDataSource(alias)) {
            throw new SQLSException("not exist datasource: " + alias);
        }
        Collection<ConnectionLifeCycleListener> connectionLifeCycleListeners = listenerCache.computeIfAbsent(alias, al -> new HashSet<>());
        connectionLifeCycleListeners.add(listener);
    }

    public static void registerDataSource(DataSource dataSource){
        if (containDataSource(DEFAULT)){
            throw new SQLSException("default datasource is aleary exist");
        }
        registerDataSource(DEFAULT, dataSource);
    }

    public static void registerDataSource(String alias, DataSource dataSource){
        if (dataSourceManager.containsKey(alias)){
            if (log.isWarnEnabled()) {
                log.warn("The data source of this alias has been " +
                        "registered and managed, existing data source: {}", alias);
            }
            return;
        }
        dataSourceManager.put(alias, dataSource);
    }

    public static void shutdown(){
        shutdown(DEFAULT);
    }

    public static void shutdown(String alias){
        DataSource dataSource = dataSourceManager.remove(alias);
        if (dataSource != null){
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
                log.info("shutdown hikariDataSource: [{}]", alias);
                hikariDataSource.close();
            }
        }
    }

    public static void unregister(){
        unregister(DEFAULT);
    }

    public static void unregister(String alias){
        dataSourceManager.remove(alias);
    }

    public static void clearAll(){
        dataSourceManager.clear();
    }

    //关闭数据源并通知监听者
    public static void closeConnection(Connection connection, String alias){
        Collection<ConnectionLifeCycleListener> cycleListeners = listenerCache.get(alias);
        if (cycleListeners != null){
            for (ConnectionLifeCycleListener listener : cycleListeners) {
                listener.abandonConnection(alias, connection);
            }
        }
        try {
            //关闭连接
            connection.close();
        } catch (SQLException e) {
            //ignore
        }
    }

    //检查数据源
    private static void checkDatasource(String alias){
        if (!dataSourceManager.containsKey(alias)){
            if (log.isErrorEnabled()) {
                log.error("Connection cannot be obtained if " +
                        "specified data source is not managed, Nonexistent data source:{}", alias);
            }
            Map<String, Connection> connectionMap = connectionManager.get();
            Connection connection = connectionMap.get(alias);
            if (connection != null){
                try {
                    connection.close();
                } catch (SQLException e) {
                    //ignore
                }
                connectionMap.remove(alias);
            }
            throw new SQLSException("nonexistent data source: " + alias);
        }
    }

    public static void closeCurrentConnection(){
        closeCurrentConnection(DEFAULT);
    }

    //关闭当前现成连接
    public static void closeCurrentConnection(String alias){
        checkDatasource(alias);

        Map<String, Connection> connectionMap = connectionManager.get();
        Connection connection = connectionMap.get(alias);
        if (connection != null){
            closeConnection(connection, alias);
        }
    }

    public static Connection getConnection(){
        return getConnection(DEFAULT);
    }

    //获取连接
    public static Connection getConnection(String alias){
        initLocal();

        //检查数据源是否存在
        checkDatasource(alias);

        DataSource dataSource = dataSourceManager.get(alias);
        Map<String, Connection> connectionMap = connectionManager.get();
        if (!connectionMap.containsKey(alias)){
            connectionMap.put(alias, openNewConnection(dataSource, alias));
        }else {
            Connection connection = connectionMap.get(alias);
            if (!checkConnection(connection)){
                //移除连接
                connectionMap.remove(alias);
                //关闭连接
                closeConnection(connection, alias);
                return getConnection(alias);
            }
        }
        return connectionMap.get(alias);
    }

    public static void initLocal(){
        if (connectionManager.get() == null) {
            connectionManager.set(new ConcurrentHashMap<>());
        }
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
            Collection<ConnectionLifeCycleListener> cycleListeners = listenerCache.get(alias);
            if (cycleListeners != null){
                for (ConnectionLifeCycleListener listener : cycleListeners) {
                    listener.createNewConnection(alias, connection);
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
