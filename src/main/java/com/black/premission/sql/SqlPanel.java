package com.black.premission.sql;

import com.black.premission.Attribute;
import com.black.premission.Panel;
import com.black.sql_v2.Sql;

import java.util.List;

public interface SqlPanel<R extends Attribute> extends Panel<R> {

    @Override
    default R findDataById(String id){
        return Sql.queryById(getTableName(), id).javaSingle(entityType());
    }

    @Override
    default List<R> dataList(R condition){
        return Sql.query(getTableName(), condition).javaList(entityType());
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
}
