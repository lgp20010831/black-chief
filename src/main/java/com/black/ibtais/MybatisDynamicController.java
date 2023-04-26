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
import com.black.api.ApiJdbcProperty;
import com.black.core.annotation.Sort;
import com.black.core.aop.servlet.OpenIbatisPage;
import com.black.core.mvc.response.Response;
import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.sql.HumpColumnConvertHandler;
import com.black.core.sql.action.Dynamic;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.util.Assert;
import com.black.core.util.StreamUtils;
import com.black.core.util.StringUtils;
import com.black.swagger.ChiefIbatisAdaptive;
import com.black.swagger.ChiefSwaggerResponseAdaptive;
import com.black.test.TestedNozzle;
import com.black.utils.ReflexHandler;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


@SuppressWarnings("all")
public abstract class MybatisDynamicController<T> extends BeanController implements Dynamic, BusinessTransformation {

    protected final ActionWrapperResolver<T> resolver;

    protected AliasColumnConvertHandler convertHandler;

    protected MybatisDynamicController() {
        resolver = new ActionWrapperResolver<>(this);
    }

    protected abstract BaseMapper<T> getMapper();

    protected String getTableName(){
        Class<T> entityType = getEntityType();
        if (entityType.isAnnotationPresent(TableName.class)){
            return entityType.getAnnotation(TableName.class).value();
        }
        return StringUtils.unruacnl(StringUtils.titleLower(entityType.getSimpleName()));
    }

    protected Class<T> entityClass;

    protected abstract <M extends BaseMapper<T>> ServiceImpl<M, T> getImpl();

    /** 获取虚拟删除的 set map, 比如: del=1, is_deleted = true */
    protected Map<String, Object> getIllSetMap(){
        throw new UnsupportedOperationException("子类需要重写提供虚拟删除 map 数据");
    }

    public AliasColumnConvertHandler getConvertHandler(){
        if (convertHandler == null){
            convertHandler = new HumpColumnConvertHandler();
        }
        return convertHandler;
    }

    protected String findIdName(){
        Class<T> entityType = getEntityType();
        ClassWrapper<T> cw = ClassWrapper.get(entityType);
        FieldWrapper fw = cw.getSingleFieldByAnnotation(TableId.class);
        return fw == null ? null : fw.getName();
    }

    protected Class<T> getEntityType(){
        ServiceImpl<BaseMapper<T>, T> impl = getImpl();
        return entityClass == null ? entityClass = impl == null ? parseEntity() : impl.getEntityClass() : entityClass;
    }

    private Class<T> parseEntity(){
        Class<? extends MybatisDynamicController> type = getClass();
        Class<?>[] genericVal = ReflexHandler.superGenericVal(type);
        if (genericVal.length != 1){
            throw new IllegalStateException("无法获取实体类类型, mapper: " + type);
        }
        return (Class<T>) genericVal[0];
    }

    public String getBlendString(){
        return null;
    }


    @Sort(1)
    @TestedNozzle(7)
    @OpenIbatisPage
    @PostMapping("selectList")
    @ChiefSwaggerResponseAdaptive("getEntityType")
    @ApiOperation(value = "列表查询接口", response = Response.class)
    @ApiJdbcProperty(response = "$<getTableName>[]", request = "$<getTableName>{}", remark = "列表查询接口")
    public Object findList(@RequestBody(required = false) @ChiefIbatisAdaptive("getEntityType") JSONObject body){
        AbstractWrapper<?, String, ?> wrapper = resolver.wriedWrapper(new QueryWrapper<>(), getEntityType(), body);
        postSelectListWrapper(wrapper);
        return getMapper().selectList((Wrapper<T>) wrapper);
    }

    //回调参数注入后的查询列表的wrapper
    protected void postSelectListWrapper(AbstractWrapper<?, String, ?> wrapper){

    }

    @Sort(2)
    @TestedNozzle(6)
    @PostMapping("selectSingle")
    @ChiefSwaggerResponseAdaptive("getEntityType")
    @ApiOperation(value = "查询一条数据", response = Response.class)
    @ApiJdbcProperty(response = "$<getTableName>{}", request = "$<getTableName>{}", remark = "查询一条数据")
    public Object selectSingle(@RequestBody(required = false) @ChiefIbatisAdaptive("getEntityType") JSONObject body){
        AbstractWrapper<?, String, ?> wrapper = resolver.wriedWrapper(new QueryWrapper<>(), getEntityType(), body);
        postSelectSingleWrapper(wrapper);
        return getMapper().selectOne((Wrapper<T>) wrapper);
    }

    //回调参数注入后的查询单挑数据的wrapper
    protected void postSelectSingleWrapper(AbstractWrapper<?, String, ?> wrapper){

    }

    @Sort(3)
    @TestedNozzle(5)
    @GetMapping("selectById")
    @ChiefSwaggerResponseAdaptive("getEntityType")
    @ApiOperation(value = "根据 id 查询接口", response = Response.class)
    @ApiJdbcProperty(request = "url: ?id=xxxx", response = "$<getTableName>{}", remark = "根据 id 查询接口")
    public Object findById(@RequestParam Serializable id){
        return getMapper().selectById(id);
    }

    @Sort(4)
    @TestedNozzle(1)
    @PostMapping("insertBatch")
    @ApiOperation(value = "批次插入接口", response = Response.class)
    @ApiJdbcProperty(request = "$<getTableName>[]", remark = "批次插入接口")
    public Object insertBatch(@RequestBody @ChiefIbatisAdaptive("getEntityType") List<Map<String, Object>> array){
        Class<T> entityType = getEntityType();
        return getImpl().saveBatch(StreamUtils.mapList(array, map -> toBean(map, entityType)));
    }


