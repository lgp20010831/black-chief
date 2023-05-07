package com.black.core.yml.controller;

import com.alibaba.fastjson.JSONObject;
import com.black.core.annotation.ChiefServlet;
import com.black.core.aop.servlet.OpenIbatisPage;
import com.black.core.util.LazyAutoWried;
import com.black.core.yml.impl.AycImpl;
import com.black.core.yml.mapper.AycMapper;
import com.black.core.yml.pojo.Ayc;
import com.black.ibtais.ObjectIbatisFullController;
import com.black.swagger.v2.V2Swagger;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;
import java.util.List;

@Log4j2
@Api(tags = "ayc表")
@ChiefServlet("ayc") @SuppressWarnings("all")
public class AycController extends ObjectIbatisFullController<Ayc> {


    @LazyAutoWried
    AycMapper aycMapper;

    @LazyAutoWried
    AycImpl aycImpl;

    //****************************************************************************************
    //                               A U T O   C R E A T E
    //****************************************************************************************

    @OpenIbatisPage
    @PostMapping("selectList")
    @ApiOperation(value = "列表查询接口", hidden = false)
    List<Ayc> findList(@RequestBody(required = false) @V2Swagger("ayc{} + {pageSize:每页数量, pageNum:当前页数}") JSONObject body){
        return doFindList(body);
    }

    @PostMapping("selectSingle")
    @ApiOperation(value = "查询一条数据", hidden = false)
    Ayc selectSingle(@RequestBody(required = false) Ayc body){
        return doSelectSingle0(body);
    }

    @GetMapping("selectById")
    @ApiOperation(value = "根据 id 查询接口", hidden = false)
    Ayc findById(@RequestParam Serializable id){
        return doFindById(id);
    }

    @PostMapping("insertBatch")
    @ApiOperation(value = "批次插入接口", hidden = false)
    void insertBatch(@RequestBody List<Ayc> array){
        doInsertBatch0(array);
    }

    @PostMapping("insert")
    @ApiOperation(value = "普通添加一条数据", hidden = false)
    void join(@RequestBody Ayc body){
        doJoin0(body);
    }

    @PostMapping("saveBatch")
    @ApiOperation(value = "批次: 修改或者添加, 有主键则修改, 否则添加", hidden = false)
    void saveBatch(@RequestBody  List<Ayc> array){
        doSaveBatch0(array);
    }

    @PostMapping("save")
    @ApiOperation(value = "修改或者添加, 有主键则修改, 否则添加", hidden = false)
    void save0(@RequestBody Ayc body){
        doSave1(body);
    }

    @PostMapping("delete")
    @ApiOperation(value = "根据条件 map 删除接口", hidden = false)
    void delete0(@RequestBody Ayc body){
        doDelete0(body);
    }

    @GetMapping("deleteById")
    @ApiOperation(value = "根据主键删除接口", hidden = false)
    void deleteById0(@RequestParam Serializable id){
        doDeleteById0(id);
    }

    @PostMapping("deleteAllById")
    @ApiOperation(value = "根据主键集合删除数据", hidden = false)
    void deleteAllById0(@RequestBody List<Object> idList){
        doDeleteAllById0(idList);
    }


}