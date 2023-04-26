package com.black.sql_v2;

import com.black.core.util.Assert;

import java.sql.Connection;
import java.util.Map;

public class JDBCEnvironmentLocal {

    private static final ThreadLocal<SqlV2Pack> sqlLocal = new ThreadLocal<>();


    public static void set(SqlV2Pack pack){
        sqlLocal.set(pack);
    }

    public static void setEnv(Map<String, Object> env){
        SqlV2Pack sqlV2Pack = sqlLocal.get();
        if (sqlV2Pack != null){
            sqlV2Pack.setEnv(env);
        }
    }

    public static SqlV2Pack getPack(){
        return sqlLocal.get();
    }

    public static Connection getConnection(){
        SqlV2Pack pack = getPack();
        return pack == null ? null : pack.getConnection();
    }

    public static Map<String, Object> getEnv(){
        SqlV2Pack pack = getPack();
        return pack == null ? null : pack.getEnv();
    }

    public static SqlExecutor getExecutor(){
        SqlV2Pack pack = getPack();
        return pack == null ? null : pack.getExecutor();
    }

    public static Object[] getParams(){
        SqlV2Pack pack = getPack();
        return pack == null ? null : pack.getParams();
    }

    public static Object attachment(){
        SqlV2Pack pack = getPack();
        return pack == null ? null : pack.getAttachment();
    }

    public static Environment getEnvironment(){
        SqlV2Pack pack = getPack();
        Environment environment = pack == null ? null : pack.getEnvironment();
        Assert.notNull(environment, "environment is null");
        return environment;
    }

    public static void remove(){
        sqlLocal.remove();
    }


}
