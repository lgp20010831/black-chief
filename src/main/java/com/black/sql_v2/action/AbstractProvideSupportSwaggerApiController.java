package com.black.sql_v2.action;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.black.swagger.v2.V2Swagger;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;
import java.util.List;


public class AbstractProvideSupportSwaggerApiController extends AbstractSqlOptServlet{

    @PostMapping("list")
    @V2Swagger("$<getTableName>{}")
    @ApiOperation(value = "列表查询", hidden = false)
    public Object list(@RequestBody @V2Swagger("$<getTableName>{}") JSONObject json){
        return list0(json);
    }

    @GetMapping("queryById")
    @V2Swagger("$<getTableName>{}")
    @ApiOperation(value = "根据id查询", hidden = false)
    public Object queryById(@RequestParam Serializable id){
        return queryById0(id);
    }

    @PostMapping("single")
    @V2Swagger("$<getTableName>{}")
    @ApiOperation(value = "查询一条", hidden = false)
    public Object single(@RequestBody @V2Swagger("$<getTableName>{}") JSONObject json){
        return single0(json);
    }
    
    @PostMapping("save")
    @ApiOperation(value = "更新/添加", hidden = false)
    public void save(@RequestBody @V2Swagger("$<getTableName>{}") JSONObject json){
        save0(json);
    }

    @PostMapping("saveBatch")
    @ApiOperation(value = "批次-更新/添加", hidden = false)
    public void saveBatch(@RequestBody @V2Swagger("$<getTableName>{}") JSONArray array){
        saveBatch0(array);
    }

    @PostMapping("delete")
    @ApiOperation(value = "删除", hidden = false)
    public void delete(@RequestBody @V2Swagger("$<getTableName>{}") JSONObject json){
        delete0(json);
    }

    @GetMapping("deleteById")
    @ApiOperation(value = "根据id删除", hidden = false)
    public void deleteById(@RequestParam Serializable id){
        deleteById0(id);
    }

    @PostMapping("deleteByIds")
    @ApiOperation(value = "根据id数组删除", hidden = false)
    public void deleteByIds(@RequestBody List<Object> ids){
        deleteByIds0(ids);
    }

}
