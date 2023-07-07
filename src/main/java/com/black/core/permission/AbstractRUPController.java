package com.black.core.permission;

import com.alibaba.fastjson.JSONObject;
import com.black.api.ApiJdbcProperty;
import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.util.Assert;
import com.black.premission.GlobalRUPConfiguration;
import com.black.premission.GlobalRUPConfigurationHolder;
import com.black.premission.Panel;
import com.black.swagger.v2.V2Swagger;
import com.black.utils.ReflexHandler;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

public class AbstractRUPController<T, P extends Panel<T>> {

    protected Class<P> type;

    protected P find(){
        if(type == null){
            Class<?>[] genericVals = ReflexHandler.loopSuperGenericVal(getClass(), 2);
            type = (Class<P>) genericVals[1];
        }
        return find(type);
    }

    protected <F> F find(Class<F> type){
        GlobalRUPConfiguration configuration = getConfiguration();
        ClassWrapper<GlobalRUPConfiguration> cw = ClassWrapper.get(GlobalRUPConfiguration.class);
        FieldWrapper fw = cw.getSingleFieldByType(type);
        Assert.notNull(fw, "unknown panel type of: " + type);
        return (F) fw.getValue(configuration);
    }

    public String getTableName(){
        P p = find();
        return p.getTableName();
    }

    protected GlobalRUPConfiguration getConfiguration(){
        GlobalRUPConfiguration configuration = GlobalRUPConfigurationHolder.getConfiguration();
        configuration.check();
        return configuration;
    }

    @RUPServletMethod
    @GetMapping("singleById")
    @ApiOperation("根据id查询单条数据")
    @V2Swagger("$<getTableName>{}")
    @ApiJdbcProperty(response = "$<getTableName>{}", remark = "根据id查询单条数据")
    public Object singleById(@RequestParam String id){
        return doGetSingleById(id);
    }

    @RUPServletMethod
    @PostMapping("single")
    @ApiOperation("根据条件查询单条数据")
    @V2Swagger("$<getTableName>{}")
    @ApiJdbcProperty(request = "$<getTableName>{}", response = "$<getTableName>{}", remark = "根据条件查询单条数据", hide = true)
    public Object single(@RequestBody @V2Swagger("$<getTableName>{}") JSONObject json){
        return doGetSingle(json);
    }

    @RUPServletMethod
    @PostMapping("list")
    @ApiOperation("根据条件查询列表数据")
    @V2Swagger("$<getTableName>{}")
    @ApiJdbcProperty(request = "$<getTableName>{}", response = "$<getTableName>[]", remark = "根据条件查询列表数据")
    public Object list(@RequestBody @V2Swagger("$<getTableName>{}") JSONObject json){
        return doGetList(json);
    }

    @RUPServletMethod
    @PostMapping("save")
    @ApiOperation("添加或修改一条数据")
    @ApiJdbcProperty(request = "$<getTableName>{}",  remark = "添加或修改一条数据")
    public Object save(@RequestBody @V2Swagger("$<getTableName>{}") JSONObject json){
        return doSaveData(json);
    }

    @RUPServletMethod
    @PostMapping("join")
    @ApiOperation("添加一条数据")
    @ApiJdbcProperty(request = "$<getTableName>{}",  remark = "添加一条数据")
    public Object join(@RequestBody @V2Swagger("$<getTableName>{}") JSONObject json){
        return doJoinData(json);
    }

    @RUPServletMethod
    @GetMapping("delete")
    @ApiOperation("根据id删除一条数据")
    @ApiJdbcProperty(request = "url: ?id=主键", remark = "根据id删除一条数据")
    public Object delete(@RequestParam String id){
        return doDeleteData(id);
    }

    protected Object doGetSingleById(String id){
        P p = find();
        return p.findDataById(id);
    }

    protected Object doGetSingle(JSONObject json){
        P p = find();
        if (p.openEntity()){
            T t = p.convert(json);
            return p.singleList(t);
        }else {
            return p.singleListByMap(json);
        }
    }

    protected Object doGetList(JSONObject json){
        P p = find();
        if (p.openEntity()){
            T t = p.convert(json);
            return p.dataList(t);
        }else {
            return p.dataListByMap(json);
        }
    }

    protected Object doJoinData(JSONObject json){
        P p = find();
        if (p.openEntity()){
            T t = p.convert(json);
            return p.join(t);
        }else {
            return p.joinByMap(json);
        }
    }

    protected Object doSaveData(JSONObject json){
        P p = find();
        if (p.openEntity()){
            T t = p.convert(json);
            return p.dataSave(t);
        }else {
            return p.dataSaveByMap(json);
        }
    }

    protected Object doDeleteData(String id){
        P p = find();
        return p.deleteData(id);
    }
}
