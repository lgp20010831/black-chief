package com.black.sql_v2;

import com.black.core.sql.code.DataSourceBuilder;
import com.black.sql.QueryResultSetParser;
import com.black.table.TableMetadata;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("all")
public class Sql {

    public static final String DEFAULT_ALIAS = "master_v2";

    private static final Map<String, SqlExecutor> executorCache = new ConcurrentHashMap<>();

    public static TableMetadata getTableMetadata(String tableName){
        return opt().getTableMetadata(tableName);
    }

    public static Set<String> getAliases(){
        return executorCache.keySet();
    }

    //--------------- auto find table name -------------------

    public static void modify(Object entity, Object... params){
        opt().modify(entity, params);
    }

    public static void modify(String tableName, Object entity, Object... params){
        opt().modify(tableName, entity, params);
    }

    public static <T> void saveBatch(Collection<T> collection, Object... params){
        opt().saveBatch(collection, params);
    }

    public static <T> void saveAndEffect(T param, boolean effect, Object... params){
        opt().saveAndEffect(param, effect, params);
    }

    public static <T> void saveAndEffectBatch(Collection<T> collection, boolean effect, Object... params){
        opt().saveAndEffectBatch(collection, effect, params);
    }

    public static <T> void update(T setMap, Object... params){
        opt().update(setMap, params);
    }

    public static Object add(Object entity, Object... params){
        return opt().add(entity, params);
    }

    public static List<Object> addBatch(Object entity, Object... params){
        return opt().addBatch(entity, params);
    }

    public static QueryResultSetParser select(Object entity, Object... params){
        return opt().select(entity, params);
    }

    public static int selectCount(Object entity, Object... params){
        return opt().selectCount(entity, params);
    }

    public static QueryResultSetParser selectPrimary(Object target, Object... params){
        return opt().selectPrimary(target, params);
    }

    //--------------- common -------------------

    public static void deleteEffect(String tableName, Object param, Object... params){
        opt().deleteEffect(tableName, param, params);
    }

    public static QueryResultSetParser queryPrimary(String tableName, Object target, Object... params){
        return opt().queryPrimary(tableName, target, params);
    }

    public static QueryResultSetParser queryById(String tableName, Object idValue, Object... params){
        return opt().queryById(tableName, idValue, params);
    }

    public static QueryResultSetParser query(String tableName, Object... params){
        return opt().query(tableName, params);
    }

    public static <T> void saveBatch(String tableName, Collection<T> collection, Object... params){
        opt().saveBatch(tableName, collection, params);
    }

    public static <T> void save(String tableName, T param, Object... params){
        opt().save(tableName, param, params);
    }

    public static <T> void saveAndEffectBatch(String tableName, Collection<T> collection, boolean effect, Object... params){
        opt().saveAndEffectBatch(tableName, collection, effect, params);
    }

    public static void saveAndEffect(String tableName, Object param, boolean effect, Object... params){
        opt().saveAndEffect(tableName, param, effect, params);
    }

    public static void updateById(String tableName, Object setMap, Object idValue, Object... params){
        opt().updateById(tableName, setMap, idValue, params);
    }

    public static int queryCount(String tableName, Object... params){
        return opt().queryCount(tableName, params);
    }

    public static void deleteById(String tableName, Object idValue, Object... params){
        opt().deleteById(tableName, idValue, params);
    }

    public static void delete(String tableName, Object... params){
        opt(DEFAULT_ALIAS).delete(tableName, params);
    }

    public static void prepareUdpate(String tableName, String setMap, Object bean, Object... params){
        opt().prepareUdpate(tableName, setMap, bean, params);
    }

    public static <T> void update(String tableName, T setMap, Object... params){
        opt().update(tableName, setMap, params);
    }


    public static Object insert(String tableName, Object... params){
        return opt().insert(tableName, params);
    }

    public static List<Object> insertBatch(String tableName, Object... params){
        return opt().insertBatch(tableName, params);
    }

    public static QueryResultSetParser nativeQuery(String sql, Object... paramArray){
        return nativeQueryWithEnv(sql, null, paramArray);
    }

    public static QueryResultSetParser nativeQueryWithEnv(String sql, Map<String, Object> env, Object... paramArray){
       return opt().nativeQueryWithEnv(sql, env, paramArray);
    }

    public static void nativeExec(String sql, Object... paramArray){
        nativeExecWithEnv(sql, null, paramArray);
    }

    public static void nativeExecWithEnv(String sql, Map<String, Object> env, Object... paramArray){
        opt().nativeExecWithEnv(sql, env, paramArray);
    }


    public static SqlExecutor lazyOpt(String alias){
        return executorCache.computeIfAbsent(alias, SqlExecutor::new);
    }

    public static SqlExecutor opt(){
        return opt(DEFAULT_ALIAS);
    }

    public static SqlExecutor opt(String alias){
        GlobalEnvironment globalEnvironment = GlobalEnvironment.getInstance();
        DataSourceBuilder dataSourceBuilder = GlobalEnvironment.getDataSourceBuilder(alias);
        if (dataSourceBuilder == null){
            dataSourceBuilder = globalEnvironment.getDataSourceBuilder();
        }
        DataSourceBuilder finalDataSourceBuilder = dataSourceBuilder;
        return executorCache.computeIfAbsent(alias, al -> new SqlExecutor(finalDataSourceBuilder, al));
    }

    public static Environment optEnvironment(){
        return optEnvironment(DEFAULT_ALIAS);
    }

    public static Environment optEnvironment(String alias){
        return opt(alias).getEnvironment();
    }

    public static void configDataSource(DataSourceBuilder builder){
        configDataSource(DEFAULT_ALIAS, builder);
    }

    public static void configDataSource(String alias, DataSourceBuilder builder){
        GlobalEnvironment environment = GlobalEnvironment.getInstance();
        environment.registerDataSource(alias, builder);
    }

}
