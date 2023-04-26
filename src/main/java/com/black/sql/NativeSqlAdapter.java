package com.black.sql;

import java.sql.Connection;
import java.util.Map;

public interface NativeSqlAdapter {

    Connection getNativeFetchConnection();

    void closeNativeFetchConnection(Connection connection);

    default QueryResultSetParser nativeQuery(String sql, Object... paramArray){
        return nativeQueryWithEnv(sql, null, paramArray);
    }

    default QueryResultSetParser nativeQueryWithEnv(String sql, Map<String, Object> env, Object... paramArray){
        Connection fetchConnection = getNativeFetchConnection();
        return NativeSql.createEnvAutonomousShutdownQuery(sql, fetchConnection, this::closeNativeFetchConnection, env, paramArray);
    }

    default void nativeExec(String sql, Object... paramArray){
        nativeExecWithEnv(sql, null, paramArray);
    }

    default void nativeExecWithEnv(String sql, Map<String, Object> env, Object... paramArray){
        Connection fetchConnection = getNativeFetchConnection();
        try {
            NativeSql.executeEnvUpdate(sql, fetchConnection, env, paramArray);
        }finally {
            closeNativeFetchConnection(fetchConnection);
        }
    }

}
