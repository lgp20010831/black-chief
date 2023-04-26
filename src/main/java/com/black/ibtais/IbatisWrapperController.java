package com.black.ibtais;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.black.utils.ServiceUtils;

import java.util.Map;

public abstract class IbatisWrapperController<T> extends MybatisDynamicController<T>{


    public UpdateWrapper<T> wiredUpdate(UpdateWrapper<T> wrapper, Map<String, Object> setMap){
        return resolver.wiredUpdateWrapper(wrapper, getEntityType(), setMap);
    }

    public UpdateWrapper<T> createSimpleUpdate(String column, Object value){
        return createUpdate(ServiceUtils.ofMap(column, value), null);
    }

    public UpdateWrapper<T> createSimpleUpdate(String column, Object value, String blendString){
        return createUpdate(ServiceUtils.ofMap(column, value), blendString);
    }

    public UpdateWrapper<T> createUpdate(Map<String, Object> condition){
        return createUpdate(condition, null);
    }

    public UpdateWrapper<T> createUpdate(Map<String, Object> condition, String blendString){
        Class<T> entityType = getEntityType();
        UpdateWrapper<T> wrapper = new UpdateWrapper<>();
        resolver.wriedBlendWrapper(wrapper, entityType, condition, blendString);
        return wrapper;
    }

    public <W extends AbstractWrapper<T, String, ?>> W wiredSimpleQuery(W wrapper, String column, Object value){
        return wiredSimpleQuery(wrapper, column, value, null);
    }

    public <W extends AbstractWrapper<T, String, ?>> W wiredSimpleQuery(W wrapper, String column, Object value, String blendString){
        return wiredQuery(wrapper, ServiceUtils.ofMap(column, value), blendString);
    }

    public <W extends AbstractWrapper<T, String, ?>> W wiredQuery(W wrapper, Map<String, Object> condition){
        return wiredQuery(wrapper, condition, null);
    }

    public <W extends AbstractWrapper<T, String, ?>> W wiredQuery(W wrapper, Map<String, Object> condition, String blendString){
        return (W) resolver.wriedBlendWrapper(wrapper, getEntityType(), condition, blendString);
    }
}
