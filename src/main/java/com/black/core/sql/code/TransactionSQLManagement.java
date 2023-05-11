package com.black.core.sql.code;

import com.black.core.sql.SQLSException;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.sql.code.log.Log;
import com.black.core.sql.code.log.SystemLog;
import com.black.core.util.Callables;
import com.black.utils.LocalSet;
import lombok.extern.log4j.Log4j2;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class TransactionSQLManagement {

    private static final ThreadLocal<Map<String, TransactionHandler>> handlerCache = new ThreadLocal<>();

    private static final LocalSet<String> activityTransactions = new LocalSet<>();

    public static boolean isActivity(String alias){
        return activityTransactions.contains(alias);
    }

    public static void idelTransaction(String alias){
        activityTransactions.remove(alias);
    }

    public static <V> V transactionCall(Callables<V> callable) throws Throwable {
        return transactionCall(callable, "*");
    }

    public static <V> V transactionCall(Callables<V> callable, String... dataSourceAlias) throws Throwable {
        Collection<TransactionHandler> handlers = new HashSet<>();
        boolean whole = whole(dataSourceAlias);
        Map<String, Boolean> loop = new HashMap<>();
        if (whole){
            handlers.addAll(TransactionSQLManagement.getTransactionConnections());
        }else {
            for (String alias : dataSourceAlias) {
                TransactionHandler transactionHandler = TransactionSQLManagement.getTransactionConnection(alias);
                if (transactionHandler != null){
                    handlers.add(transactionHandler);
                }
            }
        }
        V result;
        for (TransactionHandler handler : handlers) {
            activityTransactions.add(handler.getAlias());
            if (handler.isOpen()) {
                if (log.isDebugEnabled()) {
                    log.debug("the current transaction exists in another transaction");
                }
                loop.put(handler.getAlias(), false);
            }else {
                loop.put(handler.getAlias(), true);
                try {

                    handler.open();
                } catch (SQLException e) {
                    activityTransactions.clear();
                    throw new SQLSException("error for open transaction", e);
                }
            }
        }

        try {

            result = callable.call();
        }catch (Throwable ex){
            for (TransactionHandler handler : handlers) {
                if (loop.get(handler.getAlias())) {
                    if (log.isDebugEnabled()) {
                        log.debug("transaction interceptor do rollback: [{}]", handler.getAlias());
                    }
                    handler.rollback();
                }
            }
            throw ex;
        }finally {
            for (TransactionHandler handler : handlers) {
                if (loop.get(handler.getAlias())){
                    try {
                        if (log.isDebugEnabled()) {
                            log.debug("transaction interceptor do commit: [{}]", handler.getAlias());
                        }
                        handler.commit();
                    }finally {
                        try {
                            handler.close();
                        }catch (RuntimeException re){
                            if (log.isWarnEnabled()) {
                                log.warn("has error from close transaciton: [{}]", handler.getAlias());
                            }
                        }
                    }
                    idelTransaction(handler.getAlias());
                    ConnectionManagement.closeCurrentConnection(handler.getAlias());
                }
            }
        }
        return result;
    }

    static boolean whole(String[] values){
        return values != null && values.length == 1 && "*".equals(values[0]);
    }

    public static Set<TransactionHandler> getTransactionConnections(){
        Set<String> aliasSet = ConnectionManagement.getAliasSet();
        Set<TransactionHandler> result = new HashSet<>();
        for (String alias : aliasSet) {
            result.add(getTransactionConnection(alias));
        }
        return result;
    }

    public static TransactionHandler getTransactionConnection(String alias){
        Map<String, TransactionHandler> handlerMap = getHandlerMap(handlerCache);
        if (!handlerMap.containsKey(alias)){
            ConnectionManagement.getConnection(alias);
        }
        TransactionHandler handler = handlerMap.get(alias);
        Connection connection = handler.getConnection();
        if (!ConnectionManagement.checkConnection(connection)) {
            //如果连接已经失效
            ConnectionManagement.getConnection(alias);
        }
        return handlerMap.get(alias);
    }

    public static Map<String, TransactionHandler> getHandlerMap(ThreadLocal<Map<String, TransactionHandler>> local){
        Map<String, TransactionHandler> map = local.get();
        if (map == null){
            map = new ConcurrentHashMap<>();
            local.set(map);
        }
        return map;
    }

    public static class TransactionConnectionListener implements GlobalSQLRunningListener{


        private final Log sqlLog;

        private final String alias;

        public TransactionConnectionListener(GlobalSQLConfiguration configuration) {
            sqlLog = configuration.getLog();
            alias = configuration.getDataSourceAlias();
        }

        public TransactionConnectionListener(String alias){
            this.alias = alias;
            sqlLog = new SystemLog();
        }

        @Override
        public void abandonConnection(Connection connection, String alias) {
            Map<String, TransactionHandler> handlerMap = getHandlerMap(handlerCache);
            if (handlerMap.containsKey(alias)){
                handlerMap.remove(alias);
                if (log.isDebugEnabled()) {
                    log.debug("discard obsolete transaction processors: [{}]", alias);
                }
            }
        }

        @Override
        public void createNewConnection(Connection connection, String alias) {
            Map<String, TransactionHandler> handlerMap = getHandlerMap(handlerCache);
            if (!handlerMap.containsKey(alias)){
                DefaultTransactionHandler handler = new DefaultTransactionHandler(connection, alias, sqlLog);
                handlerMap.put(alias, handler);
                if (log.isDebugEnabled()) {
                    log.debug("create a new transaction processor：[{}]", alias);
                }
            }else {
                TransactionHandler handler = handlerMap.get(alias);
                ((DefaultTransactionHandler)handler).setConnection(connection);
            }
        }
    }


}
