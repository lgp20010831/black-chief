package com.black.sql_v2.action;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.black.core.sql.annotation.OpenTransactional;
import com.black.swagger.v2.V2Swagger;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;
import java.util.List;

@Getter @Api(tags = "") @Log4j2 @OpenTransactional @SuppressWarnings("all")
public class PureEditionSwaggerController extends AbstractSqlOptServlet{

    public String tableName = "img", alias = "chiefSql";


    //****************************************************************
    //          A   U   T   O           C   R   E   A   T   E
    //****************************************************************


    @PostMapping("list")
    @V2Swagger("img{}")
    @ApiOperation(value = "列表查询", hidden = true)
    Object list(@RequestBody @V2Swagger("img{}") JSONObject json){
        return list0(json);
    }

    @GetMapping("queryById")  @V2Swagger("img{}")
    @ApiOperation(value = "根据id查询", hidden = true)
    Object queryById(@RequestParam Serializable id){
        return queryById0(id);
    }

    @PostMapping("single") @V2Swagger("img{}")
    @ApiOperation(value = "查询一条", hidden = true)
    Object single(@RequestBody @V2Swagger("img{}") JSONObject json){
        return single0(json);
    }

    @PostMapping("save")
    @ApiOperation(value = "更新/添加", hidden = true)
    void save(@RequestBody @V2Swagger("img{}") JSONObject json){
        save0(json);
    }

    @PostMapping("saveBatch")
    @ApiOperation(value = "批次-更新/添加", hidden = true)
    void saveBatch(@RequestBody @V2Swagger("img{}") JSONArray array){
        saveBatch0(array);
    }

    @PostMapping("delete")
    @ApiOperation(value = "删除", hidden = true)
    void delete(@RequestBody @V2Swagger("img{}") JSONObject json){
        delete0(json);
    }

    @GetMapping("deleteById")
    @ApiOperation(value = "根据id删除", hidden = true)
    void deleteById(@RequestParam Serializable id){
        deleteById0(id);
    }

    @PostMapping("deleteByIds")
    @ApiOperation(value = "根据id数组删除", hidden = true)
    void deleteByIds(@RequestBody List<Object> ids){
        deleteByIds0(ids);
    }

}
