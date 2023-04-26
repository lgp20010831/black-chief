package com.black.premission.mybatis_plus;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.black.premission.Attribute;
import com.black.premission.Panel;
import com.black.core.json.JsonUtils;
import com.black.core.json.ReflexUtils;

import java.util.List;
import java.util.Map;

public interface MPPanel<R extends Attribute> extends BaseMapper<R>, Panel<R>, MybatisPlusPanel<R> {

    @Override
    default R findDataById(String id){
        return selectById(id);
    }

    @Override
    default List<R> dataList(R condition){
        JSONObject json = JsonUtils.letJson(condition);
        return selectList(convertWrapper(json));
    }

    @Override
    default R join(R r){
        insert(r);
        return null;
    }

    @Override
    default R dataSave(R r){
        String id = r.getId();
        if (id != null){
            JSONObject json = JsonUtils.letJson(r);
            UpdateWrapper<R> uw = convertUpdateWrapper(json);
            uw.eq("id", id);
            update(null, uw);
        }else {
            join(r);
        }
        return null;
    }

    @Override
    default boolean deleteData(String id){
        QueryWrapper<R> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id);
        return delete(wrapper) > 0;
    }

    default R instance(Map<String, Object> condition){
        R instance = ReflexUtils.instance(entityType());
        instance.putAll(condition);
        return instance;
    }
}
