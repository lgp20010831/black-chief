package com.black.ibtais;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.black.core.json.JsonUtils;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.tools.BeanUtil;
import com.black.core.util.StreamUtils;
import com.black.table.PrimaryKey;
import com.black.table.TableMetadata;
import com.black.utils.ReflexHandler;
import com.black.utils.ServiceUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
public abstract class ObjectIbatisFullController<T> extends IbatisFullParentServlet<T>{

    private Class<T> entityClass = null;

    private BaseMapper<T> baseMapper;

    private IService<T> service;

    @Override
    protected Class<T> getEntityType() {
        if (entityClass != null){
            return entityClass;
        }
        Class<ObjectIbatisFullController<T>> primordialClass = BeanUtil.getPrimordialClass(this);
        Class<T> result = null;
        for (;;){
            Class<?>[] genericVal = ReflexHandler.superGenericVal(primordialClass);
            if (genericVal.length != 1){
                Class<?> superclass = primordialClass.getSuperclass();
                if (superclass != null && ObjectIbatisFullController.class.isAssignableFrom(superclass)){
                    primordialClass = (Class<ObjectIbatisFullController<T>>)superclass;
                    continue;
                }else {
                    throw new IllegalStateException("error for extends ObjectIbatisFullController:" + primordialClass);
                }
            }
            result = (Class<T>) genericVal[0];
            break;
        }
        entityClass = result;
        return result;
    }

    @Override
    protected BaseMapper<T> getMapper() {
        if (baseMapper == null){
            baseMapper = IbtatisUtils.autoFindMapper(getEntityType());
        }
        return baseMapper;
    }

    @Override
    protected <M extends BaseMapper<T>> ServiceImpl<M, T> getImpl() {
        if (service == null) {
            service = IbtatisUtils.autoFindService(getEntityType());
        }
        return (ServiceImpl<M, T>) service;
    }

    @Override
    public T doFindById(Serializable id) {
        return (T) super.doFindById(id);
    }

    @Override
    protected List<T> doFindList(JSONObject body) {
        return (List<T>) super.doFindList(body);
    }


    @Override
    protected T doSelectSingle0(T body) {
        return (T) super.doSelectSingle0(body);
    }

    protected boolean verify(){
        return true;
    }

    @Override
    public T doSave1(T body) {
        String tableName = getTableName();
        AliasColumnConvertHandler convertHandler = getConvertHandler();
        TableMetadata tableMetadata = ServiceUtils.getTableMetadataBySpring(tableName);
        Collection<PrimaryKey> primaryKeys = tableMetadata.getPrimaryKeys();
        List<String> primaryLists = StreamUtils.mapList(primaryKeys, primaryKey -> {
            return convertHandler.convertAlias(primaryKey.getName());
        });
        if (ServiceUtils.isAllNull(body, primaryLists)){
            //do insert
            getMapper().insert(body);
        }else {
            Map<String, Object> condition = ServiceUtils.filterNewMapV2(body, primaryLists);
            UpdateWrapper<T> updateWrapper = new UpdateWrapper<>();
            resolver.wriedEQWrapper(updateWrapper, getEntityType(), condition);
            if (verify()){
                if (getImpl().count(updateWrapper) <= 0) {
                    //do insert
                    getMapper().insert(body);
                    return body;
                }
            }
            //do update
            resolver.wiredUpdateWrapper(updateWrapper, getEntityType(), JsonUtils.letJson(body));
            getMapper().update(null, updateWrapper);
        }
        return body;
    }
}
