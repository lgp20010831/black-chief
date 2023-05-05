package com.black.core.yml;

import com.alibaba.fastjson.JSONObject;
import com.black.Supplier;
import com.black.core.annotation.ChiefServlet;
import com.black.core.aop.servlet.OpenIbatisPage;
import com.black.core.mvc.response.Response;
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
@Api(tags = "supplier表")
@ChiefServlet("supplier") @SuppressWarnings("all")
public class SupplierController extends ObjectIbatisFullController<Supplier> {



    //****************************************************************************************
    //                               A U T O   C R E A T E
    //****************************************************************************************

    @OpenIbatisPage
    @PostMapping("selectList")
    @ApiOperation(value = "列表查询接口", hidden = false)
    Object findList(@RequestBody(required = false) @V2Swagger("supplier{} + {pageSize:每页数量, pageNum:当前页数}") JSONObject body){
        return doFindList(body);
    }

    @GetMapping("selectById")
    @V2Swagger("supplier{}")
    @ApiOperation(value = "根据 id 查询接口", hidden = false)
    Supplier findById(@RequestParam Serializable id){
        return doFindById(id);
    }

    @PostMapping("save")
    @ApiOperation(value = "修改或者添加, 有主键则修改, 否则添加", response = Response.class, hidden = false)
    void save0(@RequestBody Supplier body){
        doSave1(body);
    }

    @GetMapping("deleteById")
    @ApiOperation(value = "根据主键删除接口", response = Response.class, hidden = false)
    void deleteById0(@RequestParam Serializable id){
        doDeleteById0(id);
    }

}