package com.black.premission.sql;

import com.black.premission.Attribute;
import com.black.premission.GlobalRUPConfigurationHolder;
import com.black.premission.Panel;
import com.black.sql_v2.Sql;

import java.util.List;
import java.util.Map;

public interface SqlPanel<R extends Attribute> extends Panel<R> {

    @Override
    default R findDataById(String id){
        return Sql.queryById(getTableName(), id).javaSingle(entityType());
    }

    @Override
    default List<R> dataList(R condition){
        return Sql.query(getTableName(), condition, getBlend()).javaList(entityType());
    }

    @Override
    default R singleList(R condition) {
        return Sql.query(getTableName(), condition).javaSingle(entityType());
    }

    @Override
    default R join(R r){
         Sql.insert(getTableName(), r);
         return r;
    }

    @Override
    default R dataSave(R r){
        Sql.saveAndEffect(getTableName(), r, false);
        return r;
    }

    @Override
    default boolean deleteData(String id){
        Sql.deleteById(getTableName(), id);
        return true;
    }

    @Override
    default boolean openEntity() {
        return false;
    }

    @Override
    default List<R> dataListByMap(Map<String, Object> condition) {
        return Sql.query(getTableName(), condition, getBlend()).javaList(entityType());
    }

    @Override
    default R dataSaveByMap(Map<String, Object> t) {
        Sql.saveAndEffect(getTableName(), t, false);
        return convert(t);
    }

    @Override
    default R joinByMap(Map<String, Object> t) {
        Sql.insert(getTableName(), t);
        return convert(t);
    }

    default String getBlend(){
        String blendString = GlobalRUPConfigurationHolder.getConfiguration().getBlendString();
        return blendString == null ? "" : "$B: " + blendString;
    }
}