    @Sort(5)
    @TestedNozzle(4)
    @PostMapping("insert")
    @ApiOperation(value = "普通添加一条数据", response = Response.class)
    @ApiJdbcProperty(request = "$<getTableName>{}", remark = "普通添加一条数据")
    public Object join(@RequestBody @ChiefIbatisAdaptive("getEntityType") JSONObject body){
        return getMapper().insert(toBean(body, getEntityType()));
    }


    @Sort(6)
    @TestedNozzle(2)
    @PostMapping("saveBatch")
    @ApiOperation(value = "批次: 修改或者添加, 有主键则修改, 否则添加", response = Response.class)
    @ApiJdbcProperty(request = "$<getTableName>[]", remark = "批次: 修改或者添加, 有主键则修改, 否则添加")
    public Object saveBatch(@RequestBody @ChiefIbatisAdaptive("getEntityType") List<Map<String, Object>> array){
        Class<T> entityType = getEntityType();
        return getImpl().saveOrUpdateBatch(StreamUtils.mapList(array, map -> toBean(map, entityType)));
    }

    @Sort(7)
    @TestedNozzle(3)
    @PostMapping("save")
    @ApiOperation(value = "修改或者添加, 有主键则修改, 否则添加", response = Response.class)
    @ApiJdbcProperty(request = "$<getTableName>{}", remark = "修改或者添加, 有主键则修改, 否则添加")
    public Object save0(@RequestBody @ChiefIbatisAdaptive("getEntityType") JSONObject body){
        return getImpl().saveOrUpdate(toBean(body, getEntityType()));
    }

    @Sort(8)
    @TestedNozzle(28)
    @PostMapping("delete")
    @ApiOperation(value = "根据条件 map 删除接口", response = Response.class)
    @ApiJdbcProperty(request = "$<getTableName>{}", remark = "根据条件 map 删除接口")
    public Object delete0(@RequestBody @ChiefIbatisAdaptive("getEntityType") JSONObject body){
        AbstractWrapper<?, String, ?> wrapper = resolver.wriedWrapper(new QueryWrapper<>(), getEntityType(), body);
        postDeleteWrapper(wrapper);
        return getMapper().delete((Wrapper<T>) wrapper);
    }

    //回调参数注入后的筛选删除的wrapper
    protected void postDeleteWrapper(AbstractWrapper<?, String, ?> wrapper){

    }

    @Sort(9)
    @TestedNozzle(25)
    @GetMapping("deleteById")
    @ApiOperation(value = "根据主键删除接口", response = Response.class)
    @ApiJdbcProperty(request = "url: ?id=xxxx", remark = "根据主键删除接口")
    public Object deleteById0(@RequestParam Serializable id){
        return getMapper().deleteById(id);
    }

    @Sort(10)
    @TestedNozzle(21)
    @PostMapping("deleteAllById")
    @ApiOperation(value = "根据主键集合删除数据", response = Response.class)
    @ApiJdbcProperty(request = "$R: [id]", remark = "根据主键集合删除数据")
    public Object deleteAllById0(@RequestBody List<Object> idList){
        return getMapper().deleteBatchIds(idList);
    }

    @Sort(11)
    @TestedNozzle(18)
    @GetMapping("illDel")
    @ApiOperation(value = "根据条件 map 删除接口", response = Response.class)
    @ApiJdbcProperty(request = "$<getTableName>{}", remark = "根据条件 map 删除接口")
    public Object illDel(@RequestBody @ChiefIbatisAdaptive("getEntityType") JSONObject body){
        Map<String, Object> illSetMap = getIllSetMap();
        UpdateWrapper<T> updateWrapper = resolver.wiredUpdateWrapper(getEntityType(), illSetMap);
        AbstractWrapper<?, String, ?> wrapper = resolver.wriedWrapper(updateWrapper, getEntityType(), body);
        postIllDelWrapper(wrapper);
        return getMapper().update(null, (Wrapper<T>) wrapper);
    }

    //回调参数注入后的筛选虚拟删除的wrapper
    protected void postIllDelWrapper(AbstractWrapper<?, String, ?> wrapper){

    }

    @Sort(12)
    @TestedNozzle(19)
    @GetMapping("illDelById")
    @ApiOperation(value = "根据主键逻辑删除数据", response = Response.class)
    @ApiJdbcProperty(request = "url: ?id=xxxx", remark = "根据主键逻辑删除数据")
    public Object illDelById(@RequestParam Object id){
        Map<String, Object> illSetMap = getIllSetMap();
        UpdateWrapper<T> updateWrapper = resolver.wiredUpdateWrapper(getEntityType(), illSetMap);
        String idName = findIdName();
        Assert.notNull(idName, "unknown id: " + getEntityType());
        AliasColumnConvertHandler convertHandler = getConvertHandler();
        updateWrapper.eq(convertHandler.convertColumn(idName), id);
        return getMapper().update(null, updateWrapper);
    }

    @Sort(13)
    @TestedNozzle(17)
    @PostMapping("illDelAllById")
    @ApiOperation(value = "根据主键集合逻辑删除数据", response = Response.class)
    @ApiJdbcProperty(request = "$R: [id]", remark = "根据主键集合逻辑删除数据")
    public Object illDelAllById(@RequestBody List<Object> idList){
        Map<String, Object> illSetMap = getIllSetMap();
        UpdateWrapper<T> updateWrapper = resolver.wiredUpdateWrapper(getEntityType(), illSetMap);
        String idName = findIdName();
        Assert.notNull(idName, "unknown id: " + getEntityType());
        AliasColumnConvertHandler convertHandler = getConvertHandler();
        updateWrapper.in(convertHandler.convertColumn(idName), idList);
        return getMapper().update(null, updateWrapper);
    }

}


