package com.black.core.mybatis.source;

import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.session.SqlSession;

import java.sql.Connection;
import java.util.*;

@Log4j2
public class DynamicallyTransactionManager {

    private final static ThreadLocal<Map<String, SqlSession>> sqlSessionManager = new ThreadLocal<>();

    private final static ThreadLocal<Map<String, TransactionIbtaisSessionHandler>> transactionHandler = new ThreadLocal<>();

    //是否事务控制的标记
    private final static ThreadLocal<TransactionAopAdministrationWrapper> aopAdministrationManger = new ThreadLocal<>();


    public static TransactionAopAdministrationWrapper getCurrentAopTransaction(){
        return aopAdministrationManger.get();
    }

    public static void clearAopTransaction(){
        aopAdministrationManger.remove();
    }

    public static void registerAopTransaction(TransactionAopAdministrationWrapper aopAdministrationWrapper){
        aopAdministrationManger.set(aopAdministrationWrapper);
    }

    public static boolean isActivityTransaction(String alias){
        TransactionAopAdministrationWrapper administrationWrapper = aopAdministrationManger.get();
        if (administrationWrapper != null){
            Set<String> affectDatasourceAlias = administrationWrapper.getTransactionAffectDatasourceAlias();
            return affectDatasourceAlias.contains(alias);
        }
        return false;
    }

    public static Map<String, SqlSession> getSqlSessions(){
        Map<String, SqlSession> sessionMap = sqlSessionManager.get();
        if (sessionMap != null){
            return sessionMap;
        }
        return null;
    }

    public static SqlSession getSqlSession(String alias){
        Map<String, SqlSession> sqlSessionMap = sqlSessionManager.get();
        if (sqlSessionMap != null){
            if (sqlSessionMap.containsKey(alias)){
                return sqlSessionMap.get(alias);
            }
        }
        return null;
    }

    public static void registerHandler(TransactionIbtaisSessionHandler handler){
        if (handler != null){
            if (isActivityTransaction(handler.getAlias())) {
                getHandlers().put(handler.getAlias(), handler);
            }else {
                if (log.isWarnEnabled()) {
                    log.warn("无法注册 sqlsession 处理器");
                }
            }
        }
    }

    public static Map<String, TransactionIbtaisSessionHandler> getHandlers(){
        Map<String, TransactionIbtaisSessionHandler> handlerMap = transactionHandler.get();
        if (handlerMap == null){
            initHandlerSource();
            return transactionHandler.get();
        }
        return handlerMap;
    }

    public static void clear(){
        Map<String, TransactionIbtaisSessionHandler> handlerMap = transactionHandler.get();
        if (handlerMap == null){
            initHandlerSource();
            handlerMap = transactionHandler.get();
        }
        handlerMap.clear();
    }

    private static void initHandlerSource(){
        transactionHandler.set(new HashMap<>());
    }

    public static void removeSqlSession(String alias){
        Map<String, SqlSession> sessionMap = sqlSessionManager.get();
        if (sessionMap != null){
            sessionMap.remove(alias);
        }
    }


    public static void registerSqlSession(String alias, SqlSession session){
        Map<String, SqlSession> sessionMap = sqlSessionManager.get();
        if (isActivityTransaction(alias)) {
            if (sessionMap == null){
                sessionMap = new HashMap<>();
                sqlSessionManager.set(sessionMap);
            }
            try {
                Connection connection = session.getConnection();
                if (connection.getAutoCommit()) {
                    connection.setAutoCommit(false);
                }
            }catch (Throwable e){
                //ignore
            }
            sessionMap.put(alias, session);
        }
    }


}
