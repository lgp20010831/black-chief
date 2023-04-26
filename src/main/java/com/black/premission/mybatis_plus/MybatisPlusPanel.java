package com.black.premission.mybatis_plus;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.black.premission.EntityPanel;

import java.util.Map;

public interface MybatisPlusPanel <T> extends EntityPanel<T> {

    default QueryWrapper<T> convertWrapper(Map<String, Object> map){
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        RUPWrapperResolver.wriedEQWrapper(wrapper, entityType(), map);
        return wrapper;
    }

    default UpdateWrapper<T> convertUpdateWrapper(Map<String, Object> map){
        return RUPWrapperResolver.wriedUpdateWrapper(entityType(), map);
    }
}
