package com.black.core.sql.entity;


import com.black.core.json.ReflexUtils;
import com.black.core.sql.annotation.TableName;
import com.black.core.sql.code.mapping.GlobalParentMapping;
import com.black.core.tools.BeanUtil;
import com.black.core.util.Assert;
import com.black.core.util.StreamUtils;
import com.black.core.util.Utils;
import lombok.NonNull;

import java.util.*;

@Deprecated
public interface GlobalEntityMapper extends GlobalParentMapping {

    default <E> E selectSingleEntity(E condition){
        Class<E> primordialClass = BeanUtil.getPrimordialClass(condition);
        String tableName = findTableName(condition);
        Map<String, Object> vaildMap = BeanUtil.getVaildMap(condition);
        Map<String, Object> singleResult = globalSelectSingle(tableName, vaildMap);
        return BeanUtil.mapping(ReflexUtils.instance(primordialClass), singleResult);
    }


    default <E> List<E> selectEntityList(E condition){
        Class<E> primordialClass = BeanUtil.getPrimordialClass(condition);
        String tableName = findTableName(condition);
        Map<String, Object> vaildMap = BeanUtil.getVaildMap(condition);
        List<Map<String, Object>> list = globalSelect(tableName, vaildMap);
        return StreamUtils.mapList(list, ele -> BeanUtil.mapping(ReflexUtils.instance(primordialClass), ele));
    }

    default <E> String insertEntity(E entity){
        List<String> list = insertEntityBatch(Collections.singletonList(entity));
        return list.isEmpty() ? null : list.get(0);
    }

    default <E> List<String> insertEntityBatch(Collection<E> entityList){
        if (Utils.isEmpty(entityList)){
            return new ArrayList<>();
        }
        String tableName = null;
        for (E e : entityList) {
            tableName = findTableName(e);
            break;
        }
        Assert.notNull(tableName, "unknown entityList");
        List<Map<String, Object>> mapList = StreamUtils.mapList(entityList, BeanUtil::getVaildMap);
        return globalInsertBatch(tableName, mapList);
    }



    default String findTableName(@NonNull Object entity){
        Class<Object> primordialClass = BeanUtil.getPrimordialClass(entity);
        TableName annotation = primordialClass.getAnnotation(TableName.class);
        Assert.notNull(annotation, "unknown entity: " + primordialClass.getName());
        return annotation.value();
    }
}
