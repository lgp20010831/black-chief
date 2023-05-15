package ${location.generatePath};

import com.alibaba.fastjson.JSONObject;
import ${superPath};
import ${pojoPath}.${source.className};
import ${mapperPath}.${source.className}Mapper;
import ${implPath}.${source.className}Impl;
import com.black.core.annotation.ChiefServlet;
import com.black.core.aop.servlet.OpenIbatisPage;
import com.black.core.util.LazyAutoWried;
<#if inSwagger=true>
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import com.black.swagger.v2.V2Swagger;
</#if>
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.*;

import static com.black.utils.ServiceUtils.*;

@Log4j2
<#if inSwagger=true>@Api(tags = "${source.remark}")</#if>
@ChiefServlet("${source.lowName}") @SuppressWarnings("all")
public class ${source.className}Controller extends ${superName}<#if needGeneric=true><${source.className}></#if>{


    @LazyAutoWried
    ${source.className}Mapper ${source.lowName}Mapper;

    @LazyAutoWried
    ${source.className}Impl ${source.lowName}Impl;

    //****************************************************************************************
    //                               A U T O   C R E A T E
    //****************************************************************************************

<#if simpleController=true>
    @OpenIbatisPage
    @PostMapping("selectList")<#if inSwagger=true>
    @ApiOperation(value = "列表查询接口", hidden = true)</#if>
    List<${source.className}> findList(@RequestBody(required = false) <#if inSwagger=true>@V2Swagger("${source.lowName}{} + {pageSize:每页数量, pageNum:当前页数}")</#if> JSONObject body){
        return doFindList(body);
    }

    @GetMapping("selectById")<#if inSwagger=true>
    @V2Swagger("${source.lowName}{}")
    @ApiOperation(value = "根据 id 查询接口", hidden = true)</#if>
    ${source.className} findById(@RequestParam Serializable id){
        return doFindById(id);
    }

    @PostMapping("save")<#if inSwagger=true>
    @ApiOperation(value = "修改或者添加, 有主键则修改, 否则添加", hidden = true)</#if>
    void save0(@RequestBody ${source.className} body){
        doSave1(body);
    }

    @GetMapping("deleteById")<#if inSwagger=true>
    @ApiOperation(value = "根据主键删除接口", hidden = true)</#if>
    void deleteById0(@RequestParam Serializable id){
        doDeleteById0(id);
    }
</#if>
<#if simpleController=false>

    @OpenIbatisPage
    @PostMapping("selectList")<#if inSwagger=true>
    @ApiOperation(value = "列表查询接口", hidden = true)</#if>
    List<${source.className}> findList(@RequestBody(required = false) <#if inSwagger=true>@V2Swagger("${source.lowName}{} + {pageSize:每页数量, pageNum:当前页数}")</#if> JSONObject body){
        return doFindList(body);
    }

    @PostMapping("selectSingle")<#if inSwagger=true>
    @ApiOperation(value = "查询一条数据", hidden = true)</#if>
    ${source.className} selectSingle(@RequestBody(required = false) ${source.className} body){
        return doSelectSingle0(body);
    }

    @GetMapping("selectById")<#if inSwagger=true>
    @V2Swagger("${source.lowName}{}")
    @ApiOperation(value = "根据 id 查询接口", hidden = true)</#if>
    ${source.className} findById(@RequestParam Serializable id){
        return doFindById(id);
    }

    @PostMapping("insertBatch")<#if inSwagger=true>
    @ApiOperation(value = "批次插入接口", hidden = true)</#if>
    void insertBatch(@RequestBody List<${source.className}> array){
        doInsertBatch0(array);
    }

    @PostMapping("insert")<#if inSwagger=true>
    @ApiOperation(value = "普通添加一条数据", hidden = true)</#if>
    void join(@RequestBody ${source.className} body){
        doJoin0(body);
    }

    @PostMapping("saveBatch")<#if inSwagger=true>
    @ApiOperation(value = "批次: 修改或者添加, 有主键则修改, 否则添加", hidden = true)</#if>
    void saveBatch(@RequestBody  List<${source.className}> array){
        doSaveBatch0(array);
    }

    @PostMapping("save")<#if inSwagger=true>
    @ApiOperation(value = "修改或者添加, 有主键则修改, 否则添加", hidden = true)</#if>
    void save0(@RequestBody ${source.className} body){
        doSave1(body);
    }

    @PostMapping("delete")<#if inSwagger=true>
    @ApiOperation(value = "根据条件 map 删除接口", hidden = true)</#if>
    void delete0(@RequestBody ${source.className} body){
        doDelete0(body);
    }

    @GetMapping("deleteById")<#if inSwagger=true>
    @ApiOperation(value = "根据主键删除接口", hidden = true)</#if>
    void deleteById0(@RequestParam Serializable id){
        doDeleteById0(id);
    }

    @PostMapping("deleteAllById")<#if inSwagger=true>
    @ApiOperation(value = "根据主键集合删除数据", hidden = true)</#if>
    void deleteAllById0(@RequestBody List< Object> idList){
        doDeleteAllById0(idList);
    }
    </#if>
}