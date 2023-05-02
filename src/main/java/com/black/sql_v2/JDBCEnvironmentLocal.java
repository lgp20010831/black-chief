package com.black.sql_v2;

import com.black.core.util.Assert;

import java.sql.Connection;
import java.util.LinkedList;
import java.util.Map;

public class JDBCEnvironmentLocal {

    private static final ThreadLocal<LinkedList<SqlV2Pack>> sqlLocal = new ThreadLocal<>();

    private static LinkedList<SqlV2Pack> init(){
        LinkedList<SqlV2Pack> packs = sqlLocal.get();
        if (packs == null){
            packs = new LinkedList<>();
            sqlLocal.set(packs);
        }
        return packs;
    }

    private static SqlV2Pack peek(){
        return init().peek();
    }

    public static void set(SqlV2Pack pack){
        LinkedList<SqlV2Pack> packs = init();
        SqlV2Pack peek = packs.peek();
        if (peek == null){
            packs.add(pack);
        }else {
            if (!peek.getPassID().equals(pack.getPassID())) {
                packs.addFirst(pack);
            }
        }
    }

    public static void setEnv(Map<String, Object> env){
        SqlV2Pack sqlV2Pack = peek();
        if (sqlV2Pack != null){
            sqlV2Pack.setEnv(env);
        }
    }

    public static SqlV2Pack getPack(){
        return peek();
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
        init().poll();
    }


}
