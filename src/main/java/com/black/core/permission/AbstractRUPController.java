package com.black.core.permission;

import com.alibaba.fastjson.JSONObject;
import com.black.api.ApiJdbcProperty;
import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.util.Assert;
import com.black.premission.GlobalRUPConfiguration;
import com.black.premission.GlobalRUPConfigurationHolder;
import com.black.premission.Panel;
import com.black.utils.ReflexHandler;
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
    @PostMapping("single")
    @ApiJdbcProperty(request = "$<getTableName>{}", response = "$<getTableName>{}", remark = "根据条件查询单条数据")
    public Object single(@RequestBody JSONObject json){
        return doGetSingle(json);
    }

    @RUPServletMethod
    @PostMapping("list")
    @ApiJdbcProperty(request = "$<getTableName>{}", response = "$<getTableName>[]", remark = "根据条件查询列表数据")
    public Object list(@RequestBody JSONObject json){
        return doGetList(json);
    }

    @RUPServletMethod
    @PostMapping("save")
    @ApiJdbcProperty(request = "$<getTableName>{}",  remark = "添加或修改一条数据")
    public Object save(@RequestBody JSONObject json){
        return doSaveData(json);
    }

    @RUPServletMethod
    @PostMapping("join")
    @ApiJdbcProperty(request = "$<getTableName>{}",  remark = "添加一条数据")
    public Object join(@RequestBody JSONObject json){
        return doJoinData(json);
    }

    @RUPServletMethod
    @GetMapping("delete")
    @ApiJdbcProperty(request = "url: ?id=主键", remark = "根据id删除一条数据")
    public Object delete(@RequestParam String id){
        return doDeleteData(id);
    }

    protected Object doGetSingle(JSONObject json){
        P p = find();
        T t = p.convert(json);
        return p.singleList(t);
    }

    protected Object doGetList(JSONObject json){
        P p = find();
        T t = p.convert(json);
        return p.dataList(t);
    }

    protected Object doJoinData(JSONObject json){
        P p = find();
        T t = p.convert(json);
        return p.join(t);
    }

    protected Object doSaveData(JSONObject json){
        P p = find();
        T t = p.convert(json);
        return p.dataSave(t);
    }

    protected Object doDeleteData(String id){
        P p = find();
        return p.deleteData(id);
    }
}
