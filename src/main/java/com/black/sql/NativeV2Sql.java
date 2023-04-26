package com.black.sql;

import com.black.function.Supplier;
import lombok.NonNull;
import org.springframework.beans.factory.BeanFactory;

import java.util.Map;

import static com.black.sql.NativeSql.DEFAULT_ALIAS;

public class NativeV2Sql {

    //-------------------------------------------------------------------
    //          S       P       R       I       N       G
    //-------------------------------------------------------------------
    public static QueryResultSetParser queryBySpring(String sql, Object... paramArray){
        return queryWithEnvBySpring(sql, null, paramArray);
    }


    public static QueryResultSetParser queryWithEnvBySpring(String sql,
                                                            Map<String, Object> env,
                                                            Object... paramArray){
        return queryWithEnvBySpring(sql, null, env, paramArray);
    }

    public static QueryResultSetParser queryWithEnvBySpring(String sql,
                                                            Supplier<BeanFactory> supplier,
                                                            Map<String, Object> env,
                                                            Object... paramArray){
        return NativeSql.envQuery(sql, supplier, env, paramArray);
    }

    public static void execSpring(@NonNull String sql, Object... paramArray){
        execWithEnvBySpring(sql, null, paramArray);
    }

    public static void execWithEnvBySpring(@NonNull String sql, Map<String, Object> env, Object... paramArray){
        NativeSql.executeEnvUpdateDef(sql, env, paramArray);
    }

    //-------------------------------------------------------------------
    //          C       H       I       E       F
    //-------------------------------------------------------------------

    public static QueryResultSetParser queryMasterByChief(@NonNull String sql, Object... paramArray){
        return queryByChief(sql, DEFAULT_ALIAS, paramArray);
    }

    public static QueryResultSetParser queryMasterWithEnvByChief(@NonNull String sql,  Map<String, Object> env, Object... paramArray){
        return queryWithEnvByChief(sql, DEFAULT_ALIAS, env, paramArray);
    }

    public static QueryResultSetParser queryByChief(@NonNull String sql, String alias, Object... paramArray){
        return queryWithEnvByChief(sql, alias, null, paramArray);
    }

    public static QueryResultSetParser queryWithEnvByChief(@NonNull String sql, String alias, Map<String, Object> env, Object... paramArray){
        return NativeSql.createQueryEnv(sql, alias, env, paramArray);
    }

    public static void execMasterDef(String sql, Object... paramArray){
        execByChief(sql, DEFAULT_ALIAS, paramArray);
    }

    public static void execMasterWithEnvByChief(String sql, Map<String, Object> env, Object... paramArray){
        execWithEnvByChief(sql, DEFAULT_ALIAS, env, paramArray);
    }

    public static void execByChief(String sql, String alias, Object... paramArray){
        execWithEnvByChief(sql, alias, null, paramArray);
    }

    public static void execWithEnvByChief(String sql, String alias, Map<String, Object> env, Object... paramArray){
        NativeSql.executeEnv(sql, alias, env, paramArray);
    }
}
