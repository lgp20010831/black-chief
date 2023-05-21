package com.black.standard;

import com.black.sql.QueryResultSetParser;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
public interface SqlOperator {

    default SqlOperator getSqlDelegate(){
        return null;
    }

    default <T> void saveBatch(Collection<T> collection, Object... params){
        getSqlDelegate().saveBatch(collection, params);
    }

    default <T> void saveAndEffect(T param, boolean effect, Object... params){
        getSqlDelegate().saveAndEffect(param, effect, params);
    }

    default <T> void saveAndEffectBatch(Collection<T> collection, boolean effect, Object... params){
        getSqlDelegate().saveAndEffectBatch(collection, effect, params);
    }

    default <T> void update(T setMap, Object... params){
        getSqlDelegate().update(setMap, params);
    }

    default Object add(Object entity, Object... params){
        return getSqlDelegate().add(entity, params);
    }

    default List<Object> addBatch(Object entity, Object... params){
        return getSqlDelegate().addBatch(entity, params);
    }

    default QueryResultSetParser select(Object entity, Object... params){
        return getSqlDelegate().select(entity, params);
    }

    default int selectCount(Object entity, Object... params){
        return getSqlDelegate().selectCount(entity, params);
    }

    default QueryResultSetParser selectPrimary(Object target, Object... params){
        return getSqlDelegate().selectPrimary(target, params);
    }

    //--------------- common -------------------

    default void deleteEffect(String tableName, Object param, Object... params){
        getSqlDelegate().deleteEffect(tableName, param, params);
    }

    default QueryResultSetParser queryPrimary(String tableName, Object target, Object... params){
        return getSqlDelegate().queryPrimary(tableName, target, params);
    }

    default QueryResultSetParser queryById(String tableName, Object idValue, Object... params){
        return getSqlDelegate().queryById(tableName, idValue, params);
    }

    default QueryResultSetParser query(String tableName, Object... params){
        return getSqlDelegate().query(tableName, params);
    }

    default <T> void saveBatch(String tableName, Collection<T> collection, Object... params){
        getSqlDelegate().saveBatch(tableName, collection, params);
    }

    default <T> void save(String tableName, T param, Object... params){
        getSqlDelegate().save(tableName, param, params);
    }

    default <T> void saveAndEffectBatch(String tableName, Collection<T> collection, boolean effect, Object... params){
        getSqlDelegate().saveAndEffectBatch(tableName, collection, effect, params);
    }

    default void saveAndEffect(String tableName, Object param, boolean effect, Object... params){
        getSqlDelegate().saveAndEffect(tableName, param, effect, params);
    }

    default void updateById(String tableName, Object setMap, Object idValue, Object... params){
        getSqlDelegate().updateById(tableName, setMap, idValue, params);
    }

    default int queryCount(String tableName, Object... params){
        return getSqlDelegate().queryCount(tableName, params);
    }

    default void delete(String tableName, Object... params){
        getSqlDelegate().delete(tableName, params);
    }

    default void prepareUdpate(String tableName, String setMap, Object bean, Object... params){
        getSqlDelegate().prepareUdpate(tableName, setMap, bean, params);
    }

    default <T> void update(String tableName, T setMap, Object... params){
        getSqlDelegate().update(tableName, setMap, params);
    }


    default Object insert(String tableName, Object... params){
        return getSqlDelegate().insert(tableName, params);
    }

    default List<Object> insertBatch(String tableName, Object... params){
        return getSqlDelegate().insertBatch(tableName, params);
    }

    default QueryResultSetParser nativeQuery(String sql, Object... paramArray){
        return nativeQueryWithEnv(sql, null, paramArray);
    }

    default QueryResultSetParser nativeQueryWithEnv(String sql, Map<String, Object> env, Object... paramArray){
        return getSqlDelegate().nativeQueryWithEnv(sql, env, paramArray);
    }

    default void nativeExec(String sql, Object... paramArray){
        nativeExecWithEnv(sql, null, paramArray);
    }

    default void nativeExecWithEnv(String sql, Map<String, Object> env, Object... paramArray){
        getSqlDelegate().nativeExecWithEnv(sql, env, paramArray);
    }

}
