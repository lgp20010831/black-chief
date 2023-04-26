package com.black.premission.map_sql;

import com.black.core.json.ReflexUtils;
import com.black.core.sql.code.mapping.GlobalParentMapping;
import com.black.core.util.Assert;
import com.black.core.util.StreamUtils;
import com.black.premission.GlobalRUPConfiguration;
import com.black.premission.GlobalRUPConfigurationHolder;
import com.black.premission.Panel;
import com.black.utils.ReflexHandler;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface MSPanel<R extends AbstractRUPBean> extends Panel<R> {

    default GlobalParentMapping getMapping(){
        GlobalRUPConfiguration configuration = GlobalRUPConfigurationHolder.getConfiguration();
        GlobalParentMapping parentMapping = configuration.getParentMapping();
        Assert.notNull(parentMapping, "not find global mapping");
        return parentMapping;
    }

    String getTableName();

    default Class<R> entityType(){
        Class<? extends MSPanel> type = getClass();
        Class<?>[] genericVals = ReflexHandler.superGenericVal(type);
        if (genericVals.length != 1){
            throw new IllegalStateException("can not find generic type of class: " + type);
        }
        return (Class<R>) genericVals[0];
    }

    @Override
    default R findDataById(String id){
        Map<String, Object> map = getMapping().findById(getTableName(), id);
        R instance = ReflexUtils.instance(entityType());
        instance.setSource(map);
        return instance;
    }

    @Override
    default List<R> dataList(R condition){
        List<Map<String, Object>> mapList = getMapping().globalSelect(getTableName(), condition.attributes());
        Class<R> entityType = entityType();
        return StreamUtils.mapList(mapList, map -> {
            R instance = ReflexUtils.instance(entityType);
            instance.setSource(map);
            return instance;
        });
    }

    @Override
    default R join(R r){
        getMapping().fastJoin(getTableName(), Collections.singletonList(r.attributes()));
        return null;
    }

    @Override
    default R dataSave(R r){
        Map<String, Object> map = getMapping().save(getTableName(), r.attributes());
        Class<R> entityType = entityType();
        R instance = ReflexUtils.instance(entityType);
        instance.setSource(map);
        return instance;
    }

    @Override
    default boolean deleteData(String id){
        return getMapping().deleteById(getTableName(), id);
    }
}
