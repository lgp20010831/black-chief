package com.black.sql_v2.action;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.black.api.GetApiProperty;
import com.black.api.PostApiProperty;
import com.black.core.sql.annotation.OpenSqlPage;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;
import java.util.List;

public abstract class AbstractProvideSupportChiefApiController extends AbstractSqlOptServlet{

    @OpenSqlPage
    @PostApiProperty(url = "list", request = "$<getTableName>{}", 
            response = "$<getTableName>[]", remark = "列表查询", hide = false)
    public Object list(@RequestBody JSONObject json){
        return list0(json);
    }

    @GetApiProperty(url = "queryById", response = "$<getTableName>{}", remark = "根据id查询", hide = false)
    public Object queryById(@RequestParam Serializable id){
        return queryById0(id);
    }

    @PostApiProperty(url = "single", request = "$<getTableName>{}", 
            response = "$<getTableName>{}", remark = "查询一条", hide = false)
    public Object single(@RequestBody JSONObject json){
        return single0(json);
    }

    @PostApiProperty(url = "save", request = "$<getTableName>{}", remark = "更新/添加", hide = false)
    public void save(@RequestBody JSONObject json){
        save0(json);
    }

    @PostApiProperty(url = "saveBatch", request = "$<getTableName>[]", remark = "批次-更新/添加", hide = false)
    public void saveBatch(@RequestBody JSONArray array){
        saveBatch0(array);
    }

    @PostApiProperty(url = "delete", request = "$<getTableName>{}", remark = "删除", hide = false)
    public void delete(@RequestBody JSONObject json){
        delete0(json);
    }

    @GetApiProperty(url = "deleteById", remark = "根据id删除", hide = false)
    public void deleteById(@RequestParam Serializable id){
        deleteById0(id);
    }

    @PostApiProperty(url = "deleteByIds", request = "$R: [id]", remark = "根据id数组删除", hide = false)
    public void deleteByIds(@RequestBody List<Object> ids){
        deleteByIds0(ids);
    }

}
