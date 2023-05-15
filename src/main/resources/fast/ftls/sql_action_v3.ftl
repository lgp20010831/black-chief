package ${location.generatePath};

import ${superPath};
import com.black.core.sql.annotation.OpenSqlPage;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import com.black.core.annotation.ChiefServlet;
import com.black.core.sql.annotation.OpenTransactional;
import com.black.sql_v2.action.AbstractSqlOptServlet;
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

<#if inSwagger=true>@Api(tags = "${source.remark}")</#if>
@ChiefServlet("${source.lowName}") @Log4j2 @OpenTransactional @SuppressWarnings("all")
public class ${source.className}Controller extends ${superName}{

    public String getTableName(){
        return "${source.tableName}";
    }



    //****************************************************************
    //          A   U   T   O           C   R   E   A   T   E
    //****************************************************************
<#if simpleController=true>
    @OpenSqlPage
    @PostMapping("list")<#if inSwagger=true>
    @V2Swagger("${source.tableName}{}")
    @ApiOperation(value = "列表查询", hidden = true)</#if>
    Object list(@RequestBody <#if inSwagger=true>@V2Swagger("${source.tableName}{} + {pageSize:每页数量, pageNum:当前页数}")</#if> JSONObject json){
        return list0(json);
    }

    @GetMapping("queryById")  <#if inSwagger=true>@V2Swagger("${source.tableName}{}")</#if><#if inSwagger=true>
    @ApiOperation(value = "根据id查询", hidden = true)</#if>
    Object queryById(@RequestParam Serializable id){
        return queryById0(id);
    }

    @PostMapping("save")<#if inSwagger=true>
    @ApiOperation(value = "更新/添加", hidden = true)</#if>
    void save(@RequestBody <#if inSwagger=true>@V2Swagger("${source.tableName}{}")</#if> JSONObject json){
        save0(json);
    }

    @GetMapping("deleteById")<#if inSwagger=true>
    @ApiOperation(value = "根据id删除", hidden = true)</#if>
    void deleteById(@RequestParam Serializable id){
        deleteById0(id);
    }
</#if>
<#if simpleController=false>

    @OpenSqlPage
    @PostMapping("list")<#if inSwagger=true>
    @V2Swagger("${source.tableName}{}")
    @ApiOperation(value = "列表查询", hidden = true)</#if>
    Object list(@RequestBody <#if inSwagger=true>@V2Swagger("${source.tableName}{} + {pageSize:每页数量, pageNum:当前页数}")</#if> JSONObject json){
        return list0(json);
    }


    @GetMapping("queryById")  <#if inSwagger=true>@V2Swagger("${source.tableName}{}")</#if><#if inSwagger=true>
    @ApiOperation(value = "根据id查询", hidden = true)</#if>
    Object queryById(@RequestParam Serializable id){
        return queryById0(id);
    }

    @PostMapping("single") <#if inSwagger=true>@V2Swagger("${source.tableName}{}")</#if><#if inSwagger=true>
    @ApiOperation(value = "查询一条", hidden = true)</#if>
    Object single(@RequestBody <#if inSwagger=true>@V2Swagger("${source.tableName}{}")</#if> JSONObject json){
        return single0(json);
    }

    @PostMapping("save")<#if inSwagger=true>
    @ApiOperation(value = "更新/添加", hidden = true)</#if>
    void save(@RequestBody <#if inSwagger=true>@V2Swagger("${source.tableName}{}")</#if> JSONObject json){
        save0(json);
    }

    @PostMapping("saveBatch")<#if inSwagger=true>
    @ApiOperation(value = "批次-更新/添加", hidden = true)</#if>
    void saveBatch(@RequestBody <#if inSwagger=true>@V2Swagger("${source.tableName}{}")</#if> JSONArray array){
        saveBatch0(array);
    }

    @PostMapping("delete")<#if inSwagger=true>
    @ApiOperation(value = "删除", hidden = true)</#if>
    void delete(@RequestBody <#if inSwagger=true>@V2Swagger("${source.tableName}{}")</#if> JSONObject json){
        delete0(json);
    }

    @GetMapping("deleteById")<#if inSwagger=true>
    @ApiOperation(value = "根据id删除", hidden = true)</#if>
    void deleteById(@RequestParam Serializable id){
        deleteById0(id);
    }

    @PostMapping("deleteByIds")<#if inSwagger=true>
    @ApiOperation(value = "根据id数组删除", hidden = true)</#if>
    void deleteByIds(@RequestBody List< Object> ids){
        deleteByIds0(ids);
    }

    </#if>
}
