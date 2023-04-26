package com.black.ibtais;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.black.core.json.JsonUtils;
import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.sql.HumpColumnConvertHandler;
import com.black.core.sql.action.Dynamic;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.util.Assert;
import com.black.core.util.StreamUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public abstract class IbatisFullParentServlet<T> extends BeanController implements Dynamic, BusinessTransformation {

    protected final ActionWrapperResolver<T> resolver;

    protected AliasColumnConvertHandler convertHandler;

    public IbatisFullParentServlet(){
        resolver = new ActionWrapperResolver<>(this);
        convertHandler = new HumpColumnConvertHandler();
    }

    @Override
    public AliasColumnConvertHandler getConvertHandler() {
        return convertHandler;
    }

    public String getBlendString(){
        return null;
    }

    public String getTableName(){
        Class<T> entityType = getEntityType();
        ClassWrapper<T> classWrapper = ClassWrapper.get(entityType);
        TableName annotation = classWrapper.getAnnotation(TableName.class);
        String name = annotation == null ? null : annotation.value();
        Assert.notNull(name, "unknown table name of controller: " + name());
        return name;
    }

    protected Class<T> getEntityType(){
        return getImpl().getEntityClass();
    }

    protected abstract <M extends BaseMapper<T>> ServiceImpl<M, T> getImpl();

    protected abstract BaseMapper<T> getMapper();

    /** 获取虚拟删除的 set map, 比如: del=1, is_deleted = true */
    protected Map<String, Object> getIllSetMap(){
        throw new UnsupportedOperationException("子类需要重写提供虚拟删除 map 数据");
    }

    protected String findIdName(){
        Class<T> entityType = getEntityType();
        ClassWrapper<T> cw = ClassWrapper.get(entityType);
        FieldWrapper fw = cw.getSingleFieldByAnnotation(TableId.class);
        return fw == null ? null : fw.getName();
    }

    //回调参数注入后的查询列表的wrapper
    protected void postSelectListWrapper(AbstractWrapper<?, String, ?> wrapper){

    }

    protected Object doFindList(JSONObject body){
        AbstractWrapper<?, String, ?> wrapper = resolver.wriedWrapper(new QueryWrapper<>(), getEntityType(), body);
        postSelectListWrapper(wrapper);
        return getMapper().selectList((Wrapper<T>) wrapper);
    }

    //回调参数注入后的查询单挑数据的wrapper
    protected void postSelectSingleWrapper(AbstractWrapper<?, String, ?> wrapper){

    }

    protected Object doSelectSingle(JSONObject body){
        AbstractWrapper<?, String, ?> wrapper = resolver.wriedWrapper(new QueryWrapper<>(), getEntityType(), body);
        postSelectSingleWrapper(wrapper);
        return getMapper().selectOne((Wrapper<T>) wrapper);
    }

    protected Object doSelectSingle0(T body){
        return doSelectSingle(JsonUtils.letJson(body));
    }


    public Object doFindById(Serializable id){
        return getMapper().selectById(id);
    }

    public Object doInsertBatch(List<Map<String, Object>> array){
        Class<T> entityType = getEntityType();
        return getImpl().saveBatch(StreamUtils.mapList(array, map -> toBean(map, entityType)));
    }

    public Object doInsertBatch0(List<T> array){
        return getImpl().saveBatch(array);
    }

    public Object doJoin(JSONObject body){
        return doJoin0(toBean(body, getEntityType()));
    }

    public Object doJoin0(T bean){
        return getMapper().insert(bean);
    }


    public Object doSaveBatch(List<Map<String, Object>> array){
        Class<T> entityType = getEntityType();
        return getImpl().saveOrUpdateBatch(StreamUtils.mapList(array, map -> toBean(map, entityType)));
    }

    public Object doSaveBatch0(List<T> array){
        return getImpl().saveOrUpdateBatch(array);
    }

    public Object doSave0(JSONObject body){
        return getImpl().saveOrUpdate(toBean(body, getEntityType()));
    }

    public Object doSave1(T body){
        return getImpl().saveOrUpdate(body);
    }


    public Object doDelete0(JSONObject body){
        AbstractWrapper<?, String, ?> wrapper = resolver.wriedWrapper(new QueryWrapper<>(), getEntityType(), body);
        postDeleteWrapper(wrapper);
        return getMapper().delete((Wrapper<T>) wrapper);
    }

    public Object doDelete0(T body){
        return doDelete0(JsonUtils.letJson(body));
    }

    //回调参数注入后的筛选删除的wrapper
    protected void postDeleteWrapper(AbstractWrapper<?, String, ?> wrapper){

    }

    public Object doDeleteById0(Serializable id){
        return getMapper().deleteById(id);
    }


    public Object doDeleteAllById0(List<Object> idList){
        return getMapper().deleteBatchIds(idList);
    }

    public Object doIllDel(JSONObject body){
        Map<String, Object> illSetMap = getIllSetMap();
        UpdateWrapper<T> updateWrapper = resolver.wiredUpdateWrapper(getEntityType(), illSetMap);
        AbstractWrapper<?, String, ?> wrapper = resolver.wriedWrapper(updateWrapper, getEntityType(), body);
        postIllDelWrapper(wrapper);
        return getMapper().update(null, (Wrapper<T>) wrapper);
    }

    //回调参数注入后的筛选虚拟删除的wrapper
    protected void postIllDelWrapper(AbstractWrapper<?, String, ?> wrapper){

    }

    public Object doIllDelById(Object id){
        Map<String, Object> illSetMap = getIllSetMap();
        UpdateWrapper<T> updateWrapper = resolver.wiredUpdateWrapper(getEntityType(), illSetMap);
        String idName = findIdName();
        Assert.notNull(idName, "unknown id: " + getEntityType());
        AliasColumnConvertHandler convertHandler = getConvertHandler();
        updateWrapper.eq(convertHandler.convertColumn(idName), id);
        return getMapper().update(null, updateWrapper);
    }

    public Object doIllDelAllById(List<Object> idList){
        Map<String, Object> illSetMap = getIllSetMap();
        UpdateWrapper<T> updateWrapper = resolver.wiredUpdateWrapper(getEntityType(), illSetMap);
        String idName = findIdName();
        Assert.notNull(idName, "unknown id: " + getEntityType());
        AliasColumnConvertHandler convertHandler = getConvertHandler();
        updateWrapper.in(convertHandler.convertColumn(idName), idList);
        return getMapper().update(null, updateWrapper);
    }

}
